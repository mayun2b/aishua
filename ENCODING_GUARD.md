# Encoding Guard

This repository enforces UTF-8 text files to avoid Chinese mojibake.

## One-time setup

1. Configure Git hooks path:
   - PowerShell: `pwsh .tools/setup-git-hooks.ps1`
   - Shell: `sh .tools/setup-git-hooks.sh`
2. Keep your editor encoding fixed to UTF-8.
   - Workspace default is already set in `.vscode/settings.json`.

## Manual check

Run before commit if needed:

```bash
node .tools/check-encoding.mjs
```

To check specific files:

```bash
node .tools/check-encoding.mjs path/to/file1 path/to/file2
```
