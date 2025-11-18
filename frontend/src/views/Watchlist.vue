<template>
  <div class="page-content active">
    <header class="header">
      <div class="header-left">
        <h2>我的自选</h2>
        <p>管理您关注的股票</p>
      </div>
      <div class="header-right">
        <div class="search-box">
          <input type="text" placeholder="搜索股票代码/名称...">
          <i class="fas fa-search"></i>
        </div>
        <button class="auth-btn" style="width: auto; padding: 0.6rem 1.2rem;">
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
                <th>当前价</th>
                <th>涨跌幅</th>
                <th>成交量</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="stock in watchlistStocks" :key="stock.code">
                <td>{{ stock.code }}</td>
                <td>{{ stock.name }}</td>
                <td>{{ stock.price }}</td>
                <td :class="stock.change >= 0 ? 'positive' : 'negative'">{{ stock.changePercent }}%</td>
                <td>{{ stock.volume }}</td>
                <td>
                  <button class="icon-btn"><i class="fas fa-chart-line"></i></button>
                  <button class="icon-btn"><i class="fas fa-trash"></i></button>
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

export default {
  name: 'Watchlist',
  setup() {
    const sectorChart = ref(null)
    const performanceChart = ref(null)

    const watchlistStocks = ref([
      {
        code: '600519',
        name: '贵州茅台',
        price: '1850.00',
        change: 1.5,
        changePercent: '+1.50',
        volume: '2.3万'
      },
      {
        code: '300750',
        name: '宁德时代',
        price: '218.50',
        change: -2.1,
        changePercent: '-2.10',
        volume: '5.8万'
      },
      {
        code: '002594',
        name: '比亚迪',
        price: '255.88',
        change: 0.88,
        changePercent: '+0.88',
        volume: '15.2万'
      },
      {
        code: '000858',
        name: '五粮液',
        price: '168.50',
        change: 0.65,
        changePercent: '+0.65',
        volume: '3.2万'
      },
      {
        code: '600036',
        name: '招商银行',
        price: '42.30',
        change: -0.35,
        changePercent: '-0.35',
        volume: '8.7万'
      }
    ])

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

    onMounted(() => {
      nextTick(() => {
        initSectorChart()
        initPerformanceChart()
      })
    })

    return {
      watchlistStocks
    }
  }
}
</script>