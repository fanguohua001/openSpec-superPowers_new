# 项目上下文

本文件用于记录应跨会话保留的稳定项目知识。

## 项目概览

- 本仓库是基于 `com.ruoyi:ruoyi:3.9.1` 的 RuoYi-Vue 管理系统。
- 后端是多模块 Maven 项目。根目录 `pom.xml` 聚合 `ruoyi-admin`、`ruoyi-framework`、`ruoyi-system`、`ruoyi-quartz`、`ruoyi-generator` 和 `ruoyi-common`。
- 前端位于 `ruoyi-ui`，使用 Vue 2、Vue CLI 4、Vuex、Vue Router、Element UI、Axios，并沿用现有 RuoYi 前端约定。
- `openspec/` 下已接入 OpenSpec，配置为 `schema: spec-driven`。

## 架构说明

- `ruoyi-admin` 放置 Web Controller 和对外 REST 接口。
- `ruoyi-system` 放置系统领域对象、Mapper 接口、MyBatis XML、Service 和相关测试。
- `ruoyi-ui/src/api/` 放置前端 API 封装；`ruoyi-ui/src/views/` 放置面向用户的 Vue 页面。
- RuoYi 前端侧栏菜单由后端 `/getRouters` 返回的 `sys_menu` 数据动态生成；新增页面文件不会自动出现在菜单中。
- 新增动态菜单入口时，至少需要同时考虑 `sys_menu` 菜单数据、`sys_role_menu` 角色授权、前端 `ruoyi-ui/src/views/` 组件路径和重新登录后的路由刷新。
- 最新归档能力 `custom-approval-flow` 增加了轻量级可复用审批流：流程定义、线性节点、节点审批人规则、审批实例、审批任务和审批记录。
- 审批流集成使用 `businessType`、`businessId` 和 `businessTitle`，使业务模块可以发起审批，而不直接耦合审批持久化表。
- 审批节点审批人支持指定用户和指定角色；首版采用或签规则，即任一候选审批人同意即可通过节点。
- 最新归档能力 `minesweeper-game` 增加了纯前端扫雷休闲游戏：页面位于 `ruoyi-ui/src/views/game/minesweeper/`，核心规则拆为 `minesweeper.js`，测试使用 Node 原生 `assert`。

## 协作约定

- 项目说明、生成的自然语言内容、注释和解释默认使用简体中文，除非用户明确要求其他语言。
- 生成或更新 Markdown 文件时，自然语言标题、正文、列表项、说明、模板文本和注释默认使用简体中文。
- 代码语法、命令、路径、配置键、接口字段、日志和报错原文保持原样。
- 本仓库的技能和工作流必须显式选择启用；不要仅因任务类型匹配就自动启用。
- 如果 `.superpowers-memory/` 存在，新会话开始时先读取 `PROJECT_CONTEXT.md`、`CURRENT_STATE.md` 和最新的 `session-journal/` 条目。

## 已知约束

- 后端目标版本为 Java 1.8 和 Spring Boot 2.5.15。
- 前端包元数据要求 Node `>=8.9`、npm `>=3.0.0`，但实际构建前仍需确认本地运行时。
- RuoYi 权限风格很重要：后端接口应沿用现有 `@PreAuthorize` 风格，前端按钮权限应与 `v-hasPermi` 对齐。
- 当前前端没有现成 Jest/Vitest 测试脚本；对纯前端规则模块，可优先使用 Node 原生 `assert` 做轻量测试，避免为单个小模块引入新测试框架。
