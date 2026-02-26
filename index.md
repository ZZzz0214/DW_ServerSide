# yudao-boot-mini 目录索引

> 芋道项目基础脚手架（后端），基于 Spring Boot 2.7 + JDK 8，版本 2.4.0-jdk8-SNAPSHOT

## 根目录文件

- **[pom.xml](./pom.xml)** - Maven 多模块父工程配置文件
- **[README.md](./README.md)** - 项目说明文档（后端 3.12）
- **[LICENSE](./LICENSE)** - 项目开源许可证
- **[lombok.config](./lombok.config)** - Lombok 全局配置
- **[.gitignore](./.gitignore)** - Git 忽略文件规则
- **[database_migration.sql](./database_migration.sql)** - 财务金额表重构数据库迁移脚本
- **[update_brand_field.sql](./update_brand_field.sql)** - 品牌字段从 ID 改为名称的迁移脚本

## 核心模块

### yudao-server/

- 主应用服务器启动模块，包含 Spring Boot 启动类和应用配置
- **[src/main/resources/application.yaml](./yudao-server/src/main/resources/application.yaml)** - 主应用配置文件（Spring、Cache、Servlet 等）

### yudao-dependencies/

- Maven BOM 统一依赖版本管理模块

### yudao-framework/

核心技术框架 Starter 合集：

- **yudao-common/** - 通用工具类与基础组件（参数校验等）
- **yudao-spring-boot-starter-web/** - Web 层封装（SpringMVC、Swagger API 文档）
- **yudao-spring-boot-starter-security/** - 安全框架（Spring Security 认证授权）
- **yudao-spring-boot-starter-mybatis/** - 数据库层（MyBatis、连接池、多数据源读写分离）
- **yudao-spring-boot-starter-redis/** - Redis 缓存与 Spring Cache 集成
- **yudao-spring-boot-starter-mq/** - 消息队列（RocketMQ、RabbitMQ、Kafka、Event）
- **yudao-spring-boot-starter-job/** - 定时任务与异步任务
- **yudao-spring-boot-starter-excel/** - Excel 导入导出封装
- **yudao-spring-boot-starter-monitor/** - 监控（Actuator、Admin、SkyWalking 链路追踪）
- **yudao-spring-boot-starter-protection/** - 服务保护（限流、熔断等）
- **yudao-spring-boot-starter-websocket/** - WebSocket 实时通信
- **yudao-spring-boot-starter-biz-tenant/** - 多租户业务支持
- **yudao-spring-boot-starter-biz-data-permission/** - 数据权限控制
- **yudao-spring-boot-starter-biz-ip/** - IP 地址解析

## 业务模块

### yudao-module-erp/

ERP 进销存核心业务模块，包含：

- **yudao-module-erp-api/** - ERP 模块 API 接口定义
- **yudao-module-erp-biz/** - ERP 模块业务实现
- **[README-statistics-optimization.md](./yudao-module-erp/README-statistics-optimization.md)** - 代发批发统计报表 ES 聚合查询性能优化
- **[导入功能字段检查文档.md](./yudao-module-erp/导入功能字段检查文档.md)** - ERP 模块所有导入功能字段完整检查
- **[导入更新问题分析报告.md](./yudao-module-erp/导入更新问题分析报告.md)** - 20 个导入更新功能的分析与修复报告

### yudao-module-system/

系统管理模块（用户、角色、权限、菜单、字典、租户等）

### yudao-module-infra/

基础设施模块（代码生成、配置管理、文件管理、定时任务、API 日志等）

### yudao-module-member/

会员中心模块（会员管理、积分、等级等）

### yudao-module-pay/

支付中心模块（支付渠道、支付订单、退款等）

### yudao-module-mall/

商城模块（商品、订单、营销、物流等）

## 数据库脚本

### sql/

多数据库平台 SQL 初始化与迁移脚本：

- **mysql/** - MySQL 主数据库初始化脚本（ruoyi-vue-pro.sql、quartz.sql 等）
- **postgresql/** - PostgreSQL 适配脚本
- **oracle/** - Oracle 适配脚本
- **sqlserver/** - SQL Server 适配脚本
- **dm/** - 达梦数据库适配脚本
- **kingbase/** - 人大金仓适配脚本
- **opengauss/** - openGauss 适配脚本
- **db2/** - DB2 适配说明
- **tools/** - 数据库工具脚本（Oracle、SQL Server 用户创建等）
- **后端sql修改/** - 业务迭代 SQL 变更脚本（产品表、采购/销售订单表、货盘表、财务表、客户表等）
- **货盘表字段增强/** - 货盘表新增 13 个字段的 SQL 脚本

## 部署脚本

### script/

- **docker/** - Docker Compose 部署配置与使用指南
- **jenkins/** - Jenkins CI/CD 流水线配置（Jenkinsfile）
- **shell/** - Shell 部署脚本（deploy.sh）
- **idea/** - IntelliJ IDEA HTTP Client 环境配置

## 前端项目（内嵌）

### yudao-ui/

内嵌的多端前端项目集合：

- **yudao-ui-admin-vue3/** - Vue3 管理后台
- **yudao-ui-admin-vue2/** - Vue2 管理后台
- **yudao-ui-admin-vben/** - Vben Admin 管理后台
- **yudao-ui-admin-uniapp/** - UniApp 管理端
- **yudao-ui-mall-uniapp/** - UniApp 商城端
