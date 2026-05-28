<template>
  <div>
    <n-page-header title="导入题目" subtitle="通过文件、图片或文本导入题目，AI 自动解析">
    </n-page-header>

    <n-space vertical size="large" style="margin-top: 24px;">
      <!-- 选择题目集 -->
      <n-card>
        <template #header>
          <div class="art-title">1. 选择题目集</div>
        </template>
        <n-spin :show="loadingSubjects">
          <n-alert v-if="!loadingSubjects && subjects.length === 0" type="warning">
            暂无题目集，请先创建题目集
          </n-alert>
          <n-space v-else wrap>
            <n-button
              v-for="s in subjects"
              :key="s.id"
              size="large"
              :type="selectedSubject === s.id ? 'primary' : 'default'"
              @click="selectedSubject = s.id"
            >
              {{ s.name }}
            </n-button>
          </n-space>
        </n-spin>
      </n-card>

      <!-- 导入方式选择 -->
      <n-card>
        <template #header>
          <div class="art-title">2. 选择导入方式</div>
        </template>
        <n-space justify="space-between" wrap>
          <n-button size="large" :type="importType === 'file' ? 'primary' : 'default'" @click="importType = 'file'">文件导入</n-button>
          <n-button size="large" :type="importType === 'image' ? 'primary' : 'default'" @click="importType = 'image'">图片导入</n-button>
          <n-button size="large" :type="importType === 'text' ? 'primary' : 'default'" @click="importType = 'text'">文本导入</n-button>
        </n-space>

        <div style="margin-top: 16px;">
          <div v-if="importType === 'file'">
            <n-space vertical style="margin-top: 16px;">
              <n-alert type="info">
                文件导入分为两种模式：带图片的 Word 试卷先做图片匹配；不带图片的文件直接 AI 解析导入。<span style="color: #2080f0;">较大文件可能会导致响应过慢</span>
              </n-alert>

              <n-card size="small" title="上传选项 A：带图片的 Word 试卷（.docx / .doc）" style="margin-top: 12px;">
                <n-space vertical>
                  <n-upload
                    :custom-request="handleAnalyzeDoc"
                    :show-file-list="false"
                    :disabled="importing || analyzingDoc || confirmingDoc"
                    accept=".docx,.doc"
                  >
                    <n-upload-dragger>
                      <div style="margin-bottom: 12px;">
                        <n-icon size="48" :depth="3">
                          <document-text-outline />
                        </n-icon>
                      </div>
                      <n-text style="font-size: 16px;">
                        {{ analyzingDoc ? '正在智能分析...' : '推荐：点击或拖拽 Word 试卷，自动识题并提取图片（左右对照）' }}
                      </n-text>
                      <n-p depth="3" style="margin: 8px 0 0 0;">
                        先 AI 识别题目，再和图片做左右对照匹配，最后一键导入并绑定
                      </n-p>
                    </n-upload-dragger>
                  </n-upload>

                  <n-card v-if="analyzeResult" size="small" title="智能对照匹配（左题目 / 右图片）">
                    <n-space vertical size="large">
                      <n-alert type="success" :title="analyzeResult.message || '分析完成'" />
                      <n-grid cols="2" x-gap="12" responsive="screen">
                        <n-gi>
                          <n-card size="small" title="AI 识别题目">
                            <n-space vertical size="small" style="max-height: 560px; overflow: auto;">
                              <n-card
                                v-for="q in analyzeResult.questions"
                                :key="q.index"
                                size="small"
                                :title="`题目 #${q.index}（${q.type || '-'}）`"
                                hoverable
                              >
                                <n-ellipsis :line-clamp="3" :tooltip="true">
                                  {{ q.question }}
                                </n-ellipsis>
                              </n-card>
                            </n-space>
                          </n-card>
                        </n-gi>
                        <n-gi>
                          <n-card size="small" title="提取图片（选择绑定题目）">
                            <n-space vertical size="small" style="max-height: 560px; overflow: auto;">
                              <n-card v-for="img in analyzeResult.images" :key="`an-${img.index}`" size="small" :title="`图片 #${img.index}`" hoverable>
                                <n-space vertical>
                                  <n-image
                                    :src="`data:${img.content_type};base64,${img.base64}`"
                                    width="100%"
                                    object-fit="contain"
                                    style="border-radius: 10px;"
                                  />
                                  <n-select
                                    v-model:value="img.mapped_question_index"
                                    :options="analyzeQuestionOptions"
                                    placeholder="绑定到哪道题"
                                    clearable
                                  />
                                </n-space>
                              </n-card>
                            </n-space>
                          </n-card>
                        </n-gi>
                      </n-grid>
                      <n-button
                        type="primary"
                        :loading="confirmingDoc"
                        :disabled="!analyzeResult.questions?.length"
                        @click="handleConfirmDoc"
                      >
                        确认导入并绑定图片
                      </n-button>
                    </n-space>
                  </n-card>
                </n-space>
              </n-card>

              <n-card size="small" title="上传选项 B：不带图片的文件（直接 AI 解析）">
                <n-upload
                  :custom-request="handleFileUpload"
                  :show-file-list="false"
                  :disabled="importing"
                  accept=".pdf,.docx,.doc,.txt"
                >
                  <n-upload-dragger>
                    <div style="margin-bottom: 12px;">
                      <n-icon size="48" :depth="3">
                        <document-text-outline />
                      </n-icon>
                    </div>
                    <n-text style="font-size: 16px;">
                      {{ importing ? '正在导入...' : '点击或拖拽不带图片的文件到此区域上传' }}
                    </n-text>
                    <n-p depth="3" style="margin: 8px 0 0 0;">
                      支持 PDF、Word (.docx, .doc)、文本 (.txt) 格式
                    </n-p>
                  </n-upload-dragger>
                </n-upload>
              </n-card>
            </n-space>
          </div>

          <div v-else-if="importType === 'image'">
            <n-space vertical style="margin-top: 16px;">
              <n-alert type="info">
                支持 JPG、PNG 等图片格式。系统将使用 AI 视觉模型直接识别图片中的题目。
              </n-alert>
              
              <n-upload
                :custom-request="handleImageUpload"
                :show-file-list="false"
                :disabled="importing"
                accept="image/*"
              >
                <n-upload-dragger>
                  <div style="margin-bottom: 12px;">
                    <n-icon size="48" :depth="3">
                      <image-outline />
                    </n-icon>
                  </div>
                  <n-text style="font-size: 16px;">
                    {{ importing ? '正在导入...' : '点击或拖拽图片到此区域上传' }}
                  </n-text>
                  <n-p depth="3" style="margin: 8px 0 0 0;">
                    支持 JPG、PNG、BMP 等图片格式
                  </n-p>
                </n-upload-dragger>
              </n-upload>
            </n-space>
          </div>

          <div v-else>
            <n-space vertical style="margin-top: 16px;">
              <n-alert type="info">
                直接粘贴题目文本，AI 将自动解析题目结构。
              </n-alert>
              <n-input
                v-model:value="textContent"
                type="textarea"
                placeholder="请粘贴题目内容，例如：&#10;&#10;1. 1+1等于多少？&#10;A. 1&#10;B. 2&#10;C. 3&#10;D. 4&#10;答案：B&#10;解析：基础加法运算"
                :rows="10"
              />
              <n-button
                type="primary"
                :loading="importing"
                :disabled="!textContent || !selectedSubject"
                @click="handleTextImport"
                block
              >
                开始导入
              </n-button>
            </n-space>
          </div>
        </div>
      </n-card>

      <!-- 导入进度 -->
      <n-card v-if="importing" title="导入进度">
        <n-space vertical>
          <n-progress type="line" :percentage="100" processing />
          <n-text>正在使用 AI 解析题目，请稍候...</n-text>
          <n-alert type="warning" style="margin-top: 12px;">
            AI 识别可能需要 30-120 秒，请耐心等待，不要关闭页面
          </n-alert>
        </n-space>
      </n-card>

      <!-- 导入成功确认界面 -->
      <n-modal v-model:show="showImportSuccessModal" preset="dialog" title="导入成功确认" style="width: 560px;">
        <n-space vertical size="large">
          <n-alert type="success" :title="successSummary.title">
            {{ successSummary.message }}
          </n-alert>
          <n-descriptions :column="1" bordered>
            <n-descriptions-item label="导入方式">{{ successSummary.mode }}</n-descriptions-item>
            <n-descriptions-item label="题目集">{{ successSummary.subjectName }}</n-descriptions-item>
            <n-descriptions-item label="新增题目">{{ successSummary.count }} 道</n-descriptions-item>
          </n-descriptions>
          <n-space justify="end">
            <n-button @click="showImportSuccessModal = false">继续导入</n-button>
            <n-button type="primary" @click="goQuestionBank">前往题库管理确认</n-button>
          </n-space>
        </n-space>
      </n-modal>
    </n-space>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { createDiscreteApi } from 'naive-ui'
import { subjectApi, importApi } from '@/api'
import { DocumentTextOutline, ImageOutline } from '@vicons/ionicons5'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'

const { message } = createDiscreteApi(['message'])
const router = useRouter()
const userStore = useUserStore()
const { userId } = storeToRefs(userStore)
const importing = ref(false)
const loadingSubjects = ref(false)
const subjects = ref([])
const selectedSubject = ref(null)
const importType = ref('file')
const textContent = ref('')
const analyzingDoc = ref(false)
const confirmingDoc = ref(false)
const analyzeResult = ref(null)
const showImportSuccessModal = ref(false)
const successSummary = ref({
  title: '导入完成',
  message: '',
  mode: '',
  subjectName: '',
  count: 0
})

const analyzeQuestionOptions = computed(() => {
  const qs = analyzeResult.value?.questions || []
  return qs.map(q => ({
    label: `题目 #${q.index} ${q.type ? `(${q.type})` : ''}`,
    value: q.index
  }))
})

const selectedSubjectName = computed(() => {
  const s = subjects.value.find(x => x.id === selectedSubject.value)
  return s?.name || '-'
})

// 题目集选项
const subjectOptions = computed(() => {
  return subjects.value.map(s => ({
    label: s.name,
    value: s.id
  }))
})

// 加载题目集列表
const loadSubjects = async () => {
  loadingSubjects.value = true
  try {
    subjects.value = await subjectApi.list(userId.value)
    if (!selectedSubject.value && subjects.value.length > 0) {
      selectedSubject.value = subjects.value[0].id
    }
  } catch (error) {
    message.error(error.message || '加载题目集列表失败')
  } finally {
    loadingSubjects.value = false
  }
}

const showSuccessConfirm = (mode, count, messageText) => {
  successSummary.value = {
    title: '导入完成，请确认',
    message: messageText || '请确认导入结果，若无误可前往题库管理继续完善。',
    mode,
    subjectName: selectedSubjectName.value,
    count: Number(count || 0)
  }
  showImportSuccessModal.value = true
}

const goQuestionBank = () => {
  showImportSuccessModal.value = false
  router.push('/question-bank')
}

// 处理文件上传
const handleFileUpload = async ({ file }) => {
  if (!selectedSubject.value) {
    message.warning('请先选择题目集')
    return
  }

  // 防止重复上传
  if (importing.value) {
    console.log('[前端] 正在导入中，跳过重复请求')
    return
  }

  importing.value = true
  try {
    const result = await importApi.fromFile(
      userId.value,
      selectedSubject.value,
      file.file
    )
    message.success(result.message || '导入成功')
    showSuccessConfirm('文件导入', result.count || result.created_count || 0, result.message)
  } catch (error) {
    message.error(error.message || '导入失败')
  } finally {
    importing.value = false
  }
}

// 智能分析：AI识题 + 图片提取（用于左右对照）
const handleAnalyzeDoc = async ({ file }) => {
  if (!selectedSubject.value) {
    message.warning('请先选择题目集')
    return
  }
  if (importing.value || analyzingDoc.value || confirmingDoc.value) return
  analyzingDoc.value = true
  analyzeResult.value = null
  try {
    const res = await importApi.analyzeDoc(userId.value, selectedSubject.value, file.file)
    analyzeResult.value = res
    // 默认先按索引一一对应，方便你再微调
    if (analyzeResult.value?.images?.length) {
      analyzeResult.value.images.forEach((img, idx) => {
        if (!img.mapped_question_index && analyzeResult.value.questions?.[idx]) {
          img.mapped_question_index = analyzeResult.value.questions[idx].index
        }
      })
    }
    message.success(res.message || '分析完成')
  } catch (error) {
    message.error(error.message || '分析失败')
  } finally {
    analyzingDoc.value = false
  }
}

// 确认：导入题目并按映射绑定图片
const handleConfirmDoc = async () => {
  if (!selectedSubject.value) {
    message.warning('请先选择题目集')
    return
  }
  if (!analyzeResult.value?.questions?.length) {
    message.warning('暂无可导入题目')
    return
  }
  confirmingDoc.value = true
  try {
    const payload = {
      user_id: userId.value,
      subject_id: selectedSubject.value,
      questions: analyzeResult.value.questions.map(q => ({
        type: q.type,
        question: q.question,
        options: q.options || [],
        answer: q.answer || '',
        analysis: q.analysis || ''
      })),
      images: analyzeResult.value.images || []
    }
    const res = await importApi.confirmDoc(payload)
    message.success(res.message || '处理完成')
    showSuccessConfirm('带图片文档智能导入', res.created_count || 0, res.message)
  } catch (error) {
    message.error(error.message || '处理失败')
  } finally {
    confirmingDoc.value = false
  }
}

// 处理图片上传
const handleImageUpload = async ({ file }) => {
  if (!selectedSubject.value) {
    message.warning('请先选择题目集')
    return
  }

  // 防止重复上传
  if (importing.value) {
    console.log('[前端] 正在导入中，跳过重复请求')
    return
  }

  importing.value = true
  try {
    const result = await importApi.fromImage(
      userId.value,
      selectedSubject.value,
      file.file
    )
    message.success(result.message || '导入成功')
    showSuccessConfirm('图片导入', result.count || result.created_count || 0, result.message)
  } catch (error) {
    message.error(error.message || '导入失败')
  } finally {
    importing.value = false
  }
}

// 处理文本导入
const handleTextImport = async () => {
  if (!selectedSubject.value) {
    message.warning('请先选择题目集')
    return
  }

  if (!textContent.value.trim()) {
    message.warning('请输入题目文本')
    return
  }

  importing.value = true
  try {
    const result = await importApi.fromText({
      user_id: userId.value,
      subject_id: selectedSubject.value,
      text: textContent.value
    })
    message.success(result.message || '导入成功')
    textContent.value = ''
    showSuccessConfirm('文本导入', result.count || result.created_count || 0, result.message)
  } catch (error) {
    message.error(error.message || '导入失败')
  } finally {
    importing.value = false
  }
}

onMounted(() => {
  loadSubjects()
})
</script>

<style scoped>
.art-title {
  font-size: 22px;
  font-weight: 900;
  letter-spacing: 1px;
  background: linear-gradient(135deg, #0ea5e9, #22c55e, #10b981);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  text-shadow: 0 8px 18px rgba(16, 185, 129, 0.25);
}
</style>
