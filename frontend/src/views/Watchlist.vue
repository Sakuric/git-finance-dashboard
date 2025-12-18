<template>
  <div class="page-content active">
    <header class="header">
      <div class="header-left">
        <h2>我的自选</h2>
        <p>管理您关注的股票</p>
      </div>
      <div class="header-right">
        <div class="search-box" style="position: relative; z-index: 10000;">
          <input
            type="text"
            v-model="searchKeyword"
            @input="handleSearch"
            placeholder="搜索股票代码/名称..."
          >
          <i class="fas fa-search"></i>
          <div v-if="showSearchDropdown" class="search-dropdown" style="position: fixed !important; top: 60px; left: auto; right: 20px; width: 400px; background: var(--card-bg); border: 1px solid var(--border-color); border-radius: 8px; margin-top: 8px; max-height: 300px; overflow-y: auto; z-index: 99999 !important; box-shadow: 0 4px 12px rgba(0,0,0,0.3);">
            <div v-if="searchLoading" style="padding: 12px; text-align: center; color: var(--text-secondary);">
              <i class="fas fa-spinner fa-spin"></i> 搜索中...
            </div>
            <div v-else-if="searchError" style="padding: 12px; color: var(--color-negative);">{{ searchError }}</div>
            <div v-else-if="searchResults.length === 0" style="padding: 12px; text-align: center; color: var(--text-secondary);">暂无结果</div>
            <div v-else>
              <div
                v-for="item in searchResults"
                :key="item.stockCode"
                @click="selectSearchResult(item)"
                style="padding: 12px; cursor: pointer; border-bottom: 1px solid var(--border-color); display: flex; justify-content: space-between; align-items: center;"
                @mouseenter="$event.currentTarget.style.background = 'var(--hover-bg)'"
                @mouseleave="$event.currentTarget.style.background = 'transparent'">
                <span style="font-weight: 500;">{{ item.stockName }}</span>
                <small style="color: var(--text-secondary);">{{ item.stockCode }}</small>
              </div>
            </div>
          </div>
        </div>
      </div>
    </header>

    <div class="content-grid">
      <div class="card" style="grid-column: 1 / -1;">
        <div class="card-header">
          <h3>自选股票列表</h3>
          <div class="view-options">
            <button class="active">列表</button>
            <button>卡片</button>
          </div>
        </div>
        <div class="watchlist-table-container">
          <table class="watchlist-table">
            <thead>
              <tr>
                <th>股票代码</th>
                <th>股票名称</th>
                <th>最新价</th>
                <th>涨跌额</th>
                <th>涨跌幅</th>
                <th>成交量</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody v-if="!loading">
              <tr v-for="stock in watchlistStocks" :key="stock.code">
                <td>{{ stock.code }}</td>
                <td>{{ stock.name }}</td>
                <td :class="stock.change >= 0 ? 'positive' : 'negative'">{{ stock.price }}</td>
                <td :class="stock.change >= 0 ? 'positive' : 'negative'">{{ stock.changeAmount }}</td>
                <td :class="stock.change >= 0 ? 'positive' : 'negative'">{{ stock.changePercent }}%</td>
                <td>{{ stock.volume }}</td>
                <td>
                  <button
                    class="icon-btn"
                    @click="viewStockDetail(stock.code)"
                    title="查看详情"
                  >
                    <i class="fas fa-chart-line"></i>
                  </button>
                  <button
                    class="icon-btn"
                    @click="handleRemoveFavorite(stock.code)"
                    title="删除"
                  >
                    <i class="fas fa-trash"></i>
                  </button>
                </td>
              </tr>
            </tbody>
            <tbody v-else>
              <tr>
                <td colspan="7" style="text-align: center; padding: 20px;">
                  <i class="fas fa-spinner fa-spin"></i> 加载中...
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      
      <div class="card">
        <div class="card-header">
          <h3>自选股行业分布</h3>
        </div>
        <div id="watchlistSectorChart" class="chart-container" style="height: 300px;"></div>
      </div>
      
      <div class="card">
        <div class="card-header">
          <h3>自选股涨跌分布</h3>
        </div>
        <div id="watchlistPerformanceChart" class="chart-container" style="height: 300px;"></div>
      </div>
    </div>
  </div>
</template>

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