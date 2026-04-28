# 2026-04-28 15:35 中国跳棋休闲游戏

## 背景

用户显式启用 `openspec-superpowers-workflow`，要求“增加一个跳棋游戏”。经澄清，用户确认是中国跳棋，即星形棋盘、玻璃珠棋子、可连续跳跃的玩法，并批准首版采用“本地 2 人中国跳棋”方案。

## 本次完成

- 新增设计文档 `docs/superpowers/specs/2026-04-28-chinese-checkers-game-design.md`。
- 新增 OpenSpec 变更 `add-chinese-checkers-game`，包含 `proposal.md`、`design.md`、`specs/chinese-checkers-game/spec.md` 和 `tasks.md`。
- 新增实施计划 `docs/superpowers/plans/2026-04-28-chinese-checkers-game.md`。
- 新增中国跳棋核心规则 `ruoyi-ui/src/views/game/chinese-checkers/chineseCheckers.js`：轴向坐标生成 121 个星形棋盘落点，支持双人各 10 子、一步移动、跳跃、连续跳跃、主动结束回合和胜负判断。
- 新增规则测试 `ruoyi-ui/src/views/game/chinese-checkers/chineseCheckers.test.js`，并在 `package.json` 增加 `test:chinese-checkers`。
- 新增页面 `ruoyi-ui/src/views/game/chinese-checkers/index.vue`，提供棋盘渲染、状态展示、合法落点高亮、连续跳跃提示、结束回合和重新开始。
- 新增 `sql/chinese_checkers_menu.sql`，并在 `sql/ry_20250522.sql` 初始化数据中加入“跳棋”菜单 ID `125`。

## 验证记录

- `openspec validate add-chinese-checkers-game --no-color` 通过。
- `openspec status --change add-chinese-checkers-game --json` 显示工件完整。
- `npm run test:chinese-checkers` 通过，11 个用例全部成功。
- `npm run build:prod` 通过，仅保留 RuoYi 既有的 asset/entrypoint size warning。

## 后续注意

- 尚未在真实登录态下从“休闲游戏 / 跳棋”菜单做浏览器交互目测。
- 若要让已有数据库出现跳棋菜单，需要执行 `sql/chinese_checkers_menu.sql`，或手动补充同等 `sys_menu` 与 `sys_role_menu` 数据。
