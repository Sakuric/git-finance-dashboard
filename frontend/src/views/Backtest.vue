<template>
  <div class="page-content active">
    <!-- 头部 -->
    <el-header class="header card-glass mb-6" height="auto">
      <div class="header-left">
        <h2>量化回测中心</h2>
        <p>多因子、多维度验证投资策略的稳健性</p>
      </div>
      <div class="header-right">
        <el-button type="primary" :icon="VideoPlay" size="large" :loading="loading" @click="runBacktest">
          {{ loading ? '深度回测中...' : '运行全量回测' }}
        </el-button>
      </div>
    </el-header>

    <el-row :gutter="20">
      <!-- 左侧：参数配置 -->
      <el-col :span="8">
        <el-card shadow="never" class="h-full">
          <template #header><span><el-icon class="mr-1"><Tools /></el-icon>回测参数配置</span></template>
          <el-form :model="form" label-position="top">
            <el-form-item label="关联投资建议">
              <el-select v-model="form.adviceId" placeholder="请选择投资建议" class="w-full" filterable>
                <el-option
                  v-for="advice in adviceList"
                  :key="advice.id"
                  :label="`#${advice.id} - ${advice.title || '投资建议'}`"
                  :value="advice.id"
                >
                  <div style="display: flex; justify-content: space-between;">
                    <span>{{ advice.title || '投资建议' }}</span>
                    <span style="color: var(--el-text-color-secondary); font-size: 12px;">
                      {{ advice.createdAt ? new Date(advice.createdAt).toLocaleDateString() : '' }}
                    </span>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
            
            <el-form-item label="初始模拟资金 (CNY)">
              <el-input-number v-model="form.initialCapital" :step="10000" class="w-full" controls-position="right" />
            </el-form-item>

            <el-form-item label="回测时间跨度">
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                class="w-full"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>

            <el-form-item label="回测策略">
              <el-select v-model="form.strategy" class="w-full" @change="onStrategyChange">
                <el-option label="系统默认策略" value="default" />
                <el-option label="自定义策略" value="custom" />
              </el-select>
              <div class="strategy-tip" v-if="form.strategy === 'default'">
                <el-icon><InfoFilled /></el-icon>
                <span>止盈: +10% | 止损: -5% | 仓位: 10%</span>
              </div>
            </el-form-item>

            <template v-if="form.strategy === 'custom'">
              <el-form-item label="止盈阈值 (%)">
                <el-input-number v-model="form.takeProfitPct" :min="5" :max="50" :step="5" class="w-full" controls-position="right" />
              </el-form-item>
              <el-form-item label="止损阈值 (%)">
                <el-input-number v-model="form.stopLossPct" :min="3" :max="20" :step="1" class="w-full" controls-position="right" />
              </el-form-item>
            </template>

            <el-form-item label="验证模式">
              <el-select v-model="form.splitMethod" class="w-full">
                <el-option label="训练-测试分割 (推荐)" value="train-test" />
                <el-option label="滚动窗口验证" value="walk-forward" />
              </el-select>
            </el-form-item>

            <el-form-item label="样本内数据比例 (Train Ratio)">
              <el-slider v-model="form.trainRatio" :min="0.5" :max="0.9" :step="0.05" show-stops />
              <div class="text-right text-secondary text-xs">当前: {{ (form.trainRatio * 100).toFixed(0) }}%</div>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右侧：回测结果展示 -->
      <el-col :span="16">
        <div v-if="!result && !loading" class="empty-backtest">
          <el-empty description="待运行：请配置左侧参数并启动回测" />
        </div>

        <el-space direction="vertical" fill style="width: 100%" :size="20" v-if="result">
          <!-- 过拟合警示 -->
          <el-alert
            v-if="result.overfitting"
            :title="result.overfitting.warning"
            :type="result.overfitting.isOverfitted ? 'warning' : 'success'"
            :closable="false"
            show-icon
          >
            <div class="mt-2 flex gap-6">
              <span>性能衰减: <b :class="result.overfitting.performanceDegradation > 30 ? 'negative' : ''">{{ result.overfitting.performanceDegradation.toFixed(2) }}%</b></span>
              <span>稳定性评分: <b class="positive">{{ result.overfitting.stabilityScore.toFixed(2) }}</b></span>
            </div>
          </el-alert>

          <!-- 指数看板 -->
          <el-row :gutter="20">
            <el-col :span="12">
              <el-card shadow="never" class="result-stats-card">
                <template #header><div class="flex-align-center"><el-icon class="mr-1"><Histogram /></el-icon>训练期 (样本内)</div></template>
                <el-row>
                  <el-col :span="8" v-for="stat in trainStats" :key="stat.label" class="mb-4">
                    <el-statistic :title="stat.label" :value="stat.value" :precision="stat.precision" :suffix="stat.suffix" :value-style="{ color: stat.color }" />
                  </el-col>
                </el-row>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card shadow="never" class="result-stats-card highlight-card">
                <template #header><div class="flex-align-center"><el-icon class="mr-1"><Aim /></el-icon>测试期 (样本外)</div></template>
                <el-row>
                  <el-col :span="8" v-for="stat in testStats" :key="stat.label" class="mb-4">
                    <el-statistic :title="stat.label" :value="stat.value" :precision="stat.precision" :suffix="stat.suffix" :value-style="{ color: stat.color }" />
                  </el-col>
                </el-row>
              </el-card>
            </el-col>
          </el-row>

          <!-- 收益曲线 -->
          <el-card shadow="never">
            <template #header><span><el-icon class="mr-1"><TrendCharts /></el-icon>策略净值曲线</span></template>
            <div id="backtestChart" style="height: 380px;"></div>
          </el-card>

          <!-- AI分析结果 -->
          <el-card shadow="never" v-if="result && result.aiAnalysis" class="ai-analysis-card">
            <template #header><span><el-icon class="mr-1"><ChatDotRound /></el-icon>AI智能分析</span></template>
            <div class="ai-analysis-content" v-html="result.aiAnalysis"></div>
          </el-card>

          <!-- 策略说明 -->
          <el-card shadow="never" class="strategy-desc-card">
            <template #header><span>策略逻辑概览</span></template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="入场时机">回测首日以 10% 初始头寸建仓</el-descriptions-item>
              <el-descriptions-item label="动态止盈">价格上涨超过 20% 时触发全局平仓</el-descriptions-item>
              <el-descriptions-item label="硬性止损">价格下跌超过 10% 时触发全局平仓</el-descriptions-item>
              <el-descriptions-item label="验证机制">按 {{ (form.trainRatio * 100).toFixed(0) }}% 比例实施样本内/外交叉验证</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-space>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue'
import * as echarts from 'echarts'
import { VideoPlay, Tools, Histogram, Aim, TrendCharts, InfoFilled, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { backtestApi } from '@/api/backtest'
import { adviceApi } from '@/api/advice'

const loading = ref(false)
const result = ref(null)
const dateRange = ref(['2023-01-01', '2024-12-31'])
const adviceList = ref([])
let chart = null

const form = ref({
  adviceId: null,
  initialCapital: 100000,
  splitMethod: 'train-test',
  trainRatio: 0.7,
  strategy: 'default',
  takeProfitPct: 10,
  stopLossPct: 5
})

const onStrategyChange = () => {
  if (form.value.strategy === 'default') {
    form.value.takeProfitPct = 10
    form.value.stopLossPct = 5
  }
}

onMounted(async () => {
  const userId = localStorage.getItem('userId') || 1
  try {
    const res = await adviceApi.getAdviceList(userId)
    if (res.code === 200) {
      adviceList.value = res.data || []
    }
  } catch (e) {
    console.error('获取投资建议列表失败', e)
  }
})

const trainStats = computed(() => {
  if (!result.value?.trainPeriod) return []
  const d = result.value.trainPeriod
  return [
    { label: '总收益', value: d.totalReturn, suffix: '%', precision: 2, color: d.totalReturn >= 0 ? '#39D353' : '#F85149' },
    { label: '年化收益', value: d.annualReturn, suffix: '%', precision: 2 },
    { label: '最大回撤', value: d.maxDrawdown, suffix: '%', precision: 2, color: '#F85149' },
    { label: '夏普比率', value: d.sharpeRatio, precision: 2, color: d.sharpeRatio > 1 ? '#39D353' : '' },
    { label: '交易胜率', value: d.winRate, suffix: '%', precision: 2 },
    { label: '交易次数', value: d.tradeCount, precision: 0 }
  ]
})

const testStats = computed(() => {
  if (!result.value?.testPeriod) return []
  const d = result.value.testPeriod
  return [
    { label: '样本外收益', value: d.totalReturn, suffix: '%', precision: 2, color: d.totalReturn >= 0 ? '#39D353' : '#F85149' },
    { label: '年化收益', value: d.annualReturn, suffix: '%', precision: 2 },
    { label: '最大回撤', value: d.maxDrawdown, suffix: '%', precision: 2, color: '#F85149' },
    { label: '夏普比率', value: d.sharpeRatio, precision: 2 },
    { label: '样本外胜率', value: d.winRate, suffix: '%', precision: 2 },
    { label: '总交易数', value: d.tradeCount, precision: 0 }
  ]
})

const runBacktest = async () => {
  if (!form.value.adviceId) return ElMessage.warning('请选择需要回测的投资建议')
  
  loading.value = true
  try {
    const payload = { ...form.value, startDate: dateRange.value[0], endDate: dateRange.value[1] }
    const res = await backtestApi.runBacktest(payload)
    if (res.code === 200) {
      result.value = res.data
      await nextTick()
      if (res.data.equityCurve) initChart(res.data.equityCurve)
      ElMessage.success('回测全流程计算完成')
    }
  } catch (e) { ElMessage.error('回测引擎异常') }
  finally { loading.value = false }
}

const initChart = (data) => {
  const dom = document.getElementById('backtestChart')
  if (!dom) return
  if (chart) chart.dispose()
  chart = echarts.init(dom)
  const dates = data.map(p => p.date)
  const values = data.map(p => p.value)
  
  chart.setOption({
    grid: { left: '3%', right: '4%', bottom: '8%', top: '5%', containLabel: true },
    tooltip: { trigger: 'axis', axisPointer: { type: 'line', lineStyle: { color: '#00AFFF', width: 1 } } },
    xAxis: { type: 'category', data: dates, axisLine: { lineStyle: { color: '#30363D' } } },
    yAxis: { type: 'value', scale: true, splitLine: { lineStyle: { color: '#30363D', type: 'dashed' } } },
    dataZoom: [{ type: 'inside' }, { type: 'slider', bottom: 10, height: 20 }],
    series: [{
      type: 'line',
      data: values,
      smooth: true,
      showSymbol: false,
      lineStyle: { width: 3, color: '#00AFFF' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(0, 175, 255, 0.3)' },
          { offset: 1, color: 'transparent' }
        ])
      }
    }]
  })
}
</script>

<style scoped>
.strategy-tip {
  margin-top: 8px;
  padding: 8px 12px;
  background: rgba(0, 166, 255, 0.1);
  border-radius: 6px;
  font-size: 12px;
  color: #00A6FF;
  display: flex;
  align-items: center;
  gap: 6px;
}
.ai-analysis-card {
  background: linear-gradient(135deg, rgba(57, 211, 83, 0.05), transparent);
  border-color: rgba(57, 211, 83, 0.3) !important;
}
.ai-analysis-content {
  line-height: 1.8;
  color: var(--text-primary);
}
.ai-analysis-content :deep(p) {
  margin: 0.5rem 0;
}
.ai-analysis-content :deep(strong) {
  color: #39D353;
}
.empty-backtest { padding: 10rem 0; background: rgba(255,255,255,0.01); border: 1px dashed var(--border-color); border-radius: 12px; }
.result-stats-card { background: rgba(255,255,255,0.02); }
.highlight-card { border-color: rgba(0, 166, 255, 0.3) !important; background: linear-gradient(135deg, rgba(0, 166, 255, 0.05), transparent); }
.strategy-desc-card :deep(.el-descriptions__label) { background: rgba(0,0,0,0.2) !important; color: var(--text-tertiary); }
.w-full { width: 100%; }
.mt-2 { margin-top: 0.5rem; }
.gap-6 { gap: 1.5rem; }
</style>

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
      adviceId: null,
      initialCapital: 100000,
      startDate: '2023-01-01',
      endDate: '2024-12-31',
      splitMethod: 'train-test',
      trainRatio: 0.7
    })

    const runBacktest = async () => {
      if (!form.value.adviceId) {
        alert('请输入投资建议ID')
        return
      }

      loading.value = true
      try {
        const res = await backtestApi.runBacktest(form.value)
        if (res.code === 200) {
          result.value = res.data
          await nextTick()
          if (res.data.equityCurve && res.data.equityCurve.length > 0) {
            const chartData = res.data.equityCurve.map(point => [point.date, point.value])
            initChart(chartData)
          }
        } else {
          alert('回测失败: ' + (res.message || '未知错误'))
        }
      } catch (e) {
        alert('回测失败: ' + (e.message || '网络错误'))
      } finally {
        loading.value = false
      }
    }

    const initChart = (data) => {
      const dom = document.getElementById('backtestChart')
      if (!dom) return
      if (chart) chart.dispose()
      chart = echarts.init(dom)
      chart.setOption({
        backgroundColor: 'transparent',
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
.stat-hint {
  font-size: 0.75rem;
  color: #666;
  margin-top: 0.25rem;
}
.positive { color: #00C087; }
.negative { color: #FF4D4F; }
.overfitting-alert {
  padding: 1rem;
  background: var(--bg-secondary);
  border-radius: 8px;
  border-left: 4px solid #00C087;
}
.overfitting-alert.warning {
  border-left-color: #FF4D4F;
  background: rgba(255, 77, 79, 0.1);
}
.alert-message {
  font-size: 1rem;
  margin-bottom: 1rem;
  color: var(--text-primary);
  font-weight: 500;
}
.card-subtitle {
  font-size: 0.875rem;
  color: #999;
  margin-top: 0.25rem;
}
.strategy-info {
  padding: 1rem;
  background: rgba(0, 166, 255, 0.05);
  border-radius: 8px;
  line-height: 1.8;
}
.strategy-info p {
  margin: 0.5rem 0;
  color: #e0e0e0;
}
</style>
