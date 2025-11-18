<template>
  <div class="page-content active">
    <header class="header">
      <div class="header-left">
        <h2>AI模型管理</h2>
        <p>管理和配置您的AI投资模型</p>
      </div>
      <div class="header-right">
        <button class="auth-btn" style="width: auto; padding: 0.6rem 1.2rem;">
          <span class="btn-text">创建新模型</span>
        </button>
      </div>
    </header>

    <div class="content-grid">
      <div class="card" style="grid-column: 1 / -1;">
        <div class="card-header">
          <h3>我的AI模型</h3>
          <div class="view-options">
            <button class="active">卡片视图</button>
            <button>列表视图</button>
          </div>
        </div>
        <div class="models-grid">
          <div 
            v-for="model in aiModels" 
            :key="model.id"
            class="model-card"
            :class="{ active: model.status === 'active' }"
          >
            <div class="model-header">
              <div class="model-name">{{ model.name }}</div>
              <div class="model-status" :class="model.status">{{ model.statusLabel }}</div>
            </div>
            <div class="model-icon">
              <i :class="model.icon"></i>
            </div>
            <div class="model-details">
              <div class="model-type">{{ model.type }}</div>
              <div class="model-description">{{ model.description }}</div>
              <div class="model-stats">
                <div class="stat-item">
                  <div class="stat-label">准确率</div>
                  <div class="stat-value">{{ model.accuracy }}</div>
                </div>
                <div class="stat-item">
                  <div class="stat-label">更新频率</div>
                  <div class="stat-value">{{ model.frequency }}</div>
                </div>
              </div>
            </div>
            <div class="model-actions">
              <button class="icon-btn"><i class="fas fa-cog"></i></button>
              <button class="icon-btn"><i class="fas fa-chart-bar"></i></button>
              <button class="icon-btn">
                <i :class="model.status === 'active' ? 'fas fa-pause' : 'fas fa-play'"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
      
      <div class="card">
        <div class="card-header">
          <h3>模型性能对比</h3>
        </div>
        <div id="modelPerformanceChart" class="chart-container" style="height: 300px;"></div>
      </div>
      
      <div class="card">
        <div class="card-header">
          <h3>模型使用统计</h3>
        </div>
        <div id="modelUsageChart" class="chart-container" style="height: 300px;"></div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'

export default {
  name: 'AiModels',
  setup() {
    const performanceChart = ref(null)
    const usageChart = ref(null)

    const aiModels = ref([
      {
        id: 1,
        name: '增长模型 V2',
        type: '趋势预测',
        description: '基于历史数据和市场情绪分析，预测股票短期到中期价格趋势',
        accuracy: '78.5%',
        frequency: '每日',
        status: 'active',
        statusLabel: '运行中',
        icon: 'fas fa-chart-line'
      },
      {
        id: 2,
        name: '技术分析模型 V1',
        type: '技术指标',
        description: '使用多种技术指标(RSI, MACD, KDJ等)生成买卖信号',
        accuracy: '72.3%',
        frequency: '实时',
        status: 'active',
        statusLabel: '运行中',
        icon: 'fas fa-chart-area'
      },
      {
        id: 3,
        name: '持仓分析模型 V3',
        type: '持仓优化',
        description: '分析当前持仓表现，提供优化建议和风险评估',
        accuracy: '81.2%',
        frequency: '每周',
        status: 'active',
        statusLabel: '运行中',
        icon: 'fas fa-briefcase'
      },
      {
        id: 4,
        name: '情绪分析模型',
        type: '情绪分析',
        description: '分析新闻和社交媒体情绪，预测市场短期波动',
        accuracy: '65.8%',
        frequency: '每小时',
        status: 'inactive',
        statusLabel: '已暂停',
        icon: 'fas fa-comments'
      }
    ])

    // 初始化模型性能对比图表
    const initPerformanceChart = () => {
      const chartDom = document.getElementById('modelPerformanceChart')
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
        legend: {
          data: ['增长模型 V2', '技术分析模型 V1', '持仓分析模型 V3', '情绪分析模型'],
          textStyle: { color: '#8B949E' },
          top: 10
        },
        grid: {
          left: '10%',
          right: '8%',
          top: '15%',
          bottom: '15%'
        },
        xAxis: {
          type: 'category',
          data: ['1月', '2月', '3月', '4月', '5月', '6月'],
          axisLine: { lineStyle: { color: '#8B949E' } },
          axisLabel: { color: '#8B949E' }
        },
        yAxis: {
          type: 'value',
          name: '准确率(%)',
          min: 50,
          max: 100,
          axisLine: { lineStyle: { color: '#8B949E' } },
          splitLine: { lineStyle: { color: '#30363D' } },
          axisLabel: { color: '#8B949E' }
        },
        series: [
          {
            name: '增长模型 V2',
            type: 'line',
            data: [72, 75, 78, 78.5, 79, 78.5],
            smooth: true,
            lineStyle: { width: 2, color: '#00AFFF' },
            symbol: 'circle',
            symbolSize: 6
          },
          {
            name: '技术分析模型 V1',
            type: 'line',
            data: [68, 70, 71, 72.3, 73, 72.3],
            smooth: true,
            lineStyle: { width: 2, color: '#00B894' },
            symbol: 'circle',
            symbolSize: 6
          },
          {
            name: '持仓分析模型 V3',
            type: 'line',
            data: [78, 79, 80, 81.2, 82, 81.2],
            smooth: true,
            lineStyle: { width: 2, color: '#F39C12' },
            symbol: 'circle',
            symbolSize: 6
          },
          {
            name: '情绪分析模型',
            type: 'line',
            data: [60, 62, 64, 65.8, 67, 65.8],
            smooth: true,
            lineStyle: { width: 2, color: '#6F42C1' },
            symbol: 'circle',
            symbolSize: 6
          }
        ]
      }
      
      performanceChart.value.setOption(option)
      window.addEventListener('resize', () => performanceChart.value.resize())
    }

    // 初始化模型使用统计图表
    const initUsageChart = () => {
      const chartDom = document.getElementById('modelUsageChart')
      if (!chartDom) return

      if (usageChart.value) {
        usageChart.value.dispose()
      }

      usageChart.value = echarts.init(chartDom)
      
      const option = {
        backgroundColor: 'transparent',
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(22, 27, 34, 0.9)',
          borderColor: '#30363D',
          textStyle: { color: '#C9D1D9' }
        },
        legend: {
          data: ['增长模型 V2', '技术分析模型 V1', '持仓分析模型 V3', '情绪分析模型'],
          textStyle: { color: '#8B949E' },
          top: 10
        },
        grid: {
          left: '10%',
          right: '8%',
          top: '15%',
          bottom: '15%'
        },
        xAxis: {
          type: 'category',
          data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
          axisLine: { lineStyle: { color: '#8B949E' } },
          axisLabel: { color: '#8B949E' }
        },
        yAxis: {
          type: 'value',
          name: '使用次数',
          axisLine: { lineStyle: { color: '#8B949E' } },
          splitLine: { lineStyle: { color: '#30363D' } },
          axisLabel: { color: '#8B949E' }
        },
        series: [
          {
            name: '增长模型 V2',
            type: 'bar',
            stack: 'total',
            data: [120, 132, 101, 134, 90, 80, 65],
            itemStyle: { color: '#00AFFF' }
          },
          {
            name: '技术分析模型 V1',
            type: 'bar',
            stack: 'total',
            data: [220, 182, 191, 234, 290, 210, 150],
            itemStyle: { color: '#00B894' }
          },
          {
            name: '持仓分析模型 V3',
            type: 'bar',
            stack: 'total',
            data: [150, 232, 201, 154, 190, 120, 80],
            itemStyle: { color: '#F39C12' }
          },
          {
            name: '情绪分析模型',
            type: 'bar',
            stack: 'total',
            data: [80, 92, 91, 94, 90, 70, 45],
            itemStyle: { color: '#6F42C1' }
          }
        ]
      }
      
      usageChart.value.setOption(option)
      window.addEventListener('resize', () => usageChart.value.resize())
    }

    onMounted(() => {
      nextTick(() => {
        initPerformanceChart()
        initUsageChart()
      })
    })

    return {
      aiModels
    }
  }
}
</script>