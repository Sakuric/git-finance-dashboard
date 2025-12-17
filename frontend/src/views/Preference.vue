<template>
  <div class="page-content active">
    <header class="header">
      <div class="header-left">
        <h2>投资偏好设置</h2>
        <p>配置您的投资偏好和风险承受能力</p>
      </div>
      <div class="header-right">
        <button 
          class="auth-btn" 
          style="width: auto; padding: 0.6rem 1.2rem;"
          @click="handleSave"
          :disabled="loading"
        >
          <span class="btn-text">保存设置</span>
        </button>
      </div>
    </header>

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

    <div class="content-grid">
      <div class="card" style="grid-column: 1 / -1;">
        <div class="card-header">
          <h3>
            风险承受能力
            <span class="info-icon" @click="showRiskModal = true" title="点击查看详细说明">ⓘ</span>
          </h3>
        </div>
        <div class="preference-settings">
          <div class="preference-group">
            <div class="radio-options">
              <label class="radio-option" v-for="level in riskLevels" :key="level.value">
                <input
                  type="radio"
                  name="risk"
                  :value="level.value"
                  v-model="preference.riskToleranceLevel"
                >
                <span class="radio-check"></span>
                <span class="radio-label">
                  {{ level.label }}
                  <small>{{ level.desc }}</small>
                </span>
              </label>
            </div>
          </div>
        </div>
      </div>

      <div class="card" style="grid-column: 1 / -1;">
        <div class="card-header">
          <h3>投资期限</h3>
        </div>
        <div class="preference-settings">
          <div class="preference-group">
            <div class="radio-options">
              <label class="radio-option" v-for="term in investmentTerms" :key="term.value">
                <input
                  type="radio"
                  name="term"
                  :value="term.value"
                  v-model="preference.investmentHorizon"
                >
                <span class="radio-check"></span>
                <span class="radio-label">
                  {{ term.label }}
                  <small>{{ term.desc }}</small>
                </span>
              </label>
            </div>
          </div>
        </div>
      </div>

      <div class="card" style="grid-column: 1 / -1;">
        <div class="card-header">
          <h3>投资金额</h3>
        </div>
        <div class="preference-settings">
          <div class="preference-group">
            <div class="input-group">
              <label>投资金额（万元）</label>
              <input
                type="number"
                v-model="preference.capitalAmount"
                placeholder="请输入投资金额"
                min="0"
                step="0.1"
              >
            </div>
          </div>
        </div>
      </div>

      <div class="card" style="grid-column: 1 / -1;">
        <div class="card-header">
          <h3>关注行业</h3>
        </div>
        <div class="preference-settings">
          <div class="preference-group">
            <div class="input-group">
              <label>请输入您关注的行业板块（如：食品饮料、电子、银行等）</label>
              <input
                type="text"
                v-model="preference.preferredIndustry"
                placeholder="请输入关注的行业，多个行业用逗号分隔"
              >
              <div class="industry-hint">
                <details>
                  <summary>查看行业板块参考</summary>
                  <div class="industry-categories">
                    <div class="category">
                      <strong>A. 大消费板块</strong>：食品饮料、医药生物、汽车、家用电器、农林牧渔、纺织服饰、轻工制造、美容护理、商贸零售、社会服务
                    </div>
                    <div class="category">
                      <strong>B. TMT/大科技板块</strong>：电子、计算机、通信、传媒
                    </div>
                    <div class="category">
                      <strong>C. 周期与资源板块</strong>：石油石化、煤炭、有色金属、钢铁、基础化工
                    </div>
                    <div class="category">
                      <strong>D. 高端制造与新能源</strong>：电力设备、机械设备、国防军工
                    </div>
                    <div class="category">
                      <strong>E. 大金融与地产</strong>：银行、非银金融、房地产
                    </div>
                    <div class="category">
                      <strong>F. 基础设施与公用事业</strong>：公用事业、交通运输、建筑装饰、建筑材料、环保
                    </div>
                    <div class="category">
                      <strong>G. 其他</strong>：综合
                    </div>
                  </div>
                </details>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <h3>收益预期</h3>
        </div>
        <div class="preference-settings">
          <div class="preference-group">
            <div class="input-group">
              <label>最低预期收益率（%）</label>
              <input 
                type="number" 
                v-model="preference.minExpectedReturn"
                placeholder="请输入最低预期收益率"
                min="0"
                max="100"
                step="0.1"
              >
            </div>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <h3>风险控制</h3>
        </div>
        <div class="preference-settings">
          <div class="preference-group">
            <div class="input-group">
              <label>最大可接受亏损（%）</label>
              <input 
                type="number" 
                v-model="preference.maxAcceptableLoss"
                placeholder="请输入最大可接受亏损"
                min="0"
                max="100"
                step="0.1"
              >
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

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