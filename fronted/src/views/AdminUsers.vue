<template>
  <div class="admin-users-page">
    <n-page-header title="用户管理" subtitle="查询与维护系统用户（仅管理员）">
      <template #extra>
        <n-space>
          <n-input
            v-model:value="keyword"
            placeholder="搜索用户名"
            clearable
            style="width: 220px"
            @keyup.enter="loadList"
          />
          <n-button type="primary" @click="loadList">查询</n-button>
          <n-button type="primary" @click="openCreate">新建用户</n-button>
        </n-space>
      </template>
    </n-page-header>

    <n-card style="margin-top: 16px">
      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="pagination"
        :bordered="false"
      />
    </n-card>

    <n-modal v-model:show="showModal" preset="card" :title="modalTitle" style="width: 480px">
      <n-form ref="formRef" :model="form" :rules="formRules" label-placement="left" label-width="88">
        <n-form-item path="username" label="用户名">
          <n-input v-model:value="form.username" placeholder="登录名" :disabled="isEdit" />
        </n-form-item>
        <n-form-item path="password" :label="isEdit ? '新密码' : '密码'">
          <n-input
            v-model:value="form.password"
            type="password"
            show-password-on="click"
            :placeholder="isEdit ? '不修改请留空' : '至少 6 位'"
          />
        </n-form-item>
        <n-form-item path="role" label="角色">
          <n-select v-model:value="form.role" :options="roleOptions" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="saving" @click="submitForm">保存</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, h } from 'vue'
import { NButton, NSpace, NTag, useMessage, useDialog } from 'naive-ui'
import { adminUserApi } from '@/api'
import { useUserStore } from '@/stores/user'

const message = useMessage()
const dialog = useDialog()
const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const keyword = ref('')
const tableData = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const pagination = computed(() => ({
  page: page.value,
  pageSize: pageSize.value,
  itemCount: total.value,
  remote: true,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onChange: (p) => {
    page.value = p
    loadList()
  },
  onUpdatePageSize: (s) => {
    pageSize.value = s
    page.value = 1
    loadList()
  }
}))

const showModal = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const form = reactive({
  username: '',
  password: '',
  role: 'USER'
})
const roleOptions = [
  { label: '普通用户', value: 'USER' },
  { label: '管理员', value: 'ADMIN' }
]

const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    {
      validator: (_rule, val) => {
        if (!isEdit.value && (!val || val.length < 6)) {
          return new Error('密码至少 6 位')
        }
        if (isEdit.value && val && val.length > 0 && val.length < 6) {
          return new Error('新密码至少 6 位')
        }
        return true
      },
      trigger: 'blur'
    }
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const modalTitle = computed(() => (isEdit.value ? '编辑用户' : '新建用户'))

const columns = [
  { title: 'ID', key: 'id', width: 70 },
  { title: '用户名', key: 'username', ellipsis: { tooltip: true } },
  {
    title: '角色',
    key: 'role',
    width: 110,
    render(row) {
      return h(
        NTag,
        { type: row.role === 'ADMIN' ? 'error' : 'default', size: 'small' },
        { default: () => (row.role === 'ADMIN' ? '管理员' : '普通用户') }
      )
    }
  },
  {
    title: '注册时间',
    key: 'created_at',
    width: 180,
    render(row) {
      return row.created_at || '—'
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    render(row) {
      return h(
        NSpace,
        {},
        {
          default: () => [
            h(
              NButton,
              { size: 'small', onClick: () => openEdit(row) },
              { default: () => '编辑' }
            ),
            h(
              NButton,
              {
                size: 'small',
                type: 'error',
                disabled: row.id === userStore.userId,
                onClick: () => confirmDelete(row)
              },
              { default: () => '删除' }
            )
          ]
        }
      )
    }
  }
]

async function loadList() {
  loading.value = true
  try {
    const res = await adminUserApi.list({
      keyword: keyword.value || undefined,
      page: page.value - 1,
      size: pageSize.value
    })
    tableData.value = res.content || []
    total.value = res.total_elements != null ? res.total_elements : res.totalElements ?? 0
  } catch (e) {
    message.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  isEdit.value = false
  editingId.value = null
  form.username = ''
  form.password = ''
  form.role = 'USER'
  showModal.value = true
}

function openEdit(row) {
  isEdit.value = true
  editingId.value = row.id
  form.username = row.username
  form.password = ''
  form.role = row.role || 'USER'
  showModal.value = true
}

function confirmDelete(row) {
  dialog.warning({
    title: '确认删除',
    content: `确定删除用户「${row.username}」？此操作不可恢复。`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await adminUserApi.delete(row.id)
        message.success('已删除')
        await loadList()
      } catch (e) {
        message.error(e.message || '删除失败')
      }
    }
  })
}

async function submitForm() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  saving.value = true
  try {
    if (isEdit.value) {
      const body = {
        username: form.username,
        role: form.role
      }
      if (form.password) body.password = form.password
      await adminUserApi.update(editingId.value, body)
      message.success('已保存')
    } else {
      await adminUserApi.create({
        username: form.username,
        password: form.password,
        role: form.role
      })
      message.success('已创建')
    }
    showModal.value = false
    await loadList()
    await userStore.refreshProfile()
  } catch (e) {
    message.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.admin-users-page {
  animation: page-enter 0.35s ease;
}
@keyframes page-enter {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: none;
  }
}
</style>
