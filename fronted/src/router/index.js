import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { adminArea: true },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/AdminDashboard.vue'),
        meta: { requiresAdmin: true, adminTitle: '控制台' }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/AdminUsers.vue'),
        meta: { requiresAdmin: true, adminTitle: '用户管理' }
      }
    ]
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { userApp: true }
  },
  {
    path: '/subjects',
    name: 'Subjects',
    component: () => import('@/views/Subjects.vue'),
    meta: { userApp: true }
  },
  {
    path: '/import',
    name: 'ImportQuestion',
    component: () => import('@/views/ImportQuestion.vue'),
    meta: { userApp: true }
  },
  {
    path: '/practice',
    name: 'Practice',
    component: () => import('@/views/Practice.vue'),
    meta: { userApp: true }
  },
  {
    path: '/errors',
    name: 'ErrorBook',
    component: () => import('@/views/ErrorBook.vue'),
    meta: { userApp: true }
  },
  {
    path: '/error-practice',
    name: 'ErrorPractice',
    component: () => import('@/views/ErrorPractice.vue'),
    meta: { userApp: true }
  },
  {
    path: '/model-config',
    name: 'ModelConfig',
    component: () => import('@/views/ModelConfig.vue'),
    meta: { userApp: true }
  },
  {
    path: '/question-bank',
    name: 'QuestionBank',
    component: () => import('@/views/QuestionBank.vue'),
    meta: { userApp: true }
  },
  {
    path: '/practice-history',
    name: 'PracticeHistory',
    component: () => import('@/views/PracticeHistory.vue'),
    meta: { userApp: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

function isAdminUser(store) {
  return String(store.currentUser?.role ?? '')
    .trim()
    .toUpperCase() === 'ADMIN'
}

router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()
  if (!userStore.isLoggedIn) {
    userStore.initUser()
  }

  const isUserLogin = to.path === '/login'
  const inAdminArea = to.path === '/admin' || to.path.startsWith('/admin/')

  if (!userStore.isLoggedIn) {
    if (inAdminArea) {
      next('/login')
      return
    }
    if (!isUserLogin) {
      next('/login')
      return
    }
    next()
    return
  }

  if (userStore.currentUser?.role !== 'ADMIN' && userStore.currentUser?.role !== 'USER') {
    await userStore.refreshProfile()
  }

  const admin = isAdminUser(userStore)

  if (admin) {
    if (isUserLogin || to.meta.userApp || to.path === '/') {
      next('/admin')
      return
    }
    if (to.meta.requiresAdmin || to.meta.adminArea) {
      next()
      return
    }
    if (inAdminArea) {
      next()
      return
    }
    next('/admin')
    return
  }

  // 普通用户
  if (inAdminArea) {
    next('/')
    return
  }
  if (userStore.isLoggedIn && isUserLogin) {
    next('/')
    return
  }
  if (to.meta.requiresAdmin) {
    next('/')
    return
  }
  next()
})

export default router
