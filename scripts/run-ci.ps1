#Requires -Version 5.1
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
if (-not (Test-Path (Join-Path $root 'Jenkinsfile'))) {
  $root = (Get-Location).Path
}

function Invoke-Stage([string]$Name, [scriptblock]$Action) {
  Write-Host ""
  Write-Host "======== $Name ========" -ForegroundColor Cyan
  & $Action
  if ($LASTEXITCODE -ne 0 -and $null -ne $LASTEXITCODE) {
    throw "Stage FAILED: $Name (exit $LASTEXITCODE)"
  }
  Write-Host "OK $Name" -ForegroundColor Green
}

Set-Location $root
$env:JAVA_HOME = if ($env:JAVA_HOME) { $env:JAVA_HOME } else { 'C:\Program Files\Java\jdk-17' }
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path

Invoke-Stage 'Checkout (workspace local)' { git status }
Invoke-Stage 'Backend Build' {
  Set-Location (Join-Path $root 'backend')
  & .\mvnw.cmd -B clean compile
  Set-Location $root
}
Invoke-Stage 'Backend Tests' {
  Set-Location (Join-Path $root 'backend')
  & .\mvnw.cmd -B test
  Set-Location $root
}
Invoke-Stage 'Frontend Install' {
  Set-Location (Join-Path $root 'frontend')
  npm ci
  Set-Location $root
}
Invoke-Stage 'Frontend Build' {
  Set-Location (Join-Path $root 'frontend')
  npm run build
  Set-Location $root
}
Invoke-Stage 'Frontend Tests' {
  Set-Location (Join-Path $root 'frontend')
  npx ng test --watch=false --browsers=ChromeHeadlessNoSandbox
  Set-Location $root
}
Invoke-Stage 'ML Tests' {
  Set-Location (Join-Path $root 'ml')
  if (-not (Test-Path '.\.venv\Scripts\python.exe')) { python -m venv .venv }
  .\.venv\Scripts\python.exe -m pip install -q -r requirements.txt
  .\.venv\Scripts\python.exe -m pytest -q
  Set-Location $root
}
Invoke-Stage 'ML Training' {
  Set-Location (Join-Path $root 'ml')
  .\.venv\Scripts\python.exe data\generate_dataset.py
  .\.venv\Scripts\python.exe train.py
  if (-not (Test-Path 'models\recommender_model.pkl')) { throw 'modele manquant' }
  Set-Location $root
}
Invoke-Stage 'SonarQube' {
  Write-Host 'SKIP_SONAR=true : aucun serveur SonarQube n est configure sur cet environnement.'
}
Invoke-Stage 'Package' {
  Set-Location (Join-Path $root 'backend')
  & .\mvnw.cmd -B -DskipTests package
  Set-Location $root
  if (-not (Test-Path 'frontend\dist\frontend\browser\index.html')) { throw 'build Angular manquant' }
  if (-not (Test-Path 'ml\models\recommender_model.pkl')) { throw 'modele ML manquant' }
}
Invoke-Stage 'Docker Build' {
  docker build -t formalearn-backend:ci "$root\backend"
  docker build -t formalearn-frontend:ci "$root\frontend"
  docker build -t formalearn-ml:ci "$root\ml"
}

Write-Host ""
Write-Host 'Pipeline locale SUCCESS' -ForegroundColor Green
