<template>
  <div class="page-content active">
    <!-- 顶部 Header -->
    <el-header class="header card-glass" height="auto">
      <div class="header-left">
        <h2>仪表盘</h2>
        <p>欢迎回来, 尊敬的投资者</p>
      </div>
      <div class="header-right">
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索股票代码/名称..."
            @input="handleSearch"
            :prefix-icon="Search"
            clearable
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
          
          <el-card v-if="showSearchDropdown" class="search-dropdown-card">
            <div v-if="searchLoading" class="loading-state">
              <el-icon class="is-loading"><Loading /></el-icon> 搜索中...
            </div>
            <div v-else-if="searchError" class="error-state">{{ searchError }}</div>
            <div v-else-if="searchResults.length === 0" class="empty-state">暂无结果</div>
            <div v-else class="results-list">
              <div
                v-for="item in searchResults"
                :key="item.stockCode"
                class="result-item"
                @click="selectSearchResult(item)"
              >
                <span class="name">{{ item.stockName }}</span>
                <el-tag size="small" type="info">{{ item.stockCode }}</el-tag>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </el-header>

    <!-- 内容网格 -->
    <el-row :gutter="20" class="mt-4">
      <!-- 主要市场指数 -->
      <el-col :span="24">
        <el-row :gutter="15">
          <el-col :span="8" v-for="(index, idx) in topIndices" :key="index.code">
            <el-card shadow="hover" class="index-card-new" @click="selectTopIndex(index.code)">
              <div class="index-main">
                <div class="index-label">{{ index.name }}</div>
                <div class="index-value" :class="index.change >= 0 ? 'positive' : 'negative'">
                  {{ index.price }}
                </div>
                <div class="index-change" :class="index.change >= 0 ? 'positive' : 'negative'">
                  <el-icon><CaretTop v-if="index.change >= 0" /><CaretBottom v-else /></el-icon>
                  {{ index.change >= 0 ? '+' : '' }}{{ index.change }} ({{ index.changePercent }}%)
                </div>
              </div>
              <div :id="'miniChart' + idx" class="mini-chart-new"></div>
            </el-card>
          </el-col>
        </el-row>
      </el-col>

      <!-- 主图表卡片 -->
      <el-col :span="16" class="mt-4">
        <el-card shadow="never" class="main-chart-card">
          <template #header>
            <div class="card-header-flex">
              <div class="header-title">
                <el-icon class="mr-2"><DataLine /></el-icon>
                <span>{{ currentIndex.name }} 行情走势</span>
              </div>
              <div class="header-actions">
                <el-select v-model="selectedIndex" @change="changeIndex" style="width: 130px; margin-right: 12px;">
                  <el-option v-for="index in indices" :key="index.code" :label="index.name" :value="index.code" />
                </el-select>
                <el-radio-group v-model="selectedPeriod" size="small" @change="changePeriod">
                  <el-radio-button v-for="p in periods" :key="p.value" :label="p.value">
                    {{ p.label }}
                  </el-radio-button>
                </el-radio-group>
              </div>
            </div>
          </template>
          
          <div class="price-banner">
            <el-statistic :value="parseFloat(currentPriceData.price.replace(/,/g, ''))" :precision="2" :value-style="{ color: currentPriceData.change >= 0 ? 'var(--color-positive)' : 'var(--color-negative)' }">
              <template #title>
                <div style="display: flex; align-items: center; gap: 8px;">
                  <span>实时价格</span>
                  <span :class="currentPriceData.change >= 0 ? 'positive' : 'negative'">
                    {{ currentPriceData.change >= 0 ? '+' : '' }}{{ currentPriceData.change }} ({{ currentPriceData.changePercent }}%)
                  </span>
                </div>
              </template>
            </el-statistic>
          </div>
          
          <div id="mainKLineChart" style="width: 100%; height: 450px;"></div>
        </el-card>
      </el-col>

      <el-col :span="8" class="mt-4">
        <el-space direction="vertical" fill :size="20" style="width: 100%">
          <!-- 我的自选 -->
          <el-card shadow="never" class="watchlist-card-new">
            <template #header>
              <div class="card-header-flex">
                <span>我的自选</span>
                <el-button link type="primary" @click="router.push('/watchlist')">管理全部 <el-icon><ArrowRight /></el-icon></el-button>
              </div>
            </template>
            <div class="watchlist-list">
              <div v-for="stock in watchlistStocks" :key="stock.code" class="watchlist-row" @click="router.push(`/market?stock=${stock.code}`)">
                <div class="w-info">
                  <div class="name">{{ stock.name }}</div>
                  <div class="code">{{ stock.code }}</div>
                </div>
                <div class="w-price" :class="stock.change >= 0 ? 'positive' : 'negative'">{{ stock.price }}</div>
                <div class="w-change">
                  <el-tag :type="stock.change >= 0 ? 'success' : 'danger'" size="small">
                    {{ stock.change >= 0 ? '+' : '' }}{{ stock.changePercent }}%
                  </el-tag>
                </div>
              </div>
            </div>
          </el-card>

          <!-- AI 投资建议 -->
          <el-card shadow="never" class="ai-advice-card">
            <div class="ai-header" @click="aiExpanded = !aiExpanded" style="cursor: pointer;">
              <el-avatar :size="32" class="ai-avatar"><el-icon><Cpu /></el-icon></el-avatar>
              <span class="ai-title">智能投顾引擎</span>
              <el-icon class="expand-icon" :class="{ expanded: aiExpanded }"><ArrowRight /></el-icon>
            </div>
            <el-collapse-transition>
              <div v-show="aiExpanded">
                <div class="ai-body" v-if="aiAdvice.content">
                  <p>{{ aiAdvice.content }}</p>
                </div>
                <div class="ai-body" v-else-if="aiAdvice.loading">
                  <p style="color: var(--text-tertiary);">正在加载投资建议...</p>
                </div>
                <div class="ai-body" v-else>
                  <p style="color: var(--text-tertiary);">暂无投资建议，请先设置投资偏好并添加自选股</p>
                </div>
                <div class="ai-footer" v-if="aiAdvice.time">
                  Generated by AI • {{ aiAdvice.time }}
                </div>
              </div>
            </el-collapse-transition>
          </el-card>
        </el-space>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { Search, CaretTop, CaretBottom, DataLine, ArrowRight, Cpu, Loading } from '@element-plus/icons-vue'
import { searchStock, getWatchlist, getMarketIndices, getMarketOverview, getIndexKLine, getIndexTimeline, getLatestAdvice } from '@/api/stock'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const selectedPeriod = ref('timeline')
const selectedIndex = ref('000001')
const chartInstance = ref(null)
const miniChartInstances = ref([])

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

const currentIndex = computed(() => indices.find(i => i.code === selectedIndex.value) || indices[0])

const currentPriceData = ref({
  price: '0.00',
  change: 0.00,
  changePercent: '0.00'
})

const searchKeyword = ref('')
const searchResults = ref([])
const searchLoading = ref(false)
const searchError = ref('')
const showSearchDropdown = computed(() => searchKeyword.value && (searchLoading.value || searchError.value || searchResults.value.length > 0))

const topIndices = ref([
  { code: '000001', name: '上证指数', price: '--', change: 0, changePercent: '0.00' },
  { code: '399001', name: '深证成指', price: '--', change: 0, changePercent: '0.00' },
  { code: '399006', name: '创业板指', price: '--', change: 0, changePercent: '0.00' }
])

const marketOverview = ref({
  upCount: '--',
  downCount: '--',
  limitUpCount: '--',
  limitDownCount: '--',
  totalVolume: '--',
  turnoverRate: '--',
  updateTime: new Date().toLocaleTimeString()
})

const watchlistStocks = ref([])
const aiAdvice = ref({ content: '', time: '', loading: false })
const aiExpanded = ref(false)

// 图表颜色方案
const chartColors = {
  up: '#39D353',
  down: '#F85149',
  ma5: '#FF6B6B',
  ma10: '#4ECDC4',
  ma20: '#45B7D1',
  ma30: '#96CEB4'
}

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

const initMiniCharts = async () => {
  // 清理旧实例
  miniChartInstances.value.forEach(instance => instance.dispose())
  miniChartInstances.value = []

  for (let idx = 0; idx < topIndices.value.length; idx++) {
    const index = topIndices.value[idx]
    const chartDom = document.getElementById('miniChart' + idx)
    if (!chartDom) continue
    
    const miniChart = echarts.init(chartDom)
    miniChartInstances.value.push(miniChart)
    
    let historyData = []
    try {
      // 获取最近20天的日K数据用于展示趋势
      const res = await getIndexKLine(index.code, 20)
      if (res?.code === 200 && Array.isArray(res.data)) {
        historyData = res.data.map(item => Number(item.closePrice))
      }
    } catch (err) {
      console.warn(`获取指数${index.code}历史数据失败`, err)
    }

    if (historyData.length === 0) {
      historyData = Array.from({length: 20}, () => Math.random() * 10 + 100)
    }

    const isPositive = index.change >= 0

    miniChart.setOption({
      grid: { left: 0, right: 0, top: 10, bottom: 0 },
      xAxis: { type: 'category', show: false },
      yAxis: { type: 'value', show: false, scale: true },
      series: [{
        type: 'line',
        data: historyData,
        smooth: true,
        symbol: 'none',
        lineStyle: { width: 2, color: isPositive ? '#00B894' : '#D63031' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: isPositive ? 'rgba(0, 184, 148, 0.3)' : 'rgba(214, 48, 49, 0.3)' },
            { offset: 1, color: 'transparent' }
          ])
        }
      }]
    })
  }
}

const loadMarketIndices = async () => {
  try {
    const res = await getMarketIndices()
    if (res?.code === 200 && Array.isArray(res.data)) {
      topIndices.value = res.data.map(item => ({
        code: item.stockCode,
        name: item.stockName,
        price: Number(item.currentPrice || 0).toFixed(2),
        change: Number(item.change || 0).toFixed(2),
        changePercent: Number(item.changePercent || 0).toFixed(2)
      }))

      const current = res.data.find(item => item.stockCode === selectedIndex.value)
      if (current) {
        currentPriceData.value = {
          price: Number(current.currentPrice || 0).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ','),
          change: Number(current.change || 0).toFixed(2),
          changePercent: Number(current.changePercent || 0).toFixed(2)
        }
      }
      
      // 指数数据加载后，初始化迷你图
      nextTick(() => initMiniCharts())
    }
  } catch (err) {
    console.warn('获取大盘指数失败', err)
  }
}

const loadMarketOverview = async () => {
  try {
    const res = await getMarketOverview()
    if (res?.code === 200 && res.data) {
      marketOverview.value = {
        upCount: res.data.upCount ?? '--',
        downCount: res.data.downCount ?? '--',
        limitUpCount: res.data.limitUpCount ?? '--',
        limitDownCount: res.data.limitDownCount ?? '--',
        totalVolume: res.data.totalVolume ?? '--',
        turnoverRate: res.data.turnoverRate ?? '--',
        updateTime: res.data.updateTime ? new Date(res.data.updateTime).toLocaleTimeString() : new Date().toLocaleTimeString()
      }
    }
  } catch (err) {
    console.warn('获取市场概况失败', err)
  }
}

const initTimelineChart = async () => {
  const chartDom = document.getElementById('mainKLineChart')
  if (!chartDom) return
  if (chartInstance.value) chartInstance.value.dispose()
  chartInstance.value = echarts.init(chartDom)

  try {
    const res = await getIndexTimeline(selectedIndex.value)
    if (res?.code === 200 && res.data) {
      const { prePrice, points } = res.data
      const data = (points || []).map(item => ({
        time: item.time,
        price: Number(item.price || 0).toFixed(2),
        volume: item.volume || 0
      }))

      if (data.length === 0) return

      const yesterdayClose = prePrice || Number(data[0].price)
      let totalVolume = 0
      let totalAmount = 0
      const processedData = data.map(item => {
        totalVolume += item.volume
        totalAmount += Number(item.price) * item.volume
        const avgPrice = totalVolume > 0 ? totalAmount / totalVolume : item.price
        return { ...item, avgPrice: Number(avgPrice).toFixed(2) }
      })

      const times = processedData.map(item => item.time)
      const prices = processedData.map(item => parseFloat(item.price))
      const volumes = processedData.map(item => item.volume)
      const avgPrices = processedData.map(item => item.avgPrice)

      chartInstance.value.setOption({
        backgroundColor: 'transparent',
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'cross' },
          formatter: (params) => {
            let res = params[0].axisValue + '<br/>'
            params.forEach(item => {
              if (item.seriesName === '价格') {
                const color = item.data >= yesterdayClose ? '#00B894' : '#D63031'
                const change = (item.data - yesterdayClose).toFixed(2)
                const percent = ((change / yesterdayClose) * 100).toFixed(2)
                res += `<span style="color:${color}">${item.marker}${item.seriesName}: ${item.data} (${change >= 0 ? '+' : ''}${percent}%)</span><br/>`
              } else {
                res += `<span>${item.marker}${item.seriesName}: ${item.data}</span><br/>`
              }
            })
            return res
          }
        },
        grid: [
          { left: '5%', right: '5%', top: '10%', height: '60%' },
          { left: '5%', right: '5%', top: '75%', height: '15%' }
        ],
        xAxis: [
          { type: 'category', data: times, gridIndex: 0, axisLabel: { interval: 29 } },
          { type: 'category', data: times, gridIndex: 1, axisLabel: { show: false } }
        ],
        yAxis: [
          { 
            scale: true, 
            gridIndex: 0, 
            splitLine: { lineStyle: { color: '#30363D' } },
            axisLabel: {
              formatter: (val) => val.toFixed(2)
            }
          },
          { scale: true, gridIndex: 1, splitLine: { show: false } }
        ],
        visualMap: {
          show: false,
          pieces: [{ gt: 0, lte: yesterdayClose, color: '#D63031' }, { gt: yesterdayClose, color: '#00B894' }],
          outOfRange: { color: '#8B949E' },
          seriesIndex: 0
        },
        series: [
          { 
            name: '价格', 
            type: 'line', 
            data: prices, 
            smooth: true, 
            symbol: 'none', 
            lineStyle: { width: 1.5 }, 
            areaStyle: { 
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(0, 166, 255, 0.2)' }, 
                { offset: 1, color: 'transparent' }
              ]) 
            } 
          },
          { name: '均价', type: 'line', data: avgPrices, smooth: true, symbol: 'none', lineStyle: { width: 1, color: '#FFA500', type: 'dashed' } },
          { name: '成交量', type: 'bar', data: volumes, xAxisIndex: 1, yAxisIndex: 1, itemStyle: { color: (params) => prices[params.dataIndex] >= yesterdayClose ? '#00B894' : '#D63031' } }
        ]
      })
    }
  } catch (err) {
    console.error('获取分时数据失败', err)
  }
}

const initKLineChart = async () => {
  const chartDom = document.getElementById('mainKLineChart')
  if (!chartDom) return
  if (chartInstance.value) chartInstance.value.dispose()
  chartInstance.value = echarts.init(chartDom)

  const dataCount = selectedPeriod.value === 'yearly' ? 20 : selectedPeriod.value === 'weekly' ? 104 : selectedPeriod.value === 'monthly' ? 48 : 250

  try {
    const res = await getIndexKLine(selectedIndex.value, dataCount)
    if (res?.code === 200 && Array.isArray(res.data) && res.data.length > 0) {
      const rawData = res.data.map(item => ({
        time: item.tradeDate,
        k: [item.openPrice, item.closePrice, item.lowPrice, item.highPrice],
        volume: item.volume
      }))

      const dates = rawData.map(item => item.time)
      const kData = rawData.map(item => item.k)
      const volumes = rawData.map(item => ({ value: item.volume, itemStyle: { color: item.k[1] >= item.k[0] ? '#00B894' : '#D63031' } }))
      
      const ma5 = calculateMA(kData, 5)
      const ma10 = calculateMA(kData, 10)
      const ma20 = calculateMA(kData, 20)
      const ma30 = calculateMA(kData, 30)

      chartInstance.value.setOption({
        backgroundColor: 'transparent',
        tooltip: { trigger: 'axis', axisPointer: { type: 'cross' } },
        legend: { data: ['K线', 'MA5', 'MA10', 'MA20', 'MA30'], top: 0, textStyle: { color: '#8B949E' } },
        grid: [
          { left: '5%', right: '5%', top: '15%', height: '60%' },
          { left: '5%', right: '5%', top: '80%', height: '15%' }
        ],
        xAxis: [
          { type: 'category', data: dates, gridIndex: 0 },
          { type: 'category', data: dates, gridIndex: 1, axisLabel: { show: false } }
        ],
        yAxis: [
          { scale: true, gridIndex: 0, splitLine: { lineStyle: { color: '#30363D' } } },
          { scale: true, gridIndex: 1, splitLine: { show: false } }
        ],
        dataZoom: [{ type: 'inside', xAxisIndex: [0, 1], start: 80, end: 100 }, { show: true, xAxisIndex: [0, 1], type: 'slider', bottom: 10, start: 80, end: 100 }],
        series: [
          { name: 'K线', type: 'candlestick', data: kData, itemStyle: { color: '#00B894', color0: '#D63031', borderColor: '#00B894', borderColor0: '#D63031' } },
          { name: 'MA5', type: 'line', data: ma5, smooth: true, symbol: 'none', lineStyle: { width: 1, color: chartColors.ma5 } },
          { name: 'MA10', type: 'line', data: ma10, smooth: true, symbol: 'none', lineStyle: { width: 1, color: chartColors.ma10 } },
          { name: 'MA20', type: 'line', data: ma20, smooth: true, symbol: 'none', lineStyle: { width: 1, color: chartColors.ma20 } },
          { name: 'MA30', type: 'line', data: ma30, smooth: true, symbol: 'none', lineStyle: { width: 1, color: chartColors.ma30 } },
          { name: '成交量', type: 'bar', data: volumes, xAxisIndex: 1, yAxisIndex: 1 }
        ]
      })
    }
  } catch (err) {
    console.error('获取K线数据失败', err)
  }
}

const changePeriod = (val) => {
  if (val === 'timeline') initTimelineChart()
  else initKLineChart()
}

const changeIndex = () => {
  if (selectedPeriod.value === 'timeline') initTimelineChart()
  else initKLineChart()
  
  // 更新价格面板
  const current = topIndices.value.find(item => item.code === selectedIndex.value)
  if (current) {
    currentPriceData.value = {
      price: current.price.replace(/\B(?=(\d{3})+(?!\d))/g, ','),
      change: current.change,
      changePercent: current.changePercent
    }
  }
}

const selectTopIndex = (code) => {
  selectedIndex.value = code
  changeIndex()
}

const handleSearch = async () => {
  if (!searchKeyword.value) return searchResults.value = []
  searchLoading.value = true
  try {
    const res = await searchStock(searchKeyword.value)
    searchResults.value = res.data || []
  } finally { searchLoading.value = false }
}

const selectSearchResult = (item) => {
  router.push(`/market?stock=${item.stockCode}`)
}

const loadWatchlist = async () => {
  const userId = authStore.userId || localStorage.getItem('userId')
  if (!userId) return
  const res = await getWatchlist(userId)
  watchlistStocks.value = (res.data || []).slice(0, 5).map(i => ({
    name: i.stockName,
    code: i.stockCode,
    price: i.currentPrice || '--',
    change: i.changePercent || 0,
    changePercent: i.changePercent || 0
  }))
}

const loadAiAdvice = async () => {
  const userId = authStore.userId || localStorage.getItem('userId')
  if (!userId) return
  aiAdvice.value.loading = true
  try {
    const res = await getLatestAdvice(userId)
    if (res?.code === 200 && res.data) {
      const advice = res.data
      try {
        const content = JSON.parse(advice.content)
        const recommendations = content.recommendations || []
        if (recommendations.length > 0) {
          const firstRec = recommendations[0]
          aiAdvice.value.content = `根据您的风险偏好，当前建议关注 ${firstRec.name || '相关'} (${firstRec.code || ''})。${firstRec.thesis || ''}`
        } else {
          aiAdvice.value.content = advice.reasoning || '暂无具体建议'
        }
      } catch {
        aiAdvice.value.content = advice.reasoning || '暂无具体建议'
      }
      aiAdvice.value.time = advice.createdAt ? new Date(advice.createdAt).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) : ''
    }
  } catch (err) {
    console.warn('获取AI投资建议失败', err)
  } finally {
    aiAdvice.value.loading = false
  }
}

onMounted(async () => {
  await loadMarketIndices()
  loadMarketOverview()
  loadWatchlist()
  loadAiAdvice()
  initTimelineChart()
})

onUnmounted(() => {
  if (chartInstance.value) chartInstance.value.dispose()
  miniChartInstances.value.forEach(instance => instance.dispose())
})
</script>

<style scoped>
.mt-4 { margin-top: 1.5rem; }
.mr-2 { margin-right: 0.5rem; }

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  margin-bottom: 1rem;
}

.header-left h2 { font-size: 1.5rem; font-weight: 700; color: var(--text-primary); }
.header-left p { color: var(--text-secondary); font-size: 0.9rem; }

.search-box { position: relative; width: 300px; margin-right: 20px; }
.search-dropdown-card {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  z-index: 2000;
  margin-top: 8px;
}

.index-card-new {
  position: relative;
  overflow: hidden;
  height: 140px;
  cursor: pointer;
  border: 1px solid var(--border-color);
}

.index-main { position: relative; z-index: 2; }
.index-label { font-size: 0.9rem; color: var(--text-secondary); }
.index-value { font-size: 1.8rem; font-weight: 700; margin: 4px 0; }
.index-change { font-size: 0.95rem; display: flex; align-items: center; gap: 4px; }

.mini-chart-new {
  position: absolute;
  bottom: -10px;
  right: -10px;
  width: 150px;
  height: 80px;
  opacity: 0.6;
}

.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
.header-title { display: flex; align-items: center; font-weight: 600; }

.price-banner {
  padding: 1rem;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
  margin-bottom: 1rem;
}

.stat-box {
  padding: 12px;
  background: rgba(255, 255, 255, 0.02);
  border-radius: 8px;
  margin-bottom: 10px;
  border: 1px solid var(--border-color);
}
.stat-label { font-size: 0.8rem; color: var(--text-tertiary); margin-bottom: 4px; }
.stat-value { font-size: 1.2rem; font-weight: 600; }

.watchlist-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 8px;
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  transition: var(--transition-fast);
}
.watchlist-row:hover { background: rgba(255, 255, 255, 0.05); }
.w-info .name { font-weight: 500; font-size: 0.95rem; }
.w-info .code { font-size: 0.8rem; color: var(--text-tertiary); }
.w-price { font-family: monospace; font-weight: 600; }

.ai-advice-card {
  background: linear-gradient(135deg, rgba(0, 166, 255, 0.1), transparent);
  border: 1px solid rgba(0, 166, 255, 0.2);
}
.ai-header { display: flex; align-items: center; gap: 10px; }
.ai-avatar { background: var(--primary-accent); }
.ai-title { font-weight: 600; color: var(--primary-accent); flex: 1; }
.ai-body { margin-top: 12px; }
.ai-body p { font-size: 0.95rem; line-height: 1.6; word-break: break-word; white-space: pre-wrap; }
.highlight { color: var(--primary-accent); font-weight: 600; }
.ai-footer { margin-top: 12px; font-size: 0.75rem; color: var(--text-tertiary); }
.expand-icon { transition: transform 0.3s ease; }
.expand-icon.expanded { transform: rotate(90deg); }
</style>
