$ErrorActionPreference = "Stop"
$repo = "C:\repo\muslim-pro-plus"
$raw = Get-Content "C:\repo\secrets\github\pat.txt" -Raw
$tokenLine = ($raw -split "`r?`n" | Where-Object { $_ -match '^(ghp_|github_pat_|gho_|ghs_)' } | Select-Object -First 1)
if (-not $tokenLine) { throw "nenhum token encontrado no pat.txt" }
$token = $tokenLine.Trim()
$headers = @{ Authorization = "Bearer $token"; "User-Agent" = "codex-agent" }

$me = Invoke-RestMethod -Uri "https://api.github.com/user" -Headers $headers -TimeoutSec 30
Write-Output "login: $($me.login)"

$body = @{
  name        = "muslim-pro-plus"
  description = "Tooling to patch a shipped Android APK: resources.arsc string pool, DEX bytecode, repack and sign"
  private     = $false
  has_issues  = $true
  has_wiki    = $false
} | ConvertTo-Json

try {
  $created = Invoke-RestMethod -Uri "https://api.github.com/user/repos" -Method Post -Headers $headers -Body $body -ContentType "application/json" -TimeoutSec 60
  Write-Output "criado: $($created.html_url) private=$($created.private)"
} catch {
  $resp = $_.Exception.Response
  Write-Output "POST falhou: $($_.Exception.Message)"
}

Set-Location $repo
git remote remove origin 2>$null
git remote add origin "https://github.com/$($me.login)/muslim-pro-plus.git"
$basic = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("x-access-token:$token"))
git -c http.extraheader="AUTHORIZATION: basic $basic" push -u origin main 2>&1 | Select-Object -Last 5
