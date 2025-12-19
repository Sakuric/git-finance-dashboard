<template>
  <div class="page-content active">
    <!-- 头部：搜索与标题 -->
    <el-header class="header card-glass mb-6" height="auto">
      <div class="header-left">
        <h2>我的自选</h2>
        <p>实时追踪您最关注的资产表现</p>
      </div>
      <div class="header-right">
        <div class="search-container">
          <el-input
            v-model="searchKeyword"
            placeholder="输入代码或名称添加自选..."
            :prefix-icon="Search"
            size="large"
            @input="handleSearch"
            clearable
          />
          <el-card v-if="showSearchDropdown" class="search-results-card">
            <div v-if="searchLoading" class="p-4 text-center"><el-icon class="is-loading"><Loading /></el-icon></div>
            <div v-else class="results-list">
              <div v-for="item in searchResults" :key="item.stockCode" class="result-item" @click="selectSearchResult(item)">
                <span>{{ item.stockName }}</span>
                <el-tag size="small" type="info">{{ item.stockCode }}</el-tag>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </el-header>

    <el-row :gutter="20">
      <!-- 列表主区域 -->
      <el-col :span="24">
        <el-card shadow="never" class="table-card">
          <template #header>
            <div class="flex-between">
              <span class="font-bold">资产列表</span>
              <el-radio-group v-model="viewMode" size="small">
                <el-radio-button label="list">列表视图</el-radio-button>
                <el-radio-button label="grid" disabled>矩阵视图</el-radio-button>
              </el-radio-group>
            </div>
          </template>

          <el-table :data="watchlistStocks" v-loading="loading" style="width: 100%" class="custom-table" @row-click="row => viewStockDetail(row.code)">
            <el-table-column label="证券信息" width="220">
              <template #default="{ row }">
                <div class="stock-info-cell">
                  <div class="name">{{ row.name }}</div>
                  <div class="code-tag">{{ row.code }} <span class="exchange">{{ row.exchange }}</span></div>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column label="当前价" prop="price" sortable align="right">
              <template #default="{ row }">
                <span class="font-mono font-bold">{{ row.price }}</span>
              </template>
            </el-table-column>

            <el-table-column label="涨跌额" prop="changeAmount" align="right">
              <template #default="{ row }">
                <span :class="row.change >= 0 ? 'positive' : 'negative'">{{ row.changeAmount }}</span>
              </template>
            </el-table-column>

            <el-table-column label="涨跌幅" prop="changePercent" sortable align="right">
              <template #default="{ row }">
                <el-tag :type="row.change >= 0 ? 'success' : 'danger'" effect="dark" class="percent-tag">
                  {{ row.change >= 0 ? '+' : '' }}{{ row.changePercent }}%
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column label="成交额" prop="volume" align="right" />
            
            <el-table-column label="所属行业" prop="industry" align="center">
              <template #default="{ row }">
                <el-tag size="small" plain>{{ row.industry }}</el-tag>
              </template>
            </el-table-column>

            <el-table-column label="管理" width="120" align="center">
              <template #default="{ row }">
                <el-button-group>
                  <el-button :icon="TrendCharts" size="small" @click.stop="viewStockDetail(row.code)" />
                  <el-popconfirm title="确定移除该自选股吗？" @confirm="handleRemoveFavorite(row.code)">
                    <template #reference>
                      <el-button :icon="Delete" size="small" type="danger" plain @click.stop />
                    </template>
                  </el-popconfirm>
                </el-button-group>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 分析图表 -->
      <el-col :span="12" class="mt-6">
        <el-card shadow="never" class="chart-card-new">
          <template #header><span>自选股行业分布</span></template>
          <div id="watchlistSectorChart" style="height: 320px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12" class="mt-6">
        <el-card shadow="never" class="chart-card-new">
          <template #header><span>收益率表现对比</span></template>
          <div id="watchlistPerformanceChart" style="height: 320px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { Search, Delete, TrendCharts, Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getFavorites, removeFavorite, addFavorite } from '@/api/favorite'
import { searchStock } from '@/api/stock'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const viewMode = ref('list')
const loading = ref(false)
const watchlistStocks = ref([])
const searchKeyword = ref('')
const searchResults = ref([])
const searchLoading = ref(false)
const showSearchDropdown = computed(() => searchKeyword.value && (searchLoading.value || searchResults.value.length > 0))

const sectorChart = ref(null)
const performanceChart = ref(null)

const loadFavorites = async () => {
  const userId = authStore.userId || localStorage.getItem('userId')
  if (!userId) return
  loading.value = true
  try {
    const res = await getFavorites(userId)
    if (res.code === 200) {
      watchlistStocks.value = (res.data || []).map(s => ({
        code: s.stockCode,
        name: s.stockName,
        price: Number(s.currentPrice || 0).toFixed(2),
        change: Number(s.changePercent || 0),
        changeAmount: (Number(s.currentPrice || 0) * (Number(s.changePercent || 0)/100)).toFixed(2),
        changePercent: Number(s.changePercent || 0).toFixed(2),
        volume: (Number(s.amount || 0) / 100000000).toFixed(2) + '亿',
        industry: s.industry || '其他',
        exchange: s.stockCode.startsWith('6') ? 'SH' : 'SZ'
      }))
      nextTick(() => {
        initCharts()
      })
    }
  } finally {
    loading.value = false
  }
}

const initCharts = () => {
  const sectorDom = document.getElementById('watchlistSectorChart')
  const perfDom = document.getElementById('watchlistPerformanceChart')
  if (!sectorDom || !perfDom) return

  sectorChart.value = echarts.init(sectorDom)
  performanceChart.value = echarts.init(perfDom)

  // 行业分布圆环图
  const sectorData = {}
  watchlistStocks.value.forEach(s => sectorData[s.industry] = (sectorData[s.industry] || 0) + 1)
  
  sectorChart.value.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['50%', '80%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 8, borderColor: '#0D1117', borderWidth: 2 },
      label: { show: false, position: 'center' },
      emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold', color: '#fff' } },
      data: Object.entries(sectorData).map(([name, value]) => ({ name, value }))
    }]
  })

  // 表现对比柱状图
  performanceChart.value.setOption({
    grid: { left: '3%', right: '3%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: { type: 'value', splitLine: { lineStyle: { color: '#30363D' } } },
    yAxis: { type: 'category', data: watchlistStocks.value.map(s => s.name), axisLine: { lineStyle: { color: '#30363D' } } },
    series: [{
      type: 'bar',
      data: watchlistStocks.value.map(s => ({
        value: s.changePercent,
        itemStyle: { color: s.change >= 0 ? '#39D353' : '#F85149' }
      })),
      label: { show: true, position: 'right', formatter: '{c}%', color: '#fff' }
    }]
  })
}

const handleSearch = async () => {
  if (!searchKeyword.value) return searchResults.value = []
  searchLoading.value = true
  try {
    const res = await searchStock(searchKeyword.value)
    searchResults.value = res.data || []
  } finally { searchLoading.value = false }
}

const selectSearchResult = async (item) => {
  const userId = authStore.userId || localStorage.getItem('userId')
  const res = await addFavorite(userId, item.stockCode, '')
  if (res.code === 200) {
    ElMessage.success('已添加到自选')
    searchKeyword.value = ''
    loadFavorites()
  }
}

const handleRemoveFavorite = async (code) => {
  const userId = authStore.userId || localStorage.getItem('userId')
  const res = await removeFavorite(userId, code)
  if (res.code === 200) {
    ElMessage.success('已移除')
    loadFavorites()
  }
}

const viewStockDetail = (code) => router.push(`/market?stock=${code}`)

onMounted(() => loadFavorites())
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; padding: 1.5rem 2rem; }
.search-container { position: relative; width: 340px; }
.search-results-card { position: absolute; top: 100%; left: 0; right: 0; z-index: 2000; margin-top: 10px; }

.custom-table :deep(.el-table__row) { cursor: pointer; }
.stock-info-cell .name { font-weight: 700; font-size: 1.05rem; }
.stock-info-cell .code-tag { font-size: 0.8rem; color: var(--text-tertiary); margin-top: 2px; }
.stock-info-cell .exchange { margin-left: 4px; padding: 0 4px; border: 1px solid var(--border-color); border-radius: 2px; }

.percent-tag { width: 75px; text-align: center; font-weight: 700; }
.mt-6 { margin-top: 1.5rem; }
.result-item { padding: 10px 15px; cursor: pointer; display: flex; justify-content: space-between; }
.result-item:hover { background: rgba(0, 166, 255, 0.1); }
</style>

<script>
import { ref, computed, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getFavorites, removeFavorite, addFavorite } from '@/api/favorite'
import { searchStock } from '@/api/stock'
import { useAuthStore } from '@/stores/auth'

export default {
  name: 'Watchlist',
  setup() {
    const authStore = useAuthStore()
    const sectorChart = ref(null)
    const performanceChart = ref(null)

    const watchlistStocks = ref([])
    const loading = ref(false)
    const searchKeyword = ref('')
    const searchResults = ref([])
    const searchLoading = ref(false)
    const searchError = ref('')
    const showSearchDropdown = computed(() => searchKeyword.value && (searchLoading.value || searchError.value || searchResults.value.length > 0))

    // 初始化行业分布图表
    const initSectorChart = () => {
      const chartDom = document.getElementById('watchlistSectorChart')
      if (!chartDom) return

      if (sectorChart.value) {
        sectorChart.value.dispose()
      }

      sectorChart.value = echarts.init(chartDom)
      
      const option = {
        backgroundColor: 'transparent',
        tooltip: {
          trigger: 'item',
          backgroundColor: 'rgba(22, 27, 34, 0.9)',
          borderColor: '#30363D',
          textStyle: { color: '#C9D1D9' }
        },
        series: [
          {
            name: '行业分布',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#0D1117',
              borderWidth: 2
            },
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: 14,
                fontWeight: 'bold'
              }
            },
            data: (() => {
              const industryMap = {}
              watchlistStocks.value.forEach(stock => {
                const industry = stock.industry || '其他'
                industryMap[industry] = (industryMap[industry] || 0) + 1
              })
              const colors = ['#00AFFF', '#00B894', '#F39C12', '#6F42C1', '#D73A49', '#E74C3C', '#3498DB']
              return Object.entries(industryMap).map(([name, value], index) => ({
                value, name, itemStyle: { color: colors[index % colors.length] }
              }))
            })()
          }
        ]
      }
      
      sectorChart.value.setOption(option)
      window.addEventListener('resize', () => sectorChart.value.resize())
    }

    // 初始化涨跌分布图表
    const initPerformanceChart = () => {
      const chartDom = document.getElementById('watchlistPerformanceChart')
      if (!chartDom) return

      if (performanceChart.value) {
        performanceChart.value.dispose()
      }

      performanceChart.value = echarts.init(chartDom)
      
      const option = {
        backgroundColor: 'transparent',
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(22, 27, 34, 0.9)',
          borderColor: '#30363D',
          textStyle: { color: '#C9D1D9' }
        },
        grid: {
          left: '10%',
          right: '8%',
          top: '15%',
          bottom: '15%'
        },
        xAxis: {
          type: 'category',
          data: watchlistStocks.value.map(s => s.name),
          axisLine: { lineStyle: { color: '#8B949E' } },
          axisLabel: {
            color: '#8B949E',
            interval: 0,
            rotate: 30
          }
        },
        yAxis: {
          type: 'value',
          axisLine: { lineStyle: { color: '#8B949E' } },
          splitLine: { lineStyle: { color: '#30363D' } },
          axisLabel: {
            color: '#8B949E',
            formatter: '{value}%'
          }
        },
        series: [
          {
            name: '涨跌幅',
            type: 'bar',
            data: watchlistStocks.value.map(s => ({
              value: s.changePercent || 0,
              itemStyle: { color: (s.changePercent || 0) >= 0 ? '#00B894' : '#D63031' }
            })),
            label: {
              show: true,
              position: 'top',
              formatter: '{c}%'
            }
          }
        ]
      }
      
      performanceChart.value.setOption(option)
      window.addEventListener('resize', () => performanceChart.value.resize())
    }

    // 加载自选股数据
    const loadFavorites = async () => {
      const userId = authStore.userId || localStorage.getItem('userId')
      if (!userId) return

      try {
        loading.value = true
        const response = await getFavorites(userId)
        console.log('自选股数据响应:', response)
        
        if (response && response.code === 200 && response.data) {
          watchlistStocks.value = response.data.map(stock => {
            const price = parseFloat(stock.currentPrice) || 0
            const changePercent = parseFloat(stock.changePercent) || 0
            const changeAmount = price * (changePercent / 100)
            const industry = stock.stockName?.includes('银行') ? '金融' : (stock.industry || '其他')

            return {
              id: stock.id,
              code: stock.stockCode,
              name: stock.stockName,
              price: stock.currentPrice || '--',
              change: changePercent,
              changeAmount: changeAmount ? `${changeAmount >= 0 ? '+' : ''}${changeAmount.toFixed(2)}` : '--',
              changePercent: changePercent,
              volume: stock.volume || '--',
              industry,
              exchange: stock.exchange || '深交所',
              remark: stock.remark || ''
            }
          })

          nextTick(() => {
            initSectorChart()
            initPerformanceChart()
          })
        }
      } catch (error) {
        console.error('加载自选股失败:', error)
      } finally {
        loading.value = false
      }
    }

    // 删除自选股
    const handleRemoveFavorite = async (stockCode) => {
      if (!confirm(`确定要删除股票 ${stockCode} 吗？`)) {
        return
      }

      const userId = authStore.userId || localStorage.getItem('userId')
      if (!userId) {
        alert('请先登录')
        return
      }

      try {
        const response = await removeFavorite(userId, stockCode)
        console.log('删除响应:', response)
        
        if (response && response.code === 200) {
          alert('删除成功')
          await loadFavorites()
        } else {
          alert('删除失败: ' + (response?.message || '未知错误'))
        }
      } catch (error) {
        console.error('删除自选股失败:', error)
      }
    }

    // 搜索股票
    let searchTimeout = null
    const handleSearch = async () => {
      const keyword = searchKeyword.value.trim()
      if (!keyword) {
        searchResults.value = []
        searchError.value = ''
        return
      }

      if (searchTimeout) {
        clearTimeout(searchTimeout)
      }

      searchTimeout = setTimeout(async () => {
        searchLoading.value = true
        searchError.value = ''
        try {
          const res = await searchStock(keyword)
          if (res?.code === 200) {
            searchResults.value = res.data || []
          } else {
            searchError.value = res?.message || '搜索失败'
          }
        } catch (err) {
          console.error('搜索出错', err)
          searchError.value = '网络错误，请稍后重试'
        } finally {
          searchLoading.value = false
        }
      }, 300)
    }

    // 选择搜索结果
    const selectSearchResult = async (item) => {
      const userId = authStore.userId || localStorage.getItem('userId')
      if (!userId) {
        alert('请先登录')
        return
      }

      try {
        const response = await addFavorite(userId, item.stockCode, '')
        if (response && response.code === 200) {
          alert('添加成功')
          searchKeyword.value = ''
          searchResults.value = []
          await loadFavorites()
        } else {
          alert('添加失败: ' + (response?.message || '未知错误'))
        }
      } catch (error) {
        console.error('添加自选股失败:', error)
        alert('添加失败')
      }
    }

    // 查看股票详情
    const viewStockDetail = (stockCode) => {
      // 跳转到股票详情页
      window.location.href = `/market?stock=${stockCode}`
    }

    onMounted(() => {
      loadFavorites()
    })

    return {
      watchlistStocks,
      loading,
      searchKeyword,
      searchResults,
      searchLoading,
      searchError,
      showSearchDropdown,
      handleSearch,
      selectSearchResult,
      handleRemoveFavorite,
      viewStockDetail
    }
  }
}
</script>