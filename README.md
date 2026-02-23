# 智能金融投资平台 (Finance Dashboard)

<div align="center">

![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)
![Vue.js](https://img.shields.io/badge/Vue.js-3.3.4-green.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

一个集成AI智能分析、实时行情监控、量化回测的现代化金融投资管理平台

[功能特性](#-功能特性) • [技术栈](#-技术栈) • [快速开始](#-快速开始) • [项目结构](#-项目结构) • [部署指南](#-部署指南)

</div>

---

## 📖 项目简介

智能金融投资平台是一个面向个人投资者和专业交易员的综合性金融管理系统。平台整合了实时市场数据、AI智能分析、量化回测、投资组合管理等核心功能，帮助用户做出更明智的投资决策。

### 核心亮点

- 🤖 **AI智能分析**: 自定义大模型API，提供智能投资建议
- 📊 **实时行情监控**: 接入新浪财经API，实时获取股票、指数行情数据
- 📈 **量化回测系统**: 支持多种交易策略的历史数据回测，验证策略有效性
- 💼 **投资组合管理**: 可视化展示持仓、收益、风险指标
- 🎯 **自定义策略**: 灵活配置交易策略参数，适应不同投资风格
- 📱 **响应式设计**: 支持PC、平板、移动端多终端访问

---

## ✨ 功能特性

### 1. 仪表盘 (Dashboard)

- 实时显示上证指数、深证成指、创业板指等主要指数
- 个人资产总览：总资产、持仓市值、可用资金、今日盈亏
- 持仓股票列表及实时涨跌幅
- 市场热点板块分析
- 快速交易入口

### 2. AI智能分析

- **多模型支持**: DeepSeek、GPT-4、文心一言、通义千问等
- **智能对话**: 自然语言交互，获取投资建议
- **提示词管理**: 预设和自定义提示词模板
- **历史记录**: 保存对话历史，方便回顾
- **模型配置**: 灵活切换和配置不同AI模型

### 3. 量化回测

- **策略类型**:
  - 均线策略（MA）
  - 相对强弱指标策略（RSI）
  - 布林带策略（BOLL）
  - MACD策略
  - 自定义策略
- **回测参数**:
  - 时间范围选择
  - 初始资金设置
  - 手续费率配置
  - 策略参数调优
- **结果分析**:
  - 收益率曲线
  - 最大回撤
  - 夏普比率
  - 胜率统计
  - 交易明细

### 4. 投资组合

- 持仓股票管理
- 历史交易记录
- 收益分析图表
- 风险评估指标
- 资产配置建议

### 5. 用户管理

- 用户注册/登录
- JWT身份认证
- 个人信息管理
- 安全设置

---

## 🛠️ 技术栈

### 后端技术

- **核心框架**: Spring Boot 3.5.6
- **数据库**: MySQL 8.0
- **ORM框架**: MyBatis 3.0.5
- **安全认证**: Spring Security + JWT
- **HTTP客户端**: OkHttp 4.12.0
- **JSON处理**: Fastjson2 2.0.43
- **HTML解析**: Jsoup 1.17.2
- **工具库**: Lombok、Apache Commons Lang3

### 前端技术

- **框架**: Vue 3.3.4
- **构建工具**: Vite 4.4.9
- **UI组件库**: Element Plus 2.13.0
- **图表库**: ECharts 5.4.3
- **状态管理**: Pinia 2.1.6
- **路由**: Vue Router 4.2.4
- **HTTP客户端**: Axios 1.6.0
- **图标库**: Lucide Vue Next、Element Plus Icons

### 开发工具

- **版本控制**: Git
- **构建工具**: Maven 3.x
- **包管理**: npm
- **Java版本**: JDK 17

---

## 🚀 快速开始

### 环境要求

- **JDK**: 17+
- **Maven**: 3.6+
- **Node.js**: 18+
- **MySQL**: 8.0+
- **操作系统**: Windows / macOS / Linux

### 1. 克隆项目

```bash
git clone https://github.com/Sakuric/finance-dashboard.git
cd finance-dashboard
```

### 2. 配置数据库

```bash
# 登录MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE finance_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入数据库结构
mysql -u root -p finance_db < 数据库迭代版本/finance_db.sql
```

### 3. 配置后端

编辑 `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/finance_db?useSSL=false&serverTimezone=UTC
    username: root
    password: your_password  # 修改为你的MySQL密码

jwt:
  secret: your_jwt_secret_key  # 建议修改为自己的密钥

llm:
  api-key: your_llm_api_key  # 配置你的AI模型API密钥
  base-url: https://apis.iflow.cn/v1
  model: qwen3-vl-plus
```

### 4. 启动后端

```bash
# 编译项目
mvn clean package -DskipTests

# 运行项目
mvn spring-boot:run

# 或直接运行JAR包
java -jar target/finance-dashboard-0.0.1-SNAPSHOT.jar
```

后端服务将在 `http://localhost:8081` 启动

### 5. 启动前端

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务将在 `http://localhost:5175` 启动

### 6. 访问应用

打开浏览器访问: `http://localhost:5175`

默认测试账号（如果数据库中有初始数据）:

- 用户名: `test`
- 密码: `test123`

---

## 📁 项目结构

```
finance-dashboard/
├── src/                          # 后端源码
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/financedashboard/
│   │   │       ├── config/       # 配置类
│   │   │       ├── controller/   # 控制器
│   │   │       ├── service/      # 业务逻辑
│   │   │       ├── mapper/       # 数据访问层
│   │   │       ├── entity/       # 实体类
│   │   │       ├── dto/          # 数据传输对象
│   │   │       ├── util/         # 工具类
│   │   │       └── security/     # 安全配置
│   │   └── resources/
│   │       ├── mapper/           # MyBatis XML映射文件
│   │       ├── application.yml   # 应用配置
│   │       └── db/               # 数据库脚本
│   └── test/                     # 测试代码
├── frontend/                     # 前端源码
│   ├── src/
│   │   ├── assets/              # 静态资源
│   │   ├── components/          # Vue组件
│   │   ├── views/               # 页面视图
│   │   ├── router/              # 路由配置
│   │   ├── stores/              # Pinia状态管理
│   │   ├── api/                 # API接口
│   │   ├── utils/               # 工具函数
│   │   └── App.vue              # 根组件
│   ├── public/                  # 公共资源
│   ├── index.html               # HTML模板
│   ├── vite.config.js           # Vite配置
│   └── package.json             # 前端依赖
├── 数据库迭代版本/               # 数据库SQL文件
├── docs/                        # 项目文档
├── pom.xml                      # Maven配置
├── DEPLOYMENT.md                # 部署指南
└── README.md                    # 项目说明
```

---

## 🔌 API接口文档

### 认证接口

#### 用户登录

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "test",
  "password": "test123"
}
```

#### 用户注册

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "newuser",
  "password": "password123",
  "email": "user@example.com"
}
```

### 行情接口

#### 获取实时行情

```http
GET /api/market/realtime?symbols=sh000001,sz399001
Authorization: Bearer {token}
```

#### 获取历史K线

```http
GET /api/market/kline?symbol=sh000001&period=day&count=100
Authorization: Bearer {token}
```

### AI分析接口

#### 发送对话

```http
POST /api/ai/chat
Authorization: Bearer {token}
Content-Type: application/json

{
  "message": "分析一下当前市场走势",
  "modelId": 1
}
```

### 回测接口

#### 创建回测任务

```http
POST /api/backtest/create
Authorization: Bearer {token}
Content-Type: application/json

{
  "strategyType": "MA",
  "symbol": "sh000001",
  "startDate": "2023-01-01",
  "endDate": "2023-12-31",
  "initialCapital": 100000,
  "parameters": {
    "shortPeriod": 5,
    "longPeriod": 20
  }
}
```

---

## 🎯 核心功能实现

### 1. 实时行情获取

系统通过新浪财经API获取实时行情数据：

```java
@Service
public class MarketDataService {
    public StockQuote getRealTimeQuote(String symbol) {
        String url = "https://hq.sinajs.cn/list=" + symbol;
        // 调用API获取数据
        // 解析并返回行情数据
    }
}
```

### 2. AI智能分析

集成多种大语言模型，提供智能投资建议：

```java
@Service
public class AIService {
    public String chat(String message, Long modelId) {
        // 根据modelId获取模型配置
        // 调用对应的AI API
        // 返回分析结果
    }
}
```

### 3. 量化回测引擎

支持多种交易策略的历史回测：

```java
@Service
public class BacktestService {
    public BacktestResult runBacktest(BacktestRequest request) {
        // 加载历史数据
        // 执行策略逻辑
        // 计算收益指标
        // 返回回测结果
    }
}
```

---

## 📊 数据库设计

### 核心表结构

- **user_info**: 用户信息表
- **ai_model_config**: AI模型配置表
- **ai_prompt**: AI提示词表
- **ai_chat_history**: AI对话历史表
- **stock_info**: 股票基本信息表
- **stock_realtime**: 实时行情表
- **stock_history**: 历史K线表
- **backtest_task**: 回测任务表
- **backtest_result**: 回测结果表
- **portfolio**: 投资组合表
- **transaction**: 交易记录表

详细的数据库结构请查看 `数据库迭代版本/finance_db.sql`

---

## 🚢 部署指南

### 开发环境部署

参考上面的[快速开始](#-快速开始)章节

### 生产环境部署

详细的云服务器部署指南请查看 [DEPLOYMENT.md](./DEPLOYMENT.md)

主要步骤：

1. 准备云服务器（推荐配置：4核8G）
2. 安装Java 17、MySQL 8.0、Node.js 18、Nginx
3. 配置数据库并导入数据
4. 部署后端服务（使用systemd管理）
5. 构建并部署前端静态文件
6. 配置Nginx反向代理
7. 配置SSL证书（可选）

---

## 🔐 安全说明

### 重要提示

1. **修改默认密码**: 部署前务必修改数据库密码、JWT密钥
2. **API密钥保护**: 不要将AI模型API密钥提交到代码仓库
3. **HTTPS部署**: 生产环境建议使用HTTPS协议
4. **定期备份**: 定期备份数据库数据
5. **访问控制**: 配置防火墙规则，限制不必要的端口访问

### 环境变量配置

建议使用环境变量管理敏感信息：

```bash
export JWT_SECRET=your_jwt_secret_key
export LLM_API_KEY=your_llm_api_key
export DB_PASSWORD=your_db_password
```

---

## 🧪 测试

### 运行单元测试

```bash
mvn test
```

### 运行集成测试

```bash
mvn verify
```

### 前端测试

```bash
cd frontend
npm run test
```

---

## 📝 开发文档

- [回测功能实现文档](./BACKTEST_IMPLEMENTATION.md)
- [Bug修复报告](./BUG_FIX_REPORT.md)
- [测试指南](./TESTING_GUIDE.md)
- [部署指南](./DEPLOYMENT.md)

---

## 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

### 代码规范

- Java代码遵循阿里巴巴Java开发手册
- 前端代码遵循Vue.js风格指南
- 提交信息使用语义化提交规范

---

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

---

---

## 🙏 致谢

感谢以下开源项目和服务：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [ECharts](https://echarts.apache.org/)
- [新浪财经API](https://finance.sina.com.cn/)
- [MyBatis](https://mybatis.org/)

---

## 📈 项目状态

![GitHub stars](https://img.shields.io/github/stars/your-username/finance-dashboard?style=social)
![GitHub forks](https://img.shields.io/github/forks/your-username/finance-dashboard?style=social)
![GitHub issues](https://img.shields.io/github/issues/your-username/finance-dashboard)
![GitHub pull requests](https://img.shields.io/github/issues-pr/your-username/finance-dashboard)

---

## 🗺️ 路线图

### v1.0 (当前版本)

- ✅ 基础功能实现
- ✅ AI智能分析
- ✅ 量化回测系统
- ✅ 实时行情监控

### v1.1 (计划中)

- 🔲 移动端APP
- 🔲 更多技术指标
- 🔲 社区交流功能
- 🔲 策略市场

### v2.0 (未来规划)

- 🔲 机器学习预测
- 🔲 高频交易支持
- 🔲 多市场支持（港股、美股）
- 🔲 实盘交易接入

---

<div align="center">

**如果这个项目对你有帮助，请给一个 ⭐️ Star 支持一下！**

Made with ❤️ by InvestIQ AI Team

</div>
