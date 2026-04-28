# 学习候选积压

本文件用于跟踪可能值得未来沉淀为工作流、技能、检查清单或项目规则的可复用经验。

## 候选条目格式

```md
### 候选：<简短名称>
- candidate_id: learn-YYYY-MM-DD-<slug>
- type: learning_candidate
- status: proposed
- confidence: inferred
- last_updated: YYYY-MM-DD
- source:
- owner:
- review_after:
- trigger:
- repeated_pattern:
- impact:
- evidence_count:
- repeated_times:
- suggested_artifact:
- promote_decision:
- linked_entries:
```

## 提升指导

- 候选经验在出现重复模式或明确跨会话价值前，保持 `status: proposed`。
- 当证据足够且目标产物清晰时，移动到 `ready_for_promotion`。
- 当经验不再适用或已经转化为持久产物时，使用 `superseded`。

## 记录说明

- 持久项目事实写入 `PROJECT_CONTEXT.md`，不要写在这里。
- 当前任务状态写入 `CURRENT_STATE.md`，不要写在这里。
- 一次性会话总结写入 `session-journal/`，不要写在这里。
- 本文件只记录看起来可跨未来会话复用的经验。

### 候选：RuoYi 动态菜单接入检查清单
- candidate_id: learn-2026-04-28-ruoyi-dynamic-menu-checklist
- type: learning_candidate
- status: proposed
- confidence: verified_once
- last_updated: 2026-04-28
- source: 扫雷页面新增后菜单不显示的排查与修复
- owner: 项目团队
- review_after: 下次新增需要侧栏入口的前端页面后
- trigger: 新增 `ruoyi-ui/src/views/` 页面并希望显示在侧栏菜单
- repeated_pattern: 待验证；当前已出现一次明确案例
- impact: 可避免前端页面已存在但 `/getRouters` 不返回菜单的误判
- evidence_count: 1
- repeated_times: 1
- suggested_artifact: 项目检查清单或 OpenSpec 任务模板条目
- promote_decision: 暂不提升；若再次新增动态菜单时复现或继续适用，再提升为正式规则
- linked_entries: `failure-2026-04-28-ruoyi-menu-not-visible`

候选经验：

- 新增 RuoYi 动态菜单入口时，同时检查 `sys_menu`、`sys_role_menu`、前端组件路径、角色权限和重新登录刷新。
- 对已初始化数据库，除修改完整初始化 SQL 外，还应提供幂等增量 SQL。
- 含中文 SQL 在 Windows 下执行时，应显式指定 `utf8mb4` 并优先使用 MySQL `source`，避免管道编码损坏中文。

### 候选：技能脚本资源可用性检查
- candidate_id: learn-2026-04-28-skill-resource-availability-check
- type: learning_candidate
- status: proposed
- confidence: verified_once
- last_updated: 2026-04-28
- source: 修复 `ui-ux-pro-max` 的 `scripts` 和 `data` 资源目录
- owner: 本机 Codex/Agents 技能环境
- review_after: 下次遇到技能脚本或数据文件缺失时
- trigger: 技能文档引用 `scripts/`、`data/` 或其他资源目录，但工具运行报文件不存在
- repeated_pattern: 待验证；当前已出现一次明确案例
- impact: 可避免误判为 Python 或命令问题，快速定位技能安装资源损坏
- evidence_count: 1
- repeated_times: 1
- suggested_artifact: 技能故障排查检查清单
- promote_decision: 暂不提升；若其他技能也出现资源指针文件失效，再提升为正式规则
- linked_entries: `failure-2026-04-28-skill-resource-pointer-files`

候选经验：

- 运行技能脚本失败时，先检查资源路径是目录还是普通文件。
- 若是指针文件，读取其内容并搜索本机插件缓存中真实资源。
- 修复后运行该技能的代表性命令，而不是只看文件复制成功。
