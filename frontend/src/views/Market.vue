<template>
  <div class="page-content active">
    <header class="header">
      <div class="header-left">
        <h2>个股行情</h2>
        <p>比亚迪 (002594)</p>
      </div>
    </header>
  
    <div class="card stock-detail-container">
      <!-- 左侧：图表区域 -->
      <div class="chart-section">
        <!-- 顶部核心信息区 -->
        <div class="stock-overview-pro">
          <div class="main-price-info">
            <span class="exchange-tag">深</span>
            <h1>比亚迪</h1>
            <span class="stock-code-pro">002594</span>
            <div class="current-price positive">
              <span class="price">{{ stockData.price }}</span>
              <span class="change">+{{ stockData.change }}</span>
              <span class="change-percent">+{{ stockData.changePercent }}%</span>
            </div>
          </div>
          <div class="key-metrics-grid">
            <div 
              v-for="metric in keyMetrics" 
              :key="metric.label"
              class="metric-item"
            >
              <span class="label">{{ metric.label }}</span>
              <span 
                class="value" 
                :class="metric.class"
              >{{ metric.value }}</span>
            </div>
          </div>
        </div>
        <!-- 图表区域 -->
        <div class="chart-tabs-pro">
          <div class="tabs-header-pro">
            <button 
              v-for="tab in chartTabs" 
              :key="tab.value"
              class="tab-button-pro"
              :class="{ active: activeTab === tab.value }"
              @click="switchTab(tab.value)"
            >
              {{ tab.label }}
            </button>
          </div>
          <div class="tab-content-pro active">
            <div 
              id="stockTimelineChart" 
              class="main-chart-container"
              v-show="activeTab === 'timeline'"
            ></div>
            <div 
              id="stockKLineChart" 
              class="main-chart-container"
              v-show="activeTab.startsWith('kline')"
            ></div>
          </div>
        </div>
      </div>

      <!-- 右侧：数据区域 -->
      <div class="data-section">
        <!-- 五档盘口 -->
        <div class="order-book-card">
          <ul class="order-list" id="sell-orders">
            <li 
              v-for="order in sellOrders" 
              :key="'sell-' + order.level"
              class="order-list-item"
            >
              <span class="order-label">卖{{ order.level }}</span>
              <span class="order-price negative">{{ order.price }}</span>
              <span class="order-volume">{{ order.volume }}</span>
            </li>
          </ul>
          <div class="current-price-divider">{{ stockData.price }}</div>
          <ul class="order-list" id="buy-orders">
            <li 
              v-for="order in buyOrders" 
              :key="'buy-' + order.level"
              class="order-list-item"
            >
              <span class="order-label">买{{ order.level }}</span>
              <span class="order-price positive">{{ order.price }}</span>
              <span class="order-volume">{{ order.volume }}</span>
            </li>
          </ul>
        </div>
        <!-- 分时成交 -->
        <div class="tick-data-card">
          <div class="tick-header">
            <span>时间</span>
            <span>价格</span>
            <span>成交量(手)</span>
          </div>
          <ul class="tick-list" id="tick-list-container">
            <li 
              v-for="tick in tickData" 
              :key="tick.time"
              class="tick-item"
            >
              <span class="time">{{ tick.time }}</span>
              <span 
                class="price" 
                :class="tick.change >= 0 ? 'positive' : 'negative'"
              >{{ tick.price }}</span>
              <span class="volume">{{ tick.volume }}</span>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'

export default {
  name: 'Market',
  setup() {
    const activeTab = ref('timeline')
    const timelineChart = ref(null)
    const klineChart = ref(null)

    const stockData = ref({
      price: '255.88',
      change: '2.23',
      changePercent: '0.88'
    })

    const keyMetrics = ref([
      { label: '高', value: '258.50', class: 'positive' },
      { label: '低', value: '252.30', class: 'negative' },
      { label: '开', value: '253.00', class: 'positive' },
      { label: '昨收', value: '253.65', class: '' },
      { label: '量', value: '15.2万手', class: '' },
      { label: '额', value: '3.9亿', class: '' },
      { label: '换手', value: '1.58%', class: '' },
      { label: '市盈', value: '25.1', class: '' },
      { label: '总市值', value: '7450亿', class: '' },
      { label: '流通值', value: '4980亿', class: '' }
    ])

    const chartTabs = [
      { label: '分时', value: 'timeline' },
      { label: '日K', value: 'kline-daily' },
      { label: '周K', value: 'kline-week' },
      { label: '月K', value: 'kline-month' }
    ]

    const sellOrders = ref([
      { level: 5, price: '255.90', volume: '120' },
      { level: 4, price: '255.89', volume: '85' },
      { level: 3, price: '255.88', volume: '200' },
      { level: 2, price: '255.87', volume: '150' },
      { level: 1, price: '255.86', volume: '95' }
    ])

    const buyOrders = ref([
      { level: 1, price: '255.85', volume: '180' },
      { level: 2, price: '255.84', volume: '220' },
      { level: 3, price: '255.83', volume: '165' },
      { level: 4, price: '255.82', volume: '130' },
      { level: 5, price: '255.81', volume: '75' }
    ])

    const tickData = ref([
      { time: '10:30:00', price: '255.88', volume: '120', change: 0.05 },
      { time: '10:29:30', price: '255.87', volume: '85', change: -0.02 },
      { time: '10:29:00', price: '255.89', volume: '200', change: 0.12 },
      { time: '10:28:30', price: '255.85', volume: '150', change: -0.08 },
      { time: '10:28:00', price: '255.86', volume: '95', change: 0.03 }
    ])

    // 生成分时数据
    const generateTimelineData = () => {
      const now = new Date()
      const priceData = []
      const volumeData = []
      let basePrice = 255.88
      
      for (let i = 0; i < 240; i++) {
        const time = new Date(now)
        time.setHours(9, 30 + i)
        
        const change = (Math.random() - 0.5) * 0.5
        basePrice += change
        
        const volume = Math.floor(Math.random() * 500) + 50
        
        priceData.push([time.toTimeString().slice(0, 5), basePrice.toFixed(2)])
        volumeData.push([time.toTimeString().slice(0, 5), volume])
      }
      
      return { priceData, volumeData }
    }

    // 生成K线数据
    const generateKLineData = (count) => {
      let data = []
      let time = new Date(2023, 0, 1)
      let basePrice = 200
      
      for (let i = 0; i < count; i++) {
        let open = basePrice
        let close = open + (Math.random() - 0.5) * 20
        let high = Math.max(open, close) + Math.random() * 5
        let low = Math.min(open, close) - Math.random() * 5
        
        data.push({
          time: time.toISOString().slice(0, 10),
          k: [open.toFixed(2), close.toFixed(2), low.toFixed(2), high.toFixed(2)],
          volume: Math.floor(Math.random() * 500000) + 100000
        })
        basePrice = close
        time.setDate(time.getDate() + 1)
      }
      return data
    }

    // 计算MACD指标
    const calculateMACD = (data) => {
      let a = data.map(d => parseFloat(d.k[1]))
      let ema12 = [a[0]]
      let ema26 = [a[0]]
      let diff = [0]
      
      for (let i = 1; i < a.length; i++) {
        ema12.push(ema12[i-1]*11/13 + a[i]*2/13)
        ema26.push(ema26[i-1]*25/27 + a[i]*2/27)
        diff.push(ema12[i]-ema26[i])
      }
      
      let dea = [diff[0]]
      for (let i = 1; i < diff.length; i++) {
        dea.push(dea[i-1]*8/10 + diff[i]*2/10)
      }
      
      return data.map((d, i) => ({
        diff: diff[i],
        dea: dea[i],
        macd: (diff[i] - dea[i]) * 2
      }))
    }

    // 初始化分时图
    const initTimelineChart = () => {
      const chartDom = document.getElementById('stockTimelineChart')
      if (!chartDom) return

      if (timelineChart.value) {
        timelineChart.value.dispose()
      }

      timelineChart.value = echarts.init(chartDom)
      const { priceData, volumeData } = generateTimelineData()
      
      const option = {
        backgroundColor: 'transparent',
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross'
          },
          backgroundColor: 'rgba(22, 27, 34, 0.9)',
          borderColor: '#30363D',
          textStyle: { color: '#C9D1D9' },
          formatter: function(params) {
            let result = `时间: ${params[0].name}<br/>`
            params.forEach(param => {
              if (param.seriesName === '分时') {
                result += `价格: ${param.value[1]}<br/>`
              } else if (param.seriesName === '成交量') {
                result += `成交量: ${param.value[1]}手<br/>`
              }
            })
            return result
          }
        },
        axisPointer: {
          link: { xAxisIndex: 'all' }
        },
        grid: [
          { left: '10%', right: '8%', height: '65%' },
          { left: '10%', right: '8%', top: '75%', height: '20%' }
        ],
        xAxis: [
          {
            type: 'category',
            data: priceData.map(item => item[0]),
            boundaryGap: false,
            axisLine: { lineStyle: { color: '#8B949E' } },
            axisLabel: {
              color: '#8B949E',
              interval: 30
            }
          },
          {
            type: 'category',
            gridIndex: 1,
            data: volumeData.map(item => item[0]),
            axisLine: { lineStyle: { color: '#8B949E' } },
            axisLabel: {
              color: '#8B949E',
              interval: 30
            }
          }
        ],
        yAxis: [
          {
            type: 'value',
            scale: true,
            min: function(value) {
              return value.min - 0.5
            },
            max: function(value) {
              return value.max + 0.5
            },
            axisLine: { lineStyle: { color: '#8B949E' } },
            splitLine: { lineStyle: { color: '#30363D' } }
          },
          {
            type: 'value',
            gridIndex: 1,
            axisLine: { show: false },
            axisTick: { show: false },
            axisLabel: { show: false },
            splitLine: { show: false }
          }
        ],
        series: [
          {
            name: '分时',
            type: 'line',
            smooth: false,
            data: priceData.map(item => item[1]),
            showSymbol: false,
            lineStyle: {
              width: 1,
              color: '#00AFFF'
            },
            areaStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [{
                  offset: 0, color: 'rgba(0, 175, 255, 0.3)'
                }, {
                  offset: 1, color: 'rgba(0, 175, 255, 0.05)'
                }]
              }
            }
          },
          {
            name: '成交量',
            type: 'bar',
            xAxisIndex: 1,
            yAxisIndex: 1,
            data: volumeData.map(item => item[1]),
            itemStyle: {
              color: '#00AFFF',
              opacity: 0.7
            }
          }
        ]
      }
      
      timelineChart.value.setOption(option)
      window.addEventListener('resize', () => timelineChart.value.resize())
    }

    // 初始化K线图
    const initKLineChart = () => {
      const chartDom = document.getElementById('stockKLineChart')
      if (!chartDom) return

      if (klineChart.value) {
        klineChart.value.dispose()
      }

      klineChart.value = echarts.init(chartDom)
      const rawData = generateKLineData(200)
      const dates = rawData.map(item => item.time)
      const kData = rawData.map(item => item.k)
      const volumes = rawData.map((item, index) => [index, item.volume, item.k[1] > item.k[0] ? 1 : -1])
      const macdData = calculateMACD(rawData)

      const option = {
        animation: false,
        backgroundColor: 'transparent',
        tooltip: { 
          trigger: 'axis', 
          axisPointer: { type: 'cross' }, 
          backgroundColor: 'rgba(22, 27, 34, 0.9)', 
          borderColor: '#30363D', 
          textStyle: { color: '#C9D1D9' } 
        },
        axisPointer: { link: { xAxisIndex: 'all' } },
        grid: [
          { left: '10%', right: '8%', height: '50%' },
          { left: '10%', right: '8%', top: '65%', height: '10%' },
          { left: '10%', right: '8%', top: '80%', height: '12%' }
        ],
        xAxis: [
          { type: 'category', data: dates, axisLine: { lineStyle: { color: '#8B949E' } }, axisLabel: { show: false } },
          { type: 'category', data: dates, gridIndex: 1, axisLine: { lineStyle: { color: '#8B949E' } }, axisLabel: { show: false } },
          { type: 'category', data: dates, gridIndex: 2, axisLine: { lineStyle: { color: '#8B949E' } } }
        ],
        yAxis: [
          { scale: true, axisLine: { lineStyle: { color: '#8B949E' } }, splitLine: { lineStyle: { color: '#30363D' } }},
          { scale: true, gridIndex: 1, axisLabel: { show: false }, axisLine: { show: false }, axisTick: { show: false }, splitLine: { show: false } },
          { scale: true, gridIndex: 2, axisLabel: { show: false }, axisLine: { show: false }, axisTick: { show: false }, splitLine: { show: false } }
        ],
        dataZoom: [{ type: 'inside', xAxisIndex: [0, 1, 2], start: 80, end: 100 }],
        series: [
          { type: 'candlestick', name: '日K', data: kData, itemStyle: { color: 'var(--color-positive)', color0: 'var(--color-negative)', borderColor: 'var(--color-positive)', borderColor0: 'var(--color-negative)' } },
          { type: 'bar', name: 'Volume', data: volumes, xAxisIndex: 1, yAxisIndex: 1, itemStyle: { color: ({ value }) => (value[2] === 1 ? 'var(--color-positive)' : 'var(--color-negative)') } },
          { name: 'MACD', type: 'bar', data: macdData.map(d => d.macd), xAxisIndex: 2, yAxisIndex: 2, itemStyle: { color: ({ value }) => (value > 0 ? 'var(--color-positive)' : 'var(--color-negative)') } },
          { name: 'DIF', type: 'line', data: macdData.map(d => d.diff), symbol: 'none', lineStyle: { width: 1 }, xAxisIndex: 2, yAxisIndex: 2 },
          { name: 'DEA', type: 'line', data: macdData.map(d => d.dea), symbol: 'none', lineStyle: { width: 1 }, xAxisIndex: 2, yAxisIndex: 2 }
        ]
      }
      
      klineChart.value.setOption(option)
      window.addEventListener('resize', () => klineChart.value.resize())
    }

    // 切换标签页
    const switchTab = (tab) => {
      activeTab.value = tab
      nextTick(() => {
        if (tab === 'timeline') {
          initTimelineChart()
        } else if (tab.startsWith('kline')) {
          initKLineChart()
        }
      })
    }

    // 模拟实时数据更新
    const startRealtimeSimulation = () => {
      setInterval(() => {
        // 更新价格
        const currentPrice = parseFloat(stockData.value.price)
        const change = (Math.random() - 0.5) * 0.1
        const newPrice = (currentPrice + change).toFixed(2)
        stockData.value.price = newPrice
        
        // 更新分时成交数据
        const now = new Date()
        const newTick = {
          time: now.toTimeString().slice(0, 8),
          price: newPrice,
          volume: Math.floor(Math.random() * 100) + 1,
          change: change
        }
        
        tickData.value.unshift(newTick)
        if (tickData.value.length > 50) {
          tickData.value.pop()
        }
      }, 3000)
    }

    onMounted(() => {
      nextTick(() => {
        initTimelineChart()
        startRealtimeSimulation()
      })
    })

    return {
      activeTab,
      stockData,
      keyMetrics,
      chartTabs,
      sellOrders,
      buyOrders,
      tickData,
      switchTab
    }
  }
}
</script>