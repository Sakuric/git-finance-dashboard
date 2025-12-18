<template>
  <div class="page-content active">
    <header class="header">
      <div class="header-left">
        <h2>AI模型管理</h2>
        <p>配置和管理AI模型</p>
      </div>
      <div class="header-right">
        <label class="switch">
          <input type="checkbox" v-model="isEnabled">
          <span class="slider"></span>
        </label>
      </div>
    </header>

    <div class="ai-models-layout" :class="{ disabled: !isEnabled }">
      <!-- 左侧平台选择 -->
      <div class="platform-sidebar">
        <div class="platform-title">模型平台</div>
        <div class="platform-list">
          <div
            v-for="platform in platforms"
            :key="platform.id"
            class="platform-item"
            :class="{ active: selectedPlatform === platform.id }"
            @click="selectPlatform(platform.id)"
          >
            <span>{{ platform.name }}</span>
            <button v-if="platform.isCustom" class="icon-btn-mini danger" @click.stop="removePlatform(platform)" title="删除">
              <i class="fas fa-times"></i>
            </button>
          </div>
        </div>
      </div>

      <!-- 右侧内容区 -->
      <div class="platform-content">
        <!-- 当前选中模型显示 -->
        <div class="current-model-card" v-if="selectedModel">
          <div class="current-model-label">
            <i class="fas fa-check-circle"></i>
            当前使用模型
          </div>
          <div class="current-model-info">
            <div class="current-model-name">{{ selectedModel.name }}</div>
            <div class="current-model-badges">
              <span v-for="badge in selectedModel.badges" :key="badge.type" class="badge" :class="badge.type">
                {{ badge.label }}
              </span>
            </div>
          </div>
          <button class="btn-clear" @click="clearSelectedModel" title="取消选择">
            <i class="fas fa-times"></i>
          </button>
        </div>

        <!-- API配置卡片 -->
        <div class="card">
        <div class="card-header">
          <h3>API 配置</h3>
        </div>
        <div class="settings-section">
          <div class="setting-item" v-if="selectedPlatform === 'custom'" :class="{ 'has-error': apiErrors.platformName }">
            <label>平台名称 <span class="required">*</span></label>
            <input
              type="text"
              v-model="customPlatformName"
              placeholder="请输入平台名称，如：DeepSeek"
              class="setting-input"
              :class="{ 'input-error': apiErrors.platformName }"
            >
            <span v-if="apiErrors.platformName" class="error-msg">{{ apiErrors.platformName }}</span>
          </div>
          <div class="setting-item" :class="{ 'has-error': apiErrors.apiKey }">
            <label>API 密钥 <span class="required">*</span></label>
            <div class="input-with-actions">
              <input
                :type="showApiKey ? 'text' : 'password'"
                v-model="apiKey"
                placeholder="请输入API密钥"
                class="setting-input"
                :class="{ 'input-error': apiErrors.apiKey }"
              >
              <button class="icon-btn" @click="showApiKey = !showApiKey">
                <i :class="showApiKey ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
              </button>
              <button class="btn-primary btn-sm" @click="testApiKey">检测</button>
            </div>
            <span v-if="apiErrors.apiKey" class="error-msg">{{ apiErrors.apiKey }}</span>
          </div>
          <div class="setting-item" :class="{ 'has-error': apiErrors.apiUrl }">
            <label>API 地址 <span class="required">*</span></label>
            <input
              type="text"
              v-model="apiUrl"
              placeholder="请输入API地址"
              class="setting-input"
              :class="{ 'input-error': apiErrors.apiUrl }"
            >
            <span v-if="apiErrors.apiUrl" class="error-msg">{{ apiErrors.apiUrl }}</span>
          </div>
          <div class="setting-item" v-if="selectedPlatform === 'custom'">
            <button class="btn-primary" @click="saveCustomPlatform">
              <i class="fas fa-save"></i> 保存到模型平台
            </button>
          </div>
          <div class="setting-item" v-else-if="platforms.find(p => p.id === selectedPlatform)?.isCustom">
            <button class="btn-primary" @click="savePlatformConfig">
              <i class="fas fa-save"></i> 保存配置
            </button>
          </div>
        </div>
      </div>

      <!-- 模型列表卡片 -->
      <div class="card">
        <div class="card-header">
          <h3>模型列表</h3>
          <div class="header-actions">
            <span class="model-count">{{ totalModelsCount }} 个模型</span>
            <button class="icon-btn" @click="showSearch = !showSearch">
              <i class="fas fa-search"></i>
            </button>
            <button class="btn-primary btn-sm" @click="showAddModal = true">
              <i class="fas fa-plus"></i> 添加
            </button>
          </div>
        </div>

        <!-- 搜索框 -->
        <div class="search-wrapper" v-if="showSearch">
          <input type="text" v-model="searchQuery" placeholder="搜索模型..." class="search-input">
        </div>

        <!-- 模型分组 -->
        <div class="model-list">
          <div v-for="group in filteredModelGroups" :key="group.name" class="model-group">
            <div class="group-header" @click="toggleGroup(group.name)">
              <i :class="expandedGroups.includes(group.name) ? 'fas fa-chevron-down' : 'fas fa-chevron-right'"></i>
              <span>{{ group.name }}</span>
              <span class="group-count">{{ group.models.length }}</span>
            </div>
            
            <div class="group-content" v-if="expandedGroups.includes(group.name)">
              <div
                v-for="model in group.models"
                :key="model.id"
                class="model-item"
                :class="{ selected: selectedModel && selectedModel.id === model.id }"
                @click="selectModel(model)"
              >
                <div class="model-select-indicator">
                  <i :class="selectedModel && selectedModel.id === model.id ? 'fas fa-check-circle' : 'far fa-circle'"></i>
                </div>
                <div class="model-info">
                  <i class="fas fa-robot model-icon"></i>
                  <span class="model-name">{{ model.name }}</span>
                </div>
                <div class="model-badges">
                  <span v-for="badge in model.badges" :key="badge.type" class="badge" :class="badge.type">
                    {{ badge.label }}
                  </span>
                </div>
                <div class="model-actions">
                  <button class="icon-btn" @click.stop="configureModel(model)" title="配置">
                    <i class="fas fa-cog"></i>
                  </button>
                  <button class="icon-btn danger" @click.stop="removeModel(model)" title="删除">
                    <i class="fas fa-trash"></i>
                  </button>
                </div>
              </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加模型弹窗 -->
    <div class="modal-overlay" v-if="showAddModal" @click="showAddModal = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>添加模型</h3>
          <button class="modal-close" @click="showAddModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>模型分组</label>
            <div class="input-group">
              <i class="fas fa-folder"></i>
              <input type="text" v-model="newModel.group" placeholder="如：gemini-2.0">
            </div>
          </div>
          <div class="form-group">
            <label>模型名称</label>
            <div class="input-group">
              <i class="fas fa-robot"></i>
              <input type="text" v-model="newModel.name" placeholder="如：gemini-2.0-flash">
            </div>
          </div>
          <div class="form-group">
            <label>模型能力</label>
            <div class="capability-grid">
              <label v-for="cap in capabilities" :key="cap.type" class="capability-item">
                <input type="checkbox" v-model="newModel.badges" :value="cap">
                <span class="capability-label">
                  <i :class="cap.icon"></i>
                  {{ cap.label }}
                </span>
              </label>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-secondary" @click="showAddModal = false">取消</button>
          <button class="btn-primary" @click="addModel">确认添加</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { getAiPlatforms, saveAiPlatform, updateAiPlatform, deleteAiPlatform, testApiKey as testApiKeyApi } from '@/api/aiPlatform'

export default {
  name: 'AiModels',
  setup() {
    const isEnabled = ref(true)
    const showApiKey = ref(false)
    const apiKey = ref('')
    const apiUrl = ref('https://api.openai.com/v1/')
    const showSearch = ref(false)
    const searchQuery = ref('')
    const showAddModal = ref(false)
    const showAddPlatformModal = ref(false)
    const expandedGroups = ref(['gemini-2.0', 'gemini-2.5'])
    const selectedPlatform = ref('openai')
    const selectedModel = ref(null)

    // 表单校验错误
    const apiErrors = reactive({
      platformName: '',
      apiKey: '',
      apiUrl: ''
    })

    const defaultPlatforms = [
      { id: 'openai', name: 'OpenAI', apiUrl: 'https://api.openai.com/v1/' },
      { id: 'google', name: 'Google AI', apiUrl: 'https://generativelanguage.googleapis.com/v1/' },
      { id: 'anthropic', name: 'Anthropic', apiUrl: 'https://api.anthropic.com/v1/' },
      { id: 'azure', name: 'Azure OpenAI', apiUrl: 'https://your-resource.openai.azure.com/' },
      { id: 'local', name: '本地模型', apiUrl: 'http://localhost:11434/v1/' },
      { id: 'custom', name: '自定义', apiUrl: '' }
    ]

    const platforms = ref([...defaultPlatforms])
    const customPlatforms = ref([])
    const customPlatformName = ref('')

    const loadPlatforms = async () => {
      try {
        const userId = localStorage.getItem('userId') || 1
        const res = await getAiPlatforms(userId)
        if (res && res.data) {
          customPlatforms.value = res.data
          platforms.value = [
            ...defaultPlatforms,
            ...res.data.map(p => ({
              id: p.platformKey,
              name: p.platformName,
              icon: p.icon || 'fas fa-cog',
              apiUrl: p.apiUrl,
              apiKey: p.apiKey,
              dbId: p.id,
              isCustom: true
            }))
          ]
        }
      } catch (e) {
        console.error('加载平台失败', e)
      }
    }

    onMounted(() => {
      loadPlatforms()
    })

    const selectPlatform = (id) => {
      selectedPlatform.value = id
      const platform = platforms.value.find(p => p.id === id)
      if (platform) {
        apiUrl.value = platform.apiUrl || ''
        apiKey.value = platform.apiKey || ''
      }
      // 清除错误
      apiErrors.platformName = ''
      apiErrors.apiKey = ''
      apiErrors.apiUrl = ''
    }

    // 校验自定义平台表单
    const validateCustomPlatform = () => {
      let valid = true
      apiErrors.platformName = ''
      apiErrors.apiKey = ''
      apiErrors.apiUrl = ''

      if (!customPlatformName.value.trim()) {
        apiErrors.platformName = '请输入平台名称'
        valid = false
      }
      if (!apiKey.value.trim()) {
        apiErrors.apiKey = '请输入API密钥'
        valid = false
      }
      if (!apiUrl.value.trim()) {
        apiErrors.apiUrl = '请输入API地址'
        valid = false
      } else if (!/^https?:\/\/.+/.test(apiUrl.value)) {
        apiErrors.apiUrl = 'API地址格式不正确，需以http://或https://开头'
        valid = false
      }
      return valid
    }

    // 校验已有平台配置
    const validatePlatformConfig = () => {
      let valid = true
      apiErrors.apiKey = ''
      apiErrors.apiUrl = ''

      if (!apiKey.value.trim()) {
        apiErrors.apiKey = '请输入API密钥'
        valid = false
      }
      if (!apiUrl.value.trim()) {
        apiErrors.apiUrl = '请输入API地址'
        valid = false
      } else if (!/^https?:\/\/.+/.test(apiUrl.value)) {
        apiErrors.apiUrl = 'API地址格式不正确'
        valid = false
      }
      return valid
    }

    const saveCustomPlatform = async () => {
      if (!validateCustomPlatform()) return
      try {
        const platformKey = 'custom_' + Date.now()
        const res = await saveAiPlatform({
          platformKey: platformKey,
          platformName: customPlatformName.value,
          icon: 'fas fa-cog',
          apiUrl: apiUrl.value,
          apiKey: apiKey.value,
          isEnabled: 1
        })
        if (res.code !== 200) {
          alert('保存失败: ' + (res.message || '未知错误'))
          return
        }
        await loadPlatforms()
        // 切换到新保存的平台
        selectPlatform(platformKey)
        customPlatformName.value = ''
        alert('保存成功！平台已添加到模型平台列表')
      } catch (e) {
        console.error('保存平台失败:', e)
        alert('保存失败: ' + (e.response?.data?.message || e.message || '网络错误'))
      }
    }

    const savePlatformConfig = async () => {
      const platform = platforms.value.find(p => p.id === selectedPlatform.value)
      if (!platform || !platform.isCustom) {
        alert('只能保存自定义平台的配置')
        return
      }
      if (!validatePlatformConfig()) return
      try {
        await updateAiPlatform({
          id: platform.dbId,
          platformKey: platform.id,
          platformName: platform.name,
          icon: platform.icon,
          apiUrl: apiUrl.value,
          apiKey: apiKey.value,
          isEnabled: 1
        })
        await loadPlatforms()
        alert('保存成功')
      } catch (e) {
        alert('保存失败: ' + e.message)
      }
    }

    const removePlatform = async (platform) => {
      if (!platform.isCustom) {
        alert('默认平台不能删除')
        return
      }
      if (!confirm(`确定删除平台 ${platform.name}?`)) return
      try {
        await deleteAiPlatform(platform.dbId)
        await loadPlatforms()
        if (selectedPlatform.value === platform.id) {
          selectedPlatform.value = 'openai'
          selectPlatform('openai')
        }
      } catch (e) {
        alert('删除失败: ' + e.message)
      }
    }

    const capabilities = [
      { type: 'vision', icon: 'fas fa-eye', label: '视觉' },
      { type: 'web', icon: 'fas fa-globe', label: '联网' },
      { type: 'reasoning', icon: 'fas fa-brain', label: '推理' },
      { type: 'tools', icon: 'fas fa-tools', label: '工具' }
    ]

    const newModel = ref({
      group: '',
      name: '',
      badges: []
    })

    const modelGroups = ref([
      {
        name: 'gemini-2.0',
        models: [
          { 
            id: 1, 
            name: 'gemini-2.0-flash-exp',
            badges: [{ type: 'vision', label: '视觉' }]
          }
        ]
      },
      {
        name: 'gemini-2.5',
        models: [
          { 
            id: 2, 
            name: 'gemini-2.5-flash-preview',
            badges: [
              { type: 'vision', label: '视觉' },
              { type: 'web', label: '联网' },
              { type: 'reasoning', label: '推理' },
              { type: 'tools', label: '工具' }
            ]
          },
          { 
            id: 3, 
            name: 'gemini-2.5-pro',
            badges: [
              { type: 'vision', label: '视觉' },
              { type: 'reasoning', label: '推理' }
            ]
          }
        ]
      }
    ])

    const totalModelsCount = computed(() => {
      return modelGroups.value.reduce((sum, g) => sum + g.models.length, 0)
    })

    const filteredModelGroups = computed(() => {
      if (!searchQuery.value) return modelGroups.value
      return modelGroups.value.map(group => ({
        ...group,
        models: group.models.filter(m => 
          m.name.toLowerCase().includes(searchQuery.value.toLowerCase())
        )
      })).filter(g => g.models.length > 0)
    })

    const toggleGroup = (name) => {
      const idx = expandedGroups.value.indexOf(name)
      if (idx > -1) {
        expandedGroups.value.splice(idx, 1)
      } else {
        expandedGroups.value.push(name)
      }
    }

    const testApiKey = async () => {
      if (!apiKey.value.trim()) {
        alert('请先输入API密钥')
        return
      }
      if (!apiUrl.value.trim()) {
        alert('请先输入API地址')
        return
      }
      try {
        const res = await testApiKeyApi({
          apiKey: apiKey.value,
          apiUrl: apiUrl.value,
          modelName: selectedModel.value?.name || 'gpt-3.5-turbo'
        })
        if (res.success) {
          alert('✅ ' + res.message)
        } else {
          alert('❌ ' + res.message)
        }
      } catch (e) {
        alert('❌ 检测失败: ' + (e.response?.data?.message || e.message || '网络错误'))
      }
    }

    const selectModel = (model) => {
      selectedModel.value = model
    }

    const clearSelectedModel = () => {
      selectedModel.value = null
    }

    const configureModel = (model) => {
      alert(`配置模型: ${model.name}`)
    }

    const removeModel = (model) => {
      if (confirm(`确定删除模型 ${model.name}?`)) {
        modelGroups.value.forEach(group => {
          group.models = group.models.filter(m => m.id !== model.id)
        })
        modelGroups.value = modelGroups.value.filter(g => g.models.length > 0)
      }
    }

    const addModel = () => {
      if (!newModel.value.group || !newModel.value.name) {
        alert('请填写完整信息')
        return
      }
      
      let group = modelGroups.value.find(g => g.name === newModel.value.group)
      if (!group) {
        group = { name: newModel.value.group, models: [] }
        modelGroups.value.push(group)
      }
      
      group.models.push({
        id: Date.now(),
        name: newModel.value.name,
        badges: newModel.value.badges.map(b => ({ type: b.type, label: b.label }))
      })
      
      if (!expandedGroups.value.includes(newModel.value.group)) {
        expandedGroups.value.push(newModel.value.group)
      }
      
      newModel.value = { group: '', name: '', badges: [] }
      showAddModal.value = false
    }

    return {
      isEnabled,
      showApiKey,
      apiKey,
      apiUrl,
      showSearch,
      searchQuery,
      showAddModal,
      expandedGroups,
      capabilities,
      newModel,
      modelGroups,
      totalModelsCount,
      filteredModelGroups,
      toggleGroup,
      testApiKey,
      configureModel,
      removeModel,
      addModel,
      platforms,
      selectedPlatform,
      selectPlatform,
      selectedModel,
      selectModel,
      clearSelectedModel,
      customPlatformName,
      saveCustomPlatform,
      savePlatformConfig,
      removePlatform,
      apiErrors
    }
  }
}
</script>

<style scoped>
.ai-models-layout {
  display: flex;
  gap: 1.5rem;
  min-height: calc(100vh - 180px);
}

.platform-sidebar {
  width: 180px;
  flex-shrink: 0;
  padding-top: 0.5rem;
}

.platform-title {
  color: var(--text-tertiary);
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 0.5rem;
  padding-left: 0.75rem;
}

.platform-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.platform-item {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0.6rem 0.75rem;
  border-radius: 6px;
  cursor: pointer;
  color: var(--text-secondary);
  font-size: 0.9rem;
  transition: all 0.2s ease;
  border-left: 2px solid transparent;
}

.platform-item:hover {
  color: var(--text-primary);
  background: rgba(255, 255, 255, 0.03);
}

.platform-item.active {
  color: var(--primary-accent);
  background: rgba(0, 166, 255, 0.08);
  border-left-color: var(--primary-accent);
}

.platform-item i {
  width: 18px;
  font-size: 0.9rem;
  text-align: center;
  opacity: 0.8;
}

.platform-item.active i {
  opacity: 1;
}

.platform-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.input-with-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex: 1;
}

.setting-input {
  flex: 1;
  background: transparent;
  border: 1px solid #1E2530;
  border-radius: 6px;
  padding: 0.5rem 0.75rem;
  color: #E1E4E8;
  font-size: 0.9rem;
}

.setting-input:focus {
  outline: none;
  border-color: var(--primary-accent);
  box-shadow: 0 0 0 3px rgba(0, 175, 255, 0.1);
}

.setting-input::placeholder {
  color: #6A737D;
}

.btn-sm {
  padding: 0.4rem 0.8rem;
  font-size: 0.85rem;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.model-count {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.search-wrapper {
  margin-bottom: 1rem;
}

.search-input {
  width: 100%;
  background-color: var(--sidebar-bg);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  padding: 0.6rem 1rem;
  color: var(--text-primary);
  font-size: 0.9rem;
}

.search-input:focus {
  outline: none;
  border-color: var(--primary-accent);
}

.model-list {
  border: 1px solid var(--border-color);
  border-radius: 8px;
  overflow: hidden;
}

.model-group {
  border-bottom: 1px solid var(--border-color);
}

.model-group:last-child {
  border-bottom: none;
}

.group-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.9rem 1rem;
  background: rgba(0, 166, 255, 0.05);
  cursor: pointer;
  color: var(--text-primary);
  font-weight: 500;
  transition: var(--transition);
}

.group-header:hover {
  background: rgba(0, 166, 255, 0.1);
}

.group-header i {
  color: var(--text-secondary);
  font-size: 0.75rem;
  width: 12px;
}

.group-count {
  margin-left: auto;
  background: var(--primary-accent);
  color: white;
  padding: 0.15rem 0.5rem;
  border-radius: 10px;
  font-size: 0.75rem;
}

.group-content {
  background: var(--card-bg);
}

.current-model-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem 1.25rem;
  background: linear-gradient(135deg, rgba(0, 166, 255, 0.1), rgba(0, 136, 204, 0.05));
  border: 1px solid rgba(0, 166, 255, 0.3);
  border-radius: 10px;
  position: relative;
}

.current-model-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--primary-accent);
  font-size: 0.85rem;
  font-weight: 500;
  white-space: nowrap;
}

.current-model-label i {
  font-size: 1rem;
}

.current-model-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.current-model-name {
  color: var(--text-primary);
  font-weight: 600;
  font-size: 1rem;
}

.current-model-badges {
  display: flex;
  gap: 0.4rem;
}

.btn-clear {
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 4px;
  transition: var(--transition);
}

.btn-clear:hover {
  color: var(--text-primary);
  background: rgba(255, 255, 255, 0.1);
}

.model-item {
  display: flex;
  align-items: center;
  padding: 0.8rem 1rem;
  padding-left: 1rem;
  border-top: 1px solid var(--border-color);
  transition: var(--transition);
  cursor: pointer;
}

.model-item:hover {
  background: rgba(0, 166, 255, 0.03);
}

.model-item.selected {
  background: rgba(0, 166, 255, 0.08);
}

.model-select-indicator {
  width: 24px;
  margin-right: 0.5rem;
  color: var(--text-tertiary);
}

.model-item.selected .model-select-indicator {
  color: var(--primary-accent);
}

.model-info {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  flex: 1;
}

.model-icon {
  color: var(--primary-accent);
}

.model-name {
  color: var(--text-primary);
  font-size: 0.9rem;
}

.model-badges {
  display: flex;
  gap: 0.4rem;
  margin-right: 1rem;
}

.badge {
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  font-size: 0.7rem;
  font-weight: 500;
}

.badge.vision {
  background: rgba(0, 192, 135, 0.15);
  color: #00C087;
}

.badge.web {
  background: rgba(0, 166, 255, 0.15);
  color: var(--primary-accent);
}

.badge.reasoning {
  background: rgba(163, 113, 247, 0.15);
  color: #a371f7;
}

.badge.tools {
  background: rgba(255, 169, 64, 0.15);
  color: #FFA940;
}

.model-actions {
  display: flex;
  gap: 0.25rem;
}

.icon-btn.danger:hover {
  color: #FF4D4F;
  background: rgba(255, 77, 79, 0.1);
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
  backdrop-filter: blur(5px);
}

.modal-content {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  width: 90%;
  max-width: 480px;
  box-shadow: var(--shadow-lg);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid var(--border-color);
}

.modal-header h3 {
  margin: 0;
  color: var(--text-primary);
  font-size: 1.1rem;
}

.modal-close {
  background: none;
  border: none;
  color: var(--text-secondary);
  font-size: 1.5rem;
  cursor: pointer;
  line-height: 1;
}

.modal-close:hover {
  color: var(--text-primary);
}

.modal-body {
  padding: 1.5rem;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  padding: 1rem 1.5rem;
  border-top: 1px solid var(--border-color);
}

.capability-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.75rem;
}

.capability-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
}

.capability-item input {
  display: none;
}

.capability-label {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  color: var(--text-secondary);
  font-size: 0.85rem;
  transition: var(--transition);
  width: 100%;
}

.capability-item input:checked + .capability-label {
  border-color: var(--primary-accent);
  background: rgba(0, 166, 255, 0.1);
  color: var(--primary-accent);
}

.capability-label:hover {
  border-color: var(--primary-accent);
}

/* 禁用状态 */
.ai-models-layout.disabled {
  opacity: 0.4;
  pointer-events: none;
  filter: grayscale(0.8);
  user-select: none;
}

/* 添加平台按钮 */
.platform-item.add-platform {
  border: 1px dashed var(--border-color);
  color: var(--text-tertiary);
  margin-top: 0.5rem;
}

.platform-item.add-platform:hover {
  border-color: var(--primary-accent);
  color: var(--primary-accent);
}

/* 迷你删除按钮 */
.icon-btn-mini {
  background: none;
  border: none;
  padding: 0.15rem;
  cursor: pointer;
  color: var(--text-tertiary);
  margin-left: auto;
  border-radius: 3px;
  font-size: 0.7rem;
  opacity: 0;
  transition: all 0.2s ease;
}

.platform-item:hover .icon-btn-mini {
  opacity: 1;
}

.icon-btn-mini.danger:hover {
  color: #FF4D4F;
  background: rgba(255, 77, 79, 0.1);
}

/* 表单组样式 */
.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.input-group {
  display: flex;
  align-items: center;
  background: var(--sidebar-bg);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  padding: 0 0.75rem;
}

.input-group i {
  color: var(--text-tertiary);
  margin-right: 0.5rem;
}

.input-group input {
  flex: 1;
  background: none;
  border: none;
  padding: 0.6rem 0;
  color: var(--text-primary);
  font-size: 0.9rem;
}

.input-group input:focus {
  outline: none;
}

.input-group:focus-within {
  border-color: var(--primary-accent);
}

/* 表单校验样式 */
.required { color: #F85149; }
.error-msg { color: #F85149; font-size: 12px; margin-top: 6px; display: block; }
.input-error { border-color: #F85149 !important; }
.has-error .setting-input { border-color: #F85149; }
</style>