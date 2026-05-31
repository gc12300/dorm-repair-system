<template>
  <div class="glass-card bg-gradient-to-br from-indigo-50/60 via-white to-purple-50/60 border-indigo-100/50">
    <!-- 头部 -->
    <div class="flex items-center gap-2 mb-4">
      <div class="w-9 h-9 rounded-xl flex items-center justify-center shadow-md"
        style="background: linear-gradient(135deg,#6366f1,#a855f7);">
        <el-icon color="#fff" size="18"><Cpu /></el-icon>
      </div>
      <div class="flex-1">
        <h3 class="text-sm font-bold text-slate-700">AI 报修助手</h3>
        <p class="text-xs text-slate-400">智能故障分析</p>
      </div>
      <el-tag v-if="quota" size="small" round effect="light" class="!text-xs">
        剩余 {{ quota.remaining }} 次
      </el-tag>
    </div>

    <!-- 输入 -->
    <el-input v-model="input" type="textarea" :rows="3"
      placeholder="描述故障，如：卫生间水龙头一直漏水，关不紧..."
      maxlength="200" show-word-limit class="custom-input" />

    <el-button :loading="loading" :disabled="!input.trim()" @click="handleAnalyze"
      class="w-full mt-3 !h-11 !rounded-xl !font-semibold !border-none !text-white !shadow-lg !shadow-indigo-200"
      style="background: linear-gradient(135deg,#6366f1,#8b5cf6);">
      <el-icon><MagicStick /></el-icon> AI 智能分析
    </el-button>

    <!-- 结果 -->
    <div v-if="result" class="mt-4 space-y-3 animate-in">
      <div class="flex items-center gap-2">
        <div class="w-1.5 h-1.5 rounded-full"
          :class="result.source === 'ai' ? 'bg-emerald-500' : 'bg-amber-500'"></div>
        <span class="text-xs text-slate-400">{{ result.source === 'ai' ? 'AI 生成' : '离线模式' }}</span>
      </div>

      <div class="bg-white/80 rounded-xl p-3 border border-white/60">
        <p class="text-[10px] text-slate-400 uppercase font-semibold mb-1">分类</p>
        <el-tag size="small" round effect="dark" :color="catColor(result.category)">{{ result.category }}</el-tag>
      </div>
      <div class="bg-white/80 rounded-xl p-3 border border-white/60">
        <p class="text-[10px] text-slate-400 uppercase font-semibold mb-1">规范标题</p>
        <p class="text-sm font-semibold text-slate-700">{{ result.title }}</p>
      </div>
      <div class="bg-white/80 rounded-xl p-3 border border-white/60">
        <p class="text-[10px] text-slate-400 uppercase font-semibold mb-1">完善描述</p>
        <p class="text-sm text-slate-600 leading-relaxed">{{ result.description }}</p>
      </div>
      <div class="bg-white/80 rounded-xl p-3 border border-white/60">
        <p class="text-[10px] text-slate-400 uppercase font-semibold mb-1">临时建议</p>
        <p class="text-sm text-slate-600 whitespace-pre-wrap leading-relaxed">{{ result.advice }}</p>
      </div>

      <el-button @click="$emit('apply', result)" class="w-full !rounded-xl !font-medium" type="primary" plain>
        <el-icon><Check /></el-icon> 填入报修表单
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { analyzeFault, getAiQuota } from '@/api/ai'

defineEmits(['apply'])
const input = ref('')
const loading = ref(false)
const result = ref(null)
const quota = ref(null)

onMounted(async () => { try { const r = await getAiQuota(); quota.value = r.data } catch { } })

async function handleAnalyze() {
  if (!input.value.trim()) return
  loading.value = true
  try { const r = await analyzeFault(input.value.trim()); result.value = r.data; if (quota.value) quota.value.remaining-- } catch { }
  finally { loading.value = false; try { const q = await getAiQuota(); quota.value = q.data } catch { } }
}

function catColor(c) {
  const m = { '水电': '#ef4444', '家具': '#f59e0b', '网络': '#3b82f6', '公共设施': '#10b981' }
  return m[c] || '#64748b'
}
</script>
