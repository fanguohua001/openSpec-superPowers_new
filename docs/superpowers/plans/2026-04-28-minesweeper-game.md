# 扫雷休闲游戏实现计划

## 成功标准

- `add-minesweeper-game` OpenSpec 变更有效。
- 扫雷核心逻辑测试通过。
- 前端生产构建通过。
- SQL 初始化脚本包含“休闲游戏 / 扫雷”菜单，组件路径为 `game/minesweeper/index`。
- 新增页面不依赖后端接口，不影响现有业务页面。

## 执行步骤

1. OpenSpec 基线确认
   - 执行 `openspec validate add-minesweeper-game --no-color`。
   - verify: 命令输出 `Change 'add-minesweeper-game' is valid`。

2. 测试策略确认
   - 检查 `ruoyi-ui/package.json` 是否存在前端单元测试框架和测试脚本。
   - 若不存在，新增 Node 原生断言测试脚本，专测纯 JavaScript 游戏核心。
   - verify: `package.json` 中存在可运行扫雷测试的脚本。

3. TDD 实现游戏核心
   - 先写覆盖新局、首次安全、翻开扩散、标旗、胜负结束的测试。
   - 运行测试确认失败。
   - 实现 `minesweeper.js` 的最小规则逻辑。
   - verify: 扫雷核心测试通过。

4. 实现 Vue 页面
   - 新增 `ruoyi-ui/src/views/game/minesweeper/index.vue`。
   - 接入难度、自定义参数、重新开始、左键翻开、右键或长按标旗、计时和状态展示。
   - verify: 前端构建能解析页面和核心模块。

5. 菜单初始化 SQL
   - 检查现有 `sys_menu` ID。
   - 新增“休闲游戏”目录和“扫雷”菜单。
   - verify: SQL 中菜单 ID 未与当前初始化脚本冲突，组件路径和权限标识正确。

6. 最终验证
   - 运行扫雷核心测试。
   - 运行 `npm run build:prod`。
   - 运行 `openspec validate add-minesweeper-game --no-color`。
   - verify: 所有命令通过后再更新任务清单、归档 OpenSpec 变更并更新记忆。

## 工作区说明

当前主工作区已有用户或历史未提交内容：`AGENTS.md`、`.cursor/`、`.superpowers-memory/` 和 `CLAUDE.md`。本次实现只修改扫雷相关 OpenSpec 文档、前端页面、前端测试脚本、SQL 菜单和会话记忆，不回退无关改动。
