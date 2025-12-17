import request from './request'

export const backtestApi = {
  runBacktest() {
    return Promise.reject(new Error('Backend missing /backtest/run'))
  },

  getHistory() {
    return Promise.reject(new Error('Backend missing /backtest/history'))
  },

  getDetail(adviceId) {
    return request({
      url: `/backtest/${adviceId}`,
      method: 'get'
    })
  }
}