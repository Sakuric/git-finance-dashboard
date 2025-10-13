<!-- frontend/src/views/StockList.vue -->
<template>
  <div class="stock-list-container">
    <el-container>
      <el-header>
        <div class="header-content">
          <h1>股票列表</h1>
          <div class="user-info">
            <el-dropdown>
              <span class="el-dropdown-link">
                {{ userInfo.username }}<i class="el-icon-arrow-down el-icon--right"></i>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="$router.push('/')">返回首页</el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      <el-main>
        <el-card>
          <template #header>
            <div class="card-header">
              <span>股票信息</span>
              <el-button type="primary" @click="dialogVisible = true">添加股票</el-button>
            </div>
          </template>

          <!-- 搜索区域 -->
          <div class="search-area">
            <el-form :inline="true" :model="searchForm" class="search-form">
              <el-form-item label="关键词">
                <el-input v-model="searchForm.keyword" placeholder="股票代码/名称" clearable></el-input>
              </el-form-item>
              <el-form-item label="行业">
                <el-select v-model="searchForm.industry" placeholder="选择行业" clearable>
                  <el-option label="银行" value="银行"></el-option>
                  <el-option label="房地产" value="房地产"></el-option>
                  <el-option label="科技" value="科技"></el-option>
                  <el-option label="医药" value="医药"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSearch">搜索</el-button>
                <el-button @click="resetSearch">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 表格区域 -->
          <el-table :data="stockList" border style="width: 100%">
            <el-table-column prop="stockCode" label="股票代码" width="100"></el-table-column>
            <el-table-column prop="stockName" label="股票名称" width="120"></el-table-column>
            <el-table-column prop="exchange" label="交易所" width="80"></el-table-column>
            <el-table-column prop="industry" label="行业" width="100"></el-table-column>
            <el-table-column prop="currentPrice" label="当前价格" width="100">
              <template #default="scope">
                <span :class="{ 'price-up': scope.row.currentPrice > scope.row.preClose, 'price-down': scope.row.currentPrice < scope.row.preClose }">
                  {{ scope.row.currentPrice }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="openPrice" label="开盘价" width="100"></el-table-column>
            <el-table-column prop="highPrice" label="最高价" width="100"></el-table-column>
            <el-table-column prop="lowPrice" label="最低价" width="100"></el-table-column>
            <el-table-column prop="preClose" label="昨收价" width="100"></el-table-column>
            <el-table-column prop="volume" label="成交量" width="120"></el-table-column>
            <el-table-column prop="amount" label="成交额" width="120"></el-table-column>
            <el-table-column prop="updateTime" label="更新时间" width="160"></el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="scope">
                <el-button size="small" @click="handleView(scope.row)">查看</el-button>
                <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
                <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页区域 -->
          <div class="pagination-area">
            <el-pagination
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                :current-page="pagination.currentPage"
                :page-sizes="[10, 20, 50, 100]"
                :page-size="pagination.pageSize"
                layout="total, sizes, prev, pager, next, jumper"
                :total="pagination.total">
            </el-pagination>
          </div>
        </el-card>
      </el-main>
    </el-container>

    <!-- 添加/编辑股票对话框 -->
    <el-dialog
        :title="dialogTitle"
        v-model="dialogVisible"
        width="50%"
        @close="resetForm">
      <el-form :model="stockForm" :rules="stockRules" ref="stockFormRef" label-width="100px">
        <el-form-item label="股票代码" prop="stockCode">
          <el-input v-model="stockForm.stockCode" :disabled="dialogType === 'edit'"></el-input>
        </el-form-item>
        <el-form-item label="股票名称" prop="stockName">
          <el-input v-model="stockForm.stockName"></el-input>
        </el-form-item>
        <el-form-item label="交易所" prop="exchange">
          <el-select v-model="stockForm.exchange" placeholder="请选择交易所">
            <el-option label="上海证券交易所" value="SSE"></el-option>
            <el-option label="深圳证券交易所" value="SZSE"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="所属行业" prop="industry">
          <el-input v-model="stockForm.industry"></el-input>
        </el-form-item>
        <el-form-item label="当前价格" prop="currentPrice">
          <el-input-number v-model="stockForm.currentPrice" :precision="2" :step="0.01"></el-input-number>
        </el-form-item>
        <el-form-item label="开盘价" prop="openPrice">
          <el-input-number v-model="stockForm.openPrice" :precision="2" :step="0.01"></el-input-number>
        </el-form-item>
        <el-form-item label="最高价" prop="highPrice">
          <el-input-number v-model="stockForm.highPrice" :precision="2" :step="0.01"></el-input-number>
        </el-form-item>
        <el-form-item label="最低价" prop="lowPrice">
          <el-input-number v-model="stockForm.lowPrice" :precision="2" :step="0.01"></el-input-number>
        </el-form-item>
        <el-form-item label="昨收价" prop="preClose">
          <el-input-number v-model="stockForm.preClose" :precision="2" :step="0.01"></el-input-number>
        </el-form-item>
        <el-form-item label="成交量" prop="volume">
          <el-input-number v-model="stockForm.volume"></el-input-number>
        </el-form-item>
        <el-form-item label="成交额" prop="amount">
          <el-input-number v-model="stockForm.amount" :precision="2" :step="0.01"></el-input-number>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="handleSave" :loading="saveLoading">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStockList, addStock, updateStock, deleteStock, queryStocks } from '@/api/stock'
import { logout } from '@/api/user'

export default {
  name: 'StockList',
  setup() {
    const store = useStore()
    const router = useRouter()

    const userInfo = computed(() => store.state.userInfo)

    // 数据列表
    const stockList = ref([])

    // 搜索表单
    const searchForm = reactive({
      keyword: '',
      industry: ''
    })

    // 分页
    const pagination = reactive({
      currentPage: 1,
      pageSize: 10,
      total: 0
    })

    // 对话框
    const dialogVisible = ref(false)
    const dialogType = ref('add') // add 或 edit
    const dialogTitle = computed(() => dialogType.value === 'add' ? '添加股票' : '编辑股票')

    // 股票表单
    const stockFormRef = ref(null)
    const stockForm = reactive({
      stockCode: '',
      stockName: '',
      exchange: '',
      industry: '',
      currentPrice: 0,
      openPrice: 0,
      highPrice: 0,
      lowPrice: 0,
      preClose: 0,
      volume: 0,
      amount: 0
    })

    // 表单验证规则
    const stockRules = {
      stockCode: [
        { required: true, message: '请输入股票代码', trigger: 'blur' }
      ],
      stockName: [
        { required: true, message: '请输入股票名称', trigger: 'blur' }
      ],
      exchange: [
        { required: true, message: '请选择交易所', trigger: 'change' }
      ],
      currentPrice: [
        { required: true, message: '请输入当前价格', trigger: 'blur' }
      ]
    }

    const saveLoading = ref(false)

    // 获取股票列表
    const fetchStockList = () => {
      getStockList()
          .then(response => {
            stockList.value = response.data
            pagination.total = response.data.length
          })
          .catch(error => {
            ElMessage.error(error.message || '获取股票列表失败')
          })
    }

    // 搜索股票
    const handleSearch = () => {
      queryStocks(searchForm)
          .then(response => {
            stockList.value = response.data
            pagination.total = response.data.length
          })
          .catch(error => {
            ElMessage.error(error.message || '搜索失败')
          })
    }

    // 重置搜索
    const resetSearch = () => {
      searchForm.keyword = ''
      searchForm.industry = ''
      fetchStockList()
    }

    // 查看股票详情
    const handleView = (row) => {
      router.push(`/stocks/${row.stockCode}`)
    }

    // 编辑股票
    const handleEdit = (row) => {
      dialogType.value = 'edit'
      dialogVisible.value = true
      // 使用JSON.parse(JSON.stringify())进行深拷贝
      const rowData = JSON.parse(JSON.stringify(row))
      Object.assign(stockForm, rowData)
    }

    // 删除股票
    const handleDelete = (row) => {
      ElMessageBox.confirm(`确定要删除股票 ${row.stockName} 吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteStock(row.stockCode)
            .then(() => {
              ElMessage.success('删除成功')
              fetchStockList()
            })
            .catch(error => {
              ElMessage.error(error.message || '删除失败')
            })
      }).catch(() => {})
    }

    // 保存股票
    const handleSave = () => {
      stockFormRef.value.validate(valid => {
        if (valid) {
          saveLoading.value = true
          if (dialogType.value === 'add') {
            addStock(stockForm)
                .then(() => {
                  ElMessage.success('添加成功')
                  dialogVisible.value = false
                  fetchStockList()
                })
                .catch(error => {
                  ElMessage.error(error.message || '添加失败')
                })
                .finally(() => {
                  saveLoading.value = false
                })
          } else {
            updateStock(stockForm)
                .then(() => {
                  ElMessage.success('更新成功')
                  dialogVisible.value = false
                  fetchStockList()
                })
                .catch(error => {
                  ElMessage.error(error.message || '更新失败')
                })
                .finally(() => {
                  saveLoading.value = false
                })
          }
        }
      })
    }

    // 重置表单
    const resetForm = () => {
      stockFormRef.value?.resetFields()
      Object.assign(stockForm, {
        stockCode: '',
        stockName: '',
        exchange: '',
        industry: '',
        currentPrice: 0,
        openPrice: 0,
        highPrice: 0,
        lowPrice: 0,
        preClose: 0,
        volume: 0,
        amount: 0
      })
    }

    // 分页大小改变
    const handleSizeChange = (val) => {
      pagination.pageSize = val
      // 这里可以重新获取数据
    }

    // 当前页改变
    const handleCurrentChange = (val) => {
      pagination.currentPage = val
      // 这里可以重新获取数据
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
      fetchStockList()
    })

    return {
      userInfo,
      stockList,
      searchForm,
      pagination,
      dialogVisible,
      dialogType,
      dialogTitle,
      stockFormRef,
      stockForm,
      stockRules,
      saveLoading,
      handleSearch,
      resetSearch,
      handleView,
      handleEdit,
      handleDelete,
      handleSave,
      resetForm,
      handleSizeChange,
      handleCurrentChange,
      handleLogout
    }
  }
}
</script>

<style scoped>
.stock-list-container {
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

.search-area {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
}

.pagination-area {
  margin-top: 20px;
  text-align: right;
}

.price-up {
  color: #F56C6C;
}

.price-down {
  color: #67C23A;
}
</style>