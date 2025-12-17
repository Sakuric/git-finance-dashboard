import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getPreference, savePreference, deletePreference, hasPreference } from '@/api/preference'

export const usePreferenceStore = defineStore('preference', () => {
  // 状态
  const preference = ref({
    riskToleranceLevel: 2, // 默认稳健型
    investmentHorizon: '中期',
    capitalAmount: '',
    preferredAssetClasses: '',
    preferredIndustry: '',
    minExpectedReturn: '',
    maxAcceptableLoss: ''
  })
  
  const loading = ref(false)
  const error = ref(null)
  const hasSetPreference = ref(false)

  // 计算属性
  const riskToleranceDesc = computed(() => {
    const riskMap = {
      1: '保守型',
      2: '稳健型', 
      3: '平衡型',
      4: '积极型',
      5: '激进型'
    }
    return riskMap[preference.value.riskToleranceLevel] || '未知'
  })

  const investmentHorizonDesc = computed(() => {
    const horizonMap = {
      '短期': '3个月内',
      '中期': '3-12个月',
      '长期': '1年以上'
    }
    return horizonMap[preference.value.investmentHorizon] || preference.value.investmentHorizon
  })

  //行业格式转换
  const selectedIndustries = computed(() => {
    return preference.value.preferredIndustry ? preference.value.preferredIndustry.split(',') : []
  })

  //检查表填没填完
  const isPreferenceComplete = computed(() => {
    return preference.value.riskToleranceLevel && 
           preference.value.investmentHorizon
  })

  // 方法
  //读取问卷
  const loadPreference = async () => {
    try {
      loading.value = true
      error.value = null
      
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
        hasSetPreference.value = true
      } else {
        hasSetPreference.value = false
      }
    } catch (err) {
      error.value = err.message
      console.error('加载投资偏好失败:', err)
      hasSetPreference.value = false
    } finally {
      loading.value = false
    }
  }

  //提交/修改问卷
  const saveUserPreference = async (preferenceData) => {
    try {
      loading.value = true
      error.value = null
      
      const response = await savePreference(preferenceData)
      if (response.code === 200) {
        // 更新本地状态
        preference.value = { ...preference.value, ...preferenceData }
        hasSetPreference.value = true
        return true
      } else {
        throw new Error(response.message || '保存投资偏好失败')
      }
    } catch (err) {
      error.value = err.message
      console.error('保存投资偏好失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  //删除/重置问卷
  const deleteUserPreference = async () => {
    try {
      loading.value = true
      error.value = null
      
      const response = await deletePreference()
      if (response.code === 200) {
        // 重置本地状态
        preference.value = {
          riskToleranceLevel: 2,
          investmentHorizon: '中期',
          capitalAmount: '',
          preferredAssetClasses: '',
          preferredIndustry: '',
          minExpectedReturn: '',
          maxAcceptableLoss: ''
        }
        hasSetPreference.value = false
        return true
      } else {
        throw new Error(response.message || '删除投资偏好失败')
      }
    } catch (err) {
      error.value = err.message
      console.error('删除投资偏好失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const checkUserPreference = async () => {
    try {
      const response = await hasPreference()
      if (response.code === 200) {
        hasSetPreference.value = response.data
        return response.data
      }
      return false
    } catch (err) {
      console.error('检查投资偏好失败:', err)
      return false
    }
  }

  const updatePreferenceField = (field, value) => {
    preference.value[field] = value
  }

  //转成字符串
  const updateSelectedIndustries = (industries) => {
    preference.value.preferredIndustry = industries.join(',')
  }

  const resetPreference = () => {
    preference.value = {
      riskToleranceLevel: 2,
      investmentHorizon: '中期',
      capitalAmount: '',
      preferredAssetClasses: '',
      preferredIndustry: '',
      minExpectedReturn: '',
      maxAcceptableLoss: ''
    }
    hasSetPreference.value = false
    error.value = null
  }

  const clearError = () => {
    error.value = null
  }

  return {
    // 状态
    preference,
    loading,
    error,
    hasSetPreference,
    
    // 计算属性
    riskToleranceDesc,
    investmentHorizonDesc,
    selectedIndustries,
    isPreferenceComplete,
    
    // 方法
    loadPreference,
    saveUserPreference,
    deleteUserPreference,
    checkUserPreference,
    updatePreferenceField,
    updateSelectedIndustries,
    resetPreference,
    clearError
  }
})