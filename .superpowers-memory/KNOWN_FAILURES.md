# 已知失败模式

本文件用于记录反复出现的失败模式、环境陷阱、流程陷阱和常见误判。

## 条目模板

```md
### 失败模式：<简短标题>
- id: failure-YYYY-MM-DD-<slug>
- type: failure_pattern
- status: active
- confidence: verified
- last_updated: YYYY-MM-DD
- source:
- owner:
- review_after:

触发条件：

现象：

可能原因：

检测方式：

缓解方式：
```

## 记录说明

- 优先记录重复出现或影响较大的失败，而不是一次性小问题。
- 如果某个失败模式已完全过时，标记为 `superseded` 并说明原因。
- 尽量链接验证证据。

### 失败模式：中文文本可能显示为乱码
- id: failure-2026-04-28-mojibake-risk
- type: failure_pattern
- status: active
- confidence: verified
- last_updated: 2026-04-28
- source: 终端读取 `ReadMe.md`、`pom.xml`、已归档 OpenSpec 文件、`git diff -- AGENTS.md`、`Get-Content -Encoding UTF8 AGENTS.md` 和 `AGENTS.md` 字节检查结果
- owner: 项目团队
- review_after: 编码修复或编辑器配置确认后复审

触发条件：

通过终端或编码假设不一致的工具读取、编辑中文项目文件。

现象：

中文文本显示为乱码（mojibake）。此前 `AGENTS.md` 的 diff 显示，原本可读的中文标题或规则被替换成乱码，同时新增了 Superpowers 记忆块。后续修复中还观察到：UTF-8 无 BOM 文件在旧版 Windows PowerShell 的裸 `Get-Content` 下可能显示乱码，但 `Get-Content -Encoding UTF8` 和 `git diff` 能正常显示。

可能原因：

编码不匹配，或文件被错误编码保存。部分输出可能只是显示问题；判断前需要区分“文件内容真实损坏”和“读取工具按错误编码显示”。

检测方式：

使用 `git diff -- AGENTS.md`、`Get-Content -Encoding UTF8 AGENTS.md`，并检查文件开头字节。`AGENTS.md` 当前应为 UTF-8 无 BOM，即开头字节直接是 `23 20 ...`，而不是 `EF BB BF ...`。

缓解方式：

不要顺手大范围重写中文文档。编辑受影响文件前，先确认预期编码，并把改动范围限制在当前需求内。若只是旧版 PowerShell 显示乱码，优先使用 `-Encoding UTF8` 读取，不要仅为了裸 `Get-Content` 显示正常而重新加入 BOM。

### 失败模式：新增前端页面后菜单不显示
- id: failure-2026-04-28-ruoyi-menu-not-visible
- type: failure_pattern
- status: active
- confidence: verified
- last_updated: 2026-04-28
- source: 扫雷页面新增后侧栏未显示；检查 `sys_menu`、`sys_role_menu`、`SysMenuServiceImpl.selectMenuTreeByUserId` 和本机 `ruoyi` 数据库
- owner: 项目团队
- review_after: 下次新增动态菜单功能后复审

触发条件：

在 `ruoyi-ui/src/views/` 下新增页面，并只修改前端或只新增 `sys_menu` 初始化数据。

现象：

前端编译通过，页面文件存在，但登录后侧栏没有新菜单。非管理员用户尤其容易遇到该问题。

可能原因：

RuoYi 侧栏菜单由后端 `/getRouters` 基于数据库动态生成。管理员用户读取全部 `sys_menu` 菜单；非管理员用户必须通过 `sys_role_menu` 关联到可用角色。若只写页面文件、不更新运行库 `sys_menu`，或只写 `sys_menu`、不写 `sys_role_menu`，侧栏都不会显示。补完数据库后，如果前端未重新登录，也可能仍沿用旧路由缓存。

检测方式：

查询运行库：

```sql
select menu_id, menu_name, parent_id, path, component, perms
from sys_menu
where menu_name in ('休闲游戏', '扫雷') or perms = 'game:minesweeper:view';

select rm.role_id, r.role_key, rm.menu_id
from sys_role_menu rm
left join sys_role r on rm.role_id = r.role_id
where rm.menu_id in (123, 124);
```

同时确认组件路径能对应到 `ruoyi-ui/src/views/<component>.vue` 或目录下 `index.vue`。

缓解方式：

新增动态菜单时同步提供初始化 SQL 和已初始化数据库的增量 SQL。增量脚本应写入 `sys_menu`，并按目标角色写入 `sys_role_menu`。执行后重新登录前端，让 `/getRouters` 重新生成路由。Windows PowerShell 中不要用管道直接把含中文 SQL 传给 `mysql`，优先使用 `mysql --default-character-set=utf8mb4 ... -e "source E:/path/file.sql"`，避免中文变成 `????`。
