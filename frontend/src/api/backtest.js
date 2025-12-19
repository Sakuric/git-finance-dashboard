import request from './request'

export const backtestApi = {
  runBacktest(data) {
    return request({
      url: '/backtest/run',
      method: 'post',
      data
    })
  },

  getDetail(adviceId) {
    return request({
      url: `/backtest/${adviceId}`,
      method: 'get'
    })
  }
}