import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { parseJwt } from '@/composables/useAuth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const identity = ref(localStorage.getItem('identity') || '') // student / worker / admin

  const role = computed(() => {
    if (token.value) {
      const payload = parseJwt(token.value)
      return payload?.role || null
    }
    return null
  })

  const userId = computed(() => {
    if (token.value) {
      const payload = parseJwt(token.value)
      return payload?.sub || null
    }
    return null
  })

  const isLoggedIn = computed(() => !!token.value)

  function setToken(t) {
    token.value = t
    localStorage.setItem('token', t)
  }

  function setIdentity(id) {
    identity.value = id
    localStorage.setItem('identity', id)
  }

  function logout() {
    token.value = ''
    identity.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('identity')
  }

  return { token, identity, role, userId, isLoggedIn, setToken, setIdentity, logout }
})
