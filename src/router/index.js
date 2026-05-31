import { createRouter, createWebHistory } from 'vue-router'
import { parseJwt } from '@/composables/useAuth'

const routes = [
  {
    path: '/',
    redirect: () => {
      const token = localStorage.getItem('token')
      if (!token) return '/login'
      const identity = localStorage.getItem('identity')
      const payload = parseJwt(token)
      if (identity === 'worker') return '/worker/repairs'
      if (identity === 'admin') return '/admin/repairs'
      if (payload?.role === 'ADMIN') return '/admin/repairs'
      return '/student/repairs'
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { guest: true }
  },
  {
    path: '/student',
    redirect: '/student/repairs'
  },
  {
    path: '/student/submit',
    name: 'StudentSubmit',
    component: () => import('@/views/StudentSubmit.vue'),
    meta: { role: 'STUDENT' }
  },
  {
    path: '/student/repairs',
    name: 'StudentRepairs',
    component: () => import('@/views/StudentRepairs.vue'),
    meta: { role: 'STUDENT' }
  },
  {
    path: '/admin',
    redirect: '/admin/repairs'
  },
  {
    path: '/admin/repairs',
    name: 'AdminRepairs',
    component: () => import('@/views/AdminRepairs.vue'),
    meta: { role: 'ADMIN' }
  },
  {
    path: '/admin/query',
    name: 'AdminQuery',
    component: () => import('@/views/AdminQuery.vue'),
    meta: { role: 'ADMIN' }
  },
  {
    path: '/worker',
    redirect: '/worker/repairs'
  },
  {
    path: '/worker/repairs',
    name: 'WorkerRepairs',
    component: () => import('@/views/WorkerRepairs.vue'),
    meta: { role: 'ADMIN' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')

  if (to.meta.guest) {
    if (token) {
      const identity = localStorage.getItem('identity')
      if (identity === 'worker') return next('/worker/repairs')
      if (identity === 'admin') return next('/admin/repairs')
      const payload = parseJwt(token)
      if (payload?.role === 'ADMIN') return next('/admin/repairs')
      return next('/student/repairs')
    }
    return next()
  }

  if (!token) return next('/login')

  if (to.meta.role) {
    const payload = parseJwt(token)
    if (payload?.role !== to.meta.role) {
      const identity = localStorage.getItem('identity')
      if (identity === 'worker') return next('/worker/repairs')
      if (identity === 'admin') return next('/admin/repairs')
      if (payload?.role === 'ADMIN') return next('/admin/repairs')
      return next('/student/repairs')
    }
  }

  next()
})

export default router
