<template>
  <div class="min-h-screen flex bg-slate-100/60">
    <!-- 侧边栏 -->
    <aside class="w-60 flex flex-col bg-white/70 backdrop-blur-xl border-r border-slate-200/60 shadow-xl shadow-slate-200/30 relative z-10">
      <!-- Logo -->
      <div class="h-16 flex items-center gap-3 px-5 border-b border-slate-100">
        <div class="w-9 h-9 rounded-xl flex items-center justify-center shadow-lg"
          :style="{ background: activeGradient }">
          <el-icon color="#fff" size="18"><Tools /></el-icon>
        </div>
        <div>
          <h1 class="text-sm font-bold text-slate-700 leading-tight">校园报修</h1>
          <p class="text-[10px] text-slate-400 font-medium">{{ identityLabel }}</p>
        </div>
      </div>

      <!-- 导航 -->
      <nav class="flex-1 py-5 px-3 space-y-1 overflow-y-auto">
        <template v-if="identity === 'student'">
          <router-link to="/student/submit" class="nav-link">
            <el-icon size="18"><Plus /></el-icon>
            <span>提交报修</span>
          </router-link>
          <router-link to="/student/repairs" class="nav-link">
            <el-icon size="18"><List /></el-icon>
            <span>我的报修</span>
          </router-link>
        </template>

        <template v-if="identity === 'admin'">
          <div class="px-3 pt-2 pb-1">
            <span class="text-[10px] font-bold text-slate-400 uppercase tracking-wider">管理面板</span>
          </div>
          <router-link to="/admin/repairs" class="nav-link">
            <el-icon size="18"><DataBoard /></el-icon>
            <span>报修管理</span>
          </router-link>
          <router-link to="/admin/query" class="nav-link">
            <el-icon size="18"><Cpu /></el-icon>
            <span>AI 数据查询</span>
          </router-link>
        </template>

        <template v-if="identity === 'worker'">
          <div class="px-3 pt-2 pb-1">
            <span class="text-[10px] font-bold text-slate-400 uppercase tracking-wider">维修面板</span>
          </div>
          <router-link to="/worker/repairs" class="nav-link">
            <el-icon size="18"><Setting /></el-icon>
            <span>维修处理</span>
          </router-link>
        </template>
      </nav>

      <!-- 用户信息 -->
      <div class="p-4 border-t border-slate-100">
        <div class="flex items-center gap-3">
          <div class="w-9 h-9 rounded-xl flex items-center justify-center shadow-md"
            :style="{ background: activeGradient }">
            <span class="text-white text-xs font-bold">{{ identityLabel.charAt(0) }}</span>
          </div>
          <div class="flex-1 min-w-0">
            <p class="text-sm font-semibold text-slate-700 truncate">{{ identityLabel }}</p>
            <p class="text-[10px] text-slate-400">ID: {{ userId }}</p>
          </div>
        </div>
        <button @click="handleLogout" class="w-full mt-3 py-2 text-xs font-medium text-slate-400 hover:text-red-500 rounded-lg hover:bg-red-50 transition-all duration-200">
          退出登录
        </button>
      </div>
    </aside>

    <!-- 主内容 -->
    <main class="flex-1 overflow-y-auto">
      <slot />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const store = useUserStore()

const identity = computed(() => store.identity || (store.role === 'ADMIN' ? 'admin' : 'student'))
const identityLabel = computed(() => {
  const map = { student: '学生', worker: '维修人员', admin: '管理员' }
  return map[identity.value] || '用户'
})
const userId = computed(() => store.userId)
const activeGradient = computed(() => {
  const map = {
    student: 'linear-gradient(135deg, #6366f1, #8b5cf6)',
    worker: 'linear-gradient(135deg, #f59e0b, #ef4444)',
    admin: 'linear-gradient(135deg, #10b981, #06b6d4)'
  }
  return map[identity.value] || 'linear-gradient(135deg, #6366f1, #8b5cf6)'
})

function handleLogout() {
  store.logout()
  router.push('/login')
}
</script>

<style scoped>
.nav-link {
  @apply flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium text-slate-500
         transition-all duration-200 cursor-pointer no-underline;
}
.nav-link:hover { @apply bg-slate-100 text-slate-800; }
.nav-link.router-link-active {
  @apply bg-indigo-50 text-indigo-600 shadow-sm;
}
</style>
