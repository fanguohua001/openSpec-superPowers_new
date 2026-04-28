# 扫雷休闲游戏实现记录

## 变更内容

- 使用 `superpowers-openspec-execution-workflow` 完成“扫雷休闲游戏”从设计、OpenSpec 工件、实现、验证到归档的流程。
- 新增 `docs/superpowers/specs/2026-04-28-minesweeper-game-design.md` 和 `docs/superpowers/plans/2026-04-28-minesweeper-game.md`。
- 新增并归档 OpenSpec 变更 `add-minesweeper-game`，主规格落在 `openspec/specs/minesweeper-game/spec.md`。
- 新增 `ruoyi-ui/src/views/game/minesweeper/index.vue`，提供难度选择、自定义行列雷数、重新开始、计时、剩余雷数、左键翻开、右键或长按标旗、胜负展示。
- 新增 `ruoyi-ui/src/views/game/minesweeper/minesweeper.js`，实现扫雷纯 JavaScript 核心规则。
- 新增 `ruoyi-ui/src/views/game/minesweeper/minesweeper.test.js`，使用 Node 原生 `assert` 覆盖核心规则。
- 在 `ruoyi-ui/package.json` 新增 `test:minesweeper` 脚本。
- 在 `sql/ry_20250522.sql` 新增“休闲游戏 / 扫雷”菜单，权限标识为 `game:minesweeper:view`。
- 补充 `sql/minesweeper_menu.sql`，用于对已初始化数据库增量写入“休闲游戏 / 扫雷”菜单。
- 发现菜单不显示的原因之一是非管理员角色缺少 `sys_role_menu` 授权，已为默认 `common` 角色补充菜单 `123` 和 `124`。

## 决策

- 首版不做排行榜、历史记录或后端持久化。
- 使用现有 Element UI 和 RuoYi 页面风格，不新增运行时依赖。
- 采用延迟布雷保证首次点击安全。
- 大棋盘保持固定格子尺寸，并通过横向滚动适配窄屏。
- RuoYi 前端侧栏来自后端 `/getRouters`，不是前端文件自动注册；新增菜单后需要数据库菜单数据和角色授权同时存在。

## 验证

- 运行 `npm run test:minesweeper`，14 个扫雷核心逻辑用例全部通过。
- 运行 `npm run build:prod`，前端生产构建通过；仅保留项目已有资源体积 warning。
- 运行 `openspec validate add-minesweeper-game --no-color`，变更归档前有效。
- 运行 `openspec status --change add-minesweeper-game --json`，归档前工件完整。
- 运行 `openspec archive add-minesweeper-game --yes`，变更归档成功。
- 运行 `openspec validate minesweeper-game --no-color`，归档后的主规格有效。
- 使用 MySQL `source E:/MyDocNew/AI_F/sql/minesweeper_menu.sql` 对本机 `ruoyi` 数据库执行增量脚本，确认 `sys_menu` 中文正常且 `common` 角色已绑定菜单。

## 下一步

- 重新登录前端系统，让 `/getRouters` 重新生成侧栏，从“休闲游戏 / 扫雷”进入页面手工游玩验证。
- 若后续需要排行榜，再单独提出后端表、接口、权限和页面扩展。
