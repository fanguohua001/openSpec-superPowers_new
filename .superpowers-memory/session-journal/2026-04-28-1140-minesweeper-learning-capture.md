# 扫雷菜单与学习沉淀记录

## 背景

用户在完成扫雷休闲游戏后反馈前端页面没有显示游戏菜单。随后显式启用 `superpowers-learning-workflow`，要求把本次经验沉淀到项目记忆中。

## 学到的稳定事实

- RuoYi 侧栏菜单由后端 `/getRouters` 基于数据库 `sys_menu` 动态生成，新增前端页面文件不会自动显示在菜单中。
- 管理员用户读取全部可用菜单；非管理员用户需要 `sys_role_menu` 角色授权才能看到菜单。
- 对已初始化数据库，新增菜单应提供幂等增量 SQL；完整初始化 SQL 只影响重新初始化的环境。
- Windows PowerShell 管道传递中文 SQL 给 `mysql` 可能导致中文变成 `????`；使用 `mysql --default-character-set=utf8mb4 ... -e "source E:/path/file.sql"` 更稳。

## 更新的记忆文件

- `PROJECT_CONTEXT.md`：补充 RuoYi 动态菜单接入事实、扫雷能力位置和轻量 Node 测试策略。
- `KNOWN_FAILURES.md`：新增“新增前端页面后菜单不显示”失败模式。
- `LEARNING_BACKLOG.md`：新增“RuoYi 动态菜单接入检查清单”候选经验。
- `DECISIONS.md`：新增“扫雷首版保持纯前端休闲功能”决策。
- `CURRENT_STATE.md`：更新当前关注点和下一步。

## 下一次会话应记住

- 若用户说“页面有了但菜单没显示”，先查运行库 `sys_menu` 和 `sys_role_menu`，不要只看前端路由。
- 修改菜单 SQL 后，需要重新登录前端，让 `/getRouters` 重新生成菜单。
- 扫雷后续若要排行榜或历史战绩，应单独提出变更，不直接塞进当前纯前端首版。
