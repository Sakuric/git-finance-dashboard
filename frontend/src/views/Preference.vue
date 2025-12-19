<template>
  <div class="page-content active">
    <!-- 头部：保存控制 -->
    <el-header class="header card-glass mb-6" height="auto">
      <div class="header-left">
        <h2>个人投资画像</h2>
        <p>定制您的风险承受能力与市场关注偏好</p>
      </div>
      <div class="header-right">
        <el-button type="primary" :icon="Check" size="large" :loading="loading" @click="handleSave">
          保存并更新画像
        </el-button>
      </div>
    </el-header>

    <el-row :gutter="20">
      <el-col :span="24">
        <el-card shadow="never" class="preference-main-card">
          <el-tabs tab-position="left" class="vertical-tabs">
            <!-- 风险评测 -->
            <el-tab-pane>
              <template #label>
                <div class="tab-label"><el-icon><Warning /></el-icon> 风险画像</div>
              </template>
              
              <div class="pane-content">
                <h3 class="mb-4">风险承受能力评估</h3>
                <el-alert title="请根据您的真实资金状况和心理预期调整，这将直接影响 AI 投顾的建议逻辑。" type="info" show-icon :closable="false" class="mb-6" />
                
                <div class="risk-slider-box">
                  <div class="risk-level-display">
                    <span class="level-tag" :class="'level-' + preference.riskToleranceLevel">
                      {{ riskLevels.find(l => l.value === preference.riskToleranceLevel)?.label }}
                    </span>
                  </div>
                  <el-slider
                    v-model="preference.riskToleranceLevel"
                    :min="1"
                    :max="5"
                    :step="1"
                    show-stops
                    :marks="riskMarks"
                    class="custom-slider"
                  />
                </div>

                <div class="risk-detail-card mt-8" v-if="currentRiskDetail">
                  <h4>{{ currentRiskDetail.code }} - 特征描述</h4>
                  <p class="text-secondary">{{ currentRiskDetail.definition }}</p>
                  <div class="loss-indicator mt-4">
                    <span class="label">亏损容忍度:</span>
                    <el-tag type="danger" effect="plain">{{ currentRiskDetail.lossLevel }}</el-tag>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <!-- 行业偏好 -->
            <el-tab-pane>
              <template #label>
                <div class="tab-label"><el-icon><PieChart /></el-icon> 行业偏好</div>
              </template>
              <div class="pane-content">
                <h3 class="mb-4">关注的行业板块</h3>
                <p class="text-tertiary mb-6">选择您最关注的 1-5 个行业，AI 将优先为您挖掘相关机会。</p>
                
                <el-form-item label="核心关注点">
                  <el-select
                    v-model="selectedIndustryList"
                    multiple
                    collapse-tags
                    placeholder="从行业库中选择"
                    size="large"
                    class="w-full"
                  >
                    <el-option-group v-for="cat in industryGroups" :key="cat.name" :label="cat.name">
                      <el-option v-for="item in cat.items" :key="item" :label="item" :value="item" />
                    </el-option-group>
                  </el-select>
                </el-form-item>

                <div class="industry-tags-display mt-4">
                  <el-tag 
                    v-for="tag in selectedIndustryList" 
                    :key="tag" 
                    closable 
                    class="mr-2 mb-2"
                    @close="removeIndustry(tag)"
                  >
                    {{ tag }}
                  </el-tag>
                </div>
              </div>
            </el-tab-pane>

            <!-- 资产规划 -->
            <el-tab-pane>
              <template #label>
                <div class="tab-label"><el-icon><Money /></el-icon> 资产规划</div>
              </template>
              <div class="pane-content">
                <h3 class="mb-4">资金与期限规划</h3>
                
                <el-row :gutter="40">
                  <el-col :span="12">
                    <el-form-item label="预期投资金额 (万元)">
                      <el-input-number v-model="preference.capitalAmount" :precision="2" :step="1" class="w-full" controls-position="right" />
                    </el-form-item>
                    <el-form-item label="计划投资期限">
                      <el-select v-model="preference.investmentHorizon" class="w-full">
                        <el-option v-for="t in investmentTerms" :key="t.value" :label="t.label" :value="t.value" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="最低预期收益率 (%)">
                      <el-input-number v-model="preference.minExpectedReturn" :min="0" :max="100" class="w-full" />
                    </el-form-item>
                    <el-form-item label="止损红线 (%)">
                      <el-input-number v-model="preference.maxAcceptableLoss" :min="0" :max="100" class="w-full" />
                    </el-form-item>
                  </el-col>
                </el-row>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Check, Warning, PieChart, Money } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getPreference, savePreference } from '@/api/preference'

const loading = ref(false)
const preference = ref({
  riskToleranceLevel: 2,
  investmentHorizon: '短期1-6月',
  capitalAmount: 0,
  preferredIndustry: '',
  minExpectedReturn: 10,
  maxAcceptableLoss: 15
})

const selectedIndustryList = computed({
  get: () => preference.value.preferredIndustry ? preference.value.preferredIndustry.split(',') : [],
  set: (val) => preference.value.preferredIndustry = val.join(',')
})

const riskLevels = [
  { value: 1, code: 'C1', label: '保守型', definition: '首要目标是保持资产流动性和本金安全。不愿承受任何本金损失。', lossLevel: '< 2%' },
  { value: 2, code: 'C2', label: '稳健型', definition: '希望在保证本金安全的基础上获得略高于通胀的收益。', lossLevel: '2% - 5%' },
  { value: 3, code: 'C3', label: '平衡型', definition: '追求中长期稳健增值，有一定的风险承受能力，资产均衡配置。', lossLevel: '5% - 15%' },
  { value: 4, code: 'C4', label: '积极型', definition: '愿意承担较大波动以换取超额回报，主要投资权益类资产。', lossLevel: '15% - 30%' },
  { value: 5, code: 'C5', label: '激进型', definition: '追求资本快速增值，风险承受力极强，可能使用金融杠杆。', lossLevel: '> 30%' }
]

const riskMarks = { 1: 'C1', 2: 'C2', 3: 'C3', 4: 'C4', 5: 'C5' }

const investmentTerms = [
  { value: '短期1-6月', label: '短期 (1-6个月)' },
  { value: '短期6-12月', label: '中期 (6-12个月)' },
  { value: '长期', label: '长期 (1年以上)' }
]

const industryGroups = [
  { name: '大消费', items: ['食品饮料', '医药生物', '汽车', '家用电器'] },
  { name: '硬科技', items: ['半导体', '人工智能', '商业航天', '低空经济'] },
  { name: '新能源', items: ['光伏', '锂电', '风能', '储能'] },
  { name: '大金融', items: ['银行', '保险', '证券', '多元金融'] }
]

const currentRiskDetail = computed(() => riskLevels.find(l => l.value === preference.value.riskToleranceLevel))

const loadData = async () => {
  try {
    const res = await getPreference()
    if (res.data) preference.value = { ...preference.value, ...res.data }
  } catch (e) { console.error('加载失败', e) }
}

const handleSave = async () => {
  loading.value = true
  try {
    const res = await savePreference(preference.value)
    if (res.code === 200) ElMessage.success('投资画像更新成功')
  } finally { loading.value = false }
}

const removeIndustry = (tag) => {
  selectedIndustryList.value = selectedIndustryList.value.filter(t => t !== tag)
}

onMounted(() => loadData())
</script>

<style scoped>
.preference-main-card { min-height: 600px; padding: 0 !important; }
.vertical-tabs :deep(.el-tabs__header) { width: 220px; border-right: 1px solid var(--border-color); }
.vertical-tabs :deep(.el-tabs__item) { height: 60px; line-height: 60px; font-weight: 600; padding: 0 30px; }
.vertical-tabs :deep(.el-tabs__nav-wrap::after) { display: none; }

.tab-label { display: flex; align-items: center; gap: 10px; }
.pane-content { padding: 2rem 4rem; max-width: 900px; }

.risk-slider-box { padding: 3rem 1rem; background: rgba(0,0,0,0.2); border-radius: 12px; position: relative; }
.risk-level-display { text-align: center; margin-bottom: 3rem; }
.level-tag { font-size: 2.5rem; font-weight: 800; padding: 0.5rem 2rem; border-radius: 12px; }
.level-1 { color: #58A6FF; border: 2px solid #58A6FF; }
.level-2 { color: #39D353; border: 2px solid #39D353; }
.level-3 { color: #D29922; border: 2px solid #D29922; }
.level-4 { color: #F85149; border: 2px solid #F85149; }
.level-5 { color: #FF4400; border: 2px solid #FF4400; }

.risk-detail-card { background: rgba(255,255,255,0.02); border: 1px solid var(--border-color); border-radius: 12px; padding: 1.5rem; }
.risk-detail-card h4 { margin-bottom: 1rem; color: var(--primary-accent); }
.risk-detail-card p { line-height: 1.6; }

.w-full { width: 100%; }
.mt-8 { margin-top: 2rem; }
</style>

<script>
import { ref, onMounted, computed } from 'vue'
import { getPreference, savePreference } from '@/api/preference'

export default {
  name: 'Preference',
  setup() {
    const loading = ref(false)
    const showRiskModal = ref(false)
    const preference = ref({
      riskToleranceLevel: 2, // 默认稳健型
      investmentHorizon: '中期',
      capitalAmount: '',
      preferredAssetClasses: '',
      preferredIndustry: '',
      minExpectedReturn: '',
      maxAcceptableLoss: ''
    })

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

    // 加载投资偏好
    const loadPreference = async () => {
      try {
        loading.value = true
        const response = await getPreference()
        if (response.code === 200 && response.data) {
          preference.value = {
            riskToleranceLevel: response.data.riskToleranceLevel || 2,
            investmentHorizon: response.data.investmentHorizon || '中期',
            capitalAmount: response.data.capitalAmount || '',
            preferredAssetClasses: response.data.preferredAssetClasses || '',
            preferredIndustry: response.data.preferredIndustry || '',
            minExpectedReturn: response.data.minExpectedReturn || '',
            maxAcceptableLoss: response.data.maxAcceptableLoss || ''
          }
          
        }
      } catch (error) {
        console.error('加载投资偏好失败:', error)
        alert('加载投资偏好失败')
      } finally {
        loading.value = false
      }
    }

    // 保存投资偏好
    const handleSave = async () => {
      try {
        loading.value = true
        
        // 验证数据
        if (!preference.value.riskToleranceLevel) {
          alert('请选择风险承受能力')
          return
        }
        
        if (!preference.value.investmentHorizon) {
          alert('请选择投资期限')
          return
        }

        const response = await savePreference(preference.value)
        if (response.code === 200) {
          alert('保存成功')
        } else {
          alert(response.message || '保存失败')
        }
      } catch (error) {
        console.error('保存投资偏好失败:', error)
        alert('保存失败')
      } finally {
        loading.value = false
      }
    }

    onMounted(() => {
      loadPreference()
    })

    return {
      loading,
      showRiskModal,
      preference,
      riskLevels,
      investmentTerms,
      handleSave
    }
  }
}
</script>

<style scoped>
.preference-settings {
  padding: 20px;
}

.preference-group {
  margin-bottom: 20px;
}

.preference-group:last-child {
  margin-bottom: 0;
}

.radio-options {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.radio-option {
  display: flex;
  align-items: flex-start;
  cursor: pointer;
  padding: 15px;
  border: 2px solid #30363D;
  border-radius: 8px;
  transition: all 0.3s ease;
  flex: 1;
  min-width: 200px;
}

.radio-option:hover {
  border-color: #00AFFF;
  background-color: rgba(0, 175, 255, 0.1);
}

.radio-option input[type="radio"] {
  display: none;
}

.radio-option input[type="radio"]:checked + .radio-check + .radio-label {
  color: #00AFFF;
}

.radio-option input[type="radio"]:checked + .radio-check {
  background-color: #00AFFF;
  border-color: #00AFFF;
}

.radio-check {
  width: 20px;
  height: 20px;
  border: 2px solid #8B949E;
  border-radius: 50%;
  margin-right: 10px;
  position: relative;
  transition: all 0.3s ease;
}

.radio-check::after {
  content: '';
  width: 10px;
  height: 10px;
  background-color: white;
  border-radius: 50%;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.radio-option input[type="radio"]:checked + .radio-check::after {
  opacity: 1;
}

.radio-label {
  display: flex;
  flex-direction: column;
  color: #C9D1D9;
  font-weight: 500;
}

.radio-label small {
  color: #8B949E;
  font-size: 12px;
  margin-top: 5px;
  font-weight: normal;
}

.info-icon {
  cursor: pointer;
  color: #00AFFF;
  font-size: 16px;
  margin-left: 8px;
  vertical-align: middle;
}

.info-icon:hover {
  color: #58a6ff;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
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

.modal-header h3 {
  margin: 0;
  color: #C9D1D9;
}

.modal-close {
  background: none;
  border: none;
  color: #8B949E;
  font-size: 24px;
  cursor: pointer;
}

.modal-close:hover {
  color: #C9D1D9;
}

.modal-body {
  padding: 20px;
}

.risk-item {
  padding: 15px;
  border: 1px solid #30363D;
  border-radius: 8px;
  margin-bottom: 12px;
}

.risk-item:last-child {
  margin-bottom: 0;
}

.risk-title {
  color: #00AFFF;
  font-weight: 600;
  margin-bottom: 8px;
}

.risk-desc {
  color: #C9D1D9;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 8px;
}

.risk-loss {
  color: #F85149;
  font-size: 13px;
}

.industry-hint {
  margin-top: 12px;
}

.industry-hint details {
  background: #0D1117;
  border: 1px solid #30363D;
  border-radius: 6px;
  padding: 10px;
}

.industry-hint summary {
  cursor: pointer;
  color: #00AFFF;
  font-size: 14px;
}

.industry-categories {
  margin-top: 12px;
}

.category {
  color: #8B949E;
  font-size: 13px;
  line-height: 1.8;
  margin-bottom: 8px;
}

.category strong {
  color: #C9D1D9;
}

.input-group {
  margin-bottom: 15px;
}

.input-group label {
  display: block;
  margin-bottom: 8px;
  color: #C9D1D9;
  font-weight: 500;
}

.input-group input {
  width: 100%;
  padding: 12px;
  background-color: #0D1117;
  border: 2px solid #30363D;
  border-radius: 6px;
  color: #C9D1D9;
  font-size: 14px;
  transition: border-color 0.3s ease;
}

.input-group input:focus {
  outline: none;
  border-color: #00AFFF;
}

.input-group input::placeholder {
  color: #8B949E;
}
</style>