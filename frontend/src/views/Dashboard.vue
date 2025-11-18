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
        <div class="index-item">
          <p>上证指数</p>
          <h3 class="positive">3,145.80</h3>
          <span class="positive">+12.50 (+0.40%)</span>
        </div>
        <div class="index-item">
          <p>深证成指</p>
          <h3 class="negative">10,480.11</h3>
          <span class="negative">-25.30 (-0.24%)</span>
        </div>
        <div class="index-item">
          <p>恒生指数</p>
          <h3 class="positive">18,500.20</h3>
          <span class="positive">+210.70 (+1.15%)</span>
        </div>
      </div>

      <!-- 主图表卡片 -->
      <div class="card chart-card-large">
        <div class="card-header">
          <h3>上证指数 K线图</h3>
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
        <div id="mainKLineChart" class="chart-container"></div>
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
            <div class="stock-price" :class="stock.change >= 0 ? 'positive' : 'negative'">
              <span>{{ stock.price }}</span>
              <small>{{ stock.changePercent }}%</small>
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
    const selectedPeriod = ref('daily')
    const chartInstance = ref(null)

    const periods = [
      { label: '日K', value: 'daily' },
      { label: '周K', value: 'weekly' },
      { label: '月K', value: 'monthly' }
    ]

    const watchlistStocks = ref([
      {
        name: '贵州茅台',
        code: '600519',
        price: '1850.00',
        change: 1.5,
        changePercent: '+1.50'
      },
      {
        name: '宁德时代',
        code: '300750',
        price: '218.50',
        change: -2.1,
        changePercent: '-2.10'
      },
      {
        name: '比亚迪',
        code: '002594',
        price: '255.88',
        change: 0.88,
        changePercent: '+0.88'
      }
    ])

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
          default:
            time.setDate(time.getDate() + 1)
        }
      }
      return data
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

      const dataCount = selectedPeriod.value === 'weekly' ? 104 : 
                      selectedPeriod.value === 'monthly' ? 48 : 200
      const rawData = generateKLineData(dataCount, selectedPeriod.value)
      const dates = rawData.map(item => item.time)
      const kData = rawData.map(item => item.k)
      
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
          }
        },
        grid: {
          left: '10%',
          right: '10%',
          bottom: '15%'
        },
        xAxis: {
          type: 'category',
          data: dates,
          axisLine: {
            lineStyle: {
              color: '#8B949E'
            }
          },
          axisLabel: {
            color: '#8B949E'
          }
        },
        yAxis: {
          scale: true,
          axisLine: {
            lineStyle: {
              color: '#8B949E'
            }
          },
          splitLine: {
            lineStyle: {
              color: '#30363D'
            }
          }
        },
        dataZoom: [
          {
            type: 'inside',
            start: 80,
            end: 100
          },
          {
            show: true,
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
            name: selectedPeriod.value === 'daily' ? '日K' : 
                  selectedPeriod.value === 'weekly' ? '周K' : '月K',
            type: 'candlestick',
            data: kData,
            itemStyle: {
              color: 'var(--color-positive)',
              color0: 'var(--color-negative)',
              borderColor: 'var(--color-positive)',
              borderColor0: 'var(--color-negative)'
            }
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
        initKLineChart()
      })
    }

    onMounted(() => {
      nextTick(() => {
        initKLineChart()
      })
    })

    return {
      selectedPeriod,
      periods,
      watchlistStocks,
      changePeriod
    }
  }
}
</script>