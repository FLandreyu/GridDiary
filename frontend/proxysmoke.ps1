$ErrorActionPreference = "Continue"
$base = "http://localhost:5173"
$suffix = Get-Random -Minimum 10000 -Maximum 99999
$u = "web$suffix"
$p = "pass123"

$reg = Invoke-RestMethod -Uri "$base/api/user/register" -Method Post -ContentType "application/json" -Body (@{ username = $u; password = $p; email = "$u@test.com"; nickname = "web$suffix" } | ConvertTo-Json)
Write-Output "REGISTER code=$($reg.code) id=$($reg.data.id)"

$sess = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$lg = Invoke-RestMethod -Uri "$base/api/user/login" -Method Post -ContentType "application/json" -Body (@{ username = $u; password = $p } | ConvertTo-Json) -WebSession $sess
Write-Output "LOGIN code=$($lg.code) user=$($lg.data.username)"

$me = Invoke-RestMethod -Uri "$base/api/user/me" -Method Get -WebSession $sess
Write-Output "ME code=$($me.code) nick=$($me.data.nickname)"

$lo = Invoke-RestMethod -Uri "$base/api/user/logout" -Method Post -WebSession $sess
Write-Output "LOGOUT code=$($lo.code)"

try { Invoke-RestMethod -Uri "$base/api/user/me" -Method Get -WebSession $sess | Out-Null; Write-Output "ME_AFTER_LOGOUT unexpected200" }
catch { Write-Output "ME_AFTER_LOGOUT status=$([int]$_.Exception.Response.StatusCode)" }
