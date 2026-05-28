/**
 * 用户状态管理 Store
 * 使用 Pinia 管理全局用户状态
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api'

/** 统一角色字段（兼容接口返回 role / 旧缓存无 role / 大小写） */
function normalizeUserPayload(raw) {
  if (!raw || typeof raw !== 'object') return null
  const r = raw.role ?? raw.user_role ?? raw.userRole
  let role = 'USER'
  if (r != null && String(r).trim() !== '') {
    const up = String(r).trim().toUpperCase()
    role = up === 'ADMIN' ? 'ADMIN' : 'USER'
  }
  return {
    ...raw,
    id: raw.id,
    username: raw.username,
    role,
    created_at: raw.created_at ?? raw.createdAt,
    createdAt: raw.created_at ?? raw.createdAt
  }
}

export const useUserStore = defineStore('user', () => {
  // 状态
  const currentUser = ref(null)
  const isLoggedIn = ref(false)

  // 计算属性
  const userId = computed(() => currentUser.value?.id || null)
  const username = computed(() => currentUser.value?.username || '')
  const isAdmin = computed(() => currentUser.value?.role === 'ADMIN')

  // 初始化用户信息（从 localStorage 恢复）
  const initUser = () => {
    const token = localStorage.getItem('token')
    const userStr = localStorage.getItem('user')

    if (token && userStr) {
      try {
        currentUser.value = normalizeUserPayload(JSON.parse(userStr))
        isLoggedIn.value = true
        return true
      } catch (error) {
        console.error('解析用户信息失败:', error)
        clearUser()
        return false
      }
    }
    return false
  }

  // 设置用户信息
  const setUser = (user, token) => {
    currentUser.value = normalizeUserPayload(user)
    isLoggedIn.value = true
    localStorage.setItem('token', token)
    localStorage.setItem('user', JSON.stringify(currentUser.value))
  }

  // 清除用户信息
  const clearUser = () => {
    currentUser.value = null
    isLoggedIn.value = false
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  // 登出
  const logout = () => {
    clearUser()
  }

  /** 从服务端刷新当前用户信息（含角色），用于升级权限后同步 */
  const refreshProfile = async () => {
    const token = localStorage.getItem('token')
    if (!token) return
    try {
      const u = await authApi.me()
      setUser(u, token)
    } catch {
      /* 忽略：可能未登录或 token 过期 */
    }
  }

  return {
    // 状态
    currentUser,
    isLoggedIn,
    // 计算属性
    userId,
    username,
    isAdmin,
    // 方法
    initUser,
    setUser,
    clearUser,
    logout,
    refreshProfile
  }
})
