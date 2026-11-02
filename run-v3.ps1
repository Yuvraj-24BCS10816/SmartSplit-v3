$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot
& (Join-Path $PSScriptRoot "run.ps1")
exit $LASTEXITCODE
