<template>
  <AppLayout>
    <div class="page-wrap">
      <div class="flex items-center justify-between mb-6">
        <div>
          <h2 class="page-title">我的报修</h2>
          <p class="page-subtitle !mb-0">查看已提交的报修记录</p>
        </div>
        <router-link to="/student/submit" class="no-underline">
          <el-button type="primary" class="!rounded-xl !h-11 !font-semibold !shadow-lg !shadow-indigo-200" style="background: linear-gradient(135deg,#6366f1,#8b5cf6);border:none;">
            <el-icon><Plus /></el-icon> 提交报修
          </el-button>
        </router-link>
      </div>

      <div v-if="!loading && repairs.length === 0" class="glass-card text-center py-16">
        <el-icon size="64" color="#cbd5e1"><List /></el-icon>
        <h3 class="text-lg font-semibold text-slate-400 mt-4">暂无报修记录</h3>
        <p class="text-sm text-slate-300 mt-1">去提交第一条报修吧</p>
      </div>

      <div v-else class="space-y-4">
        <div v-for="item in repairs" :key="item.id" class="glass-card hover:shadow-xl hover:-translate-y-0.5 transition-all duration-300">
          <div class="flex items-start justify-between gap-4">
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-3 mb-2">
                <span class="text-xs text-slate-300 font-mono">#{{ item.id }}</span>
                <el-tag :type="statusType(item.status)" size="small" round effect="light">
                  {{ item.status }}
                </el-tag>
              </div>
              <h3 class="text-base font-semibold text-slate-700">{{ item.title }}</h3>
              <p class="text-sm text-slate-400 mt-1 line-clamp-2">{{ item.description }}</p>
            </div>
            <div class="text-right shrink-0">
              <p class="text-xs text-slate-400">{{ formatTime(item.createTime) }}</p>
              <el-button text type="primary" size="small" @click="showDetail(item)" class="mt-2 !text-xs">
                查看详情 →
              </el-button>
            </div>
          </div>
          <!-- 处理说明 -->
          <div v-if="item.handleDesc" class="mt-3 pt-3 border-t border-slate-100 flex items-start gap-2">
            <el-icon color="#6366f1" size="16"><InfoFilled /></el-icon>
            <p class="text-sm text-slate-500">{{ item.handleDesc }}</p>
          </div>

          <!-- 评分评价 -->
          <div v-if="item.rating" class="mt-3 pt-3 border-t border-slate-100">
            <div class="flex items-center gap-2 mb-1">
              <span class="text-xs text-slate-400">我的评价</span>
              <el-rate v-model="item.rating" disabled show-score size="small" />
            </div>
            <p v-if="item.review" class="text-sm text-slate-500">{{ item.review }}</p>
          </div>
          <div v-else-if="isCompleted(item.status)" class="mt-3 pt-3 border-t border-slate-100">
            <div class="flex items-center gap-2 mb-2">
              <span class="text-xs text-slate-400 font-semibold uppercase">评价服务</span>
              <el-rate v-model="ratingForms[item.id]" size="small" />
            </div>
            <el-input v-model="reviewForms[item.id]" type="textarea" :rows="2" placeholder="说说维修体验..." class="mb-2" />
            <el-button size="small" type="primary" plain class="!rounded-xl" :loading="ratingLoading[item.id]"
              @click="submitRating(item.id)">
              <el-icon><Check /></el-icon> 提交评价
            </el-button>
          </div>
        </div>
      </div>

      <!-- 详情弹窗 -->
      <el-dialog v-model="dialogVisible" title="报修详情" width="500px" class="detail-dialog">
        <template v-if="current">
          <el-descriptions :column="1" border class="modern-desc">
            <el-descriptions-item label="编号">{{ current.id }}</el-descriptions-item>
            <el-descriptions-item label="标题">{{ current.title }}</el-descriptions-item>
            <el-descriptions-item label="描述">{{ current.description }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="statusType(current.status)" size="small" round effect="light">{{ current.status }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="提交时间">{{ formatTime(current.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="处理说明" v-if="current.handleDesc">{{ current.handleDesc }}</el-descriptions-item>
          </el-descriptions>
        </template>
      </el-dialog>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import AppLayout from '@/components/AppLayout.vue'
import { getMyRepairs, rateRepair } from '@/api/repair'

const repairs = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const current = ref(null)
const ratingForms = reactive({})
const reviewForms = reactive({})
const ratingLoading = reactive({})

onMounted(fetchRepairs)

async function fetchRepairs() {
  loading.value = true
  try { const res = await getMyRepairs(); repairs.value = res.data || [] } catch { }
  finally { loading.value = false }
}

function showDetail(row) { current.value = row; dialogVisible.value = true }

function isCompleted(s) {
  return s === '已完成' || s === 'COMPLETED'
}

function statusType(s) {
  const m = { '待处理': 'warning', '处理中': '', '已完成': 'success', '已取消': 'info',
    'PENDING': 'warning', 'PROCESSING': '', 'COMPLETED': 'success', 'CANCELLED': 'info' }
  return m[s] || 'info'
}
function formatTime(t) { return t ? new Date(t).toLocaleString('zh-CN') : '-' }

async function submitRating(repairId) {
  const rating = ratingForms[repairId]
  if (!rating) { ElMessage.warning('请选择评分'); return }
  ratingLoading[repairId] = true
  try {
    await rateRepair({ repairId, rating, review: reviewForms[repairId] || '' })
    ElMessage.success('评价成功')
    fetchRepairs()
  } catch { }
  finally { ratingLoading[repairId] = false }
}
</script>
