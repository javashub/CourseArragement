# CLAUDE.md

@AGENTS.md

共享规范见 `AGENTS.md`；本文件只保留 Claude Code 专属约定，不重复其内容。

## Claude Code 专属

- 子目录规范：`src/AGENTS.md`（后端）、`UI/coursearrange/AGENTS.md`（前端）。打开对应目录文件时以子目录规范为准。
- 前端命令先 `cd UI/coursearrange`，后端命令在仓库根执行。
- 优先使用 `.claude/settings.local.json` 已授权的命令，不主动申请扩大 Bash 白名单。
- 多步骤任务先给出简短计划与验收方式；验证失败时保留完整输出，不静默重试。
- 背景资料按需读取 `docs/`、`doc/`，不要把长篇架构说明写回指令文件。
