# 量融金融平台微服务架构

## 项目概述

量融金融平台是一个基于Spring Cloud生态的分布式微服务架构系统，从原有的单体应用重构而来，提供股票信息展示、用户管理、数据分析等核心金融服务功能。

## 🏗️ 架构特点

- **微服务架构**: 按业务领域拆分服务，降低耦合度
- **Spring Cloud生态**: 基于成熟的Spring Cloud技术栈
- **容器化部署**: 支持Docker和Kubernetes部署
- **高可用设计**: 服务注册发现、负载均衡、熔断降级
- **完善监控**: 全链路监控、日志收集、性能分析
- **安全认证**: JWT统一认证、权限控制

## 📋 项目结构

```
finance-microservices/
├── 📄 README.md                           # 项目说明文档
├── 📄 微服务架构设计方案.md                # 架构设计文档
├── 📄 项目结构说明.md                      # 项目结构说明
├── 📄 微服务调试文档.md                    # 调试指南文档
├── 📄 系统监控与运维文档.md                # 监控运维文档
├── 📄 pom.xml                             # 父级POM文件
├── 📁 finance-common/                      # 公共模块
├── 📁 finance-registry/                    # 服务注册中心 (8761)
├── 📁 finance-config/                       # 配置中心 (8888)
├── 📁 finance-gateway/                      # API网关 (8080)
├── 📁 finance-user-service/                 # 用户服务 (8081) [待实现]
├── 📁 finance-stock-service/                # 股票服务 (8082) [待实现]
├── 📁 finance-analysis-service/             # 数据分析服务 (8083) [待实现]
└── 📁 finance-monitor/                      # 监控服务 (9001) [待实现]
```

## 🚀 快速开始

### 环境要求

- **Java**: JDK 17+
- **Maven**: 3.6+
- **Redis**: 6.0+
- **Git**: 用于配置管理

### 启动步骤

1. **启动注册中心**
   ```bash
   cd finance-registry
   mvn spring-boot:run
   ```

2. **启动配置中心**
   ```bash
   cd finance-config
   mvn spring-boot:run
   ```

3. **启动API网关**
   ```bash
   cd finance-gateway
   mvn spring-boot:run
   ```

4. **验证服务状态**
   - 注册中心: http://localhost:8761
   - 配置中心: http://localhost:8888
   - API网关: http://localhost:8080/actuator/health

## 📊 技术栈

### 核心框架
- **Spring Boot**: 3.1.5
- **Spring Cloud**: 2022.0.4
- **Spring Cloud Gateway**: API网关
- **Spring Cloud Config**: 配置中心
- **Eureka**: 服务注册发现

### 数据存储
- **MySQL**: 主数据库
- **Redis**: 缓存和会话存储
- **Git**: 配置文件存储

### 监控运维
- **Spring Boot Actuator**: 应用监控
- **Micrometer**: 指标收集
- **Prometheus**: 指标存储
- **Grafana**: 监控面板
- **ELK Stack**: 日志收集分析

### 安全认证
- **JWT**: 无状态认证
- **Spring Security**: 安全框架
- **OAuth2**: 授权协议

## 🔧 核心功能

### 已实现功能

#### ✅ 公共模块 (finance-common)
- 统一响应结果封装
- 全局异常处理
- JWT工具类
- 业务异常定义

#### ✅ 服务注册中心 (finance-registry)
- Eureka Server服务注册
- 服务健康检查
- 管理界面
- 多环境配置

#### ✅ 配置中心 (finance-config)
- Git仓库配置管理
- 配置动态刷新
- 多环境支持
- 健康检查

#### ✅ API网关 (finance-gateway)
- 动态路由配置
- JWT认证过滤器
- 限流功能 (Redis)
- 熔断器机制
- 跨域处理
- 负载均衡

### 待实现功能

#### 🔄 用户服务 (finance-user-service)
- 用户注册、登录
- 用户信息管理
- 权限控制
- JWT令牌管理

#### 🔄 股票服务 (finance-stock-service)
- 股票信息CRUD
- 股票查询和搜索
- 批量操作
- 实时数据更新

#### 🔄 数据分析服务 (finance-analysis-service)
- 技术指标计算 (MA、MACD、KDJ、RSI)
- 趋势分析
- 数据可视化
- 分析报告生成

#### 🔄 监控服务 (finance-monitor)
- Spring Boot Admin监控
- 系统指标收集
- 告警通知
- 性能分析

## 📖 文档说明

### 📋 [微服务架构设计方案.md](微服务架构设计方案.md)
详细的架构设计文档，包含：
- 现有架构分析
- 微服务拆分策略
- 技术选型说明
- 数据架构设计
- 安全架构设计
- 迁移策略

### 📋 [项目结构说明.md](项目结构说明.md)
项目结构详细说明，包含：
- 模块功能介绍
- 技术特点说明
- 端口分配表
- 启动顺序说明
- 开发注意事项

### 📋 [微服务调试文档.md](微服务调试文档.md)
全面的调试指南，包含：
- 环境准备指南
- 服务启动调试
- 问题排查方法
- 性能调试技巧
- 常见问题解决方案

### 📋 [系统监控与运维文档.md](系统监控与运维文档.md)
完整的监控运维体系，包含：
- 监控架构设计
- 告警配置规则
- 故障处理流程
- 性能优化策略
- 容量规划方案

## 🔍 服务端点

### 注册中心
- **管理界面**: http://localhost:8761
- **健康检查**: http://localhost:8761/actuator/health
- **服务列表**: http://localhost:8761/eureka/apps

### 配置中心
- **健康检查**: http://localhost:8888/actuator/health
- **配置获取**: http://localhost:8888/{application}/{profile}

### API网关
- **健康检查**: http://localhost:8080/actuator/health
- **路由信息**: http://localhost:8080/actuator/gateway/routes
- **熔断器状态**: http://localhost:8080/actuator/circuitbreakers

## 🛠️ 开发指南

### 代码规范
- 使用Spring Boot 3.x和Jakarta EE
- 遵循RESTful API设计原则
- 统一异常处理和响应格式
- 完善的日志记录

### 测试策略
- 单元测试：JUnit 5 + Mockito
- 集成测试：Spring Boot Test
- API测试：Postman + RestAssured
- 性能测试：JMeter

### 部署策略
- 开发环境：本地启动
- 测试环境：Docker Compose
- 生产环境：Kubernetes

## 🔒 安全特性

### 认证授权
- JWT无状态认证
- 统一权限控制
- 接口访问控制
- 敏感数据加密

### 网络安全
- HTTPS传输加密
- CORS跨域控制
- 请求限流保护
- SQL注入防护

## 📈 性能特性

### 缓存策略
- Redis分布式缓存
- 本地缓存优化
- 缓存预热机制
- 缓存一致性保证

### 负载均衡
- 客户端负载均衡
- 服务实例健康检查
- 故障实例自动剔除
- 流量分发策略

### 熔断降级
- 服务熔断保护
- 降级策略配置
- 自动恢复机制
- 依赖隔离设计

## 🚨 监控告警

### 应用监控
- JVM内存监控
- GC性能监控
- 接口响应时间
- 错误率统计

### 业务监控
- 用户活跃度
- 交易成功率
- 数据质量指标
- 系统可用性

### 基础设施监控
- 服务器资源监控
- 数据库性能监控
- 网络连通性监控
- 存储空间监控

## 🔄 下一步计划

### 短期目标 (1-2周)
1. 实现用户服务完整功能
2. 实现股票服务基础功能
3. 完善前端微服务适配
4. 创建Docker容器化配置

### 中期目标 (3-4周)
1. 实现数据分析服务
2. 完善监控服务功能
3. 实现分布式部署脚本
4. 性能优化和压力测试

### 长期目标 (1-2月)
1. 完善CI/CD流水线
2. 实现自动化运维
3. 完善安全防护体系
4. 生产环境部署上线

## 🤝 贡献指南

### 开发流程
1. Fork项目到个人仓库
2. 创建功能分支
3. 开发并测试功能
4. 提交Pull Request
5. 代码审查和合并

### 代码提交规范
```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 代码重构
test: 测试相关
chore: 构建过程或辅助工具的变动
```

## 📞 联系方式

- **项目维护**: 开发团队
- **技术支持**: 技术支持团队
- **问题反馈**: 通过Issue提交问题

## 📄 许可证

本项目采用 [MIT License](LICENSE) 开源协议。

---

**版本**: v1.0.0  
**最后更新**: 2025-10-22  
**维护团队**: 量融金融平台开发团队