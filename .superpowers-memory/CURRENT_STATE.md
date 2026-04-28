# 当前状态

本文件用于记录下一次会话应优先读取的最新工作上下文。

## 当前关注点

- 当前任务：使用显式启用的 `openspec-superpowers-workflow` 新增本地双人中国跳棋休闲游戏。
- 当前原因：用户确认“跳棋”指中国跳棋，即星形棋盘、玻璃珠棋子、可连续跳跃，并批准首版采用“本地 2 人中国跳棋”方案。

## 最新决策

- 决策：将 Superpowers 记忆视为被动的项目记忆辅助，而不是自动启用 Superpowers 工作流的许可。
- 原因：仓库规则要求用户显式选择启用后才调用技能/工作流。
- 决策：自定义审批流保持轻量且项目原生，不引入 BPMN/工作流引擎。
- 原因：已归档的 OpenSpec 变更目标是贴合现有 RuoYi Controller/Service/Mapper/Vue 模式的小型可复用审批能力。
- 决策：扫雷首版采用纯前端实现，不新增后端排行榜、历史记录或持久化接口。
- 原因：用户确认的是“轻量前端游戏 + 菜单接入”方案，首版目标是可玩、可测、改动收敛。
- 决策：扫雷核心规则拆到 `ruoyi-ui/src/views/game/minesweeper/minesweeper.js`，测试使用 Node 原生 `assert`。
- 原因：`ruoyi-ui` 没有现成 Jest/Vitest 脚本，不为单个轻量规则模块引入新测试框架。
- 决策：中国跳棋首版采用纯前端本地双人实现，不新增电脑对手、联网对战、排行榜或持久化接口。
- 原因：首版目标是可玩、可测、改动收敛，且可复用扫雷的前端规则模块和 Node 原生测试模式。
- 决策：中国跳棋规则核心拆到 `ruoyi-ui/src/views/game/chinese-checkers/chineseCheckers.js`，使用轴向坐标生成 121 个星形棋盘落点。
- 原因：轴向坐标天然适配六个方向的一步移动和跳跃计算，避免手写 121 个点的邻接表。
- 决策：中国跳棋连续跳跃记录本回合已到达落点，禁止同一跳跃链跳回已访问落点。
- 原因：否则一次跳跃后几乎总能沿原路跳回，连续跳跃状态难以自然结束。
- 决策：将“RuoYi 动态菜单接入检查清单”暂存为学习候选，而不是立即提升为正式规则。
- 原因：当前只有一次明确案例；若后续再次新增动态菜单仍适用，再提升成正式项目检查清单。

## 近期仓库状态

- `HEAD` 是 `2cbcc06 Add approval schema and role-based user lookup`。
- 近期已提交工作新增了 `custom-approval-flow`，覆盖后端 Controller/Service/Mapper/domain 对象、SQL、测试、前端 API 封装和 Vue 页面。
- 当前工作区最近一次 `git status --short` 显示的改动集中在：
  - `.superpowers-memory/KNOWN_FAILURES.md`
  - `.superpowers-memory/LEARNING_BACKLOG.md`
  - `ruoyi-ui/src/views/game/minesweeper/index.vue`
- 新增 OpenSpec 变更 `add-chinese-checkers-game`，当前变更工件完整，`openspec validate add-chinese-checkers-game --no-color` 通过。
- 新增设计文档 `docs/superpowers/specs/2026-04-28-chinese-checkers-game-design.md` 和实施计划 `docs/superpowers/plans/2026-04-28-chinese-checkers-game.md`。
- 新增中国跳棋页面 `ruoyi-ui/src/views/game/chinese-checkers/index.vue`。
- 新增中国跳棋核心规则 `ruoyi-ui/src/views/game/chinese-checkers/chineseCheckers.js` 和测试 `chineseCheckers.test.js`。
- `ruoyi-ui/package.json` 新增脚本 `test:chinese-checkers`。
- 新增 `sql/chinese_checkers_menu.sql` 作为已初始化数据库的跳棋菜单增量脚本，并在 `sql/ry_20250522.sql` 初始化数据中加入菜单 ID `125`。
- 跳棋菜单路径为 `game/chinese-checkers/index`，权限标识为 `game:chinese-checkers:view`，复用“休闲游戏”目录 ID `123`。
- 验证结果：`npm run test:chinese-checkers` 通过，11 个用例全部成功；`npm run build:prod` 通过，仅保留 RuoYi 既有的 asset/entrypoint size warning。
- `AGENTS.md` 当前保留“生成文件语言”和“Markdown 文件语言”两条规则，并包含 Superpowers 记忆读取说明。
- `AGENTS.md` 已确认为 UTF-8 无 BOM；使用 `Get-Content -Encoding UTF8 AGENTS.md` 和 `git diff -- AGENTS.md` 可正常显示中文。
- `.superpowers-memory/` 下的 Markdown 文件标题和自然语言正文已转换为简体中文。
- 扫雷菜单 SQL 使用 `sys_menu` ID `123` 和 `124`，路径为 `game/minesweeper/index`，权限标识为 `game:minesweeper:view`。
- OpenSpec 变更 `add-minesweeper-game` 已归档为 `openspec/changes/archive/2026-04-28-add-minesweeper-game/`，主规格为 `openspec/specs/minesweeper-game/spec.md`。
- 发现并修复：如果只写 `sys_menu` 而不写 `sys_role_menu`，非管理员用户不会显示“休闲游戏 / 扫雷”菜单。
- 已新增 `sql/minesweeper_menu.sql` 作为已初始化数据库的增量菜单脚本，并已在本机 `ruoyi` 数据库执行，确认 `common` 角色拥有菜单 `123` 和 `124`。
- 扫雷页面后续 UI 优化集中在 `ruoyi-ui/src/views/game/minesweeper/index.vue`：增加胜负结果条、用 Element 图标表达旗帜/地雷/误标、强化键盘与焦点状态，并把移动端格子尺寸提升到更适合触控的范围。
- `ui-ux-pro-max` 技能目录曾出现资源损坏：`C:\Users\Administrator\.agents\skills\ui-ux-pro-max\scripts` 和 `data` 是普通文本指针文件，不是真实目录。已从 `C:\Users\Administrator\.claude\plugins\marketplaces\ui-ux-pro-max-skill\src\ui-ux-pro-max\` 恢复真实目录，并运行 `scripts/search.py` 的 design-system、UX、React Native 查询验证。
- 本次学习沉淀已更新 `KNOWN_FAILURES.md`、`LEARNING_BACKLOG.md`，并新增当前 session journal。

## 未解决问题

- 旧版 Windows PowerShell 裸 `Get-Content AGENTS.md` 可能仍按系统默认编码显示为乱码；这不代表 `AGENTS.md` 内容再次损坏，应优先用 `-Encoding UTF8`、字节检查和 `git diff` 判断。
- 其他历史文档是否存在真实编码损坏仍需单独确认。
- `npm run build:prod` 通过，但仍输出 RuoYi 既有的 asset/entrypoint size warning；这不是扫雷或跳棋编译失败。
- 本次未重新运行 Maven 后端测试，因为扫雷首版未修改后端 Java 代码。
- 前端侧栏菜单由登录后的 `/getRouters` 动态生成；补完数据库菜单后，需要退出登录再进入或强制刷新页面，才能看到新菜单。
- 跳棋页面尚未在真实浏览器登录态下从“休闲游戏 / 跳棋”菜单做交互目测。

## 建议下一步

- 下一步：登录前端系统，从“休闲游戏 / 跳棋”进入页面，检查菜单可见性、棋盘星形布局、棋子选择、合法落点高亮、连续跳跃和结束回合体验。
- 建议负责人：下一次编码助手会话或用户本地验证。
