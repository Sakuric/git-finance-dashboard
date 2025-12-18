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
      <!-- 风险说明弹窗 -->
      <div class="modal-overlay" v-if="showRiskModal" @click="showRiskModal = false">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h3>风险承受能力说明</h3>
            <button class="modal-close" @click="showRiskModal = false">×</button>
          </div>
          <div class="modal-body">
            <div class="risk-item" v-for="level in riskLevels" :key="level.value">
              <div class="risk-title">{{ level.code }} - {{ level.label }}</div>
              <div class="risk-desc">{{ level.definition }}</div>
              <div class="risk-loss">可接受亏损：{{ level.lossLevel }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="card" style="grid-column: 1 / -1;">
        <div class="card-header">
          <h3>投资偏好设置</h3>
        </div>
        <div class="preference-settings">
          <div class="preference-group" :class="{ 'has-error': errors.risk }">
            <h4>风险承受能力 <span class="required">*</span> <span class="info-icon" @click="showRiskModal = true" title="点击查看风险等级说明">ⓘ</span></h4>
            <div class="preference-options">
              <label class="radio-option" v-for="level in riskLevels" :key="level.value">
                <input type="radio" name="risk" :value="level.value" v-model="preferences.risk">
                <span class="radio-check"></span>
                <span class="radio-label">{{ level.label }} <small>{{ level.desc }}</small></span>
              </label>
            </div>
            <span v-if="errors.risk" class="error-msg">{{ errors.risk }}</span>
          </div>
          <div class="preference-group" :class="{ 'has-error': errors.term }">
            <h4>投资期限 <span class="required">*</span></h4>
            <div class="preference-options">
              <label class="radio-option" v-for="term in investmentTerms" :key="term.value">
                <input type="radio" name="term" :value="term.value" v-model="preferences.term">
                <span class="radio-check"></span>
                <span class="radio-label">{{ term.label }} <small>{{ term.desc }}</small></span>
              </label>
            </div>
            <span v-if="errors.term" class="error-msg">{{ errors.term }}</span>
          </div>
          <div class="preference-group" :class="{ 'has-error': errors.industries }">
            <h4>关注行业 <span class="required">*</span> <small class="selected-count">(已选 {{ preferences.industries.length }} 个，至少选1个)</small></h4>
            <div class="industry-select-group">
              <div v-for="category in industryCategories" :key="category.name" class="industry-category">
                <div class="category-title">{{ category.name }}</div>
                <div class="industry-options">
                  <label v-for="industry in category.items" :key="industry" class="checkbox-option">
                    <input type="checkbox" :value="industry" v-model="preferences.industries">
                    <span class="checkbox-check"></span>
                    <span class="checkbox-label">{{ industry }}</span>
                  </label>
                </div>
              </div>
            </div>
            <span v-if="errors.industries" class="error-msg">{{ errors.industries }}</span>
          </div>
          <button class="auth-btn" style="margin-top: 1rem; align-self: flex-start;" @click="savePreferences" :disabled="saving">
            <span class="btn-text">{{ saving ? '保存中...' : '保存偏好设置' }}</span>
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
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { savePreference, getPreference } from '@/api/preference'
import { useAuthStore } from '@/stores/auth'

export default {
  name: 'Advisor',
  setup() {
    const authStore = useAuthStore()
    const router = useRouter()
    const portfolioChart = ref(null)
    const activeFilter = ref('all')
    const showRiskModal = ref(false)
    const saving = ref(false)

    const preferences = ref({
      risk: null,
      term: '',
      industries: []
    })

    const errors = reactive({
      risk: '',
      term: '',
      industries: ''
    })

    const ensureUserId = () => {
      const uid = Number(authStore.userId || 0)
      if (!uid) {
        alert('登录信息已失效，请重新登录')
        router.push('/login')
        return null
      }
      return uid
    }

    const industryCategories = [
      { name: 'A. 大消费板块', items: ['食品饮料', '医药生物', '汽车', '家用电器', '农林牧渔', '纺织服饰', '轻工制造', '美容护理', '商贸零售', '社会服务'] },
      { name: 'B. TMT/大科技板块', items: ['电子', '计算机', '通信', '传媒'] },
      { name: 'C. 周期与资源板块', items: ['石油石化', '煤炭', '有色金属', '钢铁', '基础化工'] },
      { name: 'D. 高端制造与新能源', items: ['电力设备', '机械设备', '国防军工'] },
      { name: 'E. 大金融与地产', items: ['银行', '非银金融', '房地产'] },
      { name: 'F. 基础设施与公用事业', items: ['公用事业', '交通运输', '建筑装饰', '建筑材料', '环保'] },
      { name: 'G. 其他', items: ['综合'] }
    ]

    const riskLevels = [
      { value: 1, code: 'C1', label: '保守型', desc: '不接受本金损失', definition: '首要目标是保持资产流动性和本金安全。对风险极度敏感，不愿承受本金损失。通常投资于银行存款、国债、货币基金等低风险产品。', lossLevel: '极低（不接受本金损失）' },
      { value: 2, code: 'C2', label: '稳健型', desc: '可接受小幅波动', definition: '希望在保证本金安全的基础上获得高于通胀的收益。愿意承担较小的本金风险。投资组合以债券为主，辅以少量股票或混合型基金。', lossLevel: '较低（可接受小幅波动）' },
      { value: 3, code: 'C3', label: '平衡型', desc: '可接受一定幅度亏损', definition: '在风险和收益之间寻求平衡。有一定的风险识别能力和承受能力。资金在股票、债券、现金之间均衡配置，追求中长期稳健增值。', lossLevel: '中等（可接受一定幅度的本金亏损）' },
      { value: 4, code: 'C4', label: '积极型', desc: '可接受较大亏损', definition: '偏向于资产的增值，愿意承担较大的投资风险以换取较高的预期回报。主要投资于股票、偏股型基金等权益类资产。', lossLevel: '较高（可接受较大的本金亏损）' },
      { value: 5, code: 'C5', label: '激进型', desc: '可接受本金全部亏损', definition: '追求资本的快速增值，风险承受能力极强。投资于股票、期货、期权、外汇等高波动产品，甚至使用杠杆。', lossLevel: '极高（可接受本金全部亏损甚至更多）' }
    ]

    const investmentTerms = [
      { value: '短期1-6月', label: '短期（1-6个月）', desc: '1-6个月' },
      { value: '短期6-12月', label: '短期（6-12个月）', desc: '6-12个月' },
      { value: '长期', label: '长期', desc: '1年以上' }
    ]

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

    // 表单校验
    const validateForm = () => {
      let valid = true
      errors.risk = ''
      errors.term = ''
      errors.industries = ''

      if (!preferences.value.risk) {
        errors.risk = '请选择风险承受能力'
        valid = false
      }
      if (!preferences.value.term) {
        errors.term = '请选择投资期限'
        valid = false
      }
      if (preferences.value.industries.length === 0) {
        errors.industries = '请至少选择一个关注行业'
        valid = false
      }
      return valid
    }

    // 保存偏好设置
    const savePreferences = async () => {
      if (!validateForm()) return
      const uid = ensureUserId()
      if (!uid) return

      saving.value = true
      try {
        await savePreference({
          userId: uid,
          riskToleranceLevel: preferences.value.risk,
          investmentHorizonType: 'preset',
          investmentHorizonPreset: preferences.value.term,
          investmentHorizonDisplay: preferences.value.term,
          preferredIndustry: preferences.value.industries.join(',')
        })
        alert('偏好设置保存成功')
      } catch (e) {
        const msg = e?.response?.data?.message || e.message || '网络错误'
        alert('保存失败: ' + msg)
      } finally {
        saving.value = false
      }
    }

    // 加载用户偏好
    const loadPreferences = async () => {
      const uid = ensureUserId()
      if (!uid) return
      try {
        const res = await getPreference(uid)
        if (res.data) {
          preferences.value.risk = res.data.riskToleranceLevel || null
          preferences.value.term = res.data.investmentHorizonPreset || ''
          preferences.value.industries = res.data.preferredIndustry ? res.data.preferredIndustry.split(',') : []
        }
      } catch (e) {
        console.error('加载偏好失败', e)
      }
    }

    onMounted(() => {
      loadPreferences()
      nextTick(() => {
        initPortfolioChart()
      })
    })

    return {
      showRiskModal,
      preferences,
      errors,
      saving,
      riskLevels,
      investmentTerms,
      industryCategories,
      filters,
      activeFilter,
      filteredAdvices,
      savePreferences
    }
  }
}
</script>

<style scoped>
.info-icon {
  cursor: pointer;
  color: #00AFFF;
  font-size: 16px;
  margin-left: 8px;
}
.info-icon:hover { color: #58a6ff; }

.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal-content {
  background: #161B22;
  border: 1px solid #30363D;
  border-radius: 12px;
  max-width: 700px;
  max-height: 80vh;
  overflow-y: auto;
  width: 90%;
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #30363D;
}
.modal-header h3 { margin: 0; color: #C9D1D9; }
.modal-close {
  background: none;
  border: none;
  color: #8B949E;
  font-size: 24px;
  cursor: pointer;
}
.modal-close:hover { color: #C9D1D9; }
.modal-body { padding: 20px; }

.risk-item {
  padding: 15px;
  border: 1px solid #30363D;
  border-radius: 8px;
  margin-bottom: 12px;
}
.risk-item:last-child { margin-bottom: 0; }
.risk-title { color: #00AFFF; font-weight: 600; margin-bottom: 8px; }
.risk-desc { color: #C9D1D9; font-size: 14px; line-height: 1.6; margin-bottom: 8px; }
.risk-loss { color: #F85149; font-size: 13px; }

.radio-label small {
  display: block;
  color: #8B949E;
  font-size: 12px;
  margin-top: 2px;
}

.selected-count { color: #00AFFF; font-weight: normal; font-size: 12px; }

.industry-select-group {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-height: 400px;
  overflow-y: auto;
  padding: 12px;
  background: #0D1117;
  border: 1px solid #30363D;
  border-radius: 8px;
}

.industry-category { margin-bottom: 8px; }
.category-title { color: #C9D1D9; font-weight: 600; font-size: 13px; margin-bottom: 8px; }

.industry-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.checkbox-option {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 6px 10px;
  background: #161B22;
  border: 1px solid #30363D;
  border-radius: 6px;
  transition: all 0.2s;
}
.checkbox-option:hover { border-color: #00AFFF; }
.checkbox-option input { display: none; }
.checkbox-option input:checked + .checkbox-check { background: #00AFFF; border-color: #00AFFF; }
.checkbox-option input:checked + .checkbox-check::after { content: '✓'; color: #fff; font-size: 10px; }
.checkbox-option input:checked ~ .checkbox-label { color: #00AFFF; }

.checkbox-check {
  width: 16px;
  height: 16px;
  border: 2px solid #30363D;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.checkbox-label { color: #8B949E; font-size: 13px; }

.required { color: #F85149; }
.error-msg { color: #F85149; font-size: 12px; margin-top: 6px; display: block; }
.has-error .industry-select-group { border-color: #F85149; }
.auth-btn:disabled { opacity: 0.6; cursor: not-allowed; }
</style>
