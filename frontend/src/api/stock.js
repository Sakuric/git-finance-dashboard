import request from '@/utils/request'

export function getStockList() {
    return request({
        url: '/stocks',
        method: 'get'
    })
}

export function getStockDetail(stockCode) {
    return request({
        url: `/stocks/${stockCode}`,
        method: 'get'
    })
}

export function addStock(data) {
    return request({
        url: '/stocks',
        method: 'post',
        data
    })
}

export function updateStock(data) {
    return request({
        url: '/stocks',
        method: 'put',
        data
    })
}

export function deleteStock(stockCode) {
    return request({
        url: `/stocks/${stockCode}`,
        method: 'delete'
    })
}

export function queryStocks(data) {
    return request({
        url: '/stocks/query',
        method: 'post',
        data
    })
}