import request from './request'

export const getPortfolioBalance = (userId) => {
  return request.get(`/portfolio-analysis/balance/${userId}`)
}
