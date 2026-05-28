<template>
  <div class="admin-shell">
    <n-layout has-sider class="admin-layout">
      <n-layout-sider
        bordered
        collapse-mode="width"
        :collapsed-width="64"
        :width="220"
        show-trigger
        class="admin-sider"
      >
        <div class="admin-brand">管理后台</div>
        <n-menu
          :value="activeKey"
          :options="menuOptions"
          :collapsed-width="64"
          :collapsed-icon-size="22"
          @update:value="onMenuSelect"
        />
      </n-layout-sider>
      <n-layout class="admin-main">
        <n-layout-header bordered class="admin-header">
          <span class="admin-header-title">{{ title }}</span>
          <n-space>
            <span class="admin-user-tag">{{ username }}</span>
            <n-button size="small" @click="logout">退出</n-button>
          </n-space>
        </n-layout-header>
        <n-layout-content
          class="admin-content"
          :native-scrollbar="true"
          :content-style="adminContentScrollStyle"
        >
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </n-layout-content>
      </n-layout>
    </n-layout>
  </div>
</template>

<script setup>
import { h, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NIcon, NLayout, NLayoutSider, NLayoutHeader, NLayoutContent, NMenu, NSpace, NButton, useMessage } from 'naive-ui'
import { PeopleOutline, SpeedometerOutline } from '@vicons/ionicons5'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const message = useMessage()
const userStore = useUserStore()

const username = computed(() => userStore.username || '—')

/** #app 使用 overflow:hidden，主内容区必须自带滚动，否则放大页面后底部表单会被裁切 */
const adminContentScrollStyle = {
  padding: '20px 24px',
  boxSizing: 'border-box',
  height: 'calc(100vh - 56px)',
  maxHeight: 'calc(100vh - 56px)',
  overflowY: 'auto',
  overflowX: 'hidden'
}

const title = computed(() => route.meta.adminTitle || '控制台')

const activeKey = computed(() => {
  if (route.path.includes('/users')) return '/admin/users'
  return '/admin/dashboard'
})

const renderIcon = (icon) => () => h(NIcon, null, { default: () => h(icon) })

const menuOptions = [
  {
    label: '控制台',
    key: '/admin/dashboard',
    icon: renderIcon(SpeedometerOutline)
  },
  {
    label: '用户管理',
    key: '/admin/users',
    icon: renderIcon(PeopleOutline)
  }
]

function onMenuSelect(key) {
  router.push(key)
}

function logout() {
  userStore.logout()
  router.replace('/login')
  message.info('已退出管理后台')
}
</script>

<style scoped>
.admin-shell {
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #0f172a;
}
.admin-layout {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  background: #0f172a;
}
/* 右侧主栏：占满剩余高度，便于 content 区域计算 calc(100vh - 56px) 并出现滚动条 */
.admin-main {
  flex: 1;
  min-height: 0 !important;
  overflow: hidden;
}
.admin-sider {
  background: #1e293b !important;
}
.admin-brand {
  padding: 20px 16px 16px;
  font-size: 17px;
  font-weight: 700;
  color: #e2e8f0;
  letter-spacing: 0.06em;
  border-bottom: 1px solid rgba(148, 163, 184, 0.15);
}
.admin-header {
  height: 56px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #1e293b !important;
}
.admin-header-title {
  font-size: 16px;
  font-weight: 600;
  color: #f1f5f9;
}
.admin-user-tag {
  font-size: 14px;
  color: #94a3b8;
}
.admin-content {
  background: #f8fafc;
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
:deep(.n-menu) {
  background: transparent !important;
}
:deep(.n-menu-item-content) {
  color: #cbd5e1 !important;
}
:deep(.n-menu-item-content--selected) {
  color: #38bdf8 !important;
  background: rgba(56, 189, 248, 0.12) !important;
}
</style>
