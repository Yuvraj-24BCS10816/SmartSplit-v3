$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

$mavenCommand = Get-Command mvn -ErrorAction SilentlyContinue
if ($mavenCommand) {
    $maven = $mavenCommand.Source
} elseif (Test-Path "C:\maven\apache-maven-3.10.0\bin\mvn.cmd") {
    $maven = "C:\maven\apache-maven-3.10.0\bin\mvn.cmd"
} else {
    throw "Maven was not found. Add Maven to PATH or install it at C:\maven\apache-maven-3.10.0."
}

Write-Host "Using Java:"
java -version
& $maven clean test
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
& $maven javafx:run
exit $LASTEXITCODE
