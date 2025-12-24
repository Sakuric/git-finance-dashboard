
<template>
  <div class="page-content active">
    <header class="header">
      <div class="header-left">
        <h2>智能投顾</h2>
        <p>AI驱动的个性化投资建议</p>
      </div>
      <div class="header-right">
        <button class="btn-primary" @click="generateAdvice" :disabled="loading">
          <i class="fas fa-magic"></i> {{ loading ? '生成中...' : '生成投资策略' }}
        </button>
      </div>
    </header>

    <div class="content-grid">
      <!-- 用户偏好显示板块 -->
      <div class="card preference-display-card" v-if="preferences.risk" style="grid-column: 1 / -1;">
        <div class="card-header">
          <h3><i class="fas fa-user-cog"></i> 当前投资偏好</h3>
        </div>
        <div class="preference-display">
          <div class="pref-item">
            <div class="pref-label"><i class="fas fa-shield-alt"></i> 风险承受能力</div>
            <div class="pref-value">{{ getRiskLevelLabel(preferences.risk) }}</div>
          </div>
          <div class="pref-item">
            <div class="pref-label"><i class="fas fa-clock"></i> 投资期限</div>
            <div class="pref-value">{{ preferences.term }}</div>
          </div>
          <div class="pref-item">
            <div class="pref-label"><i class="fas fa-industry"></i> 关注行业</div>
            <div class="pref-value">{{ preferences.industries.join('、') }}</div>
          </div>
        </div>
      </div>

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
          <div v-if="preferences.risk" class="preference-summary">
            <span class="summary-item"><i class="fas fa-shield-alt"></i> {{ getRiskLevelLabel(preferences.risk) }}</span>
            <span class="summary-item"><i class="fas fa-clock"></i> {{ preferences.term }}</span>
            <span class="summary-item"><i class="fas fa-industry"></i> {{ preferences.industries.length }}个行业</span>
          </div>
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
          <button class="btn-primary" style="margin-top: 1rem; align-self: flex-start;" @click="savePreferences" :disabled="saving">
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
          <div v-if="filteredAdvices.length === 0" class="empty-state">
            <i class="fas fa-robot"></i>
            <p>暂无建议，点击右上角按钮生成您的第一份 AI 投资报告</p>
          </div>
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
              <!-- 结构化内容展示 -->
              <div v-if="advice.parsed" class="advice-content-structured">
                <div v-if="advice.parsed.worldSituation" class="analysis-section">
                  <h4 class="section-title"><i class="fas fa-globe"></i> {{ advice.parsed.worldSituation.title }}</h4>
                  <p class="section-content">{{ advice.parsed.worldSituation.content }}</p>
                </div>
                <div v-if="advice.parsed.nationalPolicy" class="analysis-section">
                  <h4 class="section-title"><i class="fas fa-landmark"></i> {{ advice.parsed.nationalPolicy.title }}</h4>
                  <p class="section-content">{{ advice.parsed.nationalPolicy.content }}</p>
                </div>
                <div v-if="advice.parsed.recommendations && advice.parsed.recommendations.length" class="recommendations-section">
                  <h4 class="section-title"><i class="fas fa-lightbulb"></i> 核心建议标的</h4>
                  <div v-for="rec in advice.parsed.recommendations" :key="rec.code" class="recommendation-card">
                    <div class="rec-header">
                      <div class="rec-stock">
                        <span class="rec-name">{{ rec.name }}</span>
                        <span class="rec-code">{{ rec.code }}</span>
                      </div>
                      <span class="rec-action" :class="rec.suggestedAction?.toLowerCase()">
                        {{ rec.suggestedAction === 'BUY' ? '建议买入' : rec.suggestedAction === 'SELL' ? '建议卖出' : '建议关注' }}
                      </span>
                    </div>
                    <p class="rec-thesis">{{ rec.thesis }}</p>
                    <div class="rec-prices">
                      <div class="price-item">
                        <span class="price-label">买入区间</span>
                        <span class="price-value">¥{{ rec.entryPriceStart }}-{{ rec.entryPriceEnd }}</span>
                      </div>
                      <div class="price-item">
                        <span class="price-label">目标价</span>
                        <span class="price-value positive">¥{{ rec.takeProfitPrice }}</span>
                      </div>
                      <div class="price-item">
                        <span class="price-label">止损价</span>
                        <span class="price-value negative">¥{{ rec.stopLossPrice }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div class="advice-model">
                <i class="fas fa-robot"></i> 由 "{{ advice.model }}" 生成
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="card chart-card">
        <div class="card-header">
          <h3>配置均衡度分析</h3>
        </div>
        <div id="portfolioAnalysisChart" class="chart-container" style="height: 350px;"></div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { savePreference, getPreference } from '@/api/preference'
import { adviceApi } from '@/api/advice'
import { getPortfolioBalance } from '@/api/portfolio'
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
    const loading = ref(false)

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
      const uid = Number(authStore.userId || localStorage.getItem('userId') || 0)
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
      { value: 1, code: 'C1', label: '保守型', desc: '不接受本金损失', definition: '首要目标是保持资产流动性和本金安全。对风险极度敏感，不愿承受本金损失。', lossLevel: '极低（不接受本金损失）' },
      { value: 2, code: 'C2', label: '稳健型', desc: '可接受小幅波动', definition: '希望在保证本金安全的基础上获得高于通胀的收益。愿意承担较小的本金风险。', lossLevel: '较低（可接受小幅波动）' },
      { value: 3, code: 'C3', label: '平衡型', desc: '可接受一定幅度亏损', definition: '在风险和收益之间寻求平衡。有一定的风险识别能力和承受能力。', lossLevel: '中等（可接受一定幅度的本金亏损）' },
      { value: 4, code: 'C4', label: '积极型', desc: '可接受较大亏损', definition: '偏向于资产的增值，愿意承担较大的投资风险以换取较高的预期回报。', lossLevel: '较高（可接受较大的本金亏损）' },
      { value: 5, code: 'C5', label: '激进型', desc: '可接受本金全部亏损', definition: '追求资本的快速增值，风险承受能力极强。', lossLevel: '极高（可接受本金全部亏损甚至更多）' }
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

    const advices = ref([])

    const loadAdvices = async () => {
      const uid = ensureUserId()
      if (!uid) return
      try {
        const res = await adviceApi.getUserAdvice(uid)
        console.log('投顾建议返回:', res)
        // 处理不同的返回格式
        const adviceData = res?.data || res
        if (adviceData && adviceData.id) {
          let parsed = null
          try {
            const content = adviceData.content || adviceData.reasoning || '{}'
            parsed = typeof content === 'string' ? JSON.parse(content) : content
          } catch (e) {
            console.error('解析投顾建议JSON失败', e)
          }
          advices.value = [{
            id: adviceData.id,
            type: 'analysis',
            typeLabel: 'AI投资建议',
            time: adviceData.createdAt || new Date().toLocaleString(),
            parsed: parsed,
            model: 'AI智能投顾'
          }]
        } else if (res?.code === 404) {
          console.log('暂无投资建议')
          advices.value = []
        }
      } catch (e) {
        console.error('加载投顾建议失败', e)
        advices.value = []
      }
    }

    const generateAdvice = async () => {
      const uid = ensureUserId()
      if (!uid) return
      loading.value = true
      try {
        const res = await adviceApi.createAdvice(uid)
        console.log('生成建议返回:', res)
        if (res?.code === 200 || res?.data) {
          alert('投顾建议生成成功！')
          await loadAdvices()
        } else {
          alert('生成失败: ' + (res?.message || '未知错误'))
        }
      } catch (e) {
        console.error('生成建议错误:', e)
        const errMsg = e?.response?.data?.message || e?.message || '网络错误'
        alert('生成失败: ' + errMsg)
      } finally {
        loading.value = false
      }
    }

    const filteredAdvices = computed(() => {
      if (activeFilter.value === 'all') return advices.value
      return advices.value.filter(advice => advice.type === activeFilter.value)
    })

    const initPortfolioChart = async () => {
      const chartDom = document.getElementById('portfolioAnalysisChart')
      if (!chartDom) return
      if (portfolioChart.value) portfolioChart.value.dispose()
      portfolioChart.value = echarts.init(chartDom)

      const uid = ensureUserId()
      let currentData = [70, 70, 70, 70, 70]
      let suggestedData = [80, 80, 80, 80, 80]

      if (uid) {
        try {
          const res = await getPortfolioBalance(uid)
          if (res.data) {
            const current = res.data.current
            const suggested = res.data.suggested
            currentData = [
              current.growth || 70,
              current.value || 70,
              current.stability || 70,
              current.profitability || 70,
              current.liquidity || 70
            ]
            suggestedData = [
              suggested.growth || 80,
              suggested.value || 80,
              suggested.stability || 80,
              suggested.profitability || 80,
              suggested.liquidity || 80
            ]
          }
        } catch (e) {
          console.error('加载配置均衡度数据失败', e)
        }
      }

      const option = {
        backgroundColor: 'transparent',
        tooltip: { trigger: 'axis', backgroundColor: 'rgba(22, 27, 34, 0.9)', borderColor: '#30363D', textStyle: { color: '#C9D1D9' } },
        legend: { data: ['当前组合', '建议组合'], textStyle: { color: '#8B949E' }, top: 10 },
        radar: {
          indicator: [
            { name: '成长性', max: 100 }, { name: '价值性', max: 100 }, { name: '稳定性', max: 100 },
            { name: '收益性', max: 100 }, { name: '流动性', max: 100 }
          ],
          radius: '65%', splitNumber: 4,
          axisLine: { lineStyle: { color: '#8B949E' } },
          splitLine: { lineStyle: { color: '#30363D' } },
          splitArea: { show: false },
          axisName: { color: '#8B949E' }
        },
        series: [
          { name: '当前组合', type: 'radar', data: [{ value: currentData, name: '当前组合', areaStyle: { color: 'rgba(0, 175, 255, 0.4)' }, lineStyle: { color: '#00AFFF', width: 2 } }] },
          { name: '建议组合', type: 'radar', data: [{ value: suggestedData, name: '建议组合', areaStyle: { color: 'rgba(0, 184, 148, 0.4)' }, lineStyle: { color: '#00B894', width: 2 } }] }
        ]
      }
      portfolioChart.value.setOption(option)
      window.addEventListener('resize', () => portfolioChart.value?.resize())
    }

    const validateForm = () => {
      let valid = true
      errors.risk = ''; errors.term = ''; errors.industries = ''
      if (!preferences.value.risk) { errors.risk = '请选择风险承受能力'; valid = false }
      if (!preferences.value.term) { errors.term = '请选择投资期限'; valid = false }
      if (preferences.value.industries.length === 0) { errors.industries = '请至少选择一个关注行业'; valid = false }
      return valid
    }

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
          preferredIndustry: preferences.value.industries.join(',')
        })
        alert('偏好设置保存成功')
        await loadPreferences()
      } catch (e) {
        alert('保存失败: ' + (e?.response?.data?.message || e.message || '网络错误'))
      } finally {
        saving.value = false
      }
    }

    const loadPreferences = async () => {
      const uid = ensureUserId()
      if (!uid) return
      try {
        const res = await getPreference(uid)
        console.log('加载用户偏好:', res)
        const data = res.data || res
        if (data && data.riskToleranceLevel) {
          preferences.value.risk = data.riskToleranceLevel
          preferences.value.term = data.investmentHorizonPreset || ''
          preferences.value.industries = data.preferredIndustry ? data.preferredIndustry.split(',') : []
          console.log('偏好数据已加载:', preferences.value)
        }
      } catch (e) {
        console.error('加载偏好失败', e)
      }
    }

    const getRiskLevelLabel = (risk) => {
      const level = riskLevels.find(l => l.value === risk)
      return level ? level.label : '未设置'
    }

    onMounted(() => {
      loadPreferences()
      loadAdvices()
      nextTick(() => initPortfolioChart())
    })

    return {
      showRiskModal, preferences, errors, saving, loading, riskLevels, investmentTerms,
      industryCategories, filters, activeFilter, filteredAdvices, savePreferences, generateAdvice, getRiskLevelLabel
    }
  }
}
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding: 1.5rem;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
}
.header-left h2 { margin: 0 0 0.25rem 0; color: var(--text-primary); font-size: 1.5rem; }
.header-left p { margin: 0; color: var(--text-secondary); font-size: 0.9rem; }

.btn-primary {
  background: linear-gradient(135deg, var(--primary-accent), var(--secondary-accent));
  border: none; color: white; padding: 0.75rem 1.5rem; border-radius: 8px;
  font-weight: 600; cursor: pointer; transition: all 0.3s; display: flex; align-items: center; gap: 0.5rem;
}
.btn-primary:hover { transform: translateY(-2px); box-shadow: 0 4px 15px rgba(0, 166, 255, 0.4); }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; transform: none; }

.content-grid { display: grid; grid-template-columns: 1fr; gap: 1.5rem; }
.chart-card { max-width: 500px; }

.card {
  background: var(--card-bg); border: 1px solid var(--border-color); border-radius: 12px;
  padding: 1.5rem; backdrop-filter: blur(10px);
}
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
.card-header h3 { margin: 0; color: var(--text-primary); font-size: 1.1rem; }

.preference-summary { display: flex; gap: 1rem; align-items: center; }
.summary-item { display: flex; align-items: center; gap: 0.4rem; padding: 0.4rem 0.8rem; background: rgba(0, 166, 255, 0.1); border: 1px solid rgba(0, 166, 255, 0.3); border-radius: 6px; color: var(--primary-accent); font-size: 0.85rem; }
.summary-item i { font-size: 0.9rem; }

.preference-display { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 1rem; }
.pref-item { padding: 1rem; background: rgba(0, 166, 255, 0.05); border: 1px solid rgba(0, 166, 255, 0.2); border-radius: 8px; }
.pref-label { display: flex; align-items: center; gap: 0.5rem; color: var(--text-secondary); font-size: 0.85rem; margin-bottom: 0.5rem; }
.pref-label i { color: var(--primary-accent); }
.pref-value { color: var(--text-primary); font-size: 1rem; font-weight: 500; }

.preference-settings { display: flex; flex-direction: column; gap: 1.5rem; }
.preference-group h4 { color: var(--text-primary); margin-bottom: 1rem; font-size: 0.95rem; display: flex; align-items: center; gap: 0.5rem; }
.preference-options { display: flex; flex-wrap: wrap; gap: 0.75rem; }

.radio-option {
  display: flex; align-items: center; gap: 0.5rem; cursor: pointer;
  padding: 0.75rem 1rem; background: var(--sidebar-bg); border: 1px solid var(--border-color);
  border-radius: 8px; transition: all 0.2s;
}
.radio-option:hover { border-color: var(--primary-accent); }
.radio-option input { display: none; }
.radio-option input:checked + .radio-check { background: var(--primary-accent); border-color: var(--primary-accent); }
.radio-option input:checked + .radio-check::after { content: ''; width: 6px; height: 6px; background: white; border-radius: 50%; }
.radio-option input:checked ~ .radio-label { color: var(--primary-accent); }
.radio-check {
  width: 18px; height: 18px; border: 2px solid var(--border-color); border-radius: 50%;
  display: flex; align-items: center; justify-content: center; transition: all 0.2s;
}
.radio-label { color: var(--text-secondary); font-size: 0.9rem; }
.radio-label small { display: block; color: var(--text-tertiary); font-size: 0.75rem; margin-top: 2px; }

.info-icon { cursor: pointer; color: var(--primary-accent); font-size: 16px; }
.info-icon:hover { color: #58a6ff; }

.modal-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0, 0, 0, 0.7);
  display: flex; align-items: center; justify-content: center; z-index: 1000;
}
.modal-content {
  background: var(--sidebar-bg); border: 1px solid var(--border-color); border-radius: 12px;
  max-width: 700px; max-height: 80vh; overflow-y: auto; width: 90%;
}
.modal-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px 20px; border-bottom: 1px solid var(--border-color);
}
.modal-header h3 { margin: 0; color: var(--text-primary); }
.modal-close { background: none; border: none; color: var(--text-secondary); font-size: 24px; cursor: pointer; }
.modal-close:hover { color: var(--text-primary); }
.modal-body { padding: 20px; }

.risk-item { padding: 15px; border: 1px solid var(--border-color); border-radius: 8px; margin-bottom: 12px; }
.risk-item:last-child { margin-bottom: 0; }
.risk-title { color: var(--primary-accent); font-weight: 600; margin-bottom: 8px; }
.risk-desc { color: var(--text-primary); font-size: 14px; line-height: 1.6; margin-bottom: 8px; }
.risk-loss { color: var(--color-negative); font-size: 13px; }

.selected-count { color: var(--primary-accent); font-weight: normal; font-size: 12px; }

.industry-select-group {
  display: flex; flex-direction: column; gap: 16px; max-height: 400px; overflow-y: auto;
  padding: 12px; background: var(--bg-color); border: 1px solid var(--border-color); border-radius: 8px;
}
.industry-category { margin-bottom: 8px; }
.category-title { color: var(--text-primary); font-weight: 600; font-size: 13px; margin-bottom: 8px; }
.industry-options { display: flex; flex-wrap: wrap; gap: 8px; }

.checkbox-option {
  display: flex; align-items: center; gap: 6px; cursor: pointer; padding: 6px 10px;
  background: var(--sidebar-bg); border: 1px solid var(--border-color); border-radius: 6px; transition: all 0.2s;
}
.checkbox-option:hover { border-color: var(--primary-accent); }
.checkbox-option input { display: none; }
.checkbox-option input:checked + .checkbox-check { background: var(--primary-accent); border-color: var(--primary-accent); }
.checkbox-option input:checked + .checkbox-check::after { content: '✓'; color: #fff; font-size: 10px; }
.checkbox-option input:checked ~ .checkbox-label { color: var(--primary-accent); }
.checkbox-check {
  width: 16px; height: 16px; border: 2px solid var(--border-color); border-radius: 4px;
  display: flex; align-items: center; justify-content: center; transition: all 0.2s;
}
.checkbox-label { color: var(--text-secondary); font-size: 13px; }

.required { color: var(--color-negative); }
.error-msg { color: var(--color-negative); font-size: 12px; margin-top: 6px; display: block; }
.has-error .industry-select-group { border-color: var(--color-negative); }

.filter-tabs { display: flex; gap: 0.5rem; }
.filter-tabs button {
  background: transparent; border: 1px solid var(--border-color); color: var(--text-secondary);
  padding: 0.5rem 1rem; border-radius: 6px; cursor: pointer; transition: all 0.2s; font-size: 0.85rem;
}
.filter-tabs button:hover, .filter-tabs button.active {
  color: var(--primary-accent); background: rgba(0, 166, 255, 0.1); border-color: var(--primary-accent);
}

.empty-state { text-align: center; padding: 3rem; color: var(--text-secondary); }
.empty-state i { font-size: 3rem; margin-bottom: 1rem; color: var(--text-tertiary); }

.advice-content-structured { display: flex; flex-direction: column; gap: 20px; }
.analysis-section { padding: 16px; background: var(--bg-color); border: 1px solid var(--border-color); border-radius: 8px; }
.section-title { color: var(--primary-accent); font-size: 16px; font-weight: 600; margin: 0 0 12px 0; display: flex; align-items: center; gap: 8px; }
.section-content { color: var(--text-primary); font-size: 14px; line-height: 1.8; margin: 0; text-align: justify; }

.recommendations-section { padding: 16px; background: linear-gradient(135deg, var(--bg-color) 0%, var(--sidebar-bg) 100%); border: 1px solid var(--border-color); border-radius: 8px; }
.recommendation-card { background: var(--sidebar-bg); border: 1px solid var(--border-color); border-radius: 8px; padding: 16px; margin-top: 12px; }
.rec-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.rec-stock { display: flex; align-items: center; gap: 10px; }
.rec-name { color: var(--text-primary); font-size: 18px; font-weight: 600; }
.rec-code { color: var(--text-secondary); font-size: 14px; background: var(--bg-color); padding: 4px 8px; border-radius: 4px; }
.rec-action { padding: 6px 16px; border-radius: 6px; font-weight: 600; font-size: 14px; }
.rec-action.buy { background: rgba(46, 160, 67, 0.2); color: var(--color-positive); border: 1px solid var(--color-positive); }
.rec-action.sell { background: rgba(248, 81, 73, 0.2); color: var(--color-negative); border: 1px solid var(--color-negative); }
.rec-action.hold { background: rgba(139, 148, 158, 0.2); color: var(--text-secondary); border: 1px solid var(--text-secondary); }
.rec-thesis { color: var(--text-primary); font-size: 14px; line-height: 1.6; margin-bottom: 16px; padding: 12px; background: var(--bg-color); border-left: 3px solid var(--primary-accent); border-radius: 4px; }
.rec-prices { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 12px; }
.price-item { display: flex; flex-direction: column; gap: 6px; padding: 12px; background: var(--bg-color); border: 1px solid var(--border-color); border-radius: 6px; }
.price-label { color: var(--text-secondary); font-size: 12px; font-weight: 500; }
.price-value { color: var(--text-primary); font-size: 16px; font-weight: 600; }
.price-value.positive { color: var(--color-positive); }
.price-value.negative { color: var(--color-negative); }
.advice-model { display: flex; align-items: center; gap: 0.5rem; color: var(--text-secondary); font-size: 0.85rem; margin-top: 1rem; padding-top: 1rem; border-top: 1px solid var(--border-color); }
.advice-model i { color: var(--primary-accent); }
</style>
