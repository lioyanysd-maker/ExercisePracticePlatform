<template>
  <div>
    <n-page-header title="题库管理" subtitle="查看和管理所有题目">
    </n-page-header>

    <n-space vertical size="large" style="margin-top: 24px;">
      <!-- 筛选区域 -->
      <n-card>
        <n-space>
          <n-select
            v-model:value="filterSubjectId"
            :options="subjectOptions"
            placeholder="选择题目集"
            style="width: 200px"
            clearable
            @update:value="loadQuestions"
          />
          <n-select
            v-model:value="filterType"
            :options="typeOptions"
            placeholder="题目类型"
            style="width: 150px"
            clearable
            @update:value="loadQuestions"
          />
          <n-input
            v-model:value="searchKeyword"
            placeholder="搜索题目内容"
            style="width: 300px"
            clearable
            @keyup.enter="loadQuestions"
          >
            <template #prefix>
              <n-icon><search-outline /></n-icon>
            </template>
          </n-input>
          <n-button type="primary" @click="loadQuestions">
            <template #icon>
              <n-icon><search-outline /></n-icon>
            </template>
            搜索
          </n-button>
          <n-button @click="handleReset">
            <template #icon>
              <n-icon><refresh-outline /></n-icon>
            </template>
            重置
          </n-button>
        </n-space>
      </n-card>

      <!-- 题目统计 -->
      <n-card>
        <n-space>
          <n-statistic label="总题数" :value="totalCount" />
          <n-statistic label="单选题" :value="typeCount.single || 0" />
          <n-statistic label="多选题" :value="typeCount.multiple || 0" />
          <n-statistic label="判断题" :value="typeCount.judge || 0" />
          <n-statistic label="填空题" :value="typeCount.fill || 0" />
          <n-statistic label="大型题" :value="typeCount.major || 0" />
        </n-space>
      </n-card>

      <!-- 题目列表 -->
      <n-card title="题目列表">
        <n-spin :show="loading">
          <n-empty
            v-if="questions.length === 0"
            description="暂无题目数据"
            style="margin: 40px 0;"
          />          
          <n-list v-else bordered>
            <n-list-item v-for="(question, index) in paginatedQuestions" :key="question.id">
              <template #prefix>
                <n-tag :type="getTypeTagType(question.type)" size="small">
                  {{ getTypeLabel(question.type) }}
                </n-tag>
              </template>
              
              <n-thing>
                <template #header>
                  <div style="display: flex; align-items: center; gap: 8px;">
                    <span style="font-weight: 500;">{{ (currentPage - 1) * pageSize + index + 1 }}. </span>
                    <template v-if="question.type === 'major'">
                      <!-- 大型题只显示文本内容，不显示表格 -->
                      <div style="width: 100%; display: flex; flex-direction: column; align-items: center;">
                        <TableRenderer :content="question.question" />  
                      </div>
                    </template>
                    <template v-else>
                      <TableRenderer :content="question.question" />  
                    </template>
                  </div>
                </template>
                
                <template #header-extra>
                  <div class="question-list-actions">
                    <n-button text @click="handleView(question)" class="action-btn">
                      <template #icon><n-icon><eye-outline /></n-icon></template>
                      查看
                    </n-button>
                    <n-button text type="primary" @click="handleReference(question)" class="action-btn">
                      <template #icon><n-icon><create-outline /></n-icon></template>
                      参考
                    </n-button>
                    <n-button text type="error" @click="handleDelete(question)" class="action-btn">
                      <template #icon><n-icon><trash-outline /></n-icon></template>
                      删除
                    </n-button>
                  </div>
                </template>
                
                <!-- 移除描述部分，只显示题目类型、题目名称、查看和删除按钮 -->
              </n-thing>
            </n-list-item>
          </n-list>
          
          <!-- 分页 -->
          <div v-if="questions.length > 0" style="margin-top: 16px; display: flex; justify-content: flex-end;">
            <n-pagination
              v-model:page="currentPage"
              :page-count="pageCount"
              :page-size="pageSize"
              show-size-picker
              :page-sizes="[10, 20, 50, 100]"
              @update:page="handlePageChange"
              @update:page-size="handlePageSizeChange"
            />          
          </div>
        </n-spin>
      </n-card>
    </n-space>

    <!-- 查看题目详情对话框 -->
    <n-modal v-model:show="showDetailModal" preset="dialog" title="题目详情" style="width: 700px;">
      <div v-if="currentQuestion">
        <n-space vertical size="large">
          <n-descriptions label-placement="left" :column="1" bordered>
            <n-descriptions-item label="题目类型">
              <n-tag :type="getTypeTagType(currentQuestion.type)">
                {{ getTypeLabel(currentQuestion.type) }}
              </n-tag>
            </n-descriptions-item>
            <n-descriptions-item label="所属题目集">
              {{ getSubjectName(currentQuestion.subject_id) }}
            </n-descriptions-item>
            <n-descriptions-item label="题目内容">
              <TableRenderer :content="currentQuestion.question" />  
            </n-descriptions-item>
            <n-descriptions-item label="题目图片" v-if="detailImages.length > 0">
              <n-image-group>
                <n-space>
                  <n-image
                    v-for="img in detailImages"
                    :key="img.id"
                    :src="resolveResourceUrl(img.resource_content)"
                    width="160"
                    object-fit="contain"
                    style="border-radius: 10px;"
                  />
                </n-space>
              </n-image-group>
            </n-descriptions-item>
            <n-descriptions-item label="选项" v-if="currentQuestion.options && currentQuestion.options.length > 0">
              <n-space vertical size="small">
                <div v-for="(option, idx) in currentQuestion.options" :key="idx">
                  <FormulaRenderer :content="option" />  
                </div>
              </n-space>
            </n-descriptions-item>
            <n-descriptions-item label="正确答案">
              <n-text type="success" strong>
                <FormulaRenderer :content="currentQuestion.answer" />  
              </n-text>
            </n-descriptions-item>
            <n-descriptions-item label="题目解析">
              <FormulaRenderer :content="currentQuestion.analysis" />  
            </n-descriptions-item>
            <n-descriptions-item label="创建时间">
              {{ currentQuestion.created_at }}
            </n-descriptions-item>
          </n-descriptions>
        </n-space>
      </div>
    </n-modal>

    <!-- 参考：编辑题目内容对话框 -->
    <n-modal
      v-model:show="showEditModal"
      preset="dialog"
      title="参考 · 修改题目内容"
      positive-text="保存"
      negative-text="取消"
      style="width: 720px;"
      @positive-click="handleSaveEdit"
    >
      <div v-if="editForm">
        <n-form label-placement="left" label-width="100" style="margin-top: 12px;">
          <n-form-item label="题目类型">
            <n-select
              v-model:value="editForm.type"
              :options="typeOptions"
              style="width: 100%"
            />
          </n-form-item>
          <n-form-item label="题目内容">
            <n-input
              v-model:value="editForm.question"
              type="textarea"
              :autosize="{ minRows: 3, maxRows: 12 }"
              placeholder="题干内容，支持 LaTeX"
            />
          </n-form-item>
          <n-form-item label="选项">
            <n-space vertical style="width: 100%;">
              <div v-for="(opt, idx) in (editForm.options || [])" :key="idx" style="display: flex; align-items: center; gap: 8px;">
                <n-input
                  v-model:value="editForm.options[idx]"
                  type="textarea"
                  :autosize="{ minRows: 1, maxRows: 4 }"
                  placeholder="选项内容"
                  style="flex: 1;"
                />
                <n-button quaternary type="error" size="small" @click="removeOption(idx)">
                  <template #icon>
                    <n-icon><trash-outline /></n-icon>
                  </template>
                </n-button>
              </div>
              <n-button quaternary size="small" @click="addOption">+ 添加选项</n-button>
            </n-space>
          </n-form-item>
          <n-form-item label="正确答案">
            <n-input
              v-model:value="editForm.answer"
              type="textarea"
              :autosize="{ minRows: 1, maxRows: 4 }"
              placeholder="正确答案"
            />
          </n-form-item>
          <n-form-item label="题目解析">
            <n-input
              v-model:value="editForm.analysis"
              type="textarea"
              :autosize="{ minRows: 3, maxRows: 12 }"
              placeholder="答案解析，支持 LaTeX"
            />
          </n-form-item>

          <n-form-item label="题目图片">
            <n-space vertical style="width: 100%;">
              <n-alert type="info" v-if="editingQuestionId">
                图片会保存到后端本地 <n-text code>uploads</n-text>，并绑定为题目资源，可用于练习/错题等页面展示。
              </n-alert>

              <n-space v-if="editImages.length > 0">
                <n-image-group>
                  <n-image
                    v-for="img in editImages"
                    :key="img.id"
                    :src="resolveResourceUrl(img.resource_content)"
                    width="160"
                    object-fit="contain"
                    style="border-radius: 10px;"
                  />
                </n-image-group>
              </n-space>

              <n-space v-if="editImages.length > 0" wrap>
                <n-button
                  v-for="img in editImages"
                  :key="`del-${img.id}`"
                  quaternary
                  type="error"
                  size="small"
                  :loading="deletingResourceId === img.id"
                  @click="handleDeleteImage(img.id)"
                >
                  删除图片 #{{ img.resource_order + 1 }}
                </n-button>
              </n-space>

              <n-upload
                :custom-request="handleUploadImage"
                :show-file-list="false"
                accept="image/*"
                :disabled="!editingQuestionId"
              >
                <n-button type="primary" :disabled="!editingQuestionId" :loading="uploadingImage">
                  上传图片
                </n-button>
              </n-upload>
            </n-space>
          </n-form-item>
        </n-form>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useMessage, useDialog } from 'naive-ui'
import { questionApi, subjectApi, resourceApi } from '@/api'
import {
  SearchOutline,
  RefreshOutline,
  EyeOutline,
  TrashOutline,
  CreateOutline
} from '@vicons/ionicons5'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'
import FormulaRenderer from '@/components/FormulaRenderer.vue'
import TableRenderer from '@/components/TableRenderer.vue'

const message = useMessage()
const dialog = useDialog()
const userStore = useUserStore()
const { userId } = storeToRefs(userStore)
const loading = ref(false)
const questions = ref([])
const subjects = ref([])
const filterSubjectId = ref(null)
const filterType = ref(null)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const showDetailModal = ref(false)
const currentQuestion = ref(null)
const showEditModal = ref(false)
const editingQuestionId = ref(null)
const editForm = ref(null)
const questionResources = ref([])
const uploadingImage = ref(false)
const deletingResourceId = ref(null)

// 题型选项
const typeOptions = [
  { label: '单选题', value: 'single' },
  { label: '多选题', value: 'multiple' },
  { label: '判断题', value: 'judge' },
  { label: '填空题', value: 'fill' },
  { label: '大型题', value: 'major' }
]

// 题目集选项
const subjectOptions = computed(() => {
  return subjects.value.map(s => ({
    label: s.name,
    value: s.id
  }))
})

// 统计数据
const totalCount = computed(() => questions.value.length)
const typeCount = computed(() => {
  const count = {}
  questions.value.forEach(q => {
    count[q.type] = (count[q.type] || 0) + 1
  })
  return count
})

// 分页
const pageCount = computed(() => Math.ceil(totalCount.value / pageSize.value))
const paginatedQuestions = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return questions.value.slice(start, end)
})

// 获取题型标签类型
const getTypeTagType = (type) => {
  const map = {
    single: 'info',
    multiple: 'success',
    judge: 'warning',
    fill: 'error',
    major: 'primary'
  }
  return map[type] || 'default'
}

// 获取题型标签文本
const getTypeLabel = (type) => {
  const map = {
    single: '单选',
    multiple: '多选',
    judge: '判断',
    fill: '填空',
    major: '大型题'
  }
  return map[type] || type
}

// 获取题目集名称
const getSubjectName = (subjectId) => {
  const subject = subjects.value.find(s => s.id === subjectId)
  return subject ? subject.name : '未知题目集'
}

// 加载题目集列表
const loadSubjects = async () => {
  try {
    subjects.value = await subjectApi.list(userId.value)
  } catch (error) {
    message.error('加载题目集列表失败')
  }
}

// 加载题目列表
const loadQuestions = async () => {
  loading.value = true
  try {
    const params = {
      user_id: userId.value
    }
    
    if (filterSubjectId.value) {
      params.subject_id = filterSubjectId.value
    }
    
    if (filterType.value) {
      params.question_type = filterType.value
    }
    
    // 获取所有题目（后续可以改为服务器端分页）
    const allQuestions = filterSubjectId.value
      ? await questionApi.list(params)
      : []
    
    // 如果没有选择题目集，获取所有题目集的题目
    if (!filterSubjectId.value && subjects.value.length > 0) {
      const promises = subjects.value.map(subject =>
        questionApi.list({ user_id: userId.value, subject_id: subject.id })
      )
      const results = await Promise.all(promises)
      questions.value = results.flat()
    } else {
      questions.value = allQuestions
    }
    
    // 客户端题型过滤
    if (filterType.value) {
      questions.value = questions.value.filter(q => q.type === filterType.value)
    }
    
    // 客户端搜索过滤
    if (searchKeyword.value) {
      const keyword = searchKeyword.value.toLowerCase()
      questions.value = questions.value.filter(q =>
        q.question.toLowerCase().includes(keyword) ||
        q.answer.toLowerCase().includes(keyword) ||
        q.analysis.toLowerCase().includes(keyword)
      )
    }
    
    currentPage.value = 1
  } catch (error) {
    message.error(error.message || '加载题目失败')
  } finally {
    loading.value = false
  }
}

// 重置筛选
const handleReset = () => {
  filterSubjectId.value = null
  filterType.value = null
  searchKeyword.value = ''
  loadQuestions()
}

// 查看题目详情
const handleView = async (question) => {
  currentQuestion.value = question
  showDetailModal.value = true
  await loadResources(question.id)
}

// 参考：打开编辑题目内容
const handleReference = async (question) => {
  editingQuestionId.value = question.id
  editForm.value = {
    type: question.type,
    question: question.question || '',
    options: Array.isArray(question.options) ? [...question.options] : [],
    answer: question.answer || '',
    analysis: question.analysis || ''
  }
  showEditModal.value = true
  await loadResources(question.id)
}

const addOption = () => {
  if (!editForm.value.options) editForm.value.options = []
  editForm.value.options.push('')
}

const removeOption = (idx) => {
  if (editForm.value.options && editForm.value.options.length > idx) {
    editForm.value.options.splice(idx, 1)
  }
}

const loadResources = async (questionId) => {
  try {
    questionResources.value = await resourceApi.getByQuestion(questionId)
  } catch (e) {
    questionResources.value = []
  }
}

const detailImages = computed(() => {
  return (questionResources.value || []).filter(r => r.resource_type === 'image')
})

const editImages = computed(() => {
  return (questionResources.value || []).filter(r => r.resource_type === 'image')
})

const handleUploadImage = async ({ file }) => {
  if (!editingQuestionId.value) return
  uploadingImage.value = true
  try {
    await resourceApi.uploadImage(editingQuestionId.value, file.file, editImages.value.length)
    message.success('图片已上传')
    await loadResources(editingQuestionId.value)
  } catch (e) {
    message.error(e.message || '图片上传失败')
  } finally {
    uploadingImage.value = false
  }
}

const handleDeleteImage = async (resourceId) => {
  if (!resourceId) return
  deletingResourceId.value = resourceId
  try {
    await resourceApi.delete(resourceId)
    message.success('图片已删除')
    if (editingQuestionId.value) {
      await loadResources(editingQuestionId.value)
    }
  } catch (e) {
    message.error(e.message || '删除失败')
  } finally {
    deletingResourceId.value = null
  }
}

const resolveResourceUrl = (rawUrl) => {
  if (!rawUrl) return ''
  if (rawUrl.startsWith('/uploads/')) {
    return `/api${rawUrl}`
  }
  return rawUrl
}

// 保存编辑后的题目
const handleSaveEdit = async () => {
  if (!editingQuestionId.value || !editForm.value) return
  try {
    await questionApi.update(editingQuestionId.value, userId.value, {
      type: editForm.value.type,
      question: editForm.value.question,
      options: editForm.value.options || [],
      answer: editForm.value.answer,
      analysis: editForm.value.analysis
    })
    message.success('题目已更新')
    showEditModal.value = false
    editingQuestionId.value = null
    editForm.value = null
    questionResources.value = []
    loadQuestions()
  } catch (error) {
    message.error(error.message || '保存失败')
    throw error
  }
}

// 删除题目
const handleDelete = (question) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除题目「${question.question.substring(0, 30)}...」吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await questionApi.delete(question.id, userId.value)
        message.success('删除成功')
        loadQuestions()
      } catch (error) {
        message.error(error.message || '删除失败')
      }
    }
  })
}

// 分页处理
const handlePageChange = (page) => {
  currentPage.value = page
}

const handlePageSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

onMounted(() => {
  loadSubjects().then(() => {
    loadQuestions()
  })
})
</script>

<style scoped>
:deep(.n-list-item) {
  padding: 16px;
}

:deep(.n-thing-header) {
  margin-bottom: 8px;
}

.question-list-actions {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.question-list-actions :deep(.n-button) {
  min-width: 64px;
  margin: 0;
}
</style>
