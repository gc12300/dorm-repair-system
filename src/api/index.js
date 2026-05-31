import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

request.interceptors.response.use(
  response => {
    const data = response.data
    if (data.code !== 200) {
      ElMessage.error(data.message || '请求失败')
      if (data.code === 401) {
        clearAuth()
      }
      return Promise.reject(new Error(data.message))
    }
    return data
  },
  error => {
    if (error.response?.status === 401) {
      if (!window.location.pathname.includes('/login')) {
        clearAuth()
        ElMessage.error('登录已过期，请重新登录')
      }
    } else if (error.response?.status === 403) {
      ElMessage.error('没有权限访问')
    } else if (error.code === 'ERR_NETWORK') {
      ElMessage.error('无法连接服务器，请检查后端是否启动')
    } else {
      // 静默处理其他错误，由调用方 catch 处理
    }
    return Promise.reject(error)
  }
)

function clearAuth() {
  localStorage.removeItem('token')
  localStorage.removeItem('identity')
  window.location.href = '/login'
}

export default request
