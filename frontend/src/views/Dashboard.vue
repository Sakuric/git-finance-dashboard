<template>
  <div class="page-content active">
    <!-- 顶部 Header -->
    <header class="header">
      <div class="header-left">
        <h2>仪表盘</h2>
        <p>欢迎回来, Admin</p>
      </div>
      <div class="header-right">
        <div class="search-box">
          <input type="text" placeholder="搜索股票代码/名称...">
          <i class="fas fa-search"></i>
        </div>
        <div class="user-profile">
          <img src="https://i.pravatar.cc/40?u=admin" alt="User Avatar">
        </div>
      </div>
    </header>

    <!-- 内容网格 -->
    <div class="content-grid">
      <!-- 主要市场指数 -->
      <div class="card index-card">
        <div
          v-for="(index, idx) in topIndices"
          :key="index.code"
          class="index-item"
          @click="selectTopIndex(index.code)"
        >
          <div class="index-info">
            <p>{{ index.name }}</p>
            <h3 :class="index.change >= 0 ? 'positive' : 'negative'">{{ index.price }}</h3>
            <span :class="index.change >= 0 ? 'positive' : 'negative'">
              {{ index.change >= 0 ? '+' : '' }}{{ index.change }} ({{ index.changePercent }}%)
            </span>
          </div>
          <div :id="'miniChart' + idx" class="mini-chart"></div>
        </div>
      </div>

      <!-- 主图表卡片 -->
      <div class="card chart-card-large">
        <div class="card-header">
          <h3>{{ currentIndex.name }}</h3>
          <div class="index-selector">
            <select v-model="selectedIndex" @change="changeIndex">
              <option v-for="index in indices" :key="index.code" :value="index.code">
                {{ index.name }}
              </option>
            </select>
          </div>
          <div class="time-selector">
            <button
              v-for="period in periods"
              :key="period.value"
              :class="{ active: selectedPeriod === period.value }"
              @click="changePeriod(period.value)"
            >
              {{ period.label }}
            </button>
          </div>
        </div>
        
        <!-- 价格信息显示 -->
        <div class="price-info">
          <div class="current-price">
            <span class="price-label">当前价格</span>
            <span class="price-value" :class="currentPriceData.change >= 0 ? 'positive' : 'negative'">
              {{ currentPriceData.price }}
            </span>
            <span class="price-change" :class="currentPriceData.change >= 0 ? 'positive' : 'negative'">
              {{ currentPriceData.change >= 0 ? '+' : '' }}{{ currentPriceData.change }}
            </span>
            <span class="price-percent" :class="currentPriceData.change >= 0 ? 'positive' : 'negative'">
              ({{ currentPriceData.changePercent }}%)
            </span>
          </div>
        </div>
        
        <div id="mainKLineChart" class="chart-container"></div>
      </div>

      <!-- 市场概况 -->
      <div class="card market-overview-card">
        <div class="card-header">
          <h3>市场概况</h3>
          <span class="update-time" style="color: var(--text-secondary); font-size: 0.85rem;">更新时间: {{ marketOverview.updateTime }}</span>
        </div>
        <div class="stat-row">
          <div class="stat-item">
            <span class="stat-label">上涨家数</span>
            <span class="stat-value positive" style="color: #00C087 !important; font-size: 1.75rem !important; font-weight: 700 !important;">{{ marketOverview.upCount }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">下跌家数</span>
            <span class="stat-value negative" style="color: #FF4D4F !important; font-size: 1.75rem !important; font-weight: 700 !important;">{{ marketOverview.downCount }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">涨停家数</span>
            <span class="stat-value positive" style="color: #00C087 !important; font-size: 1.75rem !important; font-weight: 700 !important;">{{ marketOverview.limitUpCount }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">跌停家数</span>
            <span class="stat-value negative" style="color: #FF4D4F !important; font-size: 1.75rem !important; font-weight: 700 !important;">{{ marketOverview.limitDownCount }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">总成交额</span>
            <span class="stat-value" style="color: #FFFFFF !important; font-size: 1.75rem !important; font-weight: 700 !important;">{{ marketOverview.totalVolume }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">换手率</span>
            <span class="stat-value" style="color: #FFFFFF !important; font-size: 1.75rem !important; font-weight: 700 !important;">{{ marketOverview.turnoverRate }}</span>
          </div>
        </div>
      </div>

      <!-- 我的自选 -->
      <div class="card watchlist-card">
        <div class="card-header">
          <h3>我的自选</h3>
          <a href="#" class="view-all">管理全部 <i class="fas fa-arrow-right"></i></a>
        </div>
        <ul class="watchlist">
          <li
            v-for="stock in watchlistStocks"
            :key="stock.code"
            class="watchlist-item"
          >
            <div class="stock-info">
              <span>{{ stock.name }}</span><small>{{ stock.code }}</small>
            </div>
            <div class="stock-details">
              <div class="stock-price" :class="stock.change >= 0 ? 'positive' : 'negative'">
                <span>{{ stock.price }}</span>
              </div>
              <div class="stock-change" :class="stock.change >= 0 ? 'positive' : 'negative'">
                <span>{{ stock.change >= 0 ? '+' : '' }}{{ stock.change }}</span>
                <small>{{ stock.changePercent }}%</small>
              </div>
            </div>
          </li>
        </ul>
      </div>

      <!-- AI 投资建议 -->
      <div class="card advice-card">
        <div class="card-header">
          <h3>最新智能投顾建议</h3>
          <a href="#" class="view-all">查看详情 <i class="fas fa-arrow-right"></i></a>
        </div>
        <div class="advice-content">
          <i class="fas fa-robot"></i>
          <p>根据您的 "稳健增长型" 偏好和最新市场数据，建议关注 <b>新能源汽车</b> 板块。可考虑在价格回调时适度建仓 <b>比亚迪 (002594)</b>。</p>
          <small>由 "我的增长模型 V2" 生成 - 2023-10-27 10:30</small>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'

export default {
  name: 'Dashboard',
  setup() {
    const selectedPeriod = ref('timeline')
    const selectedIndex = ref('000001')
    const chartInstance = ref(null)
    const currentPriceData = ref({
      price: '3,145.80',
      change: 12.50,
      changePercent: '+0.40'
    })

    const periods = [
      { label: '分时', value: 'timeline' },
      { label: '日K', value: 'daily' },
      { label: '周K', value: 'weekly' },
      { label: '月K', value: 'monthly' },
      { label: '年K', value: 'yearly' }
    ]

    const indices = [
      { code: '000001', name: '上证指数' },
      { code: '399001', name: '深证成指' },
      { code: '399006', name: '创业板指' },
      { code: '000300', name: '沪深300' },
      { code: '000016', name: '上证50' }
    ]

    const currentIndex = ref(indices[0])

    const topIndices = ref([
      {
        code: '000001',
        name: '上证指数',
        price: '3,145.80',
        change: 12.50,
        changePercent: '+0.40'
      },
      {
        code: '399001',
        name: '深证成指',
        price: '10,480.11',
        change: -25.30,
        changePercent: '-0.24'
      },
      {
        code: '399006',
        name: '创业板指',
        price: '1,985.50',
        change: 15.80,
        changePercent: '+0.80'
      }
    ])

    const watchlistStocks = ref([
      {
        name: '贵州茅台',
        code: '600519',
        price: '1850.00',
        change: 27.50,
        changePercent: '+1.50'
      },
      {
        name: '宁德时代',
        code: '300750',
        price: '218.50',
        change: -4.68,
        changePercent: '-2.10'
      },
      {
        name: '比亚迪',
        code: '002594',
        price: '255.88',
        change: 2.23,
        changePercent: '+0.88'
      }
    ])

    const marketOverview = ref({
      upCount: '2,458',
      downCount: '1,832',
      limitUpCount: '85',
      limitDownCount: '12',
      totalVolume: '8,956亿',
      turnoverRate: '2.35%',
      updateTime: '15:00:00'
    })

    // 生成分时数据
    const generateTimelineData = (indexCode = '000001') => {
      let data = []
      let time = new Date()
      time.setHours(9, 30, 0, 0) // 从9:30开始
      
      // 根据不同指数设置不同的基准价格
      let basePrice, yesterdayClose, volatility
      switch(indexCode) {
        case '000001': // 上证指数
          basePrice = 3145.80
          volatility = 15
          break
        case '399001': // 深证成指
          basePrice = 10480.11
          volatility = 50
          break
        case '399006': // 创业板指
          basePrice = 1985.50
          volatility = 25
          break
        case '000300': // 沪深300
          basePrice = 3850.75
          volatility = 20
          break
        case '000016': // 上证50
          basePrice = 2450.30
          volatility = 18
          break
        default:
          basePrice = 3000
          volatility = 15
      }
      
      yesterdayClose = basePrice * (1 - (Math.random() - 0.5) * 0.02) // 昨收价在基准价附近
      let totalVolume = 0
      let totalValue = 0
      
      // 生成上午数据 (9:30-11:30)
      for (let i = 0; i < 120; i++) {
        // 模拟真实的价格波动，开盘和收盘波动较大
        let timeFactor = i < 30 || i > 90 ? 1.5 : 1.0
        let price = basePrice + (Math.random() - 0.5) * volatility * timeFactor
        
        // 模拟成交量，开盘和收盘成交量较大
        let volumeFactor = i < 30 || i > 90 ? 1.5 : 1.0
        let volume = Math.floor(Math.random() * 1000000 * volumeFactor)
        
        totalVolume += volume
        totalValue += price * volume
        let avgPrice = totalValue / totalVolume
        
        data.push({
          time: `${String(time.getHours()).padStart(2, '0')}:${String(time.getMinutes()).padStart(2, '0')}`,
          price: price.toFixed(2),
          volume: volume,
          avgPrice: avgPrice.toFixed(2)
        })
        basePrice = price
        time.setMinutes(time.getMinutes() + 1)
      }
      
      // 跳过午休时间
      time.setHours(13, 0, 0, 0)
      
      // 生成下午数据 (13:00-15:00)
      for (let i = 0; i < 120; i++) {
        let timeFactor = i < 30 || i > 90 ? 1.5 : 1.0
        let price = basePrice + (Math.random() - 0.5) * volatility * timeFactor
        
        let volumeFactor = i < 30 || i > 90 ? 1.5 : 1.0
        let volume = Math.floor(Math.random() * 1000000 * volumeFactor)
        
        totalVolume += volume
        totalValue += price * volume
        let avgPrice = totalValue / totalVolume
        
        data.push({
          time: `${String(time.getHours()).padStart(2, '0')}:${String(time.getMinutes()).padStart(2, '0')}`,
          price: price.toFixed(2),
          volume: volume,
          avgPrice: avgPrice.toFixed(2)
        })
        basePrice = price
        time.setMinutes(time.getMinutes() + 1)
      }
      
      return { data, yesterdayClose }
    }

    // 生成迷你分时数据
    const generateMiniTimelineData = () => {
      let data = []
      let basePrice = 100
      
      for (let i = 0; i < 60; i++) {
        let price = basePrice + (Math.random() - 0.5) * 5
        data.push(price)
        basePrice = price
      }
      
      return data
    }

    // 初始化迷你分时图
    const initMiniCharts = () => {
      topIndices.value.forEach((index, idx) => {
        const chartDom = document.getElementById('miniChart' + idx)
        if (!chartDom) return

        const miniChart = echarts.init(chartDom)
        const data = generateMiniTimelineData()
        const isPositive = index.change >= 0

        const option = {
          grid: {
            left: 0,
            right: 0,
            top: 0,
            bottom: 0
          },
          xAxis: {
            type: 'category',
            show: false,
            data: data.map((_, i) => i)
          },
          yAxis: {
            type: 'value',
            show: false,
            scale: true
          },
          series: [
            {
              type: 'line',
              data: data,
              smooth: true,
              symbol: 'none',
              lineStyle: {
                width: 1.5,
                color: isPositive ? '#00B894' : '#D63031'
              },
              areaStyle: {
                color: {
                  type: 'linear',
                  x: 0,
                  y: 0,
                  x2: 0,
                  y2: 1,
                  colorStops: [
                    {
                      offset: 0,
                      color: isPositive ? 'rgba(0, 184, 148, 0.3)' : 'rgba(214, 48, 49, 0.3)'
                    },
                    {
                      offset: 1,
                      color: isPositive ? 'rgba(0, 184, 148, 0.05)' : 'rgba(214, 48, 49, 0.05)'
                    }
                  ]
                }
              }
            }
          ]
        }

        miniChart.setOption(option)
        window.addEventListener('resize', () => miniChart.resize())
      })
    }

    // 生成K线数据
    const generateKLineData = (count, period = 'daily') => {
      let data = []
      let time = new Date(2023, 0, 1)
      let basePrice = 200
      
      for (let i = 0; i < count; i++) {
        let open = basePrice
        
        // 根据不同周期调整价格波动范围
        let priceRange
        switch(period) {
          case 'weekly':
            priceRange = 40
            break
          case 'monthly':
            priceRange = 80
            break
          case 'yearly':
            priceRange = 150
            break
          default:
            priceRange = 20
        }
        
        let close = open + (Math.random() - 0.5) * priceRange
        let high = Math.max(open, close) + Math.random() * (priceRange / 4)
        let low = Math.min(open, close) - Math.random() * (priceRange / 4)
        
        data.push({
          time: time.toISOString().slice(0, 10),
          k: [open.toFixed(2), close.toFixed(2), low.toFixed(2), high.toFixed(2)]
        })
        basePrice = close
        
        // 根据不同周期增加时间
        switch(period) {
          case 'weekly':
            time.setDate(time.getDate() + 7)
            break
          case 'monthly':
            time.setMonth(time.getMonth() + 1)
            break
          case 'yearly':
            time.setFullYear(time.getFullYear() + 1)
            break
          default:
            time.setDate(time.getDate() + 1)
        }
      }
      return data
    }

    // 计算移动平均线
    const calculateMA = (data, period) => {
      const result = []
      for (let i = 0; i < data.length; i++) {
        if (i < period - 1) {
          result.push(null)
        } else {
          let sum = 0
          for (let j = 0; j < period; j++) {
            sum += parseFloat(data[i - j][1]) // 收盘价
          }
          result.push((sum / period).toFixed(2))
        }
      }
      return result
    }

    // 初始化分时图
    const initTimelineChart = () => {
      const chartDom = document.getElementById('mainKLineChart')
      if (!chartDom) return

      if (chartInstance.value) {
        chartInstance.value.dispose()
      }

      chartInstance.value = echarts.init(chartDom)

      const { data, yesterdayClose } = generateTimelineData(selectedIndex.value)
      const times = data.map(item => item.time)
      const prices = data.map(item => parseFloat(item.price))
      const volumes = data.map(item => item.volume)
      const avgPrices = data.map(item => item.avgPrice)

      // 计算涨跌幅百分比
      const changePercentages = prices.map(price => ((price - yesterdayClose) / yesterdayClose * 100).toFixed(2))

      const option = {
        backgroundColor: 'transparent',
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross'
          },
          backgroundColor: 'rgba(22, 27, 34, 0.9)',
          borderColor: '#30363D',
          textStyle: {
            color: '#C9D1D9'
          },
          formatter: function(params) {
            let result = params[0].axisValue + '<br/>'
            params.forEach(item => {
              if (item.seriesName === '价格') {
                const change = (item.data - yesterdayClose).toFixed(2)
                const changePercent = ((change / yesterdayClose) * 100).toFixed(2)
                const color = change >= 0 ? '#00B894' : '#D63031'
                result += `<span style="color: ${color}">${item.marker}${item.seriesName}: ${item.data}</span><br/>`
                result += `<span style="color: ${color}">涨跌: ${change >= 0 ? '+' : ''}${change} (${changePercent}%)</span><br/>`
              } else if (item.seriesName === '均价') {
                result += `<span style="color: #FFA500">${item.marker}${item.seriesName}: ${item.data}</span><br/>`
              } else {
                result += `${item.marker}${item.seriesName}: ${item.data}<br/>`
              }
            })
            return result
          }
        },
        grid: [
          {
            left: '10%',
            right: '10%',
            top: '10%',
            height: '50%'
          },
          {
            left: '10%',
            right: '10%',
            top: '70%',
            height: '15%'
          }
        ],
        xAxis: [
          {
            type: 'category',
            data: times,
            gridIndex: 0,
            axisLine: {
              lineStyle: {
                color: '#8B949E'
              }
            },
            axisLabel: {
              color: '#8B949E',
              interval: 29 // 每30分钟显示一次
            }
          },
          {
            type: 'category',
            data: times,
            gridIndex: 1,
            axisLine: {
              lineStyle: {
                color: '#8B949E'
              }
            },
            axisLabel: {
              show: false
            }
          }
        ],
        yAxis: [
          {
            scale: true,
            gridIndex: 0,
            splitNumber: 4,
            axisLine: {
              lineStyle: {
                color: '#8B949E'
              }
            },
            splitLine: {
              lineStyle: {
                color: '#30363D'
              }
            },
            axisLabel: {
              color: '#8B949E',
              formatter: function(value) {
                const change = ((value - yesterdayClose) / yesterdayClose * 100).toFixed(2)
                const color = change >= 0 ? '#00B894' : '#D63031'
                return `{value|${value.toFixed(2)}}\n{percent|${change >= 0 ? '+' : ''}${change}%}`
              },
              rich: {
                value: {
                  color: '#C9D1D9',
                  fontSize: 12
                },
                percent: {
                  color: '#8B949E',
                  fontSize: 10
                }
              }
            }
          },
          {
            scale: true,
            gridIndex: 1,
            splitNumber: 2,
            axisLine: {
              lineStyle: {
                color: '#8B949E'
              }
            },
            splitLine: {
              show: false
            },
            axisLabel: {
              color: '#8B949E'
            }
          }
        ],
        visualMap: {
          show: false,
          seriesIndex: 0,
          dimension: 1,
          pieces: [
            {
              lte: yesterdayClose,
              color: '#D63031' // 红色 - 跌
            },
            {
              gt: yesterdayClose,
              color: '#00B894' // 绿色 - 涨
            }
          ]
        },
        series: [
          {
            name: '价格',
            type: 'line',
            data: prices,
            smooth: true,
            symbol: 'none',
            lineStyle: {
              width: 1.5,
              color: function(params) {
                return params.data >= yesterdayClose ? '#00B894' : '#D63031'
              }
            },
            areaStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [
                  {
                    offset: 0,
                    color: 'rgba(0, 184, 148, 0.3)'
                  },
                  {
                    offset: 1,
                    color: 'rgba(0, 184, 148, 0.05)'
                  }
                ]
              }
            },
            xAxisIndex: 0,
            yAxisIndex: 0
          },
          {
            name: '均价',
            type: 'line',
            data: avgPrices,
            smooth: true,
            symbol: 'none',
            lineStyle: {
              width: 2,
              color: '#FFA500',
              type: 'dashed'
            },
            xAxisIndex: 0,
            yAxisIndex: 0
          },
          {
            name: '成交量',
            type: 'bar',
            data: volumes,
            xAxisIndex: 1,
            yAxisIndex: 1,
            itemStyle: {
              color: function(params) {
                return prices[params.dataIndex] >= yesterdayClose ?
                  '#00B894' : '#D63031' // 绿涨红跌
              }
            }
          }
        ]
      }

      chartInstance.value.setOption(option)
      window.addEventListener('resize', () => chartInstance.value.resize())
    }

    // 初始化K线图
    const initKLineChart = () => {
      const chartDom = document.getElementById('mainKLineChart')
      if (!chartDom) return

      // 销毁可能存在的旧图表实例
      if (chartInstance.value) {
        chartInstance.value.dispose()
      }

      chartInstance.value = echarts.init(chartDom)

      const dataCount = selectedPeriod.value === 'yearly' ? 20 :
                      selectedPeriod.value === 'weekly' ? 104 :
                      selectedPeriod.value === 'monthly' ? 48 : 200
      const rawData = generateKLineData(dataCount, selectedPeriod.value)
      const dates = rawData.map(item => item.time)
      const kData = rawData.map(item => item.k)
      
      // 生成成交量数据
      const volumes = kData.map((k, index) => {
        const open = parseFloat(k[0])
        const close = parseFloat(k[1])
        const volume = Math.floor(Math.random() * 10000000 + 5000000)
        return {
          value: volume,
          itemStyle: {
            color: close >= open ? '#00B894' : '#D63031' // 绿涨红跌
          }
        }
      })
      
      // 计算均线数据
      const ma5 = calculateMA(kData, 5)
      const ma10 = calculateMA(kData, 10)
      const ma20 = calculateMA(kData, 20)
      const ma30 = calculateMA(kData, 30)
      
      const option = {
        backgroundColor: 'transparent',
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross'
          },
          backgroundColor: 'rgba(22, 27, 34, 0.9)',
          borderColor: '#30363D',
          textStyle: {
            color: '#C9D1D9'
          },
          formatter: function(params) {
            let result = params[0].axisValue + '<br/>'
            params.forEach(item => {
              if (item.seriesName === 'K线') {
                const k = item.data
                const open = parseFloat(k[0])
                const close = parseFloat(k[1])
                const high = parseFloat(k[2])
                const low = parseFloat(k[3])
                const change = (close - open).toFixed(2)
                const changePercent = ((change / open) * 100).toFixed(2)
                const color = close >= open ? '#00B894' : '#D63031'
                
                result += `<span style="color: ${color}">${item.marker}开: ${k[0]}</span><br/>`
                result += `<span style="color: ${color}">${item.marker}收: ${k[1]}</span><br/>`
                result += `<span style="color: ${color}">${item.marker}高: ${k[2]}</span><br/>`
                result += `<span style="color: ${color}">${item.marker}低: ${k[3]}</span><br/>`
                result += `<span style="color: ${color}">${item.marker}涨跌: ${change >= 0 ? '+' : ''}${change} (${changePercent}%)</span><br/>`
              } else if (item.seriesName === '成交量') {
                result += `${item.marker}${item.seriesName}: ${item.data.value.toLocaleString()}<br/>`
              } else if (item.seriesName.includes('MA')) {
                result += `<span style="color: ${item.color}">${item.marker}${item.seriesName}: ${item.data}</span><br/>`
              }
            })
            return result
          }
        },
        legend: {
          data: ['K线', 'MA5', 'MA10', 'MA20', 'MA30', '成交量'],
          textStyle: {
            color: '#8B949E'
          },
          top: 10
        },
        grid: [
          {
            left: '10%',
            right: '10%',
            top: '15%',
            height: '60%'
          },
          {
            left: '10%',
            right: '10%',
            top: '80%',
            height: '15%'
          }
        ],
        xAxis: [
          {
            type: 'category',
            data: dates,
            gridIndex: 0,
            axisLine: {
              lineStyle: {
                color: '#8B949E'
              }
            },
            axisLabel: {
              color: '#8B949E'
            }
          },
          {
            type: 'category',
            data: dates,
            gridIndex: 1,
            axisLine: {
              lineStyle: {
                color: '#8B949E'
              }
            },
            axisLabel: {
              show: false
            }
          }
        ],
        yAxis: [
          {
            scale: true,
            gridIndex: 0,
            axisLine: {
              lineStyle: {
                color: '#8B949E'
              }
            },
            splitLine: {
              lineStyle: {
                color: '#30363D'
              }
            },
            axisLabel: {
              color: '#8B949E'
            }
          },
          {
            scale: true,
            gridIndex: 1,
            axisLine: {
              lineStyle: {
                color: '#8B949E'
              }
            },
            splitLine: {
              show: false
            },
            axisLabel: {
              color: '#8B949E'
            }
          }
        ],
        dataZoom: [
          {
            type: 'inside',
            xAxisIndex: [0, 1],
            start: 80,
            end: 100
          },
          {
            show: true,
            xAxisIndex: [0, 1],
            type: 'slider',
            bottom: 10,
            start: 80,
            end: 100,
            handleStyle: {
              color: '#00AFFF'
            },
            textStyle: {
              color: '#8B949E'
            }
          }
        ],
        series: [
          {
            name: 'K线',
            type: 'candlestick',
            data: kData,
            xAxisIndex: 0,
            yAxisIndex: 0,
            itemStyle: {
              color: '#00B894', // 绿色 - 涨（阳线）
              color0: '#D63031', // 红色 - 跌（阴线）
              borderColor: '#00B894',
              borderColor0: '#D63031'
            }
          },
          {
            name: 'MA5',
            type: 'line',
            data: ma5,
            smooth: true,
            symbol: 'none',
            lineStyle: {
              width: 1,
              color: '#FF6B6B'
            },
            xAxisIndex: 0,
            yAxisIndex: 0
          },
          {
            name: 'MA10',
            type: 'line',
            data: ma10,
            smooth: true,
            symbol: 'none',
            lineStyle: {
              width: 1,
              color: '#4ECDC4'
            },
            xAxisIndex: 0,
            yAxisIndex: 0
          },
          {
            name: 'MA20',
            type: 'line',
            data: ma20,
            smooth: true,
            symbol: 'none',
            lineStyle: {
              width: 1,
              color: '#45B7D1'
            },
            xAxisIndex: 0,
            yAxisIndex: 0
          },
          {
            name: 'MA30',
            type: 'line',
            data: ma30,
            smooth: true,
            symbol: 'none',
            lineStyle: {
              width: 1,
              color: '#96CEB4'
            },
            xAxisIndex: 0,
            yAxisIndex: 0
          },
          {
            name: '成交量',
            type: 'bar',
            data: volumes,
            xAxisIndex: 1,
            yAxisIndex: 1
          }
        ]
      }
      
      chartInstance.value.setOption(option)
      window.addEventListener('resize', () => chartInstance.value.resize())
    }

    // 切换时间周期
    const changePeriod = (period) => {
      selectedPeriod.value = period
      nextTick(() => {
        if (period === 'timeline') {
          initTimelineChart()
        } else {
          initKLineChart()
        }
      })
    }

    // 切换指数
    const changeIndex = () => {
      const index = indices.find(i => i.code === selectedIndex.value)
      if (index) {
        currentIndex.value = index
        // 更新顶部指数数据
        updateTopIndicesData(selectedIndex.value)
        nextTick(() => {
          if (selectedPeriod.value === 'timeline') {
            initTimelineChart()
          } else {
            initKLineChart()
          }
        })
      }
    }

    // 选择顶部指数
    const selectTopIndex = (code) => {
      selectedIndex.value = code
      const index = indices.find(i => i.code === code)
      if (index) {
        currentIndex.value = index
        updateTopIndicesData(code)
        nextTick(() => {
          if (selectedPeriod.value === 'timeline') {
            initTimelineChart()
          } else {
            initKLineChart()
          }
        })
      }
    }

    // 更新顶部指数数据
    const updateTopIndicesData = (selectedIndexCode) => {
      // 根据选择的指数更新数据
      topIndices.value.forEach(index => {
        const { data, yesterdayClose } = generateTimelineData(index.code)
        const lastData = data[data.length - 1]
        const currentPrice = parseFloat(lastData.price)
        const change = currentPrice - yesterdayClose
        const changePercent = (change / yesterdayClose * 100).toFixed(2)
        
        index.price = currentPrice.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
        index.change = change.toFixed(2)
        index.changePercent = changePercent >= 0 ? `+${changePercent}` : changePercent
      })
      
      // 更新当前价格数据
      const { data, yesterdayClose } = generateTimelineData(selectedIndexCode)
      const lastData = data[data.length - 1]
      const currentPrice = parseFloat(lastData.price)
      const change = currentPrice - yesterdayClose
      const changePercent = (change / yesterdayClose * 100).toFixed(2)
      
      currentPriceData.value = {
        price: currentPrice.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ','),
        change: change.toFixed(2),
        changePercent: changePercent >= 0 ? `+${changePercent}` : changePercent
      }
    }

    onMounted(() => {
      nextTick(() => {
        // 初始化顶部指数数据
        updateTopIndicesData(selectedIndex.value)
        initMiniCharts()
        initTimelineChart()
      })
    })

    return {
      topIndices,
      selectedPeriod,
      selectedIndex,
      periods,
      indices,
      currentIndex,
      watchlistStocks,
      marketOverview,
      currentPriceData,
      changePeriod,
      changeIndex,
      selectTopIndex,
      updateTopIndicesData
    }
  }
}
</script>