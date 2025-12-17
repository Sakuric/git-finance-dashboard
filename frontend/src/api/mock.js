// 模拟数据服务，用于在后端服务不可用时提供数据
export const mockApi = {
  // 模拟延迟
  delay(ms = 500) {
    return new Promise(resolve => setTimeout(resolve, ms))
  },

  // 模拟自选股数据
  mockFavorites: [
    {
      id: 1,
      stockCode: '000001',
      stockName: '平安银行',
      currentPrice: 12.85,
      changePercent: 1.2,
      industry: '金融',
      exchange: '深交所',
      remark: '长期持有'
    },
    {
      id: 2,
      stockCode: '000002',
      stockName: '万科A',
      currentPrice: 18.45,
      changePercent: -0.8,
      industry: '房地产',
      exchange: '深交所',
      remark: '观察中'
    },
    {
      id: 3,
      stockCode: '600519',
      stockName: '贵州茅台',
      currentPrice: 1850.00,
      changePercent: 0.5,
      industry: '消费',
      exchange: '上交所',
      remark: '核心持仓'
    },
    {
      id: 4,
      stockCode: '000858',
      stockName: '五粮液',
      currentPrice: 165.80,
      changePercent: 1.8,
      industry: '消费',
      exchange: '深交所',
      remark: '优质白马股'
    },
    {
      id: 5,
      stockCode: '300750',
      stockName: '宁德时代',
      currentPrice: 218.50,
      changePercent: -2.1,
      industry: '新能源',
      exchange: '深交所',
      remark: '新能源龙头'
    }
  ],

  // 模拟投资建议数据
  mockAdvices: [
    {
      id: 1,
      title: '新能源汽车板块投资建议',
      content: '基于当前市场分析，新能源汽车板块具有较好的投资价值。随着政策支持力度加大和消费者接受度提升，行业龙头企业在技术、品牌和渠道方面具备明显优势。建议重点关注比亚迪、宁德时代等龙头企业，预计未来12-18个月将有较好表现。风险提示：关注补贴政策变化和原材料价格波动。',
      createdAt: '2023-10-27',
      type: 'buy',
      status: 'active',
      riskLevel: 'medium',
      expectedReturn: '15-25%',
      holdingPeriod: '12-18个月'
    },
    {
      id: 2,
      title: '金融股配置建议',
      content: '银行板块估值处于历史低位，具有较好的安全边际。随着经济复苏预期增强，银行业资产质量有望改善，净息差压力缓解。建议配置大型国有银行和优质股份制银行，适合稳健型投资者长期持有。重点关注工商银行、建设银行等龙头股。',
      createdAt: '2023-10-26',
      type: 'hold',
      status: 'active',
      riskLevel: 'low',
      expectedReturn: '8-12%',
      holdingPeriod: '18-24个月'
    },
    {
      id: 3,
      title: '科技股风险提示',
      content: '部分科技股估值偏高，短期面临调整压力。人工智能、半导体等热门赛道虽然长期前景向好，但当前估值已充分反映预期。建议谨慎对待，可等待市场回调后再择机介入。重点关注业绩确定性高的细分领域龙头。',
      createdAt: '2023-10-25',
      type: 'sell',
      status: 'active',
      riskLevel: 'high',
      expectedReturn: '-5-10%',
      holdingPeriod: '3-6个月'
    },
    {
      id: 4,
      title: '消费升级主题投资机会',
      content: '随着居民收入水平提升和消费结构升级，高端消费、健康医疗、教育培训等领域迎来发展机遇。建议关注白酒、医药、教育等细分行业的优质公司，这些企业具有较强的品牌护城河和定价能力。',
      createdAt: '2023-10-24',
      type: 'buy',
      status: 'active',
      riskLevel: 'medium',
      expectedReturn: '12-20%',
      holdingPeriod: '12-15个月'
    },
    {
      id: 5,
      title: '房地产行业谨慎观望',
      content: '房地产行业仍处于调整期，政策调控效果逐步显现。虽然部分优质房企已具备投资价值，但行业整体风险仍需警惕。建议投资者保持谨慎，可关注现金流充裕、土地储备优质的龙头企业，但需控制仓位。',
      createdAt: '2023-10-23',
      type: 'hold',
      status: 'active',
      riskLevel: 'high',
      expectedReturn: '0-8%',
      holdingPeriod: '6-12个月'
    }
  ],

  // 模拟回测历史数据
  mockBacktestHistory: [
    {
      id: 1,
      adviceId: 1,
      adviceTitle: '新能源汽车板块投资建议',
      backtestStartDate: '2022-01-01',
      backtestEndDate: '2023-10-27',
      totalReturn: 15.8,
      annualizedReturn: 12.5,
      maxDrawdown: -8.5,
      sharpeRatio: 1.25,
      winRate: 65.0,
      volatility: 12.3,
      isSuccess: true,
      createdAt: '2023-10-27 10:30:00'
    },
    {
      id: 2,
      adviceId: 2,
      adviceTitle: '金融股配置建议',
      backtestStartDate: '2022-01-01',
      backtestEndDate: '2023-10-27',
      totalReturn: 8.2,
      annualizedReturn: 6.8,
      maxDrawdown: -5.2,
      sharpeRatio: 0.95,
      winRate: 58.0,
      volatility: 8.7,
      isSuccess: true,
      createdAt: '2023-10-26 14:20:00'
    },
    {
      id: 3,
      adviceId: 3,
      adviceTitle: '科技股风险提示',
      backtestStartDate: '2022-01-01',
      backtestEndDate: '2023-10-27',
      totalReturn: -3.5,
      annualizedReturn: -2.8,
      maxDrawdown: -15.8,
      sharpeRatio: -0.25,
      winRate: 42.0,
      volatility: 18.5,
      isSuccess: false,
      failureReason: '最大回撤超过阈值',
      createdAt: '2023-10-25 16:45:00'
    }
  ],

  // 获取自选股列表
  async getFavorites() {
    await this.delay()
    return {
      code: 200,
      message: '获取成功',
      data: this.mockFavorites
    }
  },

  // 添加自选股
  async addFavorite(data) {
    await this.delay()
    const newFavorite = {
      id: this.mockFavorites.length + 1,
      stockCode: data.stockCode,
      stockName: this.getStockName(data.stockCode),
      currentPrice: this.getRandomPrice(),
      changePercent: this.getRandomChange(),
      industry: '其他',
      exchange: '深交所',
      remark: data.remark || ''
    }
    this.mockFavorites.push(newFavorite)
    return {
      code: 200,
      message: '添加成功',
      data: newFavorite
    }
  },

  // 删除自选股
  async removeFavorite(stockCode) {
    await this.delay()
    const index = this.mockFavorites.findIndex(fav => fav.stockCode === stockCode)
    if (index > -1) {
      this.mockFavorites.splice(index, 1)
      return {
        code: 200,
        message: '删除成功'
      }
    }
    return {
      code: 404,
      message: '股票不存在'
    }
  },

  // 获取投资建议列表
  async getAdviceList() {
    await this.delay()
    return {
      code: 200,
      message: '获取成功',
      data: this.mockAdvices
    }
  },

  // 运行回测
  async runBacktest(config) {
    console.log('模拟回测API被调用，参数:', config)
    await this.delay(1000)
    
    const totalReturn = parseFloat((Math.random() * 30 + 5).toFixed(2))
    const annualReturn = parseFloat((totalReturn * 1.1).toFixed(2))
    const maxDrawdown = parseFloat((-5 - Math.random() * 15).toFixed(2))
    const sharpeRatio = parseFloat((1 + Math.random()).toFixed(2))
    const tradeCount = Math.floor(10 + Math.random() * 30)
    const winRate = parseFloat((50 + Math.random() * 20).toFixed(2))
    
    // 生成收益曲线
    const equityCurve = []
    let capital = config.initialCapital || 100000
    const start = new Date(config.startDate || '2023-01-01')
    const end = new Date(config.endDate || '2023-12-31')
    for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 7)) {
      capital *= (1 + (Math.random() - 0.45) * 0.03)
      equityCurve.push([d.toISOString().split('T')[0], Math.round(capital)])
    }
    
    const result = {
      totalReturn,
      annualReturn,
      maxDrawdown,
      sharpeRatio,
      tradeCount,
      winRate,
      equityCurve
    }
    
    return { code: 200, data: result }
  },

  // 获取回测历史
  async getBacktestHistory() {
    await this.delay()
    return {
      code: 200,
      message: '获取成功',
      data: this.mockBacktestHistory
    }
  },

  // 获取回测详情
  async getBacktestDetail(id) {
    await this.delay()
    const detail = this.mockBacktestHistory.find(item => item.id === parseInt(id))
    if (detail) {
      return {
        code: 200,
        message: '获取成功',
        data: detail
      }
    }
    return {
      code: 404,
      message: '回测记录不存在'
    }
  },

  // 辅助方法
  getStockName(code) {
    const names = {
      '000001': '平安银行',
      '000002': '万科A',
      '600519': '贵州茅台',
      '000858': '五粮液',
      '300750': '宁德时代',
      '002594': '比亚迪',
      '600036': '招商银行'
    }
    return names[code] || `${code}股票`
  },

  getRandomPrice() {
    return (Math.random() * 200 + 10).toFixed(2)
  },

  getRandomChange() {
    return (Math.random() * 10 - 5).toFixed(2)
  },

  getRandomReturn() {
    return (Math.random() * 40 - 10).toFixed(2)
  },

  getRandomDrawdown() {
    return -(Math.random() * 20).toFixed(2)
  },

  getRandomSharpe() {
    return (Math.random() * 2 - 0.5).toFixed(2)
  },

  getRandomWinRate() {
    return (Math.random() * 40 + 40).toFixed(1)
  },

  getRandomVolatility() {
    return (Math.random() * 20 + 5).toFixed(1)
  },

  // 获取股票列表
  async getStockList() {
    await this.delay()
    return {
      code: 200,
      message: '获取成功',
      data: this.mockFavorites.map(fav => ({
        stockCode: fav.stockCode,
        stockName: fav.stockName,
        currentPrice: fav.currentPrice,
        changePercent: fav.changePercent,
        volume: Math.floor(Math.random() * 1000000),
        marketCap: Math.floor(Math.random() * 10000000000)
      }))
    }
  },

  // 获取股票详情
  async getStockDetail(stockCode) {
    await this.delay()
    const stock = this.mockFavorites.find(fav => fav.stockCode === stockCode)
    if (stock) {
      return {
        code: 200,
        message: '获取成功',
        data: {
          ...stock,
          pe: Math.random() * 50,
          pb: Math.random() * 10,
          roe: Math.random() * 20,
          eps: Math.random() * 5,
          bvps: Math.random() * 50,
          marketCap: Math.floor(Math.random() * 10000000000),
          volume: Math.floor(Math.random() * 1000000),
          turnover: (Math.random() * 10).toFixed(2),
          high52: stock.currentPrice * 1.2,
          low52: stock.currentPrice * 0.8
        }
      }
    }
    return { code: 404, message: '股票不存在' }
  },

  // 获取股票实时数据
  async getStockRealtime(stockCode) {
    await this.delay()
    const stock = this.mockFavorites.find(fav => fav.stockCode === stockCode)
    if (stock) {
      return {
        code: 200,
        message: '获取成功',
        data: {
          price: stock.currentPrice,
          change: stock.changePercent,
          changeAmount: (stock.currentPrice * stock.changePercent / 100).toFixed(2),
          volume: Math.floor(Math.random() * 100000),
          timestamp: new Date().toISOString()
        }
      }
    }
    return { code: 404, message: '股票不存在' }
  },

  // 获取K线数据
  async getStockKLine(stockCode, period = 'daily') {
    await this.delay()
    const data = []
    const basePrice = 100
    for (let i = 0; i < 100; i++) {
      const open = basePrice + (Math.random() - 0.5) * 10
      const close = open + (Math.random() - 0.5) * 5
      const high = Math.max(open, close) + Math.random() * 3
      const low = Math.min(open, close) - Math.random() * 3
      data.push({
        date: new Date(Date.now() - (100 - i) * 24 * 60 * 60 * 1000).toISOString().slice(0, 10),
        open: open.toFixed(2),
        close: close.toFixed(2),
        high: high.toFixed(2),
        low: low.toFixed(2),
        volume: Math.floor(Math.random() * 1000000)
      })
    }
    return {
      code: 200,
      message: '获取成功',
      data
    }
  },

  // 获取市场指数
  async getMarketIndices() {
    await this.delay()
    return {
      code: 200,
      message: '获取成功',
      data: [
        { name: '上证指数', code: 'SH000001', price: 3200.5, change: 1.2, changePercent: 0.38 },
        { name: '深证成指', code: 'SZ399001', price: 11200.8, change: -15.3, changePercent: -0.14 },
        { name: '创业板指', code: 'SZ399006', price: 2300.2, change: 20.1, changePercent: 0.88 },
        { name: '科创50', code: 'SH000688', price: 1050.3, change: 8.7, changePercent: 0.83 }
      ]
    }
  },

  // 搜索股票
  async searchStock(keyword) {
    await this.delay()
    const results = this.mockFavorites.filter(fav =>
      fav.stockCode.includes(keyword) || fav.stockName.includes(keyword)
    ).slice(0, 5)
    return {
      code: 200,
      message: '搜索成功',
      data: results
    }
  },

  // 获取用户信息
  async getUserInfo() {
    await this.delay()
    return {
      code: 200,
      message: '获取成功',
      data: {
        id: 1,
        username: 'testuser',
        email: 'test@example.com',
        phone: '138****8888',
        avatar: 'https://i.pravatar.cc/150?u=testuser',
        createTime: '2023-01-01',
        lastLoginTime: new Date().toISOString(),
        status: 'active'
      }
    }
  },

  // 获取投资偏好
  async getPreference() {
    await this.delay()
    return {
      code: 200,
      message: '获取成功',
      data: {
        id: 1,
        userId: 1,
        riskLevel: 'medium',
        investmentAmount: 100000,
        investmentPeriod: '1-3年',
        investmentGoals: ['资产增值', '稳健收益'],
        stockPreference: ['大盘股', '成长股'],
        industryPreference: ['科技', '消费', '医疗'],
        maxDrawdown: 15,
        expectedReturn: 15,
        updateTime: new Date().toISOString()
      }
    }
  },

  // 保存投资偏好
  async savePreference(data) {
    await this.delay()
    console.log('保存投资偏好:', data)
    return {
      code: 200,
      message: '保存成功',
      data: { ...data, id: Date.now() }
    }
  }
}