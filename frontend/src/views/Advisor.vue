<template>
  <div class="page-content active">
    <!-- 顶部标题 -->
    <el-header class="header card-glass mb-6" height="auto">
      <div class="header-left">
        <h2>智能投顾中心</h2>
        <p>基于大模型的深度市场分析与个性化建议</p>
      </div>
      <div class="header-right">
        <el-button type="primary" :icon="MagicStick" size="large" :loading="loading" @click="generateAdvice">
          生成全新投资策略
        </el-button>
      </div>
    </el-header>

    <el-tabs v-model="activeMainTab" class="advisor-tabs">
      <!-- 偏好设置标签页 -->
      <el-tab-pane label="投资偏好配置" name="preferences">
        <el-row :gutter="20">
          <el-col :span="16">
            <el-card shadow="never" class="preference-card">
              <el-form :model="preferences" label-position="top">
                <el-form-item label="风险承受能力评估">
                  <el-radio-group v-model="preferences.risk" class="risk-radio-group">
                    <el-radio v-for="level in riskLevels" :key="level.value" :label="level.value" border>
                      <div class="risk-item-content">
                        <span class="label">{{ level.label }}</span>
                        <span class="desc">{{ level.desc }}</span>
                      </div>
                    </el-radio>
                  </el-radio-group>
                  <el-button link type="primary" @click="showRiskModal = true" class="mt-2">查看详细等级定义</el-button>
                </el-form-item>

                <el-form-item label="投资期限偏好">
                  <el-radio-group v-model="preferences.term">
                    <el-radio-button v-for="term in investmentTerms" :key="term.value" :label="term.value">
                      {{ term.label }}
                    </el-radio-button>
                  </el-radio-group>
                </el-form-item>

                <el-form-item label="关注行业板块 (多选)">
                  <el-collapse class="industry-collapse">
                    <el-collapse-item v-for="category in industryCategories" :key="category.name" :title="category.name">
                      <el-checkbox-group v-model="preferences.industries">
                        <el-checkbox v-for="item in category.items" :key="item" :label="item">{{ item }}</el-checkbox>
                      </el-checkbox-group>
                    </el-collapse-item>
                  </el-collapse>
                </el-form-item>

                <el-button type="primary" size="large" :loading="saving" @click="savePreferences" style="width: 200px">
                  保存并同步配置
                </el-button>
              </el-form>
            </el-card>
          </el-col>
          
          <el-col :span="8">
            <el-card shadow="never" class="chart-card-new">
              <template #header><span>配置均衡度分析</span></template>
              <div id="portfolioAnalysisChart" style="height: 400px;"></div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <!-- 投资建议标签页 -->
      <el-tab-pane label="AI 深度建议" name="advices">
        <div v-if="advices.length === 0" class="empty-state-container">
          <el-empty description="暂无建议，点击右上角按钮生成您的第一份 AI 投资报告" />
        </div>
        
        <div v-else class="advice-container">
          <div v-for="advice in advices" :key="advice.id" class="advice-card-item">
            <el-card shadow="hover">
              <template #header>
                <div class="flex-between">
                  <div class="flex-align-center">
                    <el-avatar :size="32" class="ai-avatar mr-2"><el-icon><Cpu /></el-icon></el-avatar>
                    <span class="font-bold">深度研报 - {{ advice.time }}</span>
                  </div>
                  <el-tag effect="plain" type="success">{{ advice.model }}</el-tag>
                </div>
              </template>

              <!-- 结构化内容 -->
              <div class="advice-sections">
                <el-row :gutter="20">
                  <el-col :span="12" v-if="advice.parsed?.worldSituation">
                    <div class="info-section">
                      <h4><el-icon><Global /></el-icon> {{ advice.parsed.worldSituation.title }}</h4>
                      <p>{{ advice.parsed.worldSituation.content }}</p>
                    </div>
                  </el-col>
                  <el-col :span="12" v-if="advice.parsed?.nationalPolicy">
                    <div class="info-section">
                      <h4><el-icon><OfficeBuilding /></el-icon> {{ advice.parsed.nationalPolicy.title }}</h4>
                      <p>{{ advice.parsed.nationalPolicy.content }}</p>
                    </div>
                  </el-col>
                </el-row>

                <el-divider />

                <div v-if="advice.parsed?.recommendations" class="rec-grid">
                  <h4 class="mb-4">💡 核心建议标的</h4>
                  <el-row :gutter="20">
                    <el-col :span="12" v-for="rec in advice.parsed.recommendations" :key="rec.code">
                      <div class="stock-rec-card">
                        <div class="stock-header">
                          <div>
                            <span class="name">{{ rec.name }}</span>
                            <span class="code">{{ rec.code }}</span>
                          </div>
                          <el-tag :type="rec.suggestedAction === 'BUY' ? 'success' : 'warning'">
                            {{ rec.suggestedAction === 'BUY' ? '建议买入' : '建议关注' }}
                          </el-tag>
                        </div>
                        <p class="thesis">{{ rec.thesis }}</p>
                        <el-descriptions :column="2" border size="small">
                          <el-descriptions-item label="买入区间">¥{{ rec.entryPriceStart }}-{{ rec.entryPriceEnd }}</el-descriptions-item>
                          <el-descriptions-item label="目标价"><span class="positive">¥{{ rec.takeProfitPrice }}</span></el-descriptions-item>
                          <el-descriptions-item label="止损价"><span class="negative">¥{{ rec.stopLossPrice }}</span></el-descriptions-item>
                        </el-descriptions>
                      </div>
                    </el-col>
                  </el-row>
                </div>
              </div>
            </el-card>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 风险说明对话框 -->
    <el-dialog v-model="showRiskModal" title="风险承受能力评级说明" width="600px">
      <el-table :data="riskLevels" border stripe>
        <el-table-column property="code" label="代码" width="60" />
        <el-table-column property="label" label="等级" width="100" />
        <el-table-column property="definition" label="定义描述" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { MagicStick, Cpu, OfficeBuilding, Connection as Global } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { savePreference, getPreference } from '@/api/preference'
import { adviceApi } from '@/api/advice'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const router = useRouter()
const activeMainTab = ref('preferences')
const loading = ref(false)
const saving = ref(false)
const showRiskModal = ref(false)

const preferences = ref({ risk: null, term: '', industries: [] })
const advices = ref([])

const industryCategories = [
  { name: '大消费板块', items: ['食品饮料', '医药生物', '汽车', '家用电器', '农林牧渔'] },
  { name: 'TMT/大科技', items: ['电子', '计算机', '通信', '传媒'] },
  { name: '周期与资源', items: ['石油石化', '煤炭', '有色金属', '钢铁'] },
  { name: '新能源与高端制造', items: ['电力设备', '机械设备', '国防军工'] },
  { name: '金融地产', items: ['银行', '非银金融', '房地产'] }
]

const riskLevels = [
  { value: 1, code: 'C1', label: '保守型', desc: '首要保本', definition: '对风险极度敏感，不愿承受本金损失。' },
  { value: 2, code: 'C2', label: '稳健型', desc: '小幅波动', definition: '愿意承担较小的本金风险，追求略高于通胀。' },
  { value: 3, code: 'C3', label: '平衡型', desc: '收益风险均衡', definition: '在风险和收益之间寻求平衡，可接受一定亏损。' },
  { value: 4, code: 'C4', label: '积极型', desc: '追求高回报', definition: '愿意承担较大风险以换取较高的预期回报。' },
  { value: 5, code: 'C5', label: '激进型', desc: '高收益高风险', definition: '承受能力极强，追求资本快速增值。' }
]

const investmentTerms = [
  { value: '短期1-6月', label: '1-6个月 (短期)' },
  { value: '短期6-12月', label: '6-12个月 (中期)' },
  { value: '长期', label: '1年以上 (长期)' }
]

const loadAdvices = async () => {
  const uid = authStore.userId || localStorage.getItem('userId')
  if (!uid) return
  try {
    const res = await adviceApi.getUserAdvice(uid)
    if (res.data) {
      const content = typeof res.data.content === 'string' ? JSON.parse(res.data.content) : res.data.content
      advices.value = [{
        id: res.data.id,
        time: new Date(res.data.createdAt).toLocaleString(),
        parsed: content,
        model: 'GPT-4o Finance Turbo'
      }]
    }
  } catch (e) { console.warn('建议加载失败', e) }
}

const generateAdvice = async () => {
  const uid = authStore.userId || localStorage.getItem('userId')
  loading.value = true
  try {
    await adviceApi.createAdvice(uid)
    ElMessage.success('投顾建议已生成！')
    activeMainTab.value = 'advices'
    loadAdvices()
  } catch (e) { ElMessage.error('生成失败') }
  finally { loading.value = false }
}

const savePreferences = async () => {
  const uid = authStore.userId || localStorage.getItem('userId')
  saving.value = true
  try {
    await savePreference({
      userId: uid,
      riskToleranceLevel: preferences.value.risk,
      investmentHorizonPreset: preferences.value.term,
      preferredIndustry: preferences.value.industries.join(',')
    })
    ElMessage.success('设置已保存')
  } finally { saving.value = false }
}

const initCharts = () => {
  const dom = document.getElementById('portfolioAnalysisChart')
  if (!dom) return
  const chart = echarts.init(dom)
  chart.setOption({
    backgroundColor: 'transparent',
    radar: {
      indicator: [
        { name: '成长性', max: 100 }, { name: '价值性', max: 100 }, { name: '稳定性', max: 100 },
        { name: '收益性', max: 100 }, { name: '流动性', max: 100 }
      ],
      axisLine: { lineStyle: { color: '#30363D' } },
      splitLine: { lineStyle: { color: '#30363D' } },
      splitArea: { show: false }
    },
    series: [{
      type: 'radar',
      data: [{
        value: [85, 70, 90, 75, 80],
        name: '当前配置',
        areaStyle: { color: 'rgba(0, 166, 255, 0.4)' },
        lineStyle: { color: '#00AFFF', width: 2 }
      }]
    }]
  })
}

onMounted(() => {
  getPreference(authStore.userId).then(res => {
    if (res.data) {
      preferences.value.risk = res.data.riskToleranceLevel
      preferences.value.term = res.data.investmentHorizonPreset
      preferences.value.industries = res.data.preferredIndustry ? res.data.preferredIndustry.split(',') : []
    }
  })
  loadAdvices()
  nextTick(() => initCharts())
})
</script>

<style scoped>
.advisor-tabs :deep(.el-tabs__header) { margin-bottom: 2rem; }
.advisor-tabs :deep(.el-tabs__nav-wrap::after) { display: none; }

.risk-radio-group { display: flex; flex-direction: column; gap: 10px; width: 100%; }
.risk-radio-group :deep(.el-radio) { margin-right: 0; width: 100%; height: auto; padding: 12px 20px; }
.risk-item-content { display: flex; flex-direction: column; }
.risk-item-content .label { font-weight: 700; font-size: 1rem; }
.risk-item-content .desc { font-size: 0.8rem; color: var(--text-tertiary); margin-top: 4px; }

.industry-collapse { border: 1px solid var(--border-color); border-radius: 8px; overflow: hidden; }

.advice-card-item { margin-bottom: 2rem; }
.info-section h4 { color: var(--primary-accent); display: flex; align-items: center; gap: 8px; margin-bottom: 12px; }
.info-section p { line-height: 1.8; color: var(--text-secondary); font-size: 0.95rem; text-align: justify; }

.stock-rec-card { background: rgba(0,0,0,0.2); border: 1px solid var(--border-color); border-radius: 12px; padding: 1.5rem; }
.stock-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
.stock-header .name { font-size: 1.2rem; font-weight: 700; margin-right: 10px; }
.stock-header .code { font-family: monospace; color: var(--text-tertiary); }
.thesis { font-size: 0.9rem; color: var(--text-secondary); line-height: 1.6; margin-bottom: 1.5rem; padding-left: 10px; border-left: 3px solid var(--primary-accent); }

.empty-state-container { padding: 5rem 0; }
</style>

<script>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { savePreference, getPreference } from '@/api/preference'
import { adviceApi } from '@/api/advice'
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

    const advices = ref([])
    const loading = ref(false)

    // 加载投顾建议列表
    const loadAdvices = async () => {
      const uid = ensureUserId()
      if (!uid) return
      loading.value = true
      try {
        const res = await adviceApi.getUserAdvice(uid)
        if (res && res.data) {
          let parsed = null
          try {
            const content = res.data.content || res.data.reasoning || '{}'
            parsed = typeof content === 'string' ? JSON.parse(content) : content
          } catch (e) {
            console.error('解析投顾建议JSON失败', e)
          }

          advices.value = [{
            id: res.data.id,
            type: 'analysis',
            typeLabel: 'AI投资建议',
            time: res.data.createdAt || new Date().toLocaleString(),
            parsed: parsed,
            model: 'AI智能投顾'
          }]
        }
      } catch (e) {
        console.error('加载投顾建议失败', e)
      } finally {
        loading.value = false
      }
    }

    const generateAdvice = async () => {
      const uid = ensureUserId()
      if (!uid) return
      loading.value = true
      try {
        await adviceApi.createAdvice(uid)
        alert('投顾建议生成成功！')
        await loadAdvices()
      } catch (e) {
        alert('生成失败: ' + (e?.response?.data?.message || e.message || '网络错误'))
      } finally {
        loading.value = false
      }
    }

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
      loadAdvices()
      nextTick(() => {
        initPortfolioChart()
      })
    })

    return {
      showRiskModal,
      preferences,
      errors,
      saving,
      loading,
      riskLevels,
      investmentTerms,
      industryCategories,
      filters,
      activeFilter,
      filteredAdvices,
      savePreferences,
      generateAdvice
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

/* 结构化投资建议样式 */
.advice-content-structured {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.analysis-section {
  padding: 16px;
  background: #0D1117;
  border: 1px solid #30363D;
  border-radius: 8px;
}

.section-title {
  color: #58A6FF;
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 12px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-content {
  color: #C9D1D9;
  font-size: 14px;
  line-height: 1.8;
  margin: 0;
  text-align: justify;
}

.recommendations-section {
  padding: 16px;
  background: linear-gradient(135deg, #0D1117 0%, #161B22 100%);
  border: 1px solid #30363D;
  border-radius: 8px;
}

.recommendation-card {
  background: #161B22;
  border: 1px solid #30363D;
  border-radius: 8px;
  padding: 16px;
  margin-top: 12px;
}

.rec-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.rec-stock {
  display: flex;
  align-items: center;
  gap: 10px;
}

.rec-name {
  color: #C9D1D9;
  font-size: 18px;
  font-weight: 600;
}

.rec-code {
  color: #8B949E;
  font-size: 14px;
  background: #0D1117;
  padding: 4px 8px;
  border-radius: 4px;
}

.rec-action {
  padding: 6px 16px;
  border-radius: 6px;
  font-weight: 600;
  font-size: 14px;
}

.rec-action.buy {
  background: rgba(46, 160, 67, 0.2);
  color: #3FB950;
  border: 1px solid #3FB950;
}

.rec-action.sell {
  background: rgba(248, 81, 73, 0.2);
  color: #F85149;
  border: 1px solid #F85149;
}

.rec-action.hold {
  background: rgba(139, 148, 158, 0.2);
  color: #8B949E;
  border: 1px solid #8B949E;
}

.rec-thesis {
  color: #C9D1D9;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 16px;
  padding: 12px;
  background: #0D1117;
  border-left: 3px solid #58A6FF;
  border-radius: 4px;
}

.rec-prices {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 12px;
}

.price-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 12px;
  background: #0D1117;
  border: 1px solid #30363D;
  border-radius: 6px;
}

.price-label {
  color: #8B949E;
  font-size: 12px;
  font-weight: 500;
}

.price-value {
  color: #C9D1D9;
  font-size: 16px;
  font-weight: 600;
}

.price-value.positive {
  color: #3FB950;
}

.price-value.negative {
  color: #F85149;
}
</style>
