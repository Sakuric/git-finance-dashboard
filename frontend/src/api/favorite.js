import request from './request'

export function getFavorites(userId) {
  return request({
    url: '/favorites/list',
    method: 'get',
    params: { user_id: userId }
  })
}

export function addFavorite(userId, stockCode, remark = '') {
  return request({
    url: '/favorites/add',
    method: 'post',
    params: { user_id: userId, stock_code: stockCode, remark }
  })
}

export function removeFavorite(userId, stockCode) {
  return request({
    url: '/favorites/remove',
    method: 'delete',
    params: { user_id: userId, stock_code: stockCode }
  })
}

export function updateSortOrder() {
  return Promise.reject(new Error('Sort endpoint not implemented'))
}

export function updateRemark() {
  return Promise.reject(new Error('Remark endpoint not implemented'))
}

export function batchUpdateSortOrder() {
  return Promise.reject(new Error('Batch sort endpoint not implemented'))
}

export function checkFavorite(userId, stockCode) {
  return request({
    url: '/favorites/check',
    method: 'get',
    params: { user_id: userId, stock_code: stockCode }
  })
}

export function getFavoriteCount() {
  return Promise.reject(new Error('Count endpoint not implemented'))
}