<template>
  <n-card class="login-card" title="欢迎使用快练习题系统">
    <n-tabs v-model:value="authTab" type="segment" animated>
      <n-tab-pane name="login" tab="登录">
        <n-form ref="loginFormRef" :model="loginForm" :rules="loginRules" style="margin-top: 16px;">
          <n-form-item path="username" label="用户名">
            <n-input v-model:value="loginForm.username" placeholder="请输入用户名" @keyup.enter="handleLogin" />
          </n-form-item>
          <n-form-item path="password" label="密码">
            <n-input
              v-model:value="loginForm.password"
              type="password"
              show-password-on="click"
              placeholder="请输入密码"
              @keyup.enter="handleLogin"
            />
          </n-form-item>
          <n-button type="primary" block :loading="loading" @click="handleLogin">
            登录
          </n-button>
        </n-form>
      </n-tab-pane>

      <n-tab-pane name="register" tab="注册">
        <n-form ref="registerFormRef" :model="registerForm" :rules="registerRules" style="margin-top: 16px;">
          <n-form-item path="username" label="用户名">
            <n-input v-model:value="registerForm.username" placeholder="3-50个字符" />
          </n-form-item>
          <n-form-item path="password" label="密码">
            <n-input v-model:value="registerForm.password" type="password" show-password-on="click" placeholder="至少6个字符" />
          </n-form-item>
          <n-form-item path="confirmPassword" label="确认密码">
            <n-input
              v-model:value="registerForm.confirmPassword"
              type="password"
              show-password-on="click"
              placeholder="再次输入密码"
              @keyup.enter="handleRegister"
            />
          </n-form-item>
          <n-button type="primary" block :loading="loading" @click="handleRegister">
            注册
          </n-button>
        </n-form>
      </n-tab-pane>
    </n-tabs>
  </n-card>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { authApi } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const message = useMessage()
const userStore = useUserStore()

const authTab = ref('login')
const loading = ref(false)

const loginFormRef = ref(null)
const loginForm = ref({ username: '', password: '' })
const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerFormRef = ref(null)
const registerForm = ref({ username: '', password: '', confirmPassword: '' })
const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度为3-50个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule, value) => value === registerForm.value.password,
      message: '两次输入的密码不一致',
      trigger: 'blur'
    }
  ]
}

const handleLogin = async () => {
  try {
    await loginFormRef.value?.validate()
    loading.value = true
    const response = await authApi.login(loginForm.value)
    userStore.setUser(response.user, response.token)
    await userStore.refreshProfile()
    const role = String(userStore.currentUser?.role ?? '')
      .trim()
      .toUpperCase()
    if (role === 'ADMIN') {
      message.success('登录成功')
      router.replace('/admin')
      return
    }
    message.success('登录成功')
    router.push('/')
  } catch (error) {
    if (error?.errors) return
    message.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  try {
    await registerFormRef.value?.validate()
    loading.value = true
    const response = await authApi.register({
      username: registerForm.value.username,
      password: registerForm.value.password
    })
    userStore.setUser(response.user, response.token)
    await userStore.refreshProfile()
    message.success('注册成功')
    router.push('/')
  } catch (error) {
    if (error?.errors) return
    message.error(error.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-card {
  width: min(460px, 96vw);
}
</style>

