package com.lio.exercisepracticesystem.service;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class FileReaderService {

    public String readFile(Path filePath) {
        try {
            String filename = filePath.getFileName().toString().toLowerCase();
            if (filename.endsWith(".txt") || filename.endsWith(".text")) {
                return readTextFile(filePath);
            } else if (filename.endsWith(".pdf")) {
                throw new UnsupportedOperationException("PDF 文件解析功能需要添加 PDFBox 依赖");
            } else if (filename.endsWith(".docx")) {
                return readDocxFile(filePath);
            } else if (filename.endsWith(".doc")) {
                return readDocFile(filePath);
            } else {
                throw new IllegalArgumentException("不支持的文件格式: " + filename);
            }
        } catch (Exception e) {
            throw new RuntimeException("读取文件失败: " + e.getMessage(), e);
        }
    }

    private String readTextFile(Path filePath) {
        try {
            return Files.readString(filePath);
        } catch (Exception e) {
            throw new RuntimeException("读取文本文件失败: " + e.getMessage(), e);
        }
    }

    private String readDocxFile(Path filePath) {
        try {
            byte[] bytes = Files.readAllBytes(filePath);
            try (InputStream is = new ByteArrayInputStream(bytes);
                 XWPFDocument doc = new XWPFDocument(is)) {
                return doc.getParagraphs().stream()
                        .map(XWPFParagraph::getText)
                        .filter(s -> s != null && !s.isEmpty())
                        .collect(Collectors.joining("\n"));
            } catch (Exception strictOrPoiError) {
                String xmlText = readDocxXmlText(bytes);
                if (xmlText == null || xmlText.isBlank()) {
                    throw strictOrPoiError;
                }
                return xmlText;
            }
        } catch (Exception e) {
            throw new RuntimeException("读取 Word 文件失败: " + e.getMessage(), e);
        }
    }

    private String readDocFile(Path filePath) {
        try (InputStream is = new FileInputStream(filePath.toFile());
             HWPFDocument doc = new HWPFDocument(is)) {
            return doc.getDocumentText();
        } catch (Exception e) {
            throw new RuntimeException("读取 Word 文件失败: " + e.getMessage(), e);
        }
    }

    private String readDocxXmlText(byte[] fileBytes) {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(fileBytes), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if ("word/document.xml".equalsIgnoreCase(entry.getName())) {
                    String xml = new String(zis.readAllBytes(), StandardCharsets.UTF_8);
                    String text = xml.replaceAll("</w:p>", "\n")
                            .replaceAll("<[^>]+>", " ")
                            .replaceAll("&lt;", "<")
                            .replaceAll("&gt;", ">")
                            .replaceAll("&amp;", "&")
                            .replaceAll("\\s+", " ")
                            .trim();
                    return text;
                }
            }
        } catch (Exception ignored) {
        }
        return "";
    }
}
