<template>
  <div class="page-content active">
    <header class="header">
      <div class="header-left">
        <h2>个股行情</h2>
        <p>{{ stockData.name || '--' }} ({{ stockData.code || stockCode }})</p>
      </div>
    </header>
  
    <div class="card stock-detail-container">
      <!-- 左侧：图表区域 -->
      <div class="chart-section">
        <!-- 顶部核心信息区 -->
        <div class="stock-overview-pro">
          <div class="main-price-info">
            <span class="exchange-tag">{{ stockData.exchange || '--' }}</span>
            <h1>{{ stockData.name || '--' }}</h1>
            <span class="stock-code-pro">{{ stockData.code || stockCode }}</span>
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
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
import { getStockDetail, getStockRealtime, getStockTimeline, getStockKLine, getEastMoneyQuote } from '@/api/stock'

export default {
  name: 'Market',
  setup() {
    const route = useRoute()
    const stockCode = ref(route.query.stock || '002594')
    const loading = ref(false)
    const activeTab = ref('timeline')
    const timelineChart = ref(null)
    const klineChart = ref(null)
    const apiTimeline = ref([])
    const apiKline = ref([])

    const stockData = ref({
      name: '--',
      code: stockCode.value,
      exchange: '--',
      price: '--',
      change: '--',
      changePercent: '--'
    })

    const keyMetrics = ref([
      { label: '高', value: '--', class: '' },
      { label: '低', value: '--', class: '' },
      { label: '开', value: '--', class: '' },
      { label: '昨收', value: '--', class: '' },
      { label: '量', value: '--', class: '' },
      { label: '额', value: '--', class: '' },
      { label: '换手', value: '--', class: '' },
      { label: '市盈', value: '--', class: '' },
      { label: '总市值', value: '--', class: '' },
      { label: '流通值', value: '--', class: '' }
    ])

    const chartTabs = [
      { label: '分时', value: 'timeline' },
      { label: '日K', value: 'kline-daily' },
      { label: '周K', value: 'kline-week' },
      { label: '月K', value: 'kline-month' }
    ]

    const sellOrders = ref([])
    const buyOrders = ref([])
    const tickData = ref([])

    const generateOrderBook = (currentPrice) => {
      const price = Number(currentPrice) || 255.85
      sellOrders.value = Array.from({ length: 5 }, (_, i) => ({
        level: 5 - i,
        price: (price + (5 - i) * 0.01).toFixed(2),
        volume: Math.floor(Math.random() * 200 + 50).toString()
      }))
      buyOrders.value = Array.from({ length: 5 }, (_, i) => ({
        level: i + 1,
        price: (price - (i + 1) * 0.01).toFixed(2),
        volume: Math.floor(Math.random() * 200 + 50).toString()
      }))
    }

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

    // API数据转换
    const buildTimelineFromApi = (list = []) => {
      if (!Array.isArray(list) || !list.length) return null
      return {
        priceData: list.map(item => [item.tradeDate, Number(item.closePrice || 0).toFixed(2)]),
        volumeData: list.map(item => [item.tradeDate, Number(item.volume || 0)])
      }
    }

    const buildKLineFromApi = (list = []) => {
      if (!Array.isArray(list) || !list.length) return null
      return list.map(item => ({
        time: item.tradeDate,
        k: [
          Number(item.openPrice || 0).toFixed(2),
          Number(item.closePrice || 0).toFixed(2),
          Number(item.lowPrice || 0).toFixed(2),
          Number(item.highPrice || 0).toFixed(2)
        ],
        volume: Number(item.volume || 0)
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

      const built = buildTimelineFromApi(apiTimeline.value)
      if (!built) return
      const priceData = built.priceData
      const volumeData = built.volumeData
      
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
      const rawData = buildKLineFromApi(apiKline.value)
      if (!rawData) return
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

    // 实时数据更新
    let realtimeInterval = null
    const startRealtimeSimulation = () => {
      if (realtimeInterval) {
        clearInterval(realtimeInterval)
      }

      realtimeInterval = setInterval(async () => {
        try {
          const res = await getStockRealtime(stockCode.value)
          if (res?.code === 200 && res.data) {
            const { price, change, changeAmount, volume, timestamp } = res.data
            stockData.value.price = Number(price || stockData.value.price || 0).toFixed(2)
            stockData.value.change = (changeAmount || change || 0).toString()
            stockData.value.changePercent = (change || 0).toString()

            tickData.value.unshift({
              time: timestamp ? timestamp.slice(11, 19) : new Date().toTimeString().slice(0, 8),
              price: stockData.value.price,
              volume: volume || Math.floor(Math.random() * 100) + 1,
              change: change || 0
            })
            if (tickData.value.length > 50) {
              tickData.value.pop()
            }
          }
        } catch (err) {
          console.warn('实时更新失败', err)
        }
      }, 5000)
    }

    const loadStockData = async () => {
      loading.value = true
      try {
        const [realtimeRes, timelineRes, klineRes, eastmoneyRes] = await Promise.all([
          getStockRealtime(stockCode.value),
          getStockTimeline(stockCode.value, 30),
          getStockKLine(stockCode.value, { days: 200 }),
          getEastMoneyQuote(stockCode.value)
        ])

        if (timelineRes?.code === 200 && Array.isArray(timelineRes.data) && timelineRes.data.length > 0) {
          apiTimeline.value = timelineRes.data
          const latest = timelineRes.data[timelineRes.data.length - 1]
          const realtime = realtimeRes?.code === 200 ? realtimeRes.data : null

          stockData.value = {
            name: realtime?.stockName || latest.stockName || stockCode.value,
            code: stockCode.value,
            exchange: stockCode.value.startsWith('6') ? '上交所' : '深交所',
            price: realtime ? Number(realtime.currentPrice || 0).toFixed(2) : Number(latest.closePrice || 0).toFixed(2),
            change: realtime ? Number(realtime.change || 0).toFixed(2) : Number(latest.changeAmount || 0).toFixed(2),
            changePercent: realtime ? Number(realtime.changePercent || 0).toFixed(2) : Number(latest.changePercent || 0).toFixed(2)
          }

          const eastmoney = eastmoneyRes?.code === 200 ? eastmoneyRes.data : null

          keyMetrics.value = [
            { label: '高', value: realtime ? Number(realtime.highPrice || 0).toFixed(2) : Number(latest.highPrice || 0).toFixed(2), class: '' },
            { label: '低', value: realtime ? Number(realtime.lowPrice || 0).toFixed(2) : Number(latest.lowPrice || 0).toFixed(2), class: '' },
            { label: '开', value: realtime ? Number(realtime.openPrice || 0).toFixed(2) : Number(latest.openPrice || 0).toFixed(2), class: '' },
            { label: '昨收', value: realtime ? Number(realtime.preClosePrice || 0).toFixed(2) : '--', class: '' },
            { label: '量', value: realtime ? (realtime.volume / 100).toFixed(2) + '万手' : (latest.volume ? (latest.volume / 10000).toFixed(2) + '万手' : '--'), class: '' },
            { label: '额', value: realtime ? (realtime.amount / 100000000).toFixed(2) + '亿' : (latest.amount ? (latest.amount / 100000000).toFixed(2) + '亿' : '--'), class: '' },
            { label: '换手', value: eastmoney?.turnoverRate ? eastmoney.turnoverRate.toFixed(2) + '%' : '--', class: '' },
            { label: '市盈', value: eastmoney?.pe ? eastmoney.pe.toFixed(2) : '--', class: '' },
            { label: '总市值', value: eastmoney?.totalMarketCap ? (eastmoney.totalMarketCap / 100000000).toFixed(2) + '亿' : '--', class: '' },
            { label: '流通值', value: eastmoney?.circulationMarketCap ? (eastmoney.circulationMarketCap / 100000000).toFixed(2) + '亿' : '--', class: '' }
          ]
          generateOrderBook(realtime?.currentPrice || latest.closePrice)
        }

        if (klineRes?.code === 200 && Array.isArray(klineRes.data)) {
          apiKline.value = klineRes.data
        }

        nextTick(() => {
          initTimelineChart()
          initKLineChart()
        })
      } catch (err) {
        console.error('加载股票数据失败', err)
        nextTick(() => {
          initTimelineChart()
          initKLineChart()
        })
      } finally {
        loading.value = false
      }
    }

    onMounted(() => {
      nextTick(() => {
        loadStockData()
        startRealtimeSimulation()
      })
    })

    onBeforeUnmount(() => {
      if (realtimeInterval) {
        clearInterval(realtimeInterval)
      }
    })

    watch(() => route.query.stock, (newStock) => {
      if (newStock && newStock !== stockCode.value) {
        stockCode.value = newStock
        stockData.value.code = newStock
        tickData.value = []
        loadStockData()
        startRealtimeSimulation()
      }
    })

    return {
      stockCode,
      loading,
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