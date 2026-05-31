import request from './index'

export function analyzeFault(query) {
  return request.post('/ai/student/analyze', { query })
}

export function suggestRepair(query) {
  return request.post('/ai/worker/suggest', { query })
}

export function nl2SqlQuery(query) {
  return request.post('/ai/admin/query', { query })
}

export function getAiQuota() {
  return request.get('/ai/quota')
}
