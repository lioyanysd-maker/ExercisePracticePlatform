package com.lio.exercisepracticesystem.service;

import com.lio.exercisepracticesystem.dto.DocumentImageItem;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.hwpf.usermodel.PictureType;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.model.PicturesTable;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFHeaderFooter;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class DocxImageExtractService {

    private static final Pattern QNO_PATTERN_1 = Pattern.compile("^\\s*(\\d{1,3})\\s*[\\.、]\\s*");
    private static final Pattern QNO_PATTERN_2 = Pattern.compile("^\\s*[（(]\\s*(\\d{1,3})\\s*[）)]\\s*");

    public static class DocxExtractResult {
        private final List<Integer> questionNumbers;
        private final List<DocumentImageItem> images;

        public DocxExtractResult(List<Integer> questionNumbers, List<DocumentImageItem> images) {
            this.questionNumbers = questionNumbers;
            this.images = images;
        }

        public List<Integer> getQuestionNumbers() {
            return questionNumbers;
        }

        public List<DocumentImageItem> getImages() {
            return images;
        }
    }

    public DocxExtractResult extract(byte[] fileBytes) {
        Set<Integer> qnos = new LinkedHashSet<>();
        List<DocumentImageItem> images = new ArrayList<>();
        int imgIndex = 1;

        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(fileBytes))) {
            Integer currentQno = null;

            int[] idxBox = new int[]{imgIndex};
            Integer[] qnoBox = new Integer[]{currentQno};
            for (IBodyElement el : doc.getBodyElements()) {
                qnoBox[0] = processBodyElement(el, qnos, images, idxBox, qnoBox[0]);
            }

            for (XWPFHeader header : doc.getHeaderList()) {
                qnoBox[0] = processHeaderFooter(header, qnos, images, idxBox, qnoBox[0]);
            }
            for (XWPFFooter footer : doc.getFooterList()) {
                qnoBox[0] = processHeaderFooter(footer, qnos, images, idxBox, qnoBox[0]);
            }

            if (images.isEmpty()) {
                Set<Integer> seenHash = new HashSet<>();
                for (XWPFPictureData pd : doc.getAllPictures()) {
                    if (pd == null) continue;
                    byte[] bytes = pd.getData();
                    if (bytes == null || bytes.length == 0) continue;
                    int h = java.util.Arrays.hashCode(bytes);
                    if (!seenHash.add(h)) continue;

                    String ext = safe(pd.suggestFileExtension());
                    String contentType = contentType(ext);
                    String base64 = Base64.getEncoder().encodeToString(bytes);
                    String fileName = "img_" + String.format("%03d", idxBox[0]) + (ext.isBlank() ? "" : "." + ext);
                    images.add(new DocumentImageItem(idxBox[0], fileName, contentType, base64, null));
                    idxBox[0]++;
                }
            }
            imgIndex = idxBox[0];
        } catch (Exception ignored) {
        }

        if (images.isEmpty()) {
            images.addAll(extractFromDocxZip(fileBytes, imgIndex));
        }

        if (images.isEmpty()) {
            throw new RuntimeException("DOCX 解析失败: 未检测到可提取图片（可能为外链图片或非常规文档结构）");
        }

        return new DocxExtractResult(new ArrayList<>(qnos), images);
    }

    private Integer processHeaderFooter(XWPFHeaderFooter hf,
                                        Set<Integer> qnos,
                                        List<DocumentImageItem> images,
                                        int[] idxBox,
                                        Integer currentQno) {
        if (hf == null) return currentQno;
        Integer qno = currentQno;
        for (IBodyElement el : hf.getBodyElements()) {
            qno = processBodyElement(el, qnos, images, idxBox, qno);
        }
        return qno;
    }

    private Integer processBodyElement(IBodyElement el,
                                      Set<Integer> qnos,
                                      List<DocumentImageItem> images,
                                      int[] idxBox,
                                      Integer currentQno) {
        if (el == null) return currentQno;
        Integer qno = currentQno;
        if (el instanceof XWPFParagraph paragraph) {
            qno = processParagraph(paragraph, qnos, images, idxBox, qno);
        } else if (el instanceof XWPFTable table) {
            for (XWPFTableRow row : table.getRows()) {
                if (row == null) continue;
                for (XWPFTableCell cell : row.getTableCells()) {
                    if (cell == null) continue;
                    for (IBodyElement cellEl : cell.getBodyElements()) {
                        qno = processBodyElement(cellEl, qnos, images, idxBox, qno);
                    }
                }
            }
        }
        return qno;
    }

    private Integer processParagraph(XWPFParagraph paragraph,
                                    Set<Integer> qnos,
                                    List<DocumentImageItem> images,
                                    int[] idxBox,
                                    Integer currentQno) {
        if (paragraph == null) return currentQno;
        Integer qno = currentQno;
        String text = safe(paragraph.getText());
        Integer foundQno = parseQuestionNo(text);
        if (foundQno != null) {
            qno = foundQno;
            qnos.add(foundQno);
        }

        List<XWPFRun> runs = paragraph.getRuns();
        if (runs == null || runs.isEmpty()) return qno;
        for (XWPFRun run : runs) {
            if (run == null) continue;
            List<XWPFPicture> pics = run.getEmbeddedPictures();
            if (pics == null || pics.isEmpty()) continue;
            for (XWPFPicture pic : pics) {
                XWPFPictureData data = pic.getPictureData();
                if (data == null) continue;
                byte[] bytes = data.getData();
                if (bytes == null || bytes.length == 0) continue;

                String ext = safe(data.suggestFileExtension());
                String contentType = contentType(ext);
                String base64 = Base64.getEncoder().encodeToString(bytes);
                String fileName = "img_" + String.format("%03d", idxBox[0]) + (ext.isBlank() ? "" : "." + ext);

                images.add(new DocumentImageItem(
                        idxBox[0],
                        fileName,
                        contentType,
                        base64,
                        qno
                ));
                idxBox[0]++;
            }
        }
        return qno;
    }

    public DocxExtractResult extractDoc(InputStream inputStream) {
        try (HWPFDocument doc = new HWPFDocument(inputStream)) {
            Integer currentQno = null;
            Set<Integer> qnos = new LinkedHashSet<>();
            List<DocumentImageItem> images = new ArrayList<>();
            int imgIndex = 1;

            Range range = doc.getRange();
            PicturesTable picturesTable = doc.getPicturesTable();

            for (int i = 0; i < range.numParagraphs(); i++) {
                Paragraph paragraph = range.getParagraph(i);
                String text = safe(paragraph.text());
                Integer foundQno = parseQuestionNo(text);
                if (foundQno != null) {
                    currentQno = foundQno;
                    qnos.add(foundQno);
                }

                for (int j = 0; j < paragraph.numCharacterRuns(); j++) {
                    CharacterRun run = paragraph.getCharacterRun(j);
                    if (!picturesTable.hasPicture(run)) continue;
                    Picture picture = picturesTable.extractPicture(run, false);
                    if (picture == null) continue;

                    byte[] bytes = picture.getContent();
                    if (bytes == null || bytes.length == 0) continue;

                    PictureType type = picture.suggestPictureType();
                    String ext = type != null ? safe(type.getExtension()) : "";
                    if (ext.isBlank()) {
                        ext = safe(picture.suggestFileExtension());
                    }

                    String contentType = contentType(ext);
                    String base64 = Base64.getEncoder().encodeToString(bytes);
                    String fileName = "img_" + String.format("%03d", imgIndex) + (ext.isBlank() ? "" : "." + ext);

                    images.add(new DocumentImageItem(
                            imgIndex,
                            fileName,
                            contentType,
                            base64,
                            currentQno
                    ));
                    imgIndex++;
                }
            }

            return new DocxExtractResult(new ArrayList<>(qnos), images);
        } catch (Exception e) {
            throw new RuntimeException(".doc 解析失败: " + e.getMessage(), e);
        }
    }

    private Integer parseQuestionNo(String paragraphText) {
        if (paragraphText == null) return null;
        String t = paragraphText.trim();
        if (t.isEmpty()) return null;

        Matcher m1 = QNO_PATTERN_1.matcher(t);
        if (m1.find()) return toInt(m1.group(1));

        Matcher m2 = QNO_PATTERN_2.matcher(t);
        if (m2.find()) return toInt(m2.group(1));

        return null;
    }

    private Integer toInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return null;
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String contentType(String ext) {
        String e = ext == null ? "" : ext.toLowerCase();
        return switch (e) {
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "bmp" -> "image/bmp";
            case "webp" -> "image/webp";
            case "tif", "tiff" -> "image/tiff";
            case "emf" -> "image/emf";
            case "wmf" -> "image/wmf";
            default -> "application/octet-stream";
        };
    }

    private List<DocumentImageItem> extractFromDocxZip(byte[] fileBytes, int startIndex) {
        List<DocumentImageItem> images = new ArrayList<>();
        int idx = startIndex;
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(fileBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName() == null ? "" : entry.getName().toLowerCase();
                if (entry.isDirectory()) continue;
                if (!name.startsWith("word/media/")) continue;

                String ext = "";
                int dot = name.lastIndexOf(".");
                if (dot >= 0 && dot < name.length() - 1) {
                    ext = name.substring(dot + 1);
                }

                byte[] bytes = zis.readAllBytes();
                if (bytes == null || bytes.length == 0) continue;

                String base64 = Base64.getEncoder().encodeToString(bytes);
                String fileName = "img_" + String.format("%03d", idx) + (ext.isBlank() ? "" : "." + ext);
                images.add(new DocumentImageItem(
                        idx,
                        fileName,
                        contentType(ext),
                        base64,
                        null
                ));
                idx++;
            }
        } catch (Exception e) {
        }
        return images;
    }
}

