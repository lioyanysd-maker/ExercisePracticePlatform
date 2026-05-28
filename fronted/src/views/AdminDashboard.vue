<template>
  <div class="admin-dashboard">
    <n-page-header title="控制台" subtitle="系统概览、练习趋势、动态与首页公告">
      <template #extra>
        <n-button :loading="loading" @click="loadAll">
          <template #icon>
            <n-icon><refresh-outline /></n-icon>
          </template>
          刷新
        </n-button>
      </template>
    </n-page-header>

    <n-spin :show="loading">
      <n-space vertical size="large" style="margin-top: 16px">
        <!-- 指标卡片 -->
        <n-grid cols="2 s:3 m:3 l:6" responsive="screen" :x-gap="14" :y-gap="14">
          <n-gi>
            <n-card class="metric-card" size="small" :bordered="false">
              <div class="metric-label">注册用户</div>
              <div class="metric-value">{{ overview.total_users ?? 0 }}</div>
            </n-card>
          </n-gi>
          <n-gi>
            <n-card class="metric-card metric-b" size="small" :bordered="false">
              <div class="metric-label">题目集</div>
              <div class="metric-value">{{ overview.total_subjects ?? 0 }}</div>
            </n-card>
          </n-gi>
          <n-gi>
            <n-card class="metric-card metric-c" size="small" :bordered="false">
              <div class="metric-label">题目总数</div>
              <div class="metric-value">{{ overview.total_questions ?? 0 }}</div>
            </n-card>
          </n-gi>
          <n-gi>
            <n-card class="metric-card metric-d" size="small" :bordered="false">
              <div class="metric-label">错题记录</div>
              <div class="metric-value">{{ overview.total_error_book_entries ?? 0 }}</div>
            </n-card>
          </n-gi>
          <n-gi>
            <n-card class="metric-card metric-e" size="small" :bordered="false">
              <div class="metric-label">今日练习场次</div>
              <div class="metric-value">{{ overview.today_practice_sessions ?? 0 }}</div>
            </n-card>
          </n-gi>
          <n-gi>
            <n-card class="metric-card metric-f" size="small" :bordered="false">
              <div class="metric-label">今日活跃学员</div>
              <div class="metric-value">{{ overview.today_active_users ?? 0 }}</div>
            </n-card>
          </n-gi>
        </n-grid>

        <n-grid cols="1 l:2" :x-gap="16" :y-gap="16" responsive="screen">
          <!-- 近 7 日练习趋势 -->
          <n-gi>
            <n-card title="近 7 日练习场次" class="panel-card">
              <div class="trend-wrap">
                <div v-for="(row, idx) in practiceTrend" :key="idx" class="trend-col">
                  <div class="trend-bar-area">
                    <div
                      class="trend-bar"
                      :style="{ height: barHeightPct(row.practice_count) }"
                      :title="String(row.practice_count)"
                    />
                  </div>
                  <span class="trend-label">{{ row.label }}</span>
                </div>
              </div>
              <n-text depth="3" style="font-size: 12px">按系统日统计 practice_sessions 记录数</n-text>
            </n-card>
          </n-gi>

          <!-- 动态 -->
          <n-gi>
            <n-card title="最近动态" class="panel-card">
              <n-scrollbar style="max-height: 280px">
                <n-empty v-if="!activity.length" description="暂无动态" />
                <div v-else class="feed">
                  <div v-for="(it, i) in activity" :key="i" class="feed-item">
                    <span class="feed-dot" :class="it.event_type === 'register' ? 'is-reg' : 'is-prac'" />
                    <div class="feed-body">
                      <div class="feed-text">{{ it.text }}</div>
                      <div class="feed-time">{{ formatTime(it.occurred_at) }}</div>
                    </div>
                  </div>
                </div>
              </n-scrollbar>
            </n-card>
          </n-gi>
        </n-grid>

        <!-- 首页公告 -->
        <n-card title="学员端首页公告" class="panel-card banner-card">
          <n-space vertical size="medium">
            <n-space align="center">
              <span>显示公告条</span>
              <n-switch :value="bannerEnabled" @update:value="(v) => (bannerEnabled = v)" />
            </n-space>
            <n-input
              :value="bannerText"
              type="textarea"
              placeholder="展示在学员首页顶部的提示文案"
              :autosize="{ minRows: 2, maxRows: 5 }"
              @update:value="(v) => (bannerText = v)"
            />
            <n-space>
              <n-button type="primary" :loading="savingBanner" @click="saveBanner">保存公告</n-button>
            </n-space>
          </n-space>
        </n-card>
      </n-space>
    </n-spin>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import {
  NPageHeader,
  NButton,
  NIcon,
  NSpin,
  NSpace,
  NGrid,
  NGi,
  NCard,
  NText,
  NScrollbar,
  NEmpty,
  NInput,
  NSwitch,
  useMessage
} from 'naive-ui'
import { RefreshOutline } from '@vicons/ionicons5'
import { adminDashboardApi } from '@/api'

const message = useMessage()
const loading = ref(false)
const savingBanner = ref(false)

const overview = reactive({
  total_users: 0,
  total_subjects: 0,
  total_questions: 0,
  total_error_book_entries: 0,
  today_practice_sessions: 0,
  today_active_users: 0
})

const practiceTrend = ref([])
const activity = ref([])

const bannerText = ref('')
const bannerEnabled = ref(true)

const maxTrend = computed(() => {
  const arr = practiceTrend.value.map((r) => r.practice_count ?? 0)
  return Math.max(1, ...arr)
})

function barHeightPct(count) {
  const c = count ?? 0
  const pct = (c / maxTrend.value) * 100
  return `${Math.max(c > 0 ? 12 : 0, pct)}%`
}

function formatTime(t) {
  if (!t) return '—'
  try {
    const d = typeof t === 'string' ? new Date(t) : new Date(t)
    if (Number.isNaN(d.getTime())) return String(t)
    return d.toLocaleString('zh-CN', { hour12: false })
  } catch {
    return String(t)
  }
}

async function loadSummary() {
  const res = await adminDashboardApi.summary({ trend_days: 7 })
  const o = res.overview || {}
  overview.total_users = o.total_users
  overview.total_subjects = o.total_subjects
  overview.total_questions = o.total_questions
  overview.total_error_book_entries = o.total_error_book_entries
  overview.today_practice_sessions = o.today_practice_sessions
  overview.today_active_users = o.today_active_users
  practiceTrend.value = res.practice_trend || []
  activity.value = res.activity || []
}

async function loadBanner() {
  const b = await adminDashboardApi.getBanner()
  bannerText.value = b.home_banner_text || ''
  bannerEnabled.value = b.home_banner_enabled !== false
}

async function loadAll() {
  loading.value = true
  try {
    await Promise.all([loadSummary(), loadBanner()])
  } catch (e) {
    message.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function saveBanner() {
  savingBanner.value = true
  try {
    await adminDashboardApi.updateBanner({
      home_banner_text: bannerText.value,
      home_banner_enabled: bannerEnabled.value
    })
    message.success('公告已保存')
  } catch (e) {
    message.error(e.message || '保存失败')
  } finally {
    savingBanner.value = false
  }
}

onMounted(() => {
  loadAll()
})
</script>

<style scoped>
.admin-dashboard {
  max-width: 1200px;
  margin: 0 auto;
}

.metric-card {
  background: linear-gradient(145deg, #f8fafc 0%, #eef2ff 100%);
  border-radius: 12px;
  box-shadow: 0 1px 0 rgba(15, 23, 42, 0.06);
}
.metric-b {
  background: linear-gradient(145deg, #f0fdf4 0%, #ecfdf5 100%);
}
.metric-c {
  background: linear-gradient(145deg, #fffbeb 0%, #fff7ed 100%);
}
.metric-d {
  background: linear-gradient(145deg, #fdf2f8 0%, #fce7f3 100%);
}
.metric-e {
  background: linear-gradient(145deg, #ecfeff 0%, #e0f2fe 100%);
}
.metric-f {
  background: linear-gradient(145deg, #f5f3ff 0%, #ede9fe 100%);
}
.metric-label {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 6px;
}
.metric-value {
  font-size: 26px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.02em;
}

.panel-card {
  border-radius: 12px;
}

.trend-wrap {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 8px;
  padding: 8px 4px 0;
  margin-bottom: 8px;
}
.trend-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.trend-bar-area {
  height: 140px;
  width: 100%;
  max-width: 40px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}
.trend-bar {
  width: 100%;
  background: linear-gradient(180deg, #38bdf8 0%, #0ea5e9 100%);
  border-radius: 6px 6px 2px 2px;
  min-height: 0;
  transition: height 0.25s ease;
}
.trend-label {
  font-size: 11px;
  color: #64748b;
  margin-top: 8px;
}

.feed {
  padding-right: 8px;
}
.feed-item {
  display: flex;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
}
.feed-item:last-child {
  border-bottom: none;
}
.feed-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-top: 6px;
  flex-shrink: 0;
}
.feed-dot.is-reg {
  background: #22c55e;
}
.feed-dot.is-prac {
  background: #0ea5e9;
}
.feed-text {
  font-size: 14px;
  color: #334155;
  line-height: 1.45;
}
.feed-time {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
}

.banner-card {
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
}
</style>
