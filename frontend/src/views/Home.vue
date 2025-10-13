<!-- frontend/src/views/Home.vue -->
<template>
  <div class="home-container">
    <el-container>
      <el-header>
        <div class="header-content">
          <h1>金融数据平台</h1>
          <div class="user-info">
            <el-dropdown>
              <span class="el-dropdown-link">
                {{ userInfo.username }}<i class="el-icon-arrow-down el-icon--right"></i>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="$router.push('/stocks')">股票列表</el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      <el-main>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-card>
              <template #header>
                <h2>欢迎使用金融数据平台</h2>
              </template>
              <div class="welcome-content">
                <p>这是一个金融数据展示和分析平台，提供实时股票数据、技术分析和AI预测功能。</p>
                <p>目前平台正在开发中，敬请期待更多功能。</p>
                <el-button type="primary" @click="$router.push('/stocks')">查看股票列表</el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-main>
    </el-container>
  </div>
</template>

<script>
import { computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserInfo, logout } from '@/api/user'

export default {
  name: 'Home',
  setup() {
    const store = useStore()
    const router = useRouter()

    const userInfo = computed(() => store.state.userInfo)

    onMounted(() => {
      // 如果没有用户信息，获取用户信息
      if (!userInfo.value.id) {
        getUserInfo()
            .then(response => {
              const { data } = response
              store.commit('setUserInfo', data)
            })
            .catch(error => {
              ElMessage.error(error.message || '获取用户信息失败')
              store.commit('clearUserInfo')
              router.push('/login')
            })
      }
    })

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

    return {
      userInfo,
      handleLogout
    }
  }
}
</script>

<style scoped>
.home-container {
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

.user-info {
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  cursor: pointer;
  color: white;
}

.welcome-content {
  line-height: 2;
  font-size: 16px;
  text-align: center;
}
</style>