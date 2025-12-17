import request from './request'

export function getAiPlatforms(userId) {
  return request({
    url: `/ai-models/user/${userId}`,
    method: 'get'
  })
}

export function saveAiPlatform(data) {
  return request({
    url: '/ai-models',
    method: 'post',
    data
  })
}

export function updateAiPlatform(id, data) {
  return request({
    url: `/ai-models/${id}`,
    method: 'put',
    data
  })
}

export function deleteAiPlatform(id) {
  return request({
    url: `/ai-models/${id}`,
    method: 'delete'
  })
}