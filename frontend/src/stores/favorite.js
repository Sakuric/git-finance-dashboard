import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getFavorites, addFavorite, removeFavorite, updateSortOrder, updateRemark } from '@/api/favorite'

export const useFavoriteStore = defineStore('favorite', () => {
  // 状态
  const favorites = ref([])
  const loading = ref(false)
  const error = ref(null)

  // 计算属性
  const favoriteCount = computed(() => favorites.value.length)
  
  const favoriteStockCodes = computed(() => favorites.value.map(fav => fav.stockCode))
  
  const sortedFavorites = computed(() => {
    return [...favorites.value].sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
  })

  // 方法
  // 加载自选股
  const loadFavorites = async () => {
    try {
      loading.value = true
      error.value = null
      const response = await getFavorites()
      if (response.code === 200) {
        favorites.value = response.data || []
      } else {
        throw new Error(response.message || '加载自选股失败')
      }
    } catch (err) {
      error.value = err.message
      console.error('加载自选股失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // 添加自选股
  const addStockToFavorites = async (stockCode, remark = '') => {
    try {
      loading.value = true
      error.value = null
      
      const response = await addFavorite({ stockCode, remark })
      if (response.code === 200) {
        // 重新加载列表
        await loadFavorites()
        return true
      } else {
        throw new Error(response.message || '添加自选股失败')
      }
    } catch (err) {
      error.value = err.message
      console.error('添加自选股失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // 移除自选股
  const removeStockFromFavorites = async (stockCode) => {
    try {
      loading.value = true
      error.value = null
      
      const response = await removeFavorite(stockCode)
      if (response.code === 200) {
        // 从本地列表中移除
        favorites.value = favorites.value.filter(fav => fav.stockCode !== stockCode)
        return true
      } else {
        throw new Error(response.message || '删除自选股失败')
      }
    } catch (err) {
      error.value = err.message
      console.error('删除自选股失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const updateFavoriteSort = async (stockCode, sortOrder) => {
    try {
      const response = await updateSortOrder({ stockCode, sortOrder })
      if (response.code === 200) {
        // 更新本地数据
        const favorite = favorites.value.find(fav => fav.stockCode === stockCode)
        if (favorite) {
          favorite.sortOrder = sortOrder
        }
        return true
      } else {
        throw new Error(response.message || '更新排序失败')
      }
    } catch (err) {
      error.value = err.message
      console.error('更新排序失败:', err)
      throw err
    }
  }

  const updateFavoriteRemark = async (stockCode, remark) => {
    try {
      const response = await updateRemark({ stockCode, remark })
      if (response.code === 200) {
        // 更新本地数据
        const favorite = favorites.value.find(fav => fav.stockCode === stockCode)
        if (favorite) {
          favorite.remark = remark
        }
        return true
      } else {
        throw new Error(response.message || '更新备注失败')
      }
    } catch (err) {
      error.value = err.message
      console.error('更新备注失败:', err)
      throw err
    }
  }

  const isFavorite = (stockCode) => {
    return favoriteStockCodes.value.includes(stockCode)
  }

  const clearError = () => {
    error.value = null
  }

  const clearFavorites = () => {
    favorites.value = []
    error.value = null
  }

  return {
    // 状态
    favorites,
    loading,
    error,
    
    // 计算属性
    favoriteCount,
    favoriteStockCodes,
    sortedFavorites,
    
    // 方法
    loadFavorites,
    addStockToFavorites,
    removeStockFromFavorites,
    updateFavoriteSort,
    updateFavoriteRemark,
    isFavorite,
    clearError,
    clearFavorites
  }
})