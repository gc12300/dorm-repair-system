import request from './index'

export function submitRepair(data) {
  return request.post('/repair/submit', data)
}

export function getMyRepairs() {
  return request.get('/repair/my')
}

export function getAllRepairs() {
  return request.get('/repair/all')
}

export function getRepairDetail(id) {
  return request.get(`/repair/${id}`)
}

export function handleRepair(data) {
  return request.put('/repair/handle', data)
}

export function rateRepair(data) {
  return request.post('/repair/rate', data)
}
