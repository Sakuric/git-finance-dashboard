import request from './request'

export const adviceApi = {
  getAdviceList(userId) {
    return request({
      url: '/advice/list',
      method: 'get',
      params: { userId }
    })
  },

  getAdviceDetail(adviceId) {
    return request({
      url: `/structured-advice/${adviceId}`,
      method: 'get'
    })
  },

  createAdvice(userId) {
    return request({
      url: '/advice/generate',
      method: 'post',
      params: { userId }
    })
  },

  updateAdvice() {
    return Promise.reject(new Error('Advice update endpoint not implemented'))
  },

  deleteAdvice() {
    return Promise.reject(new Error('Advice delete endpoint not implemented'))
  },

  getUserAdvice(userId) {
    return request({
      url: '/advice/latest',
      method: 'get',
      params: { userId }
    })
  }
}