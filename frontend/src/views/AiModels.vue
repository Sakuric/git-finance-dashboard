<template>
  <div class="page-content active">
    <!-- 头部：全局控制 -->
    <el-header class="header card-glass mb-6" height="auto">
      <div class="header-left">
        <h2>AI 模型管理中心</h2>
        <p>配置大语言模型（LLM）驱动系统的核心动力</p>
      </div>
      <div class="header-right">
        <div class="flex-align-center">
          <span class="mr-3 text-secondary">AI 引擎状态:</span>
          <el-switch v-model="isEnabled" active-text="已启用" inactive-text="已禁用" inline-prompt />
        </div>
      </div>
    </el-header>

    <el-container class="ai-management-container" :class="{ 'is-disabled': !isEnabled }">
      <!-- 左侧：平台导航 -->
      <el-aside width="220px" class="platform-aside">
        <div class="aside-title">模型平台</div>
        <el-menu :default-active="selectedPlatform" class="platform-menu" @select="selectPlatform">
          <el-menu-item v-for="p in platforms" :key="p.id" :index="String(p.id)">
            <el-icon><component :is="p.id === 'openai' ? Connection : Cpu" /></el-icon>
            <span>{{ p.name }}</span>
            <el-icon v-if="p.isCustom" class="delete-icon" @click.stop="removePlatform(p)">
              <component :is="Close" />
            </el-icon>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 右侧：工作区 -->
      <el-main class="management-main">
        <el-row :gutter="20">
          <!-- 当前激活模型 -->
          <el-col :span="24" class="mb-4" v-if="selectedModel">
            <el-alert type="success" :closable="false" show-icon>
              <template #title>
                <div class="flex-between w-full">
                  <span>当前正使用: <b>{{ selectedModel.name }}</b></span>
                  <el-button link @click="clearSelectedModel">取消选择</el-button>
                </div>
              </template>
            </el-alert>
          </el-col>

          <!-- API 配置面板 -->
          <el-col :span="10">
            <el-card shadow="never">
              <template #header><div class="card-header-flex"><span><el-icon><Key /></el-icon> API 凭据配置</span></div></template>
              <el-form label-position="top">
                <el-form-item label="平台显示名称" v-if="selectedPlatform === 'custom'">
                  <el-input v-model="customPlatformName" placeholder="例如: DeepSeek" />
                </el-form-item>
                
                <el-form-item label="API Endpoint (接口地址)">
                  <el-input v-model="apiUrl" placeholder="https://api.example.com/v1" />
                </el-form-item>
                
                <el-form-item label="API Secret Key (密钥)">
                  <el-input v-model="apiKey" type="password" show-password placeholder="sk-...">
                    <template #append>
                      <el-button @click="testApiKey">连通测试</el-button>
                    </template>
                  </el-input>
                </el-form-item>

                <el-button type="primary" class="w-full mt-4" @click="savePlatformConfig">
                  保存平台配置
                </el-button>
              </el-form>
            </el-card>
          </el-col>

          <!-- 模型清单 -->
          <el-col :span="14">
            <el-card shadow="never">
              <template #header>
                <div class="flex-between">
                  <span><el-icon><List /></el-icon> 可用模型清单</span>
                  <el-button type="primary" size="small" :icon="Plus" @click="showAddModal = true">添加模型</el-button>
                </div>
              </template>
              
              <el-input v-model="searchQuery" placeholder="搜索模型名称..." :prefix-icon="Search" class="mb-4" clearable />

              <el-collapse v-model="expandedGroups">
                <el-collapse-item v-for="group in filteredModelGroups" :key="group.name" :name="group.name">
                  <template #title>
                    <div class="flex-between w-full pr-4">
                      <span>{{ group.name }}</span>
                      <el-tag size="small" round>{{ group.models.length }}</el-tag>
                    </div>
                  </template>
                  
                  <div class="model-list-new">
                    <div v-for="model in group.models" :key="model.id" 
                      class="model-item-new" 
                      :class="{ 'is-selected': selectedModel?.id === model.id }"
                      @click="selectModel(model)"
                    >
                      <div class="model-info-box">
                        <el-icon class="mr-2">
                          <component :is="selectedModel?.id === model.id ? CircleCheckFilled : CircleCheck" />
                        </el-icon>
                        <span class="name">{{ model.name }}</span>
                      </div>
                      <div class="model-tags">
                        <el-tag v-for="b in model.badges" :key="b.type" size="small" effect="plain" class="ml-1">{{ b.label }}</el-tag>
                      </div>
                      <div class="model-ops">
                        <el-button :icon="model.dbData.isDefault ? StarFilled : Star" circle size="small" :type="model.dbData.isDefault ? 'warning' : 'default'" @click.stop="setDefaultModel(model)" />
                        <el-button :icon="Delete" circle size="small" type="danger" @click.stop="removeModel(model)" />
                      </div>
                    </div>
                  </div>
                </el-collapse-item>
              </el-collapse>
            </el-card>
          </el-col>
        </el-row>
      </el-main>
    </el-container>

    <!-- 新增模型对话框 -->
    <el-dialog v-model="showAddModal" title="登记新 AI 模型" width="450px">
      <el-form label-position="top">
        <el-form-item label="厂商/分组">
          <el-input v-model="newModel.group" placeholder="例如: OpenAI" />
        </el-form-item>
        <el-form-item label="模型 ID (Identifier)">
          <el-input v-model="newModel.name" placeholder="例如: gpt-4-turbo" />
        </el-form-item>
        <el-form-item label="核心能力标签">
          <el-checkbox-group v-model="newModel.badges">
            <el-checkbox label="vision">视觉分析</el-checkbox>
            <el-checkbox label="web">实时联网</el-checkbox>
            <el-checkbox label="reasoning">深度推理</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddModal = false">取消</el-button>
        <el-button type="primary" @click="addModel">确认添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import {
  Connection, Cpu, Key, List, Plus, Search,
  Close, Delete, Star, StarFilled, CircleCheck, CircleCheckFilled
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAiPlatforms, saveAiPlatform, deleteAiPlatform } from '@/api/aiPlatform'
import { getAiModels, addAiModel, updateAiModel, deleteAiModel, testApiKey as testApiKeyRequest } from '@/api/aiModel'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

const isEnabled = ref(true)
const apiKey = ref('')
const apiUrl = ref('')
const searchQuery = ref('')
const showAddModal = ref(false)
const expandedGroups = ref([])
const selectedPlatform = ref('openai')
const selectedModel = ref(null)
const customPlatformName = ref('')

const defaultPlatforms = [
  { id: 'openai', name: 'OpenAI', apiUrl: 'https://api.openai.com/v1/', isDefault: true },
  { id: 'google', name: 'Google AI', apiUrl: 'https://generativelanguage.googleapis.com/v1/', isDefault: true },
  { id: 'anthropic', name: 'Anthropic', apiUrl: 'https://api.anthropic.com/v1/', isDefault: true },
  { id: 'local', name: 'Local Ollama', apiUrl: 'http://localhost:11434/v1/', isDefault: true }
]

const platforms = ref([...defaultPlatforms])
const modelGroups = ref([])

const loadData = async () => {
  const uid = authStore.userId || localStorage.getItem('userId')
  if (!uid) return

  const mRes = await getAiModels(uid)

  if (mRes.data) {
    const groups = {}
    const customPlatforms = new Set()

    mRes.data.forEach(m => {
      if (!groups[m.modelProvider]) groups[m.modelProvider] = { name: m.modelProvider, models: [] }
      groups[m.modelProvider].models.push({
        id: m.id, name: m.modelName, dbData: m,
        badges: (m.modelType || '').split(',').filter(Boolean).map(t => ({ type: t, label: t === 'vision' ? '视觉' : t === 'web' ? '联网' : '推理' }))
      })

      // 收集自定义平台
      const isDefaultPlatform = defaultPlatforms.some(p => p.id === m.modelProvider.toLowerCase())
      if (!isDefaultPlatform) {
        customPlatforms.add(m.modelProvider)
      }
    })

    modelGroups.value = Object.values(groups)
    expandedGroups.value = modelGroups.value.map(g => g.name)

    // 动态更新平台列表
    const newPlatforms = [...defaultPlatforms]
    customPlatforms.forEach(provider => {
      const firstModel = mRes.data.find(m => m.modelProvider === provider)
      newPlatforms.push({
        id: provider.toLowerCase(),
        name: provider,
        apiUrl: firstModel?.apiEndpoint || '',
        apiKey: firstModel?.apiKey || '',
        isCustom: true
      })
    })
    platforms.value = newPlatforms
  }
}

const selectPlatform = (id) => {
  selectedPlatform.value = id
  const p = platforms.value.find(x => x.id === id)
  if (p) {
    apiUrl.value = p.apiUrl || ''
    apiKey.value = p.apiKey || ''
    if (p.isCustom) {
      customPlatformName.value = p.name
    }
  }
}

const savePlatformConfig = async () => {
  ElMessage.success('配置已同步')
}

const testApiKey = async () => {
  if (!selectedModel.value) return ElMessage.warning('请先从右侧列表中选择一个模型')
  try {
    const res = await testApiKeyRequest({ apiKey: apiKey.value, apiUrl: apiUrl.value, modelName: selectedModel.value.name })
    if (res.success) ElMessage.success('连接成功！')
    else ElMessage.error('连接失败: ' + res.message)
  } catch (e) { ElMessage.error('网络错误') }
}

const filteredModelGroups = computed(() => {
  if (!searchQuery.value) return modelGroups.value
  return modelGroups.value.map(g => ({
    ...g,
    models: g.models.filter(m => m.name.toLowerCase().includes(searchQuery.value.toLowerCase()))
  })).filter(g => g.models.length > 0)
})

const selectModel = (m) => selectedModel.value = m
const clearSelectedModel = () => selectedModel.value = null

const newModel = reactive({ group: '', name: '', badges: [] })
const addModel = async () => {
  const uid = authStore.userId || localStorage.getItem('userId')
  await addAiModel({
    userId: uid, modelName: newModel.name, modelProvider: newModel.group,
    apiKey: apiKey.value, apiEndpoint: apiUrl.value, modelType: newModel.badges.join(','),
    isDefault: 0, maxTokens: 4096, temperature: 0.7
  })
  ElMessage.success('模型已登记')
  showAddModal.value = false
  loadData()
}

const removeModel = async (m) => {
  await ElMessageBox.confirm(`确定删除模型 ${m.name} 吗？`, '警告')
  await deleteAiModel(m.id)
  loadData()
}

const setDefaultModel = async (m) => {
  const uid = authStore.userId || localStorage.getItem('userId')
  await updateAiModel(m.id, {
    userId: uid,
    modelName: m.dbData.modelName,
    modelProvider: m.dbData.modelProvider,
    apiKey: m.dbData.apiKey,
    apiEndpoint: m.dbData.apiEndpoint,
    modelType: m.dbData.modelType,
    isDefault: 1,
    maxTokens: m.dbData.maxTokens,
    temperature: m.dbData.temperature
  })
  ElMessage.success('已设为默认模型')
  loadData()
}

const removePlatform = async (p) => {
  await ElMessageBox.confirm(`确定移除平台 ${p.name} 吗？`, '警告')
  ElMessage.info('平台功能暂未实现')
}

onMounted(() => loadData())
</script>

<style scoped>
.ai-management-container { border: 1px solid var(--border-color); border-radius: 12px; overflow: hidden; background: var(--sidebar-bg); }
.platform-aside { border-right: 1px solid var(--border-color); }
.aside-title { padding: 1.5rem 1.2rem 0.8rem; font-size: 0.75rem; color: var(--text-tertiary); text-transform: uppercase; letter-spacing: 1px; }
.platform-menu { border-right: none; }

.management-main { background: var(--bg-color); padding: 1.5rem; }
.is-disabled { opacity: 0.5; pointer-events: none; filter: grayscale(1); }

.model-list-new { display: flex; flex-direction: column; gap: 8px; }
.model-item-new { 
  display: flex; align-items: center; padding: 10px 15px; border-radius: 8px;
  background: rgba(255,255,255,0.02); border: 1px solid transparent; cursor: pointer; transition: all 0.2s;
}
.model-item-new:hover { background: rgba(255,255,255,0.05); border-color: var(--border-color); }
.model-item-new.is-selected { background: rgba(0, 166, 255, 0.1); border-color: var(--primary-accent); }

.model-info-box { flex: 1; display: flex; align-items: center; }
.model-info-box .name { font-weight: 600; font-size: 0.95rem; }
.model-ops { opacity: 0; transition: opacity 0.2s; }
.model-item-new:hover .model-ops { opacity: 1; }

.delete-icon { font-size: 14px; color: var(--text-tertiary); }
.delete-icon:hover { color: var(--color-negative); }
.w-full { width: 100%; }
.mt-4 { margin-top: 1rem; }
.mb-4 { margin-bottom: 1rem; }
.mr-2 { margin-right: 0.5rem; }
.pr-4 { padding-right: 1rem; }
.flex-align-center { display: flex; align-items: center; }
.flex-between { display: flex; justify-content: space-between; align-items: center; }
</style>