# 当前状态

本文件用于记录下一次会话应优先读取的最新工作上下文。

## 当前关注点

- 当前任务：使用 `superpowers-learning-workflow` 沉淀扫雷菜单不显示的排查经验和扫雷首版实现经验。
- 当前原因：扫雷功能已完成并验证，随后发现动态菜单还需要运行库 `sys_menu` 和 `sys_role_menu` 同步；该经验对后续新增 RuoYi 菜单有复用价值。

## 最新决策

- 决策：将 Superpowers 记忆视为被动的项目记忆辅助，而不是自动启用 Superpowers 工作流的许可。
- 原因：仓库规则要求用户显式选择启用后才调用技能/工作流。
- 决策：自定义审批流保持轻量且项目原生，不引入 BPMN/工作流引擎。
- 原因：已归档的 OpenSpec 变更目标是贴合现有 RuoYi Controller/Service/Mapper/Vue 模式的小型可复用审批能力。
- 决策：扫雷首版采用纯前端实现，不新增后端排行榜、历史记录或持久化接口。
- 原因：用户确认的是“轻量前端游戏 + 菜单接入”方案，首版目标是可玩、可测、改动收敛。
- 决策：扫雷核心规则拆到 `ruoyi-ui/src/views/game/minesweeper/minesweeper.js`，测试使用 Node 原生 `assert`。
- 原因：`ruoyi-ui` 没有现成 Jest/Vitest 脚本，不为单个轻量规则模块引入新测试框架。
- 决策：将“RuoYi 动态菜单接入检查清单”暂存为学习候选，而不是立即提升为正式规则。
- 原因：当前只有一次明确案例；若后续再次新增动态菜单仍适用，再提升成正式项目检查清单。

## 近期仓库状态

- `HEAD` 是 `2cbcc06 Add approval schema and role-based user lookup`。
- 近期已提交工作新增了 `custom-approval-flow`，覆盖后端 Controller/Service/Mapper/domain 对象、SQL、测试、前端 API 封装和 Vue 页面。
- 本次新增扫雷相关未提交内容：
  - `ruoyi-ui/src/views/game/minesweeper/index.vue`
  - `ruoyi-ui/src/views/game/minesweeper/minesweeper.js`
  - `ruoyi-ui/src/views/game/minesweeper/minesweeper.test.js`
  - `ruoyi-ui/package.json`
  - `sql/ry_20250522.sql`
  - `docs/superpowers/specs/2026-04-28-minesweeper-game-design.md`
  - `docs/superpowers/plans/2026-04-28-minesweeper-game.md`
  - `openspec/specs/minesweeper-game/spec.md`
  - `openspec/changes/archive/2026-04-28-add-minesweeper-game/`
- 当前工作区仍有本次任务外的既有未提交/未跟踪内容：`AGENTS.md`、`.cursor/`、`.superpowers-memory/` 和 `CLAUDE.md`。
- `AGENTS.md` 当前保留“生成文件语言”和“Markdown 文件语言”两条规则，并包含 Superpowers 记忆读取说明。
- `AGENTS.md` 已确认为 UTF-8 无 BOM；使用 `Get-Content -Encoding UTF8 AGENTS.md` 和 `git diff -- AGENTS.md` 可正常显示中文。
- `.superpowers-memory/` 下的 Markdown 文件标题和自然语言正文已转换为简体中文。
- 扫雷菜单 SQL 使用 `sys_menu` ID `123` 和 `124`，路径为 `game/minesweeper/index`，权限标识为 `game:minesweeper:view`。
- OpenSpec 变更 `add-minesweeper-game` 已归档为 `openspec/changes/archive/2026-04-28-add-minesweeper-game/`，主规格为 `openspec/specs/minesweeper-game/spec.md`。
- 发现并修复：如果只写 `sys_menu` 而不写 `sys_role_menu`，非管理员用户不会显示“休闲游戏 / 扫雷”菜单。
- 已新增 `sql/minesweeper_menu.sql` 作为已初始化数据库的增量菜单脚本，并已在本机 `ruoyi` 数据库执行，确认 `common` 角色拥有菜单 `123` 和 `124`。
- 本次学习沉淀已更新 `PROJECT_CONTEXT.md`、`KNOWN_FAILURES.md`、`LEARNING_BACKLOG.md`、`DECISIONS.md` 和最新 session journal。

## 未解决问题

- 旧版 Windows PowerShell 裸 `Get-Content AGENTS.md` 可能仍按系统默认编码显示为乱码；这不代表 `AGENTS.md` 内容再次损坏，应优先用 `-Encoding UTF8`、字节检查和 `git diff` 判断。
- 其他历史文档是否存在真实编码损坏仍需单独确认。
- `npm run build:prod` 通过，但仍输出 RuoYi 既有的 asset/entrypoint size warning；这不是扫雷编译失败。
- 本次未重新运行 Maven 后端测试，因为扫雷首版未修改后端 Java 代码。
- 前端侧栏菜单由登录后的 `/getRouters` 动态生成；补完数据库菜单后，需要退出登录再进入或强制刷新页面，才能看到新菜单。

## 建议下一步

- 下一步：重新登录前端系统，从“休闲游戏 / 扫雷”进入页面做一次浏览器交互检查。
- 建议负责人：下一次编码助手会话或用户本地验证。
