<template>
  <div class="page-content active">
    <header class="header">
      <div class="header-left">
        <h2>建议回测</h2>
        <p>验证投资建议的历史表现</p>
      </div>
    </header>

    <div class="content-grid">
      <!-- 回测参数设置 -->
      <div class="card" style="grid-column: 1 / -1;">
        <div class="card-header">
          <h3>回测参数</h3>
        </div>
        <div class="backtest-form">
          <div class="form-row">
            <div class="form-group">
              <label>股票代码</label>
              <input type="text" v-model="form.stockCode" placeholder="如: 600519">
            </div>
            <div class="form-group">
              <label>初始资金</label>
              <input type="number" v-model="form.initialCapital" placeholder="100000">
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>开始日期</label>
              <input type="date" v-model="form.startDate">
            </div>
            <div class="form-group">
              <label>结束日期</label>
              <input type="date" v-model="form.endDate">
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>策略类型</label>
              <select v-model="form.strategy">
                <option value="ma">均线策略</option>
                <option value="rsi">RSI策略</option>
                <option value="macd">MACD策略</option>
              </select>
            </div>
          </div>
          <button class="auth-btn" @click="runBacktest" :disabled="loading">
            <span class="btn-text">{{ loading ? '回测中...' : '开始回测' }}</span>
          </button>
        </div>
      </div>

      <!-- 回测结果 -->
      <div class="card" style="grid-column: 1 / -1;" v-if="result">
        <div class="card-header">
          <h3>回测结果</h3>
        </div>
        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-label">总收益率</div>
            <div class="stat-value" :class="result.totalReturn >= 0 ? 'positive' : 'negative'">
              {{ result.totalReturn.toFixed(2) }}%
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-label">年化收益</div>
            <div class="stat-value">{{ result.annualReturn.toFixed(2) }}%</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">最大回撤</div>
            <div class="stat-value negative">{{ result.maxDrawdown.toFixed(2) }}%</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">夏普比率</div>
            <div class="stat-value">{{ result.sharpeRatio.toFixed(2) }}</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">交易次数</div>
            <div class="stat-value">{{ result.tradeCount }}</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">胜率</div>
            <div class="stat-value">{{ result.winRate.toFixed(2) }}%</div>
          </div>
        </div>
      </div>

      <!-- 收益曲线图表 -->
      <div class="card" style="grid-column: 1 / -1;" v-if="result">
        <div class="card-header">
          <h3>收益曲线</h3>
        </div>
        <div id="backtestChart" class="chart-container" style="height: 350px;"></div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, nextTick } from 'vue'
import * as echarts from 'echarts'
import { backtestApi } from '@/api/backtest'

export default {
  name: 'Backtest',
  setup() {
    const loading = ref(false)
    const result = ref(null)
    let chart = null

    const form = ref({
      stockCode: '600519',
      initialCapital: 100000,
      startDate: '2023-01-01',
      endDate: '2023-12-31',
      strategy: 'ma'
    })

    const runBacktest = async () => {
      loading.value = true
      //后台数据
      try {
        const res = await backtestApi.runBacktest(form.value)
        if (res.code === 200) {
          result.value = res.data
          nextTick(() => initChart(res.data.equityCurve))
        }
      } catch (e) {
        // 模拟数据用于演示
        result.value = {
          totalReturn: 25.6,
          annualReturn: 28.3,
          maxDrawdown: -12.5,
          sharpeRatio: 1.85,
          tradeCount: 24,
          winRate: 62.5,
          equityCurve: generateMockCurve()
        }
        nextTick(() => initChart(result.value.equityCurve))
      } finally {
        loading.value = false
      }
    }

    const generateMockCurve = () => {
      const data = []
      let value = 100000
      const start = new Date(form.value.startDate)
      const end = new Date(form.value.endDate)
      for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 7)) {
        value *= (1 + (Math.random() - 0.45) * 0.03)
        data.push([d.toISOString().split('T')[0], Math.round(value)])
      }
      return data
    }

    const initChart = (data) => {
      const dom = document.getElementById('backtestChart')
      if (!dom) return
      if (chart) chart.dispose()
      chart = echarts.init(dom)
      chart.setOption({
        backgroundColor: 'transparent',
        //悬停
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: data.map(d => d[0]) },
        yAxis: { type: 'value' },
        series: [{
          type: 'line',
          data: data.map(d => d[1]),
          smooth: true,
          areaStyle: { color: 'rgba(0, 166, 255, 0.2)' },
          lineStyle: { color: '#00A6FF' }
        }]
      })
      window.addEventListener('resize', () => chart?.resize())
    }

    return { form, loading, result, runBacktest }
  }
}
</script>

<style scoped>
.backtest-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 1rem;
}
.form-row {
  display: flex;
  gap: 1.5rem;
  flex-wrap: wrap;
}
.form-group {
  flex: 1;
  min-width: 200px;
}
.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: var(--text-secondary);
}
.form-group input, .form-group select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background: var(--bg-secondary);
  color: var(--text-primary);
}
/*自动排列*/
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 1rem;
  padding: 1rem;
}
.stat-item {
  text-align: center;
  padding: 1rem;
  background: var(--bg-secondary);
  border-radius: 8px;
}
.stat-label {
  color: var(--text-secondary);
  font-size: 0.85rem;
  margin-bottom: 0.5rem;
}
.stat-value {
  font-size: 1.5rem;
  font-weight: 600;
}
.positive { color: #FF4D4F; }
.negative { color: #00C087; }
</style>