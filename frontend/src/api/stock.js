import request from './request'

// 获取股票列表
export function getStockList(params) {
  return request({
    url: '/stock/list',
    method: 'get',
    params
  })
}

// 获取股票详情
export function getStockDetail(stockCode) {
  return request({
    url: `/stock/${stockCode}`,
    method: 'get'
  })
}

// 获取股票实时数据
export function getStockRealtime(stockCode) {
  return request({
    url: `/stock/${stockCode}/realtime`,
    method: 'get'
  })
}

// 获取股票K线数据
export function getStockKLine(stockCode, params) {
  return request({
    url: `/stock/${stockCode}/kline`,
    method: 'get',
    params
  })
}

// 获取股票分时数据
export function getStockTimeline(stockCode) {
  return request({
    url: `/stock/${stockCode}/timeline`,
    method: 'get'
  })
}

// 获取自选股列表
export function getWatchlist() {
  return request({
    url: '/stock/watchlist',
    method: 'get'
  })
}

// 添加自选股
export function addToWatchlist(stockCode) {
  return request({
    url: '/stock/watchlist/add',
    method: 'post',
    data: { stockCode }
  })
}

// 移除自选股
export function removeFromWatchlist(stockCode) {
  return request({
    url: '/stock/watchlist/remove',
    method: 'post',
    data: { stockCode }
  })
}

// 获取市场指数
export function getMarketIndices() {
  return request({
    url: '/stock/indices',
    method: 'get'
  })
}

// 搜索股票
export function searchStock(keyword) {
  return request({
    url: '/stock/search',
    method: 'get',
    params: { keyword }
  })
}