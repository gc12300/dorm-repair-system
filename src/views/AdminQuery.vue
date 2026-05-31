<template>
  <AppLayout>
    <div class="page-wrap">
      <div class="flex items-center gap-3 mb-2">
        <div class="w-10 h-10 rounded-xl flex items-center justify-center shadow-md" style="background: linear-gradient(135deg,#10b981,#06b6d4);">
          <el-icon color="#fff" size="20"><Cpu /></el-icon>
        </div>
        <div>
          <h2 class="page-title !mb-0">AI 智能查询</h2>
          <p class="page-subtitle !mb-0">自然语言查数据，AI 自动生成 SQL</p>
        </div>
      </div>

      <!-- 查询输入 -->
      <div class="glass-card mb-6">
        <div class="flex gap-3">
          <el-input v-model="query" placeholder="如：帮我查本周未处理的报修有多少条" size="large" clearable class="flex-1 custom-input"
            @keyup.enter="handleQuery">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-button size="large" :loading="loading" @click="handleQuery"
            class="!rounded-xl !font-semibold !shadow-lg !border-none !text-white !px-6"
            style="background: linear-gradient(135deg,#10b981,#06b6d4);">
            <el-icon><Connection /></el-icon> AI 查询
          </el-button>
        </div>
        <div class="mt-3 flex flex-wrap gap-2 items-center">
          <span class="text-xs text-slate-400">试试：</span>
          <el-tag v-for="ex in examples" :key="ex" size="small" class="cursor-pointer hover:scale-105 transition-transform"
            @click="query = ex; handleQuery()" type="info" round>{{ ex }}</el-tag>
        </div>
      </div>

      <!-- 生成 SQL -->
      <div v-if="result" class="glass-card mb-5 animate-in">
        <div class="flex items-center justify-between mb-3">
          <h3 class="text-sm font-bold text-slate-600">生成 SQL</h3>
          <el-tag :type="result.source === 'ai' ? 'success' : 'warning'" size="small" round effect="light">
            {{ result.source === 'ai' ? 'AI 生成' : '离线模式' }}
          </el-tag>
        </div>
        <div class="bg-slate-900 text-emerald-400 rounded-xl p-5 font-mono text-sm overflow-x-auto shadow-inner">
          {{ result.sql }}
        </div>
        <p v-if="result.explanation" class="mt-3 text-sm text-slate-400">
          <el-icon><InfoFilled /></el-icon> {{ result.explanation }}
        </p>
      </div>

      <!-- 结果 -->
      <div v-if="result && result.data && result.data.length > 0" class="glass-card animate-in">
        <h3 class="text-sm font-bold text-slate-600 mb-4">查询结果 ({{ result.data.length }} 条)</h3>
        <el-table :data="result.data" stripe border max-height="400" class="w-full" table-layout="auto">
          <el-table-column v-for="col in columns" :key="col.prop" :prop="col.prop" :label="col.label" min-width="120" show-overflow-tooltip>
            <template #default="{ row }">
              <el-tag v-if="col.prop === 'status'" :type="statusTagType(row.status)" size="small" round effect="dark">
                {{ row.status }}
              </el-tag>
              <el-tag v-else-if="col.prop === 'role'" size="small" round effect="light" :type="row.role === 'ADMIN' ? '' : 'info'">
                {{ row.role === 'ADMIN' ? '管理员' : row.role === 'STUDENT' ? '学生' : row.role }}
              </el-tag>
              <span v-else-if="col.prop.endsWith('_time') || col.prop === 'createTime' || col.prop === 'updateTime'">
                {{ formatTime(row[col.prop]) }}
              </span>
              <span v-else>{{ row[col.prop] }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-if="result && result.data && result.data.length === 0 && result.source === 'ai'" class="glass-card text-center py-12">
        <el-empty description="查询无结果" />
      </div>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import AppLayout from '@/components/AppLayout.vue'
import { nl2SqlQuery } from '@/api/ai'

const query = ref('')
const loading = ref(false)
const result = ref(null)
const examples = ['本周未处理的报修有多少条', '各栋宿舍楼的报修数量', '最近7天已完成的报修', '报修最多的5个宿舍']

const fieldLabelMap = {
  id: '编号',
  title: '标题',
  description: '描述',
  status: '状态',
  handle_desc: '处理说明',
  handleDesc: '处理说明',
  admin_id: '管理员ID',
  adminId: '管理员ID',
  admin_name: '处理人',
  adminName: '处理人',
  student_id: '学生ID',
  studentId: '学生ID',
  student_no: '学号',
  studentNo: '学号',
  student_name: '报修人',
  studentName: '报修人',
  name: '姓名',
  phone: '电话',
  student_phone: '电话',
  studentPhone: '电话',
  building: '楼栋',
  room_no: '房间号',
  roomNo: '房间号',
  dormitory_id: '宿舍ID',
  dormitoryId: '宿舍ID',
  create_time: '创建时间',
  createTime: '创建时间',
  update_time: '更新时间',
  updateTime: '更新时间',
  username: '用户名',
  role: '角色',
  user_id: '用户ID',
  userId: '用户ID',
  password: '密码',
  count: '数量',
  total: '总计',
  cnt: '数量',
  estimated_hours: '预估工时',
  estimatedHours: '预估工时',
  priority: '优先级',
  plan: '维修方案',
  tools: '所需工具',
  category: '分类',
  advice: '建议',
  source: '来源'
}

const columns = computed(() => {
  if (!result.value?.data?.length) return []
  return Object.keys(result.value.data[0]).map(key => ({
    prop: key,
    label: fieldLabelMap[key] || key
  }))
})

function statusTagType(s) {
  const m = {
    '待处理': 'warning', '处理中': '', '已完成': 'success', '已取消': 'info',
    'PENDING': 'warning', 'PROCESSING': '', 'COMPLETED': 'success', 'CANCELLED': 'info'
  }
  return m[s] || 'info'
}

function formatTime(t) {
  if (!t) return '-'
  return new Date(t).toLocaleString('zh-CN')
}

async function handleQuery() {
  if (!query.value.trim()) { ElMessage.warning('请输入查询内容'); return }
  loading.value = true
  try { const res = await nl2SqlQuery(query.value.trim()); result.value = res.data } catch { }
  finally { loading.value = false }
}
</script>
