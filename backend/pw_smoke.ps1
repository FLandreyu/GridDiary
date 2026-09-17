$ErrorActionPreference = "Continue"
$base = "http://localhost:8080"
$u = "uitest1"

function Login($pass) {
    $s = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    try {
        $r = Invoke-RestMethod -Uri "$base/api/user/login" -Method Post -ContentType "application/json" -Body (@{ username = $u; password = $pass } | ConvertTo-Json) -WebSession $s
        return $r.code
    } catch { return 401 }
}

# 旧密码登录
Write-Output "LOGIN_OLD code=$(Login 'pass123')"

# 改密码 pass123 -> pass456（需要登录 session）
$s = New-Object Microsoft.PowerShell.Commands.WebRequestSession
Invoke-RestMethod -Uri "$base/api/user/login" -Method Post -ContentType "application/json" -Body (@{ username = $u; password = "pass123" } | ConvertTo-Json) -WebSession $s | Out-Null
$c1 = Invoke-RestMethod -Uri "$base/api/user/password" -Method Put -ContentType "application/json" -Body (@{ oldPassword = "pass123"; newPassword = "pass456" } | ConvertTo-Json) -WebSession $s
Write-Output "CHANGE code=$($c1.code)"

# 新密码登录
Write-Output "LOGIN_NEW code=$(Login 'pass456')"

# 错误旧密码应 400
$s2 = New-Object Microsoft.PowerShell.Commands.WebRequestSession
Invoke-RestMethod -Uri "$base/api/user/login" -Method Post -ContentType "application/json" -Body (@{ username = $u; password = "pass456" } | ConvertTo-Json) -WebSession $s2 | Out-Null
try { Invoke-RestMethod -Uri "$base/api/user/password" -Method Put -ContentType "application/json" -Body (@{ oldPassword = "wrong1"; newPassword = "pass789" } | ConvertTo-Json) -WebSession $s2 | Out-Null; Write-Output "WRONG_OLD unexpected200" }
catch { Write-Output "WRONG_OLD status=$([int]$_.Exception.Response.StatusCode)" }

# 改回 pass123
$s3 = New-Object Microsoft.PowerShell.Commands.WebRequestSession
Invoke-RestMethod -Uri "$base/api/user/login" -Method Post -ContentType "application/json" -Body (@{ username = $u; password = "pass456" } | ConvertTo-Json) -WebSession $s3 | Out-Null
$c2 = Invoke-RestMethod -Uri "$base/api/user/password" -Method Put -ContentType "application/json" -Body (@{ oldPassword = "pass456"; newPassword = "pass123" } | ConvertTo-Json) -WebSession $s3
Write-Output "CHANGE_BACK code=$($c2.code)"
Write-Output "LOGIN_FINAL code=$(Login 'pass123')"
