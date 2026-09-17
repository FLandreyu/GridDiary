$ErrorActionPreference = "Continue"
$base = "http://localhost:8081"
$suffix = Get-Random -Minimum 10000 -Maximum 99999
$u = "smoke$suffix"
$p = "pass123"

$reg = Invoke-RestMethod -Uri "$base/api/user/register" -Method Post -ContentType "application/json" -Body (@{ username = $u; password = $p; email = "$u@test.com"; nickname = "nick$suffix" } | ConvertTo-Json)
Write-Output "REGISTER code=$($reg.code) id=$($reg.data.id)"

$sess = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$lg = Invoke-RestMethod -Uri "$base/api/user/login" -Method Post -ContentType "application/json" -Body (@{ username = $u; password = $p } | ConvertTo-Json) -WebSession $sess
Write-Output "LOGIN code=$($lg.code) user=$($lg.data.username)"

$me = Invoke-RestMethod -Uri "$base/api/user/me" -Method Get -WebSession $sess
Write-Output "ME code=$($me.code) nick=$($me.data.nickname)"

$l0 = Invoke-RestMethod -Uri "$base/api/diary" -Method Get
Write-Output "PUBLIC_LIST_ANON code=$($l0.code) total=$($l0.data.total)"

$crBody = @{ title = "diary$suffix"; content = "body text"; isPublic = $true; images = @() } | ConvertTo-Json
$cr = Invoke-RestMethod -Uri "$base/api/diary" -Method Post -ContentType "application/json" -Body $crBody -WebSession $sess
Write-Output "CREATE code=$($cr.code) id=$($cr.data)"
$did = $cr.data

$det = Invoke-RestMethod -Uri "$base/api/diary/$did" -Method Get
Write-Output "DETAIL code=$($det.code) title=$($det.data.title) author=$($det.data.authorNickname) imgCnt=$($det.data.images.Count)"

$l1 = Invoke-RestMethod -Uri "$base/api/diary" -Method Get
Write-Output "LIST_AFTER code=$($l1.code) total=$($l1.data.total) firstAuthor=$($l1.data.records[0].authorNickname)"

$mine = Invoke-RestMethod -Uri "$base/api/diary/mine" -Method Get -WebSession $sess
Write-Output "MINE code=$($mine.code) total=$($mine.data.total)"

$upBody = @{ title = "edited$suffix"; content = "edited body"; isPublic = $true; images = @() } | ConvertTo-Json
$up = Invoke-RestMethod -Uri "$base/api/diary/$did" -Method Put -ContentType "application/json" -Body $upBody -WebSession $sess
Write-Output "UPDATE code=$($up.code)"

$det2 = Invoke-RestMethod -Uri "$base/api/diary/$did" -Method Get
Write-Output "DETAIL_AFTER_UPDATE title=$($det2.data.title)"

$dl = Invoke-RestMethod -Uri "$base/api/diary/$did" -Method Delete -WebSession $sess
Write-Output "DELETE code=$($dl.code)"

try { Invoke-RestMethod -Uri "$base/api/diary/$did" -Method Get | Out-Null; Write-Output "AFTER_DEL unexpected200" }
catch { Write-Output "AFTER_DEL status=$([int]$_.Exception.Response.StatusCode)" }

try { Invoke-RestMethod -Uri "$base/api/diary/mine" -Method Get | Out-Null; Write-Output "ANON_MINE unexpected200" }
catch { Write-Output "ANON_MINE status=$([int]$_.Exception.Response.StatusCode)" }

$lo = Invoke-RestMethod -Uri "$base/api/user/logout" -Method Post -WebSession $sess
Write-Output "LOGOUT code=$($lo.code)"
