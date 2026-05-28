<template>
  <div>
    <n-page-header title="开始练习" subtitle="自定义题量，开始你的练习">
    </n-page-header>

    <!-- 配置练习 -->
    <n-card v-if="!practicing" style="margin-top: 24px;">
      <template #header>
        <div class="art-title">配置练习</div>
      </template>
      <n-space vertical size="large">
        <!-- 选择题目集 -->
        <n-form-item label="选择题目集" class="art-form-item">
          <n-spin :show="loadingSubjects" style="width: 100%;">
            <n-space wrap>
              <n-button
                v-for="s in subjects"
                :key="s.id"
                class="subject-pill"
                :class="{ 'subject-pill--active': config.subject_id === s.id }"
                @click="setPracticeSubject(s.id)"
              >
                {{ s.name }}
              </n-button>
            </n-space>
          </n-spin>
        </n-form-item>

        <!-- 题型配置 -->
        <n-form-item label="题型与数量" class="art-form-item">
          <n-space vertical style="width: 100%;">
            <n-alert v-if="availableTypes.length === 0" type="warning">
              该题目集下暂无题目，请先导入题目
            </n-alert>

            <n-grid :cols="2" x-gap="16" y-gap="16">
              <n-gi v-for="type in typeOptions" :key="type.value">
                <n-card
                  class="type-card"
                  :class="{ 'type-card--active': selectedTypes.includes(type.value), 'type-card--disabled': !availableTypes.includes(type.value) }"
                  hoverable
                  @click="toggleType(type.value)"
                >
                  <n-space vertical>
                    <n-space justify="space-between" align="center">
                      <n-space align="center" size="small">
                        <n-tag :type="selectedTypes.includes(type.value) ? 'primary' : 'default'" size="small">
                          {{ type.label }}
                        </n-tag>
                        <n-text depth="3">点击选择</n-text>
                      </n-space>
                      <n-tag size="small" type="info">库存 {{ availableTypeCounts[type.value] || 0 }} 题</n-tag>
                    </n-space>

                    <div class="stepper-row">
                      <n-button
                        size="large"
                        secondary
                        :disabled="!selectedTypes.includes(type.value) || config.question_counts[type.value] <= 0"
                        @click.stop="decCount(type.value)"
                      >
                        -
                      </n-button>
                      <div class="stepper-value">
                        <div class="stepper-num">{{ config.question_counts[type.value] || 0 }}</div>
                        <div class="stepper-unit">题</div>
                      </div>
                      <n-button
                        size="large"
                        type="primary"
                        :disabled="!selectedTypes.includes(type.value) || (config.question_counts[type.value] >= (availableTypeCounts[type.value] || 0))"
                        @click.stop="incCount(type.value)"
                      >
                        +
                      </n-button>
                    </div>
                  </n-space>
                </n-card>
              </n-gi>
            </n-grid>
          </n-space>
        </n-form-item>

        <n-button
          type="primary"
          size="large"
          :disabled="!canStart"
          :loading="starting"
          @click="startPractice"
          block
        >
          开始练习
        </n-button>
      </n-space>
    </n-card>

    <!-- 练习中 -->
    <div v-else style="display: flex; gap: 16px; margin-top: 24px;">
      <!-- 左侧题目区域 - 显示所有题目 -->
      <div style="flex: 1;">
        <n-space vertical size="large">
          <n-card 
            v-for="(question, qIndex) in questions" 
            :key="question.id"
            :id="`question-${qIndex}`"
          >
            <template #header>
              <div style="display: flex; align-items: center; gap: 8px;">
                <span>{{ qIndex + 1 }}.({{ getTypeLabel(question.type) }}) </span>
                <TableRenderer :content="question.question" />
              </div>
            </template>
            <n-space vertical size="large">
              <n-image-group v-if="questionImages[question.id] && questionImages[question.id].length > 0">
                <n-space>
                  <n-image
                    v-for="img in questionImages[question.id]"
                    :key="img.id"
                    :src="resolveResourceUrl(img.resource_content)"
                    width="180"
                    object-fit="contain"
                    style="border-radius: 10px;"
                  />
                </n-space>
              </n-image-group>
              <n-text strong style="font-size: 14px; color: #666;">分值: {{ question.score || 2 }}分</n-text>
              
              <!-- 单选题和判断题 -->
              <div v-if="question.type === 'single' || question.type === 'judge'">
                <n-radio-group v-model:value="answers[question.id]">
                  <n-space vertical>
                    <n-radio
                      v-for="(option, index) in question.options"
                      :key="index"
                      :value="getOptionValue(option)"
                      size="large"
                    >
                      <FormulaRenderer :content="option" />
                    </n-radio>
                  </n-space>
                </n-radio-group>
              </div>

              <!-- 多选题 -->
              <div v-else-if="question.type === 'multiple'">
                <n-checkbox-group v-model:value="multiAnswers[question.id]">
                  <n-space vertical>
                    <n-checkbox
                      v-for="(option, index) in question.options"
                      :key="index"
                      :value="getOptionValue(option)"
                      size="large"
                    >
                      <FormulaRenderer :content="option" />
                    </n-checkbox>
                  </n-space>
                </n-checkbox-group>
              </div>

              <!-- 填空题 -->
              <template v-if="question.type === 'fill'">
                <n-input
                  v-model:value="answers[question.id]"
                  placeholder="请输入答案"
                  size="large"
                />
              </template>
              <!-- 大题：文本+图片上传 -->
              <template v-else-if="question.type === 'major'">
                <n-input
                  v-model:value="answers[question.id]"
                  placeholder="请输入答案"
                  size="large"
                  style="margin-bottom: 8px;"
                />
                <ImageUploader v-model="majorImages[question.id]" :max="3" />
              </template>
            </n-space>
          </n-card>
        </n-space>
      </div>

      <!-- 右侧答题卡 - 固定定位 -->
      <div style="width: 300px; position: sticky; top: 24px; align-self: flex-start;">
        <n-card :title="`答题卡 (${answeredCount}/${questions.length})`" size="small">
          <n-space vertical size="large">
            <!-- 题号网格 -->
            <div style="display: grid; grid-template-columns: repeat(5, 1fr); gap: 8px;">
              <n-button
                v-for="(q, idx) in questions"
                :key="idx"
                :type="isAnswered(idx) ? 'primary' : 'default'"
                size="medium"
                @click="scrollToQuestion(idx)"
                style="width: 100%;"
              >
                {{ idx + 1 }}
              </n-button>
            </div>

            <!-- 提交按钮 -->
            <n-button
              type="primary"
              block
              size="large"
              :loading="submitting"
              @click="submitAnswers"
            >
              提交
            </n-button>
          </n-space>
        </n-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { useRouter } from 'vue-router'
import { subjectApi, questionApi, practiceApi, resourceApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'
import FormulaRenderer from '@/components/FormulaRenderer.vue'
import TableRenderer from '@/components/TableRenderer.vue'
import ImageUploader from '@/components/ImageUploader.vue'

const message = useMessage()
const router = useRouter()
const userStore = useUserStore()
const { userId } = storeToRefs(userStore)
const loadingSubjects = ref(false)
const starting = ref(false)
const submitting = ref(false)
const practicing = ref(false)
const subjects = ref([])
const availableTypes = ref([])
const availableTypeCounts = ref({
  single: 0,
  multiple: 0,
  judge: 0,
  fill: 0,
  major: 0
})
const selectedTypes = ref([])
const questions = ref([])
const answers = ref({})
const multiAnswers = ref({})
const majorImages = ref({}) // { [questionId]: [base64, ...] }
const questionImages = ref({})

const config = ref({
  subject_id: null,
  question_counts: {
    single: 0,
    multiple: 0,
    judge: 0,
    fill: 0,
    major: 0
  }
})

const typeOptions = [
  { label: '单选题', value: 'single' },
  { label: '多选题', value: 'multiple' },
  { label: '判断题', value: 'judge' },
  { label: '填空题', value: 'fill' },
  { label: '大型题', value: 'major' }
]

const subjectOptions = computed(() => {
  return subjects.value.map(s => ({
    label: s.name,
    value: s.id
  }))
})

const answeredCount = computed(() => {
  let count = 0
  questions.value.forEach(q => {
    if (q.type === 'multiple') {
      if (multiAnswers.value[q.id] && multiAnswers.value[q.id].length > 0) {
        count++
      }
    } else {
      if (answers.value[q.id]) {
        count++
      }
    }
  })
  return count
})

const canStart = computed(() => {
  if (!config.value.subject_id) return false
  const total = Object.values(config.value.question_counts).reduce((a, b) => a + b, 0)
  return total > 0
})

const getTypeLabel = (type) => {
  const map = {
    single: '单选题',
    multiple: '多选题',
    judge: '判断题',
    fill: '填空题',
    major: '大型题'
  }
  return map[type] || type
}

const getOptionValue = (option) => {
  const match = option.match(/^([A-Z])\./)
  return match ? match[1] : option
}

const isAnswered = (index) => {
  const q = questions.value[index]
  if (!q) return false
  
  if (q.type === 'multiple') {
    return multiAnswers.value[q.id] && multiAnswers.value[q.id].length > 0
  }
  return answers.value[q.id] && answers.value[q.id].trim() !== ''
}

const loadSubjects = async () => {
  loadingSubjects.value = true
  try {
    subjects.value = await subjectApi.list(userId.value)
  } catch (error) {
    message.error(error.message || '加载题目集列表失败')
  } finally {
    loadingSubjects.value = false
  }
}

const handleSubjectChange = async (subjectId) => {
  try {
    const [typesData, allQuestions] = await Promise.all([
      questionApi.getTypes(subjectId, userId.value),
      questionApi.list({ user_id: userId.value, subject_id: subjectId })
    ])
    availableTypes.value = typesData.types
    const counts = { single: 0, multiple: 0, judge: 0, fill: 0, major: 0 }
    ;(allQuestions || []).forEach(q => {
      if (counts[q.type] !== undefined) counts[q.type] += 1
    })
    availableTypeCounts.value = counts
    selectedTypes.value = []
    config.value.question_counts = {
      single: 0,
      multiple: 0,
      judge: 0,
      fill: 0,
      major: 0
    }
  } catch (error) {
    message.error(error.message || '获取题型失败')
  }
}

const setPracticeSubject = async (subjectId) => {
  if (config.value.subject_id === subjectId) return
  config.value.subject_id = subjectId
  await handleSubjectChange(subjectId)
}

const toggleType = (type) => {
  if (!availableTypes.value.includes(type)) return
  if (selectedTypes.value.includes(type)) {
    selectedTypes.value = selectedTypes.value.filter(t => t !== type)
    config.value.question_counts[type] = 0
  } else {
    selectedTypes.value.push(type)
    if (!config.value.question_counts[type] || config.value.question_counts[type] <= 0) {
      config.value.question_counts[type] = 1
    }
  }
}

const incCount = (type) => {
  if (!selectedTypes.value.includes(type)) return
  const max = availableTypeCounts.value[type] || 0
  const cur = config.value.question_counts[type] || 0
  if (cur < max) config.value.question_counts[type] = cur + 1
}

const decCount = (type) => {
  if (!selectedTypes.value.includes(type)) return
  const cur = config.value.question_counts[type] || 0
  if (cur > 0) config.value.question_counts[type] = cur - 1
}

const startPractice = async () => {
  starting.value = true
  try {
    const data = await practiceApi.start({
      user_id: userId.value,
      subject_id: config.value.subject_id,
      question_counts: config.value.question_counts
    })
    
    questions.value = data.questions
    practicing.value = true
    
    // 初始化答案对象，确保每个题目都有对应的空答案
    const newAnswers = {}
    const newMultiAnswers = {}
    data.questions.forEach(q => {
      if (q.type === 'multiple') {
        newMultiAnswers[q.id] = []
      } else {
        newAnswers[q.id] = ''
      }
      if (q.type === 'major') {
        majorImages.value[q.id] = []
      }
    })
    answers.value = newAnswers
    multiAnswers.value = newMultiAnswers

    // 加载每道题的图片资源
    const imgMap = {}
    await Promise.all(
      data.questions.map(async (q) => {
        try {
          const list = await resourceApi.getByQuestion(q.id)
          imgMap[q.id] = (list || []).filter(r => r.resource_type === 'image')
        } catch (e) {
          imgMap[q.id] = []
        }
      })
    )
    questionImages.value = imgMap
  } catch (error) {
    message.error(error.message || '开始练习失败')
  } finally {
    starting.value = false
  }
}

const scrollToQuestion = (index) => {
  const element = document.getElementById(`question-${index}`)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'center' })
  }
}

const submitAnswers = async () => {
  // 检查是否所有题都已作答
  const unanswered = questions.value.filter(q => {
    if (q.type === 'multiple') {
      return !multiAnswers.value[q.id] || multiAnswers.value[q.id].length === 0
    }
    return !answers.value[q.id]
  })

  if (unanswered.length > 0) {
    message.warning(`还有 ${unanswered.length} 题未作答`)
    return
  }

  submitting.value = true
  try {
    // 格式化答案
    const formattedAnswers = questions.value.map(q => {
      let user_answer = ''
      if (q.type === 'multiple') {
        user_answer = (multiAnswers.value[q.id] || []).sort().join(',')
      } else {
        user_answer = answers.value[q.id] || ''
      }
      let images = q.type === 'major' ? (majorImages.value[q.id] || []) : []
      return {
        question_id: q.id,
        user_answer,
        images
      }
    })

    const data = await practiceApi.submit({
      user_id: userId.value,
      subject_id: config.value.subject_id,
      answers: formattedAnswers
    })

    // 提交成功后跳转到练习记录页面
    message.success(`提交成功！正确 ${data.correct} 题，错误 ${data.wrong} 题，准确率 ${data.accuracy}%，成绩 ${data.grade}`)
    
    // 延迟跳转，让用户看到成功提示
    setTimeout(() => {
      router.push('/practice-history')
    }, 1500)
  } catch (error) {
    message.error(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

const resetPractice = () => {
  practicing.value = false
  questions.value = []
  answers.value = {}
  multiAnswers.value = {}
  questionImages.value = {}
}

const resolveResourceUrl = (rawUrl) => {
  if (!rawUrl) return ''
  if (rawUrl.startsWith('/uploads/')) return `/api${rawUrl}`
  return rawUrl
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

.art-form-item :deep(.n-form-item-label__text) {
  font-size: 18px;
  font-weight: 900;
  letter-spacing: 0.8px;
  background: linear-gradient(135deg, #0ea5e9, #22c55e);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  text-shadow: 0 8px 18px rgba(14, 165, 233, 0.18);
}

.type-card {
  min-height: 120px;
  cursor: pointer;
  border: 1px solid rgba(148, 163, 184, 0.28);
  background: #ffffff;
  transition: all 0.22s ease;
}

.subject-pill {
  border-radius: 999px;
  border: 1px solid rgba(59, 130, 246, 0.25);
  background: #ffffff;
  transition: all 0.22s ease;
}

.subject-pill:hover {
  transform: translateY(-1px);
  border-color: rgba(59, 130, 246, 0.55);
}

.subject-pill--active {
  color: #fff !important;
  border-color: transparent !important;
  background: linear-gradient(135deg, #1890ff 0%, #3b82f6 55%, #2563eb 100%) !important;
  box-shadow: 0 10px 24px rgba(37, 99, 235, 0.28);
}

.type-card--active {
  border-color: rgba(16, 185, 129, 0.22) !important;
  background: linear-gradient(140deg, #34d399 0%, #22c55e 45%, #16a34a 100%) !important;
  box-shadow: 0 14px 30px rgba(34, 197, 94, 0.32), 0 0 0 3px rgba(74, 222, 128, 0.2) !important;
  transform: translateY(-2px);
}

.type-card--active :deep(.n-text),
.type-card--active :deep(.n-tag),
.type-card--active :deep(.n-tag__content) {
  color: #fff !important;
}

.type-card--disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.stepper-row {
  display: grid;
  grid-template-columns: 1fr 1.3fr 1fr;
  gap: 12px;
  align-items: center;
  margin-top: 6px;
}

.stepper-value {
  text-align: center;
  padding: 10px 12px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.65);
  border: 1px solid rgba(24, 144, 255, 0.12);
}

.type-card--active .stepper-value {
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.45);
}

.stepper-num {
  font-size: 26px;
  font-weight: 800;
  line-height: 1.1;
}

.stepper-unit {
  margin-top: 2px;
  font-size: 14px;
  color: rgba(31, 41, 55, 0.65);
}

.type-card--active .stepper-unit,
.type-card--active .stepper-num {
  color: #ffffff;
}
</style>
