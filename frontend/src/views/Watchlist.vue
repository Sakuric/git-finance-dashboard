<template>
  <div class="page-content active">
    <header class="header">
      <div class="header-left">
        <h2>我的自选</h2>
        <p>管理您关注的股票</p>
      </div>
      <div class="header-right">
        <div class="search-box">
          <input
            type="text"
            v-model="searchKeyword"
            placeholder="输入股票代码添加..."
            @keyup.enter="handleAddFavorite"
          >
          <i class="fas fa-search"></i>
        </div>
        <button
          class="auth-btn"
          style="width: auto; padding: 0.6rem 1.2rem;"
          @click="handleAddFavorite"
          :disabled="loading"
        >
          <span class="btn-text">添加股票</span>
        </button>
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
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getFavorites, removeFavorite, addFavorite } from '@/api/favorite'
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
            data: [
              { value: 35, name: '科技', itemStyle: { color: '#00AFFF' } },
              { value: 25, name: '金融', itemStyle: { color: '#00B894' } },
              { value: 20, name: '消费', itemStyle: { color: '#F39C12' } },
              { value: 15, name: '医疗', itemStyle: { color: '#6F42C1' } },
              { value: 5, name: '能源', itemStyle: { color: '#D73A49' } }
            ]
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
          data: ['贵州茅台', '宁德时代', '比亚迪', '五粮液', '招商银行'],
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
            data: [
              { value: 1.5, itemStyle: { color: '#00B894' } },
              { value: -2.1, itemStyle: { color: '#D63031' } },
              { value: 0.88, itemStyle: { color: '#00B894' } },
              { value: 0.65, itemStyle: { color: '#00B894' } },
              { value: -0.35, itemStyle: { color: '#D63031' } }
            ],
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
      try {
        loading.value = true
        const response = await getFavorites()
        console.log('自选股数据响应:', response)
        
        if (response && response.code === 200 && response.data) {
          watchlistStocks.value = response.data.map(stock => ({
            id: stock.id,
            code: stock.stockCode,
            name: stock.stockName,
            price: stock.currentPrice || '--',
            change: parseFloat(stock.changePercent) || 0,
            changeAmount: stock.changePercent ? `${stock.changePercent >= 0 ? '+' : ''}${stock.changePercent}%` : '--',
            changePercent: parseFloat(stock.changePercent) || 0,
            volume: stock.volume || '--',
            industry: stock.industry || '其他',
            exchange: stock.exchange || '深交所',
            remark: stock.remark || ''
          }))

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

    // 添加自选股
    const handleAddFavorite = async () => {
      if (!searchKeyword.value.trim()) {
        alert('请输入股票代码')
        return
      }

      const userId = authStore.userId || localStorage.getItem('userId')
      if (!userId) {
        alert('请先登录')
        return
      }

      try {
        const response = await addFavorite(userId, searchKeyword.value.trim().toUpperCase(), '')
        
        console.log('添加响应:', response)
        
        if (response && response.code === 200) {
          alert('添加成功')
          searchKeyword.value = ''
          await loadFavorites()
        } else {
          alert('添加失败: ' + (response?.message || '未知错误'))
        }
      } catch (error) {
        console.error('添加自选股失败:', error)
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
      handleRemoveFavorite,
      handleAddFavorite,
      viewStockDetail
    }
  }
}
</script>