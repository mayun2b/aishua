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

## Troubleshooting

If you see mojibake (for example: ``\u7487\u5b58\u69d1\u951b...``), the file is usually UTF-8 but opened with a non-UTF-8 decoder.

1. In IntelliJ IDEA, set `Settings -> Editor -> File Encodings`:
   - `Global Encoding`: `UTF-8`
   - `Project Encoding`: `UTF-8`
   - `Default encoding for properties files`: `UTF-8`
2. Reopen the file and use `File -> File Properties -> Reload from Disk`.
3. In PowerShell, read files with explicit encoding:
   - `Get-Content -Encoding UTF8 path/to/file`
4. Write files with explicit UTF-8 (no BOM):
   - `Set-Content -Encoding utf8NoBOM path/to/file $content`
