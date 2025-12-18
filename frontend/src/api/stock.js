import request from './request'

export function getStockList(params) {
  return request({
    url: '/stocks',
    method: 'get',
    params
  })
}

export function getStockDetail(stockCode) {
  return request({
    url: `/stocks/${stockCode}`,
    method: 'get'
  })
}

export function getStockRealtime(stockCode) {
  return request({
    url: `/test/sina/realtime/${stockCode}`,
    method: 'get'
  })
}

export function getStockKLine(stockCode, params) {
  return request({
    url: `/stock-data/kline/${stockCode}`,
    method: 'get',
    params
  })
}

export function getStockTimeline(stockCode, days = 30) {
  return request({
    url: `/stock-data/kline/${stockCode}/recent`,
    method: 'get',
    params: { days }
  })
}

export function getWatchlist(userId) {
  return request({
    url: '/favorites/list',
    method: 'get',
    params: { user_id: userId }
  })
}

export function addToWatchlist(userId, stockCode, remark = '') {
  return request({
    url: '/favorites/add',
    method: 'post',
    params: { user_id: userId, stock_code: stockCode, remark }
  })
}

export function removeFromWatchlist(userId, stockCode) {
  return request({
    url: '/favorites/remove',
    method: 'delete',
    params: { user_id: userId, stock_code: stockCode }
  })
}

export function getMarketIndices() {
  return request({
    url: '/market/indices',
    method: 'get'
  })
}

export function getMarketOverview() {
  return request({
    url: '/market/overview',
    method: 'get'
  })
}

export function getIndexKLine(indexCode, days = 250) {
  return request({
    url: `/market/indices/${indexCode}/kline`,
    method: 'get',
    params: { days }
  })
}

export function searchStock(keyword) {
  return request({
    url: '/stocks/query',
    method: 'post',
    data: { keyword }
  })
}
