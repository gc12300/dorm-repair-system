<template>
  <AppLayout>
    <div class="page-wrap">
      <h2 class="page-title">提交报修</h2>
      <p class="page-subtitle">描述故障信息，提交维修申请</p>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- 表单 -->
        <div class="lg:col-span-2">
          <div class="glass-card">
            <el-steps :active="activeStep" align-center class="mb-8">
              <el-step title="填写信息" />
              <el-step title="确认提交" />
              <el-step title="完成" />
            </el-steps>

            <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large">
              <el-form-item label="报修标题" prop="title">
                <el-input v-model="form.title" placeholder="如：卫生间水龙头漏水、空调不制冷..."
                  maxlength="100" show-word-limit class="custom-input" />
              </el-form-item>
              <el-form-item label="故障描述" prop="description">
                <el-input v-model="form.description" type="textarea" :rows="5"
                  placeholder="请详细描述故障情况、发生时间、具体位置等..."
                  maxlength="500" show-word-limit class="custom-input" />
              </el-form-item>
              <el-form-item>
                <el-button :loading="submitting" @click="handleSubmit"
                  class="w-full md:w-auto !h-11 !rounded-xl !font-semibold !border-none"
                  style="background: linear-gradient(135deg, #6366f1, #8b5cf6); color: #fff;">
                  <el-icon><Check /></el-icon> 提交报修
                </el-button>
                <el-button @click="handleReset" class="!h-11 !rounded-xl !font-medium">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 提交成功 -->
          <div v-if="submitted" class="glass-card mt-5 text-center py-8 animate-in">
            <div class="w-16 h-16 rounded-full bg-green-100 mx-auto flex items-center justify-center mb-4">
              <el-icon color="#10b981" size="32"><Check /></el-icon>
            </div>
            <h3 class="text-lg font-bold text-slate-700">报修提交成功</h3>
            <p class="text-sm text-slate-400 mt-1">您的报修已提交，维修人员将尽快处理</p>
            <el-button class="mt-4 !rounded-xl" @click="submitted = false; form = { title: '', description: '' }">
              继续报修
            </el-button>
          </div>
        </div>

        <!-- AI 助手 -->
        <div class="lg:col-span-1">
          <AiAssistant @apply="applyAiResult" />
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import AppLayout from '@/components/AppLayout.vue'
import AiAssistant from '@/components/AiAssistant.vue'
import { submitRepair } from '@/api/repair'

const formRef = ref(null)
const submitting = ref(false)
const submitted = ref(false)
const activeStep = ref(0)
const form = reactive({ title: '', description: '' })
const rules = {
  title: [{ required: true, message: '请输入报修标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入故障描述', trigger: 'blur' }]
}

function applyAiResult(data) {
  form.title = data.title || ''
  form.description = data.description || ''
  ElMessage.success('AI 分析结果已填入表单')
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true; activeStep.value = 1
  try {
    await submitRepair({ title: form.title, description: form.description })
    submitted.value = true; activeStep.value = 2
  } catch { activeStep.value = 0 }
  finally { submitting.value = false }
}

function handleReset() { formRef.value?.resetFields(); activeStep.value = 0 }
</script>
