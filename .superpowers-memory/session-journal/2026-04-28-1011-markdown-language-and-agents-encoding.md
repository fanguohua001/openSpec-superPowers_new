# Markdown 中文化与 AGENTS 编码修复记录

## 变更内容

- 将 `.superpowers-memory/` 下生成的 Markdown 文件标题、模板说明和自然语言正文转换为简体中文。
- 在 `AGENTS.md` 中保留“生成文件语言”规则，并新增“Markdown 文件语言”规则。
- 修复 `AGENTS.md` 编码表现：文件内容为正确中文，当前保存为 UTF-8 无 BOM，避免 `git diff` 首行出现 BOM 前缀。

## 决策

- Markdown 自然语言内容默认使用简体中文；代码块、命令、路径、配置键、接口字段、日志和原始报错保持原样。
- 判断中文乱码时，先区分文件真实损坏和终端读取方式错误。

## 验证

- 运行 `git diff -- AGENTS.md`，确认 diff 中中文正常且不再出现首行 BOM 前缀。
- 检查 `AGENTS.md` 文件开头字节，确认不是 `EF BB BF`。
- 使用 `Get-Content -Encoding UTF8 AGENTS.md` 确认中文正文正常。
- 未运行 Maven、前端构建或 OpenSpec 验证；本次仅涉及文档和记忆文件。

## 下一步

- 下次会话若看到旧版 PowerShell 裸 `Get-Content AGENTS.md` 显示乱码，先使用 `Get-Content -Encoding UTF8 AGENTS.md` 或 `git diff -- AGENTS.md` 复核，不要直接判定文件内容损坏。
