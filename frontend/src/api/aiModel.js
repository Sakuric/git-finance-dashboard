import request from './request'

export const getAiModels = (userId) => {
  return request.get(`/ai-models/user/${userId}`)
}

export const addAiModel = (data) => {
  return request.post('/ai-models', data)
}

export const updateAiModel = (id, data) => {
  return request.put(`/ai-models/${id}`, data)
}

export const deleteAiModel = (id) => {
  return request.delete(`/ai-models/${id}`)
}

export const testApiKey = (data) => {
  return request.post('/ai-models/test', data)
}
