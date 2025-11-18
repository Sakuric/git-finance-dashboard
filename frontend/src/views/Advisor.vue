<template>
  <div class="page-content active">
    <header class="header">
      <div class="header-left">
        <h2>智能投顾</h2>
        <p>AI驱动的个性化投资建议</p>
      </div>
      <div class="header-right">
        <div class="search-box">
          <input type="text" placeholder="搜索投顾建议...">
          <i class="fas fa-search"></i>
        </div>
      </div>
    </header>

    <div class="content-grid">
      <div class="card" style="grid-column: 1 / -1;">
        <div class="card-header">
          <h3>投资偏好设置</h3>
        </div>
        <div class="preference-settings">
          <div class="preference-group">
            <h4>风险承受能力</h4>
            <div class="preference-options">
              <label class="radio-option">
                <input type="radio" name="risk" value="conservative" v-model="preferences.risk">
                <span class="radio-check"></span>
                <span class="radio-label">保守型</span>
              </label>
              <label class="radio-option">
                <input type="radio" name="risk" value="balanced" v-model="preferences.risk">
                <span class="radio-check"></span>
                <span class="radio-label">稳健型</span>
              </label>
              <label class="radio-option">
                <input type="radio" name="risk" value="aggressive" v-model="preferences.risk">
                <span class="radio-check"></span>
                <span class="radio-label">激进型</span>
              </label>
            </div>
          </div>
          <div class="preference-group">
            <h4>投资期限</h4>
            <div class="preference-options">
              <label class="radio-option">
                <input type="radio" name="term" value="short" v-model="preferences.term">
                <span class="radio-check"></span>
                <span class="radio-label">短期 (3个月内)</span>
              </label>
              <label class="radio-option">
                <input type="radio" name="term" value="medium" v-model="preferences.term">
                <span class="radio-check"></span>
                <span class="radio-label">中期 (3-12个月)</span>
              </label>
              <label class="radio-option">
                <input type="radio" name="term" value="long" v-model="preferences.term">
                <span class="radio-check"></span>
                <span class="radio-label">长期 (1年以上)</span>
              </label>
            </div>
          </div>
          <div class="preference-group">
            <h4>关注行业</h4>
            <div class="preference-options">
              <label class="checkbox-option">
                <input type="checkbox" value="tech" v-model="preferences.industries">
                <span class="checkbox-check"></span>
                <span class="checkbox-label">科技</span>
              </label>
              <label class="checkbox-option">
                <input type="checkbox" value="finance" v-model="preferences.industries">
                <span class="checkbox-check"></span>
                <span class="checkbox-label">金融</span>
              </label>
              <label class="checkbox-option">
                <input type="checkbox" value="healthcare" v-model="preferences.industries">
                <span class="checkbox-check"></span>
                <span class="checkbox-label">医疗</span>
              </label>
              <label class="checkbox-option">
                <input type="checkbox" value="consumer" v-model="preferences.industries">
                <span class="checkbox-check"></span>
                <span class="checkbox-label">消费</span>
              </label>
              <label class="checkbox-option">
                <input type="checkbox" value="energy" v-model="preferences.industries">
                <span class="checkbox-check"></span>
                <span class="checkbox-label">能源</span>
              </label>
            </div>
          </div>
          <button class="auth-btn" style="margin-top: 1rem; align-self: flex-start;" @click="savePreferences">
            <span class="btn-text">保存偏好设置</span>
          </button>
        </div>
      </div>

      <div class="card advice-card-large" style="grid-column: 1 / -1;">
        <div class="card-header">
          <h3>最新智能投顾建议</h3>
          <div class="filter-tabs">
            <button 
              v-for="filter in filters" 
              :key="filter.value"
              :class="{ active: activeFilter === filter.value }"
              @click="activeFilter = filter.value"
            >
              {{ filter.label }}
            </button>
          </div>
        </div>
        <div class="advice-list">
          <div 
            v-for="advice in filteredAdvices" 
            :key="advice.id"
            class="advice-item"
          >
            <div class="advice-header">
              <div class="advice-type" :class="advice.type">{{ advice.typeLabel }}</div>
              <div class="advice-time">{{ advice.time }}</div>
            </div>
            <div class="advice-content">
              <div class="advice-stock">
                <div class="stock-icon">{{ advice.stock.icon }}</div>
                <div class="stock-info">
                  <div class="stock-name">{{ advice.stock.name }} ({{ advice.stock.code }})</div>
                  <div class="stock-price">
                    {{ advice.stock.price }} 
                    <span :class="advice.stock.change >= 0 ? 'positive' : 'negative'">{{ advice.stock.changePercent }}%</span>
                  </div>
                </div>
              </div>
              <div class="advice-details">
                <p>{{ advice.content }}</p>
                <div class="advice-model">
                  <i class="fas fa-robot"></i> 由 "{{ advice.model }}" 生成
                </div>
              </div>
            </div>
            <div class="advice-actions">
              <button class="btn-primary" style="padding: 0.5rem 1rem; font-size: 0.85rem;">查看详情</button>
              <button class="btn-secondary" style="padding: 0.5rem 1rem; font-size: 0.85rem;">忽略建议</button>
            </div>
          </div>
        </div>
      </div>
      
      <div class="card">
        <div class="card-header">
          <h3>投资组合分析</h3>
        </div>
        <div id="portfolioAnalysisChart" class="chart-container" style="height: 300px;"></div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'

export default {
  name: 'Advisor',
  setup() {
    const portfolioChart = ref(null)
    const activeFilter = ref('all')

    const preferences = ref({
      risk: 'balanced',
      term: 'medium',
      industries: ['tech', 'finance', 'consumer']
    })

    const filters = [
      { label: '全部', value: 'all' },
      { label: '买入建议', value: 'buy' },
      { label: '卖出建议', value: 'sell' },
      { label: '持仓分析', value: 'analysis' }
    ]

    const advices = ref([
      {
        id: 1,
        type: 'buy',
        typeLabel: '买入建议',
        time: '2023-10-27 10:30',
        stock: {
          icon: 'BYD',
          name: '比亚迪',
          code: '002594',
          price: '255.88',
          change: 0.88,
          changePercent: '+0.88'
        },
        content: '根据您的 "稳健增长型" 偏好和最新市场数据，建议关注新能源汽车板块。比亚迪作为行业龙头，近期技术指标显示买入信号，建议在价格回调至252元附近时适度建仓。',
        model: '我的增长模型 V2'
      },
      {
        id: 2,
        type: 'sell',
        typeLabel: '卖出建议',
        time: '2023-10-27 09:15',
        stock: {
          icon: '300750',
          name: '宁德时代',
          code: '300750',
          price: '218.50',
          change: -2.1,
          changePercent: '-2.10'
        },
        content: '宁德时代近期技术指标显示卖出信号，RSI指标进入超买区域。建议部分减仓，锁定利润，等待更好的入场时机。',
        model: '技术分析模型 V1'
      },
      {
        id: 3,
        type: 'analysis',
        typeLabel: '持仓分析',
        time: '2023-10-26 16:00',
        stock: {
          icon: '600519',
          name: '贵州茅台',
          code: '600519',
          price: '1850.00',
          change: 1.5,
          changePercent: '+1.50'
        },
        content: '您的贵州茅台持仓表现良好，近期走势强于大盘。根据基本面分析，公司业绩稳定增长，建议继续持有，可考虑在季度报告发布后适当加仓。',
        model: '持仓分析模型 V3'
      }
    ])

    // 过滤后的建议
    const filteredAdvices = computed(() => {
      if (activeFilter.value === 'all') {
        return advices.value
      }
      return advices.value.filter(advice => advice.type === activeFilter.value)
    })

    // 初始化投资组合分析图表
    const initPortfolioChart = () => {
      const chartDom = document.getElementById('portfolioAnalysisChart')
      if (!chartDom) return

      if (portfolioChart.value) {
        portfolioChart.value.dispose()
      }

      portfolioChart.value = echarts.init(chartDom)
      
      const option = {
        backgroundColor: 'transparent',
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(22, 27, 34, 0.9)',
          borderColor: '#30363D',
          textStyle: { color: '#C9D1D9' }
        },
        legend: {
          data: ['当前组合', '建议组合'],
          textStyle: { color: '#8B949E' },
          top: 10
        },
        radar: {
          indicator: [
            { name: '成长性', max: 100 },
            { name: '价值性', max: 100 },
            { name: '稳定性', max: 100 },
            { name: '收益性', max: 100 },
            { name: '流动性', max: 100 }
          ],
          radius: '65%',
          splitNumber: 4,
          axisLine: {
            lineStyle: {
              color: '#8B949E'
            }
          },
          splitLine: {
            lineStyle: {
              color: '#30363D'
            }
          },
          splitArea: {
            show: false
          },
          axisName: {
            color: '#8B949E'
          }
        },
        series: [
          {
            name: '当前组合',
            type: 'radar',
            data: [
              {
                value: [85, 70, 90, 75, 80],
                name: '当前组合',
                areaStyle: {
                  color: 'rgba(0, 175, 255, 0.4)'
                },
                lineStyle: {
                  color: '#00AFFF',
                  width: 2
                }
              }
            ]
          },
          {
            name: '建议组合',
            type: 'radar',
            data: [
              {
                value: [90, 80, 85, 85, 90],
                name: '建议组合',
                areaStyle: {
                  color: 'rgba(0, 184, 148, 0.4)'
                },
                lineStyle: {
                  color: '#00B894',
                  width: 2
                }
              }
            ]
          }
        ]
      }
      
      portfolioChart.value.setOption(option)
      window.addEventListener('resize', () => portfolioChart.value.resize())
    }

    // 保存偏好设置
    const savePreferences = () => {
      console.log('保存偏好设置:', preferences.value)
      // 这里可以添加保存到后端的逻辑
      alert('偏好设置已保存')
    }

    onMounted(() => {
      nextTick(() => {
        initPortfolioChart()
      })
    })

    return {
      preferences,
      filters,
      activeFilter,
      filteredAdvices,
      savePreferences
    }
  }
}
</script>