<!-- frontend/src/views/StockDetail.vue -->
<template>
  <div class="stock-detail-container">
    <el-container>
      <el-header>
        <div class="header-content">
          <h1>股票详情</h1>
          <div class="user-info">
            <el-dropdown>
              <span class="el-dropdown-link">
                {{ userInfo.username }}<i class="el-icon-arrow-down el-icon--right"></i>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="$router.push('/stocks')">返回列表</el-dropdown-item>
                  <el-dropdown-item @click="$router.push('/')">返回首页</el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      <el-main>
        <el-card v-if="stockInfo">
          <template #header>
            <div class="card-header">
              <span>{{ stockInfo.stockName }} ({{ stockInfo.stockCode }})</span>
              <el-button type="primary" @click="$router.push(`/stocks/${stockCode}/edit`)">编辑</el-button>
            </div>
          </template>

          <el-descriptions title="基本信息" :column="3" border>
            <el-descriptions-item label="股票代码">{{ stockInfo.stockCode }}</el-descriptions-item>
            <el-descriptions-item label="股票名称">{{ stockInfo.stockName }}</el-descriptions-item>
            <el-descriptions-item label="交易所">{{ stockInfo.exchange }}</el-descriptions-item>
            <el-descriptions-item label="所属行业">{{ stockInfo.industry }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ stockInfo.updateTime }}</el-descriptions-item>
          </el-descriptions>

          <el-descriptions title="价格信息" :column="3" border class="price-info">
            <el-descriptions-item label="当前价格">
              <span :class="{ 'price-up': stockInfo.currentPrice > stockInfo.preClose, 'price-down': stockInfo.currentPrice < stockInfo.preClose }">
                {{ stockInfo.currentPrice }}
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="开盘价">{{ stockInfo.openPrice }}</el-descriptions-item>
            <el-descriptions-item label="最高价">{{ stockInfo.highPrice }}</el-descriptions-item>
            <el-descriptions-item label="最低价">{{ stockInfo.lowPrice }}</el-descriptions-item>
            <el-descriptions-item label="昨收价">{{ stockInfo.preClose }}</el-descriptions-item>
            <el-descriptions-item label="涨跌幅">
              <span :class="{ 'price-up': stockInfo.currentPrice > stockInfo.preClose, 'price-down': stockInfo.currentPrice < stockInfo.preClose }">
                {{ calculateChangePercent() }}%
              </span>
            </el-descriptions-item>
          </el-descriptions>

          <el-descriptions title="交易信息" :column="3" border>
            <el-descriptions-item label="成交量">{{ stockInfo.volume }}</el-descriptions-item>
            <el-descriptions-item label="成交额">{{ stockInfo.amount }}</el-descriptions-item>
          </el-descriptions>

          <!-- 这里可以添加图表展示区域 -->
          <div class="chart-area">
            <h3>价格走势图</h3>
            <div class="chart-placeholder">
              <p>图表功能正在开发中...</p>
            </div>
          </div>
        </el-card>
        <el-card v-else>
          <div class="loading-container">
            <el-empty description="股票信息加载中..." />
          </div>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStockDetail } from '@/api/stock'
import { logout } from '@/api/user'

export default {
  name: 'StockDetail',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const store = useStore()

    const userInfo = computed(() => store.state.userInfo)
    const stockCode = computed(() => route.params.stockCode)
    const stockInfo = ref(null)

    // 获取股票详情
    const fetchStockDetail = () => {
      getStockDetail(stockCode.value)
          .then(response => {
            stockInfo.value = response.data
          })
          .catch(error => {
            ElMessage.error(error.message || '获取股票详情失败')
            router.push('/stocks')
          })
    }

    // 计算涨跌幅
    const calculateChangePercent = () => {
      if (!stockInfo.value || stockInfo.value.preClose === 0) {
        return 0
      }
      const change = stockInfo.value.currentPrice - stockInfo.value.preClose
      return ((change / stockInfo.value.preClose) * 100).toFixed(2)
    }

    // 退出登录
    const handleLogout = () => {
      ElMessageBox.confirm('确定要退出登录吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        logout()
            .then(() => {
              store.commit('clearUserInfo')
              ElMessage.success('退出成功')
              router.push('/login')
            })
            .catch(error => {
              ElMessage.error(error.message || '退出失败')
            })
      }).catch(() => {})
    }

    onMounted(() => {
      fetchStockDetail()
    })

    return {
      userInfo,
      stockCode,
      stockInfo,
      calculateChangePercent,
      handleLogout
    }
  }
}
</script>

<style scoped>
.stock-detail-container {
  height: 100vh;
}

.el-header {
  background-color: #409EFF;
  color: white;
  padding: 0 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
}

.el-dropdown-link {
  cursor: pointer;
  color: white;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price-info {
  margin-top: 20px;
}

.price-up {
  color: #F56C6C;
  font-weight: bold;
}

.price-down {
  color: #67C23A;
  font-weight: bold;
}

.chart-area {
  margin-top: 30px;
}

.chart-placeholder {
  height: 300px;
  background-color: #f5f7fa;
  display: flex;
  justify-content: center;
  align-items: center;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
}
</style>