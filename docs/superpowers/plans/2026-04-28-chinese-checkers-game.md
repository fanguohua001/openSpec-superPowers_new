# 中国跳棋休闲游戏实现计划

## 成功标准

- `add-chinese-checkers-game` OpenSpec 变更有效。
- 中国跳棋核心逻辑测试通过。
- 前端生产构建通过。
- SQL 增量脚本包含“休闲游戏 / 跳棋”菜单，组件路径为 `game/chinese-checkers/index`。
- 新增页面不依赖后端接口，不影响现有扫雷、审批、系统管理和工具页面。

## 执行步骤

1. OpenSpec 基线确认
   - 执行 `openspec validate add-chinese-checkers-game --no-color`。
   - verify: 命令输出变更有效。

2. 测试策略确认
   - 检查 `ruoyi-ui/package.json` 已有测试脚本。
   - 沿用扫雷方案，新增 Node 原生断言测试脚本，专测纯 JavaScript 游戏核心。
   - verify: `package.json` 中存在可运行中国跳棋测试的脚本。

3. TDD 实现游戏核心
   - 先写覆盖棋盘、初始摆放、选择、一步移动、跳跃、连续跳跃、胜负判断的测试。
   - 运行测试确认失败。
   - 实现 `chineseCheckers.js` 的最小规则逻辑。
   - verify: 中国跳棋核心测试通过。

4. 实现 Vue 页面
   - 新增 `ruoyi-ui/src/views/game/chinese-checkers/index.vue`。
   - 接入状态展示、棋盘渲染、棋子选择、合法落点高亮、移动、连续跳跃、结束回合和重新开始。
   - verify: 前端构建能解析页面和核心模块。

5. 菜单增量 SQL
   - 复用或补充“休闲游戏”目录。
   - 新增“跳棋”菜单和 `common` 角色授权。
   - verify: SQL 中组件路径和权限标识正确，菜单 ID 避开扫雷已有 ID。

6. 最终验证
   - 运行中国跳棋核心测试。
   - 运行 `npm run build:prod`。
   - 运行 `openspec validate add-chinese-checkers-game --no-color`。
   - verify: 所有命令通过后再更新任务清单和项目记忆。

## 工作区说明

当前主工作区已有历史未提交内容，集中在 `.superpowers-memory/` 和扫雷页面。本次实现只修改中国跳棋相关 OpenSpec 文档、前端页面、前端测试脚本、SQL 菜单、`package.json` 测试脚本和会话记忆，不回退无关改动。
