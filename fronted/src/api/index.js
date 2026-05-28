import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 120000  // 全局超时改为 120 秒
})

// 请求拦截器 - 添加token
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器（兼容 Python FastAPI detail / Java Spring message）
request.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    const data = error.response?.data
    const message = data?.message || data?.detail || error.message || '请求失败'
    return Promise.reject(new Error(typeof message === 'string' ? message : JSON.stringify(message)))
  }
)

// ==================== 用户认证 API ====================
export const authApi = {
  // 用户注册
  register: (data) => request.post('/auth/register', data),

  // 用户登录
  login: (data) => request.post('/auth/login', data),

  // 获取当前用户（依赖请求头 Authorization: Bearer <token>）
  me: () => request.get('/auth/me'),

  // 用户登出
  logout: () => request.post('/auth/logout')
}

// ==================== 公开站点信息（无需登录） ====================
export const publicSiteApi = {
  getBanner: () => request.get('/public/site/banner')
}

// ==================== 管理员：控制台与站点公告 ====================
export const adminDashboardApi = {
  summary: (params) => request.get('/admin/dashboard/summary', { params }),
  getBanner: () => request.get('/admin/site/banner'),
  updateBanner: (data) => request.put('/admin/site/banner', data)
}

// ==================== 管理员：用户管理 API ====================
export const adminUserApi = {
  list: (params) => request.get('/admin/users', { params }),
  get: (id) => request.get(`/admin/users/${id}`),
  create: (data) => request.post('/admin/users', data),
  update: (id, data) => request.put(`/admin/users/${id}`, data),
  delete: (id) => request.delete(`/admin/users/${id}`)
}

// ==================== 题目集管理 API ====================
export const subjectApi = {
  // 创建题目集
  create: (data) => request.post('/subjects', data),

  // 获取题目集列表
  list: (userId) => request.get('/subjects', { params: { user_id: userId } }),

  // 获取单个题目集
  get: (subjectId, userId) => request.get(`/subjects/${subjectId}`, { params: { user_id: userId } }),

  // 删除题目集
  delete: (subjectId, userId) => request.delete(`/subjects/${subjectId}`, { params: { user_id: userId } })
}

// ==================== 题目管理 API ====================
export const questionApi = {
  // 创建题目
  create: (data) => request.post('/questions', data),

  // 获取题目列表
  list: (params) => request.get('/questions', { params }),

  // 获取题目集题型
  getTypes: (subjectId, userId) =>
    request.get(`/questions/types/${subjectId}`, { params: { user_id: userId } }),

  // 获取单个题目
  get: (questionId, userId) => request.get(`/questions/${questionId}`, { params: { user_id: userId } }),

  // 更新题目（参考/编辑题目内容）
  update: (questionId, userId, data) =>
    request.put(`/questions/${questionId}`, data, { params: { user_id: userId } }),

  // 删除题目
  delete: (questionId, userId) => request.delete(`/questions/${questionId}`, { params: { user_id: userId } })
}

// ==================== 题目导入 API ====================
export const importApi = {
  // 从文件导入
  fromFile: (userId, subjectId, file) => {
    const formData = new FormData()
    formData.append('user_id', userId)
    formData.append('subject_id', subjectId)
    formData.append('file', file)
    return request.post('/import/file', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 180000  // 文件导入单独设置 180 秒超时
    })
  },

  // 从图片导入
  fromImage: (userId, subjectId, image) => {
    const formData = new FormData()
    formData.append('user_id', userId)
    formData.append('subject_id', subjectId)
    formData.append('image', image)
    return request.post('/import/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 180000  // 图片导入单独设置 180 秒超时（AI 识别需要较长时间）
    })
  },

  // 从文本导入
  fromText: (data) => request.post('/import/text', data),

  // 预览（从 DOCX 提取图片并按题号初步匹配）
  previewDoc: (userId, subjectId, file) => {
    const formData = new FormData()
    formData.append('user_id', userId)
    formData.append('subject_id', subjectId)
    formData.append('file', file)
    return request.post('/import/doc/preview', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 180000
    })
  },

  // 智能分析：AI识题 + 图片提取
  analyzeDoc: (userId, subjectId, file) => {
    const formData = new FormData()
    formData.append('user_id', userId)
    formData.append('subject_id', subjectId)
    formData.append('file', file)
    return request.post('/import/doc/analyze', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 240000
    })
  },

  // 智能确认：导入题目并按映射绑定图片
  confirmDoc: (data) => request.post('/import/doc/confirm', data)
}

// 题号-图片绑定（用于带图片 Word 试卷预览后确认）
export const docBindApi = {
  bindImages: (data) => request.post('/import/doc/bind-images', data)
}

// ==================== 练习 API ====================
export const practiceApi = {
  // 开始练习
  start: (data) => request.post('/practice/start', data),

  // 提交答案
  submit: (data) => request.post('/practice/submit', data),

  // 今日统计
  todayStats: (userId) =>
    request.get('/practice/statistics/today', { params: { user_id: userId } }),

  // 本周统计
  weekStats: (userId) =>
    request.get('/practice/statistics/week', { params: { user_id: userId } }),

  // 全部统计
  allStats: (userId) =>
    request.get('/practice/statistics/all', { params: { user_id: userId } }),

  // 首页统计
  homeStats: (userId) =>
    request.get('/practice/statistics/home', { params: { user_id: userId } }),

  // 练习历史（params: user_id, subject_id?, limit?）
  history: (params) =>
    request.get('/practice/history', { params }),

  // 某次练习的答题详情
  getSessionDetails: (sessionId, userId) =>
    request.get(`/practice/session/${sessionId}/details`, { params: { user_id: userId } })
}

// ==================== 错题集 API ====================
export const errorApi = {
  // 获取错题列表
  list: (params) => request.get('/errors', { params }),

  // 获取错题数量
  count: (params) => request.get('/errors/count', { params }),

  // 获取错题题型
  getTypes: (subjectId, userId) =>
    request.get(`/errors/types/${subjectId}`, { params: { user_id: userId } }),

  // 开始错题练习
  practice: (data) => request.post('/errors/practice', data),

  // 移除错题
  remove: (errorId, userId) => request.delete(`/errors/${errorId}`, { params: { user_id: userId } })
}

// ==================== AI 模型配置 API ====================
export const modelApi = {
  // 创建模型配置
  create: (data) => request.post('/models', data),

  // 获取模型配置列表
  list: (userId) => request.get('/models', { params: { user_id: userId } }),

  // 获取单个模型配置
  get: (modelId) => request.get(`/models/${modelId}`),

  // 更新模型配置
  update: (modelId, data) => request.put(`/models/${modelId}`, data),

  // 删除模型配置
  delete: (modelId) => request.delete(`/models/${modelId}`)
}

// ==================== 题目资源 API ====================
export const resourceApi = {
  // 创建资源
  create: (data) => request.post('/resources', data),

  // 获取题目的所有资源
  getByQuestion: (questionId) => request.get(`/resources/question/${questionId}`),

  // 上传题目图片到本地 uploads 并绑定资源记录
  uploadImage: (questionId, file, resourceOrder = 0) => {
    const formData = new FormData()
    formData.append('question_id', questionId)
    formData.append('resource_order', resourceOrder)
    formData.append('file', file)
    return request.post('/resources/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // 删除资源
  delete: (resourceId) => request.delete(`/resources/${resourceId}`)
}

// ==================== 共享管理 API ====================
export const shareApi = {
  // 设置共享
  setShare: (data) => request.post('/shares', data),

  // 取消共享
  cancelShare: (subjectId, params) =>
    request.delete(`/shares/${subjectId}`, { params }),

  // 获取共享状态
  getStatus: (subjectId) => request.get(`/shares/status/${subjectId}`),

  // 获取我共享的题目集
  getMyShared: (userId) =>
    request.get('/shares/my-shared', { params: { user_id: userId } }),

  // 搜索用户
  searchUsers: (keyword, userId) =>
    request.get('/shares/users/search', { params: { keyword, current_user_id: userId } })
}

export default request
