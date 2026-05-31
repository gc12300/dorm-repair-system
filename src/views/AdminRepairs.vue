<template>
  <AppLayout>
    <div class="page-wrap">
      <h2 class="page-title">报修管理</h2>
      <p class="page-subtitle">管理所有报修申请</p>

      <!-- 统计 -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
        <div class="glass-card-sm flex items-center gap-4">
          <div class="w-11 h-11 rounded-xl bg-amber-100 flex items-center justify-center shadow-sm">
            <el-icon color="#f59e0b" size="22"><Clock /></el-icon>
          </div>
          <div><p class="text-2xl font-extrabold text-slate-700">{{ stats.pending }}</p><p class="text-xs text-slate-400">待处理</p></div>
        </div>
        <div class="glass-card-sm flex items-center gap-4">
          <div class="w-11 h-11 rounded-xl bg-blue-100 flex items-center justify-center shadow-sm">
            <el-icon color="#3b82f6" size="22"><Loading /></el-icon>
          </div>
          <div><p class="text-2xl font-extrabold text-slate-700">{{ stats.processing }}</p><p class="text-xs text-slate-400">处理中</p></div>
        </div>
        <div class="glass-card-sm flex items-center gap-4">
          <div class="w-11 h-11 rounded-xl bg-emerald-100 flex items-center justify-center shadow-sm">
            <el-icon color="#10b981" size="22"><Check /></el-icon>
          </div>
          <div><p class="text-2xl font-extrabold text-slate-700">{{ stats.completed }}</p><p class="text-xs text-slate-400">已完成</p></div>
        </div>
        <div class="glass-card-sm flex items-center gap-4">
          <div class="w-11 h-11 rounded-xl bg-slate-100 flex items-center justify-center shadow-sm">
            <el-icon color="#64748b" size="22"><DataBoard /></el-icon>
          </div>
          <div><p class="text-2xl font-extrabold text-slate-700">{{ stats.total }}</p><p class="text-xs text-slate-400">总计</p></div>
        </div>
      </div>

      <!-- 筛选 -->
      <div class="glass-card-sm flex flex-wrap items-center gap-3 mb-5">
        <el-select v-model="filterStatus" placeholder="状态筛选" clearable class="!w-32" size="default">
          <el-option label="待处理" value="待处理" /><el-option label="处理中" value="处理中" />
          <el-option label="已完成" value="已完成" /><el-option label="已取消" value="已取消" />
        </el-select>
        <el-input v-model="searchKey" placeholder="搜索标题/学号..." clearable class="!w-56" />
        <el-button @click="fetchRepairs" class="!rounded-xl"><el-icon><Refresh /></el-icon> 刷新</el-button>
      </div>

      <!-- 表格 -->
      <div class="glass-card !p-0 overflow-hidden">
        <el-table :data="filteredRepairs" stripe v-loading="loading" empty-text="暂无数据" class="w-full">
          <el-table-column prop="id" label="#" width="60" />
          <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
          <el-table-column label="报修人" width="100">
            <template #default="{ row }">{{ row.studentName || '-' }}</template>
          </el-table-column>
          <el-table-column label="学号" width="110">
            <template #default="{ row }"><span class="text-xs text-slate-400">{{ row.studentNo || '-' }}</span></template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)" size="small" round effect="light">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="处理人" width="90">
            <template #default="{ row }">{{ row.adminName || '-' }}</template>
          </el-table-column>
          <el-table-column label="时间" width="170">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="{ row }">
              <el-button text type="primary" size="small" @click="showDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-dialog v-model="dialogVisible" title="报修详情" width="500px">
        <template v-if="current">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="编号">{{ current.id }}</el-descriptions-item>
            <el-descriptions-item label="标题">{{ current.title }}</el-descriptions-item>
            <el-descriptions-item label="描述">{{ current.description }}</el-descriptions-item>
            <el-descriptions-item label="报修人">{{ current.studentName }} ({{ current.studentNo }})</el-descriptions-item>
            <el-descriptions-item label="电话">{{ current.studentPhone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="statusType(current.status)" size="small" round effect="light">{{ current.status }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="处理人">{{ current.adminName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="处理说明" v-if="current.handleDesc">{{ current.handleDesc }}</el-descriptions-item>
            <el-descriptions-item label="时间">{{ formatTime(current.createTime) }}</el-descriptions-item>
          </el-descriptions>
        </template>
      </el-dialog>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import AppLayout from '@/components/AppLayout.vue'
import { getAllRepairs } from '@/api/repair'

const repairs = ref([])
const loading = ref(false)
const filterStatus = ref('')
const searchKey = ref('')
const dialogVisible = ref(false)
const current = ref(null)

const filteredRepairs = computed(() => {
  let list = repairs.value
  if (filterStatus.value) list = list.filter(r => r.status === filterStatus.value)
  if (searchKey.value) {
    const k = searchKey.value.toLowerCase()
    list = list.filter(r => (r.title || '').toLowerCase().includes(k) || (r.studentNo || '').toLowerCase().includes(k) || (r.studentName || '').toLowerCase().includes(k))
  }
  return list
})

const stats = computed(() => {
  const d = repairs.value
  return {
    pending: d.filter(r => ['待处理','PENDING'].includes(r.status)).length,
    processing: d.filter(r => ['处理中','PROCESSING'].includes(r.status)).length,
    completed: d.filter(r => ['已完成','COMPLETED'].includes(r.status)).length,
    total: d.length
  }
})

onMounted(fetchRepairs)
async function fetchRepairs() {
  loading.value = true
  try { const res = await getAllRepairs(); repairs.value = res.data || [] } catch { }
  finally { loading.value = false }
}
function showDetail(r) { current.value = r; dialogVisible.value = true }
function statusType(s) {
  const m = { '待处理':'warning','处理中':'','已完成':'success','已取消':'info','PENDING':'warning','PROCESSING':'','COMPLETED':'success','CANCELLED':'info' }
  return m[s]||'info'
}
function formatTime(t) { return t ? new Date(t).toLocaleString('zh-CN') : '-' }
</script>
