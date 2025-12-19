<template>
  <div class="page-content active">
    <!-- 头部信息 -->
    <el-header class="header card-glass mb-4" height="auto">
      <div class="header-left">
        <div class="flex-align-center">
          <el-tag effect="dark" class="mr-2">{{ stockData.exchange || '交易所' }}</el-tag>
          <h2 class="stock-name">{{ stockData.name || '--' }}</h2>
          <span class="stock-code-tag">{{ stockData.code || stockCode }}</span>
        </div>
        <p class="text-tertiary mt-1">实时行情监控中...</p>
      </div>
      <div class="header-right">
        <div class="main-price-display" :class="stockData.change >= 0 ? 'positive' : 'negative'">
          <div class="price-val">{{ stockData.price }}</div>
          <div class="price-change-group">
            <span>{{ stockData.change >= 0 ? '+' : '' }}{{ stockData.change }}</span>
            <span>({{ stockData.changePercent }}%)</span>
          </div>
        </div>
      </div>
    </el-header>
  
    <el-row :gutter="20">
      <!-- 左侧：图表与基本面 -->
      <el-col :span="18">
        <el-card shadow="never" class="chart-main-card">
          <el-tabs v-model="activeTab" class="custom-tabs" @tab-change="switchTab">
            <el-tab-pane v-for="tab in chartTabs" :key="tab.value" :label="tab.label" :name="tab.value" />
          </el-tabs>
          
          <div class="chart-viewport">
            <div id="stockTimelineChart" class="chart-el" v-show="activeTab === 'timeline'"></div>
            <div id="stockKLineChart" class="chart-el" v-show="activeTab.startsWith('kline')"></div>
          </div>
        </el-card>

        <!-- 基本面指标 -->
        <el-card shadow="never" class="mt-4 metrics-card">
          <template #header>
            <div class="flex-between">
              <span class="font-bold"><el-icon class="mr-1"><InfoFilled /></el-icon>关键指标</span>
              <el-tag size="small" type="success">实时数据</el-tag>
            </div>
          </template>
          <el-descriptions :column="5" border size="small">
            <el-descriptions-item v-for="metric in keyMetrics" :key="metric.label" :label="metric.label">
              <span :class="metric.class" class="font-mono font-bold">{{ metric.value }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <!-- 右侧：五档与成交 -->
      <el-col :span="6">
        <el-space direction="vertical" fill style="width: 100%" :size="20">
          <!-- 五档报价 -->
          <el-card shadow="never" class="orderbook-card">
            <template #header><span>五档报价</span></template>
            <div class="orderbook-body">
              <div class="sell-side">
                <div v-for="order in sellOrders" :key="order.level" class="order-row">
                  <span class="label">卖{{ order.level }}</span>
                  <span class="price negative">{{ order.price }}</span>
                  <span class="volume">{{ order.volume }}</span>
                </div>
              </div>
              <div class="price-divider">
                <div class="current" :class="stockData.change >= 0 ? 'bg-pos' : 'bg-neg'">
                  {{ stockData.price }}
                </div>
              </div>
              <div class="buy-side">
                <div v-for="order in buyOrders" :key="order.level" class="order-row">
                  <span class="label">买{{ order.level }}</span>
                  <span class="price positive">{{ order.price }}</span>
                  <span class="volume">{{ order.volume }}</span>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 逐笔成交 -->
          <el-card shadow="never" class="ticks-card">
            <template #header><span>逐笔成交</span></template>
            <div class="ticks-header">
              <span>时间</span><span>价格</span><span>量</span>
            </div>
            <el-scrollbar height="300px">
              <div v-for="tick in tickData" :key="tick.time" class="tick-row">
                <span class="time">{{ tick.time }}</span>
                <span class="price" :class="tick.change >= 0 ? 'positive' : 'negative'">{{ tick.price }}</span>
                <span class="vol">{{ tick.volume }}</span>
              </div>
            </el-scrollbar>
          </el-card>
        </el-space>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
import { InfoFilled, TrendCharts } from '@element-plus/icons-vue'
import { getStockRealtime, getStockTimeline, getStockKLine, getEastMoneyQuote } from '@/api/stock'

const route = useRoute()
const stockCode = ref(route.query.stock || '002594')
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
  change: 0,
  changePercent: '0.00'
})

const keyMetrics = ref([])
const chartTabs = [
  { label: '分时图', value: 'timeline' },
  { label: '日K线', value: 'kline-daily' },
  { label: '周K线', value: 'kline-week' },
  { label: '月K线', value: 'kline-month' }
]

const sellOrders = ref([])
const buyOrders = ref([])
const tickData = ref([])

// 初始化图表颜色
const colors = { up: '#39D353', down: '#F85149', primary: '#00A6FF' }

const loadStockData = async () => {
  try {
    const [realtimeRes, timelineRes, klineRes, eastmoneyRes] = await Promise.all([
      getStockRealtime(stockCode.value),
      getStockTimeline(stockCode.value, 30),
      getStockKLine(stockCode.value, { days: 200 }),
      getEastMoneyQuote(stockCode.value)
    ])

    if (realtimeRes?.code === 200 && realtimeRes.data) {
      const d = realtimeRes.data
      stockData.value = {
        name: d.stockName || '--',
        code: d.stockCode,
        exchange: d.stockCode.startsWith('6') ? 'SH' : 'SZ',
        price: Number(d.currentPrice || 0).toFixed(2),
        change: Number(d.change || 0).toFixed(2),
        changePercent: Number(d.changePercent || 0).toFixed(2)
      }
      
      const em = eastmoneyRes?.code === 200 ? eastmoneyRes.data : {}
      keyMetrics.value = [
        { label: '最高', value: d.highPrice || '--', class: 'positive' },
        { label: '最低', value: d.lowPrice || '--', class: 'negative' },
        { label: '今开', value: d.openPrice || '--', class: '' },
        { label: '昨收', value: d.preClosePrice || '--', class: '' },
        { label: '成交量', value: (d.volume / 10000).toFixed(2) + '万', class: '' },
        { label: '成交额', value: (d.amount / 100000000).toFixed(2) + '亿', class: '' },
        { label: '换手率', value: (em.turnoverRate || 0).toFixed(2) + '%', class: '' },
        { label: '市盈率', value: (em.pe || 0).toFixed(2), class: '' },
        { label: '总市值', value: (em.totalMarketCap / 100000000).toFixed(2) + '亿', class: '' },
        { label: '流通值', value: (em.circulationMarketCap / 100000000).toFixed(2) + '亿', class: '' }
      ]

      // 生成盘口模拟
      sellOrders.value = Array.from({length: 5}, (_, i) => ({
        level: 5 - i,
        price: (Number(d.currentPrice) + (5-i)*0.01).toFixed(2),
        volume: Math.floor(Math.random() * 500)
      }))
      buyOrders.value = Array.from({length: 5}, (_, i) => ({
        level: i + 1,
        price: (Number(d.currentPrice) - (i+1)*0.01).toFixed(2),
        volume: Math.floor(Math.random() * 500)
      }))
    }

    if (timelineRes?.code === 200) apiTimeline.value = timelineRes.data
    if (klineRes?.code === 200) apiKline.value = klineRes.data

    nextTick(() => {
      initTimelineChart()
      initKLineChart()
    })
  } catch (err) {
    console.error('加载行情失败', err)
  }
}

const initTimelineChart = () => {
  const chartDom = document.getElementById('stockTimelineChart')
  if (!chartDom) return
  if (timelineChart.value) timelineChart.value.dispose()
  timelineChart.value = echarts.init(chartDom)
  
  const data = apiTimeline.value.map(i => [i.tradeDate, i.closePrice])
  timelineChart.value.setOption({
    grid: { left: '4%', right: '4%', top: '5%', bottom: '5%', containLabel: true },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data.map(i => i[0]), axisLine: { lineStyle: { color: '#30363D' } } },
    yAxis: { type: 'value', scale: true, splitLine: { lineStyle: { color: '#30363D' } } },
    series: [{
      type: 'line',
      data: data.map(i => i[1]),
      smooth: true,
      symbol: 'none',
      lineStyle: { width: 2, color: colors.primary },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(0, 166, 255, 0.2)' },
          { offset: 1, color: 'transparent' }
        ])
      }
    }]
  })
}

const initKLineChart = () => {
  const chartDom = document.getElementById('stockKLineChart')
  if (!chartDom) return
  if (klineChart.value) klineChart.value.dispose()
  klineChart.value = echarts.init(chartDom)
  
  const data = apiKline.value.map(i => [i.openPrice, i.closePrice, i.lowPrice, i.highPrice])
  klineChart.value.setOption({
    grid: { left: '4%', right: '4%', top: '5%', bottom: '5%', containLabel: true },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: apiKline.value.map(i => i.tradeDate), axisLine: { lineStyle: { color: '#30363D' } } },
    yAxis: { type: 'value', scale: true, splitLine: { lineStyle: { color: '#30363D' } } },
    series: [{
      type: 'candlestick',
      data: data,
      itemStyle: { color: colors.up, color0: colors.down, borderColor: colors.up, borderColor0: colors.down }
    }]
  })
}

const switchTab = () => {
  nextTick(() => {
    if (activeTab.value === 'timeline') initTimelineChart()
    else initKLineChart()
  })
}

onMounted(() => loadStockData())

watch(() => route.query.stock, (val) => {
  if (val) {
    stockCode.value = val
    loadStockData()
  }
})
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; padding: 1.5rem 2rem; }
.stock-name { font-size: 1.8rem; font-weight: 700; margin-right: 1rem; }
.stock-code-tag { font-size: 1.2rem; color: var(--text-tertiary); font-family: monospace; }
.main-price-display { text-align: right; }
.price-val { font-size: 2.4rem; font-weight: 800; font-family: 'Inter', sans-serif; line-height: 1; }
.price-change-group { font-size: 1rem; font-weight: 600; margin-top: 4px; }

.chart-main-card { min-height: 500px; }
.chart-viewport { height: 420px; width: 100%; position: relative; }
.chart-el { height: 100%; width: 100%; }

.custom-tabs :deep(.el-tabs__nav-wrap::after) { display: none; }
.custom-tabs :deep(.el-tabs__item) { font-weight: 600; font-size: 1rem; }

.orderbook-body { display: flex; flex-direction: column; gap: 4px; }
.order-row { display: flex; justify-content: space-between; font-size: 0.9rem; padding: 4px 8px; border-radius: 4px; transition: background 0.2s; }
.order-row:hover { background: rgba(255, 255, 255, 0.05); }
.order-row .label { color: var(--text-tertiary); width: 40px; }
.order-row .price { font-weight: 700; flex: 1; text-align: right; margin-right: 15px; }
.order-row .volume { color: var(--text-secondary); width: 60px; text-align: right; }

.price-divider { margin: 10px 0; padding: 8px 0; border-top: 1px dashed var(--border-color); border-bottom: 1px dashed var(--border-color); text-align: center; }
.price-divider .current { display: inline-block; padding: 4px 20px; border-radius: 4px; font-weight: 800; font-size: 1.2rem; }
.bg-pos { background: rgba(57, 211, 83, 0.15); color: var(--color-positive); }
.bg-neg { background: rgba(248, 81, 73, 0.15); color: var(--color-negative); }

.ticks-header { display: flex; justify-content: space-between; padding: 8px; font-size: 0.8rem; color: var(--text-tertiary); border-bottom: 1px solid var(--border-color); }
.tick-row { display: flex; justify-content: space-between; padding: 6px 8px; font-size: 0.85rem; border-bottom: 1px solid rgba(255, 255, 255, 0.02); }
.tick-row .time { color: var(--text-tertiary); }
.tick-row .price { font-weight: 600; }
</style>

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
          { type: 'candlestick', name: '日K', data: kData, itemStyle: { color: colors.up, color0: colors.down, borderColor: colors.up, borderColor0: colors.down } },
          { type: 'bar', name: 'Volume', data: volumes, xAxisIndex: 1, yAxisIndex: 1, itemStyle: { color: ({ value }) => (value[2] === 1 ? colors.up : colors.down) } },
          { name: 'MACD', type: 'bar', data: macdData.map(d => d.macd), xAxisIndex: 2, yAxisIndex: 2, itemStyle: { color: ({ value }) => (value > 0 ? colors.up : colors.down) } },
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