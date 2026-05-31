<template>
  <AppLayout>
    <div class="page-wrap">
      <div class="flex items-center justify-between mb-6">
        <div>
          <h2 class="page-title">维修处理</h2>
          <p class="page-subtitle !mb-0">处理待维修的报修申请</p>
        </div>
        <el-button @click="fetchRepairs" :loading="loading" class="!rounded-xl">
          <el-icon><Refresh /></el-icon> 刷新
        </el-button>
      </div>

      <div v-if="!loading && pendingRepairs.length === 0" class="glass-card text-center py-16">
        <el-icon size="64" color="#cbd5e1"><Setting /></el-icon>
        <h3 class="text-lg font-semibold text-slate-400 mt-4">暂无待处理报修</h3>
        <p class="text-sm text-slate-300 mt-1">所有报修已处理完毕</p>
      </div>

      <div v-else class="space-y-4">
        <div v-for="item in pendingRepairs" :key="item.id"
          class="glass-card hover:shadow-xl transition-all duration-300">
          <!-- 头部 -->
          <div class="flex items-start justify-between gap-4">
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-3 mb-2">
                <span class="text-xs text-slate-300 font-mono">#{{ item.id }}</span>
                <el-tag :type="statusType(item.status)" size="small" round effect="light">
                  {{ item.status }}
                </el-tag>
                <span class="text-xs text-slate-400">{{ formatTime(item.createTime) }}</span>
              </div>
              <h3 class="text-base font-bold text-slate-700">{{ item.title }}</h3>
              <p class="text-sm text-slate-500 mt-1">{{ item.description }}</p>
              <div class="flex flex-wrap gap-3 mt-3 text-xs text-slate-400">
                <span class="flex items-center gap-1"><el-icon><User /></el-icon> {{ item.studentName }}</span>
                <span class="flex items-center gap-1"><el-icon><Document /></el-icon> {{ item.studentNo }}</span>
                <span class="flex items-center gap-1"><el-icon><Phone /></el-icon> {{ item.studentPhone }}</span>
              </div>
            </div>
            <div class="flex flex-col gap-2">
              <el-button @click="openHandleDialog(item)"
                class="!rounded-xl !font-semibold !shadow-md !border-none !text-white"
                style="background: linear-gradient(135deg,#f59e0b,#ef4444);">
                <el-icon><Setting /></el-icon> 处理
              </el-button>
              <el-button @click="getAiSuggestion(item)" class="!rounded-xl" :loading="aiLoading[item.id]">
                <el-icon><Cpu /></el-icon> AI 建议
              </el-button>
            </div>
          </div>

          <!-- 评分评价 -->
          <div v-if="item.rating" class="mt-3 pt-3 border-t border-slate-100">
            <div class="flex items-center gap-3">
              <span class="text-xs text-slate-400 font-semibold uppercase">学生评价</span>
              <el-rate v-model="item.rating" disabled show-score size="small" />
              <span class="text-xs text-slate-300 ml-auto">{{ formatTime(item.createTime) }}</span>
            </div>
            <p v-if="item.review" class="text-sm text-slate-500 mt-1.5 italic">"{{ item.review }}"</p>
          </div>

          <!-- AI 建议卡片 -->
          <div v-if="aiResults[item.id]" class="mt-4 p-4 rounded-xl animate-in"
            style="background: linear-gradient(135deg, #eff6ff, #faf5ff);">
            <div class="flex items-center gap-2 mb-3">
              <div class="w-7 h-7 rounded-lg flex items-center justify-center" style="background: linear-gradient(135deg,#6366f1,#a855f7);">
                <el-icon color="#fff" size="14"><Cpu /></el-icon>
              </div>
              <span class="text-sm font-bold text-indigo-600">AI 维修建议</span>
              <el-tag :type="priorityTag(aiResults[item.id].priority)" size="small" round effect="dark" class="ml-auto">
                {{ aiResults[item.id].priority }}优先级
              </el-tag>
            </div>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
              <div>
                <p class="text-xs text-slate-400 mb-1 font-semibold uppercase">维修方案</p>
                <p class="text-slate-600 whitespace-pre-wrap leading-relaxed">{{ aiResults[item.id].plan }}</p>
              </div>
              <div>
                <p class="text-xs text-slate-400 mb-1 font-semibold uppercase">所需工具</p>
                <div class="flex flex-wrap gap-1.5">
                  <el-tag v-for="t in aiResults[item.id].tools" :key="t" size="small" round>{{ t }}</el-tag>
                </div>
                <p class="text-xs text-slate-400 mt-3 mb-1 font-semibold uppercase">预估工时</p>
                <p class="text-slate-600 font-bold text-lg">{{ aiResults[item.id].estimatedHours }} <span class="text-xs font-normal text-slate-400">小时</span></p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 处理弹窗 -->
      <el-dialog v-model="dialogVisible" title="处理报修" width="480px" class="handle-dialog">
        <template v-if="currentRepair">
          <div class="rounded-xl p-4 mb-5" style="background: linear-gradient(135deg,#fffbeb,#fef3c7);">
            <p class="text-sm font-semibold text-amber-800">{{ currentRepair.title }}</p>
            <p class="text-xs text-amber-600 mt-1">{{ currentRepair.description }}</p>
            <p class="text-xs text-amber-500 mt-2">报修人：{{ currentRepair.studentName }}</p>
          </div>
        </template>
        <el-form :model="handleForm" label-position="top" size="large">
          <el-form-item label="处理状态" required>
            <el-select v-model="handleForm.status" class="w-full">
              <el-option label="处理中" value="处理中" />
              <el-option label="已完成" value="已完成" />
              <el-option label="已取消" value="已取消" />
            </el-select>
          </el-form-item>
          <el-form-item label="处理说明">
            <el-input v-model="handleForm.handleDesc" type="textarea" :rows="4"
              placeholder="请填写维修过程、使用材料等..." />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false" class="!rounded-xl">取消</el-button>
          <el-button :loading="handling" @click="handleSubmit" class="!rounded-xl !font-semibold !border-none !text-white"
            style="background: linear-gradient(135deg,#6366f1,#8b5cf6);">确认处理</el-button>
        </template>
      </el-dialog>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import AppLayout from '@/components/AppLayout.vue'
import { getAllRepairs, handleRepair } from '@/api/repair'
import { suggestRepair } from '@/api/ai'

const repairs = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const currentRepair = ref(null)
const handling = ref(false)
const aiResults = reactive({})
const aiLoading = reactive({})
const handleForm = reactive({ repairId: null, status: '处理中', handleDesc: '' })

const pendingRepairs = computed(() =>
  repairs.value.filter(r => ['待处理','PENDING','处理中','PROCESSING'].includes(r.status))
)

onMounted(fetchRepairs)

async function fetchRepairs() {
  loading.value = true
  try { const res = await getAllRepairs(); repairs.value = res.data || [] } catch { }
  finally { loading.value = false }
}

async function getAiSuggestion(item) {
  aiLoading[item.id] = true
  try { const res = await suggestRepair(item.description || item.title); aiResults[item.id] = res.data } catch { }
  finally { aiLoading[item.id] = false }
}

function openHandleDialog(item) {
  currentRepair.value = item
  handleForm.repairId = item.id; handleForm.status = '处理中'; handleForm.handleDesc = ''
  dialogVisible.value = true
}

async function handleSubmit() {
  handling.value = true
  try {
    await handleRepair({ repairId: handleForm.repairId, status: handleForm.status, handleDesc: handleForm.handleDesc })
    ElMessage.success('处理成功'); dialogVisible.value = false; fetchRepairs()
  } catch { }
  finally { handling.value = false }
}

function statusType(s) {
  const m = { '待处理':'warning','处理中':'','已完成':'success','已取消':'info','PENDING':'warning','PROCESSING':'','COMPLETED':'success','CANCELLED':'info' }
  return m[s]||'info'
}
function priorityTag(p) {
  const m = { '紧急':'danger','高':'warning','中':'','低':'info' }
  return m[p]||''
}
function formatTime(t) { return t ? new Date(t).toLocaleString('zh-CN') : '-' }
</script>
