<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-100 via-indigo-50 to-purple-50 p-4 relative overflow-hidden">
    <!-- 背景装饰 -->
    <div class="absolute inset-0 pointer-events-none">
      <div class="absolute top-[-10%] left-[-5%] w-72 h-72 bg-indigo-300/20 rounded-full blur-3xl"></div>
      <div class="absolute bottom-[-10%] right-[-5%] w-96 h-96 bg-purple-300/20 rounded-full blur-3xl"></div>
      <div class="absolute top-[40%] right-[20%] w-48 h-48 bg-pink-300/10 rounded-full blur-2xl"></div>
    </div>

    <div class="relative w-full max-w-md animate-in">
      <!-- Logo -->
      <div class="text-center mb-8">
        <div class="inline-flex items-center justify-center w-16 h-16 rounded-2xl mb-4 shadow-xl shadow-indigo-200"
          style="background: linear-gradient(135deg, #6366f1, #a855f7);">
          <el-icon color="#fff" size="30"><Tools /></el-icon>
        </div>
        <h1 class="text-2xl font-extrabold text-slate-800 tracking-tight">校园报修服务平台</h1>
        <p class="text-sm text-slate-400 mt-1.5">Campus Repair Service</p>
      </div>

      <!-- 身份选择卡片 -->
      <div class="glass-card mb-5">
        <p class="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-3">选择身份</p>
        <div class="grid grid-cols-3 gap-3">
          <div v-for="item in roles" :key="item.key"
            class="role-card" :class="{ active: activeRole === item.key }"
            @click="activeRole = item.key">
            <div class="role-icon" :style="{ background: item.gradient }">
              <el-icon size="22"><component :is="item.icon" /></el-icon>
            </div>
            <span class="role-label">{{ item.label }}</span>
          </div>
        </div>
      </div>

      <!-- 登录表单 -->
      <div class="glass-card">
        <div class="flex items-center gap-2 mb-5">
          <div class="w-1 h-5 rounded-full" :style="{ background: currentRole.gradient }"></div>
          <span class="text-sm font-semibold text-slate-600">{{ currentRole.label }}登录</span>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User"
              class="custom-input" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" placeholder="请输入密码"
              show-password :prefix-icon="Lock" class="custom-input" />
          </el-form-item>
          <el-form-item class="!mb-0">
            <el-button class="w-full !h-11 !text-base !font-semibold !rounded-xl !border-none"
              :loading="loading" @click="handleLogin"
              :style="{ background: currentRole.gradient }">
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="relative my-5">
          <div class="absolute inset-0 flex items-center"><div class="w-full border-t border-slate-100"></div></div>
          <div class="relative flex justify-center"><span class="px-3 bg-white text-xs text-slate-300">或</span></div>
        </div>

        <el-button class="w-full !h-11 !text-base !font-medium !rounded-xl" plain @click="showRegister = true">
          注册新账号
        </el-button>
      </div>

      <p class="text-center text-xs text-slate-300 mt-5">软件工程课程设计</p>
    </div>

    <!-- 注册弹窗 -->
    <el-dialog v-model="showRegister" :title="currentRole.label + '注册'" width="460px" :close-on-click-modal="false"
      class="register-dialog">
      <el-form ref="regRef" :model="regForm" :rules="regRules" label-position="top" size="large">
        <div class="grid grid-cols-2 gap-x-4">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="regForm.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="regForm.password" type="password" placeholder="请输入密码" show-password />
          </el-form-item>
          <el-form-item :label="activeRole === 'student' ? '学号' : '工号'" prop="studentNo">
            <el-input v-model="regForm.studentNo" :placeholder="activeRole === 'student' ? '请输入学号' : '请输入工号'" />
          </el-form-item>
          <el-form-item label="姓名" prop="name">
            <el-input v-model="regForm.name" placeholder="请输入姓名" />
          </el-form-item>
          <el-form-item label="联系电话" prop="phone">
            <el-input v-model="regForm.phone" placeholder="请输入联系电话" />
          </el-form-item>
          <el-form-item v-if="activeRole === 'student'" label="宿舍楼号" prop="dormitoryId">
            <el-select v-model="regForm.dormitoryId" placeholder="请选择宿舍楼" class="w-full">
              <el-option v-for="d in dorms" :key="d.id" :label="d.name" :value="d.id" />
            </el-select>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="showRegister = false">取消</el-button>
        <el-button type="primary" :loading="regLoading" @click="handleRegister"
          style="background: linear-gradient(135deg, #6366f1, #8b5cf6); border: none;">
          注册
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, School, Setting, Monitor } from '@element-plus/icons-vue'
import { login, register } from '@/api/user'
import { useUserStore } from '@/stores/user'
import request from '@/api'

const router = useRouter()
const store = useUserStore()

const roles = [
  { key: 'student', label: '学生', icon: School, gradient: 'linear-gradient(135deg, #6366f1, #8b5cf6)' },
  { key: 'worker', label: '维修人员', icon: Setting, gradient: 'linear-gradient(135deg, #f59e0b, #ef4444)' },
  { key: 'admin', label: '管理员', icon: Monitor, gradient: 'linear-gradient(135deg, #10b981, #06b6d4)' }
]
const activeRole = ref('student')
const currentRole = computed(() => roles.find(r => r.key === activeRole.value))

const loading = ref(false)
const formRef = ref(null)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// Register
const showRegister = ref(false)
const regLoading = ref(false)
const regRef = ref(null)
const regForm = reactive({ username: '', password: '', studentNo: '', name: '', phone: '', dormitoryId: null })
const regRules = computed(() => {
  const base = {
    username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
    studentNo: [{ required: true, message: activeRole.value === 'student' ? '请输入学号' : '请输入工号', trigger: 'blur' }],
    name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
    phone: [{ required: true, message: '请输入电话', trigger: 'blur' }]
  }
  if (activeRole.value === 'student') {
    base.dormitoryId = [{ required: true, message: '请选择宿舍楼', trigger: 'change' }]
  }
  return base
})
const dorms = ref([])

onMounted(async () => {
  try { const res = await request.get('/admin/dormitories'); dorms.value = res.data || [] } catch { /* */ }
})

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await login({ username: form.username, password: form.password })
    const token = res.data
    store.setToken(token)
    store.setIdentity(activeRole.value)

    // 验证身份匹配
    if (activeRole.value === 'student' && store.role !== 'STUDENT') {
      ElMessage.error('该账号不是学生账号')
      store.logout(); loading.value = false; return
    }
    if ((activeRole.value === 'worker' || activeRole.value === 'admin') && store.role !== 'ADMIN') {
      ElMessage.error('该账号没有管理权限')
      store.logout(); loading.value = false; return
    }

    ElMessage.success('登录成功')
    if (activeRole.value === 'worker') router.push('/worker/repairs')
    else if (activeRole.value === 'admin') router.push('/admin/repairs')
    else router.push('/student/repairs')
  } catch { /* handled */ }
  finally { loading.value = false }
}

async function handleRegister() {
  const valid = await regRef.value?.validate().catch(() => false)
  if (!valid) return
  regLoading.value = true
  try {
    const payload = {
      username: regForm.username, password: regForm.password,
      studentNo: regForm.studentNo, name: regForm.name, phone: regForm.phone,
      role: activeRole.value === 'student' ? 'STUDENT' : 'ADMIN'
    }
    if (activeRole.value === 'student') {
      payload.dormitoryId = regForm.dormitoryId
    }
    await register(payload)
    ElMessage.success('注册成功，请登录')
    showRegister.value = false
    form.username = regForm.username
  } catch { /* handled */ }
  finally { regLoading.value = false }
}
</script>

<style scoped>
.role-card {
  @apply flex flex-col items-center gap-2 py-3 px-2 rounded-xl cursor-pointer transition-all duration-300 border-2 border-transparent;
}
.role-card:hover { @apply bg-slate-50; }
.role-card.active {
  @apply border-indigo-200 bg-indigo-50/50 shadow-sm;
}
.role-icon {
  @apply w-10 h-10 rounded-xl flex items-center justify-center text-white shadow-md;
  transition: transform 0.3s;
}
.role-card:hover .role-icon { transform: scale(1.08); }
.role-card.active .role-icon { transform: scale(1.1); }
.role-label {
  @apply text-xs font-semibold text-slate-500;
}
.role-card.active .role-label {
  @apply text-indigo-600;
}
.custom-input :deep(.el-input__wrapper) {
  @apply rounded-xl shadow-sm;
}
</style>
