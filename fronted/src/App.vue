<template>
  <n-config-provider :theme-overrides="themeOverrides">
    <n-message-provider>
      <n-dialog-provider>
        <n-notification-provider>
          <!-- 管理后台：独立全屏路由，不使用学员端布局 -->
          <router-view v-if="isAdminBackend" />
          <!-- 学员端主界面 -->
          <template v-else-if="!isPlainLoginRoute">
          <div class="app-bg-orb orb-a"></div>
          <div class="app-bg-orb orb-b"></div>
          <div class="app-bg-orb orb-c"></div>
          <n-layout class="main-layout">
            <n-layout-header bordered class="app-header">
              <div class="header-title-wrap">
                <h2 class="header-title">快练习题系统</h2>
              </div>
              <n-space v-if="isLoggedIn">
                <n-button @click="showSystemInfo">
                  <template #icon>
                    <n-icon><information-circle-outline /></n-icon>
                  </template>
                  系统信息
                </n-button>
                <span class="header-username">{{ currentUser?.username }}</span>
                <n-button text @click="handleLogout">
                  <template #icon>
                    <n-icon><log-out-outline /></n-icon>
                  </template>
                  退出
                </n-button>
              </n-space>
            </n-layout-header>
            
            <n-layout has-sider class="main-content-layout">
              <n-layout-sider
                bordered
                show-trigger
                collapse-mode="width"
                :collapsed-width="78"
                :width="300"
                :native-scrollbar="false"
                class="app-sider"
              >
                <n-menu
                  :collapsed-width="78"
                  :collapsed-icon-size="24"
                  :options="menuOptions"
                  :value="activeKey"
                  @update:value="handleMenuSelect"
                />
              </n-layout-sider>
              
              <n-layout-content
                content-style="padding: 24px;"
                :native-scrollbar="false"
                class="app-content"
              >
                <router-view v-slot="{ Component }">
                  <transition name="page-fade-slide" mode="out-in">
                    <component :is="Component" />
                  </transition>
                </router-view>
              </n-layout-content>
            </n-layout>
          </n-layout>
          </template>
          <!-- 登录页：学员端 / 管理端 -->
          <div v-else class="login-route-wrap">
            <router-view />
          </div>
        </n-notification-provider>
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { h, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { createDiscreteApi } from 'naive-ui'
import { storeToRefs } from 'pinia'
import { 
  NIcon,
  NConfigProvider,
  NMessageProvider,
  NDialogProvider,
  NNotificationProvider,
  NLayout,
  NLayoutHeader,
  NLayoutSider,
  NLayoutContent,
  NMenu,
  NSpace,
  NButton
} from 'naive-ui'
import {
  HomeOutline,
  BookOutline,
  CloudUploadOutline,
  CreateOutline,
  AlertCircleOutline,
  SettingsOutline,
  LogOutOutline,
  LibraryOutline,
  TimeOutline,
  InformationCircleOutline
} from '@vicons/ionicons5'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 创建独立的 message 和 notification API
const { message, notification } = createDiscreteApi(['message', 'notification'])

// 认证状态（使用 store）
const { isLoggedIn, currentUser } = storeToRefs(userStore)

/** 登录页（全屏表单，无侧栏） */
const isPlainLoginRoute = computed(() => route.path === '/login')
/** 管理后台区域（由 AdminLayout 自绘侧栏） */
const isAdminBackend = computed(() => {
  const p = route.path
  return p === '/admin' || p.startsWith('/admin/')
})

// 登出处理
const handleLogout = () => {
  userStore.logout()
  router.push('/login')
  message.info('已退出登录')
}

// 显示系统信息
const showSystemInfo = () => {
  notification.info({
    title: '系统信息',
    content: '快练习题系统 - 智能练习与学习管理平台',
    meta: '版本：v1.0.0',
    duration: 5000,
    keepAliveOnHover: true
  })
}

// 初始化检查登录状态，并拉取最新用户信息（含管理员角色）
onMounted(async () => {
  userStore.initUser()
  if (userStore.isLoggedIn) {
    await userStore.refreshProfile()
  }
})

// 当前激活的菜单项
const activeKey = computed(() => route.path)

// 主题配置 - 蓝白配色
const themeOverrides = {
  common: {
    primaryColor: '#1890ff',
    primaryColorHover: '#40a9ff',
    primaryColorPressed: '#096dd9',
    primaryColorSuppl: '#1890ff',
    fontSize: '16px',
    fontSizeMini: '14px',
    fontSizeSmall: '15px',
    fontSizeMedium: '16px',
    fontSizeLarge: '18px',
    fontSizeHuge: '20px'
  },
  Button: {
    textColorPrimary: '#ffffff',
    textColorHoverPrimary: '#ffffff',
    textColorPressedPrimary: '#ffffff',
    textColorFocusPrimary: '#ffffff',
    textColorDisabledPrimary: '#bfbfbf',
    colorPrimary: '#1890ff',
    colorHoverPrimary: '#40a9ff',
    colorPressedPrimary: '#096dd9',
    colorFocusPrimary: '#1890ff',
    colorDisabledPrimary: '#d9d9d9',
    borderPrimary: '#1890ff',
    borderHoverPrimary: '#40a9ff',
    borderPressedPrimary: '#096dd9',
    borderFocusPrimary: '#1890ff',
    borderDisabledPrimary: '#d9d9d9',
    rippleColorPrimary: '#e6f7ff',
    heightMedium: '40px',
    heightLarge: '44px',
    fontSizeMedium: '16px',
    fontSizeLarge: '17px'
  },
  Card: {
    color: '#ffffff',
    colorModal: '#ffffff',
    colorTarget: '#ffffff',
    colorEmbedded: '#fafafa',
    colorEmbeddedModal: '#ffffff',
    colorEmbeddedPopover: '#ffffff'
  },
  Menu: {
    itemColorActive: '#e6f7ff',
    itemColorActiveHover: '#bae7ff',
    itemColorActiveCollapsed: '#e6f7ff',
    itemHeight: '52px',
    itemFontSize: '18px',
    itemIconSize: '20px'
  },
  Layout: {
    color: '#ffffff',
    headerColor: '#ffffff',
    footerColor: '#ffffff',
    siderColor: '#fafafa'
  }
}

// 渲染图标
const renderIcon = (icon) => {
  return () => h(NIcon, null, { default: () => h(icon) })
}

// 学员端菜单（管理员从统一登录页进入后会跳转 /admin）
const menuOptions = [
  {
    label: '首页',
    key: '/',
    icon: renderIcon(HomeOutline)
  },
  {
    label: '题目集管理',
    key: '/subjects',
    icon: renderIcon(BookOutline)
  },
  {
    label: '题库管理',
    key: '/question-bank',
    icon: renderIcon(LibraryOutline)
  },
  {
    label: '导入题目',
    key: '/import',
    icon: renderIcon(CloudUploadOutline)
  },
  {
    label: '开始练习',
    key: '/practice',
    icon: renderIcon(CreateOutline)
  },
  {
    label: '错题集',
    key: '/errors',
    icon: renderIcon(AlertCircleOutline)
  },
  {
    label: '做题记录',
    key: '/practice-history',
    icon: renderIcon(TimeOutline)
  },
  { type: 'divider' },
  {
    label: '配置',
    key: 'config',
    icon: renderIcon(SettingsOutline),
    children: [
      {
        label: 'AI 模型配置',
        key: '/model-config'
      }
    ]
  }
]

// 菜单选择处理
const handleMenuSelect = (key) => {
  router.push(key)
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background: radial-gradient(circle at top left, #eef6ff 0%, #f4f7fc 45%, #f0f2f5 100%);
  color: #1f2937;
  font-size: 16px;
}

#app {
  height: 100vh;
  background: transparent;
  position: relative;
  overflow: hidden;
}

.main-layout {
  height: 100vh;
  background: transparent !important;
  position: relative;
  z-index: 2;
}

.login-route-wrap {
  position: relative;
  z-index: 2;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.main-content-layout {
  height: calc(100vh - 72px);
}

.app-header {
  height: 72px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  background: linear-gradient(130deg, rgba(24, 144, 255, 0.92) 0%, rgba(9, 109, 217, 0.9) 60%, rgba(6, 82, 163, 0.86) 100%) !important;
  backdrop-filter: blur(8px);
  box-shadow: 0 8px 24px rgba(9, 109, 217, 0.24);
}

.header-title-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.header-title {
  margin: 0;
  color: #ffffff;
  font-weight: 700;
  letter-spacing: 0.4px;
  font-size: 24px;
}

/* 顶栏用户名：白字 + 深底，与蓝色背景区分 */
.header-username {
  color: #fff;
  font-weight: 600;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.18);
  font-size: 16px;
}

.app-sider {
  background: rgba(255, 255, 255, 0.82) !important;
  border-right: 1px solid rgba(24, 144, 255, 0.12) !important;
  backdrop-filter: blur(8px);
}

.app-content {
  background: transparent !important;
}

.n-card {
  background: rgba(255, 255, 255, 0.86) !important;
  border: 1px solid rgba(24, 144, 255, 0.08) !important;
  border-radius: 14px !important;
  box-shadow: 0 8px 24px rgba(31, 41, 55, 0.08) !important;
  transition: transform 0.28s ease, box-shadow 0.28s ease;
}

.n-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 30px rgba(15, 23, 42, 0.14) !important;
}

.n-button--primary-type {
  background: linear-gradient(135deg, #1890ff, #2563eb) !important;
  border-color: transparent !important;
  color: #ffffff !important;
  box-shadow: 0 8px 18px rgba(37, 99, 235, 0.28);
}

.n-button--primary-type .n-button__content {
  color: #ffffff !important;
}

.n-button--primary-type:hover {
  background: linear-gradient(135deg, #3ea5ff, #3873ff) !important;
  border-color: transparent !important;
  color: #ffffff !important;
}

.n-button--primary-type:hover .n-button__content {
  color: #ffffff !important;
}

.n-menu-item--selected {
  background: linear-gradient(90deg, rgba(24, 144, 255, 0.14), rgba(56, 115, 255, 0.08)) !important;
  color: #0b66d6 !important;
  border-radius: 8px;
}

.n-menu-item--selected::before {
  background-color: #1890ff !important;
}

.n-menu-item-content {
  transition: transform 0.2s ease;
  font-size: 18px !important;
  font-weight: 600;
}

.n-menu-item:hover .n-menu-item-content {
  transform: translateX(2px);
}

.n-button {
  min-height: 40px;
  font-size: 17px !important;
  border-radius: 12px !important;
}

.n-input .n-input__input-el,
.n-base-selection-label,
.n-form-item-label__text {
  font-size: 17px !important;
}

.n-input,
.n-base-selection,
.n-input-wrapper,
.n-base-selection-label {
  border-radius: 12px !important;
}

.n-input .n-input-wrapper,
.n-base-selection .n-base-selection-label {
  min-height: 44px !important;
}

.n-tabs-nav-scroll-content,
.n-tabs-rail,
.n-tabs-tab {
  border-radius: 14px !important;
}

.n-tabs-tab {
  font-size: 16px !important;
  font-weight: 600;
  padding: 10px 18px !important;
}

.n-data-table-th,
.n-data-table-td,
.n-pagination-item,
.n-tabs-tab__label {
  font-size: 15px !important;
}

.n-card-header__main,
.n-page-header__title {
  font-size: 20px !important;
}

.n-statistic .n-statistic-value__content {
  font-size: 28px !important;
}

.empty-wrap {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-empty {
  margin-top: -80px;
}

.app-bg-orb {
  position: absolute;
  border-radius: 999px;
  filter: blur(3px);
  opacity: 0.32;
  z-index: 1;
  pointer-events: none;
  animation: float 9s ease-in-out infinite;
}

.orb-a {
  width: 300px;
  height: 300px;
  top: -90px;
  right: -70px;
  background: radial-gradient(circle at 35% 35%, #60a5fa 0%, #2563eb 80%);
}

.orb-b {
  width: 240px;
  height: 240px;
  left: -80px;
  bottom: 45px;
  background: radial-gradient(circle at 40% 40%, #22d3ee 0%, #0ea5e9 80%);
  animation-delay: 1.2s;
}

.orb-c {
  width: 180px;
  height: 180px;
  top: 38%;
  left: 42%;
  background: radial-gradient(circle at 35% 35%, #93c5fd 0%, #3b82f6 85%);
  animation-delay: 2.1s;
}

.page-fade-slide-enter-active,
.page-fade-slide-leave-active {
  transition: all 0.28s ease;
}

.page-fade-slide-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.page-fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) translateX(0);
  }
  50% {
    transform: translateY(-12px) translateX(10px);
  }
}
</style>
