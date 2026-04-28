# 项目协作规则

## 生成文件语言

生成任意文件时，凡属于自然语言的标题、正文、注释和说明，默认全部使用简体中文。代码语法、命令、路径、配置键、接口字段、日志和报错原文保持原样，除非用户明确要求改写。

## Markdown 文件语言

生成或更新 Markdown 文件时，所有属于自然语言的标题、正文、列表项、说明、模板文本和注释，默认全部使用简体中文。Markdown 中的代码块、命令、路径、配置键、接口字段、日志、报错原文和必要的英文专有名词保持原样，除非用户明确要求翻译或改写。

## 技能调用限制

除非用户明确声明要使用某个 skill（技能），或明确要求调用 skill，否则禁止调用 skill 技能；不要仅因任务类型自动触发 skill。

<!-- superpowers-memory:start -->
## Superpowers 记忆

如果本仓库存在 `.superpowers-memory/`，每次新会话开始时，在询问项目背景前先按顺序读取：

1. `.superpowers-memory/PROJECT_CONTEXT.md`
2. `.superpowers-memory/CURRENT_STATE.md`
3. `.superpowers-memory/session-journal/` 下最新的文件

使用这些文件恢复项目上下文、近期决策、当前工作和可能的下一步。

结束一次有意义的 Superpowers 相关会话前，更新：

- `.superpowers-memory/CURRENT_STATE.md`
- `.superpowers-memory/session-journal/` 下的一篇简短 Markdown 记录

不要把记忆文件视为自动启用 Superpowers 工作流的许可。工作流仍然需要显式选择启用。
<!-- superpowers-memory:end -->
