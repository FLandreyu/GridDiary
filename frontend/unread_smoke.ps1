$ErrorActionPreference = "Continue"
$base = "http://localhost:8080"

# uitest1 -> 拿 id
$s1 = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$lg1 = Invoke-RestMethod -Uri "$base/api/user/login" -Method Post -ContentType "application/json" -Body (@{ username = "uitest1"; password = "pass123" } | ConvertTo-Json) -WebSession $s1
$me1 = Invoke-RestMethod -Uri "$base/api/user/me" -Method Get -WebSession $s1
$uid = $me1.data.id
Write-Output "UITEST_ID=$uid"

# userA28243 -> 给 uitest1 发私信
$s2 = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$lg2 = Invoke-RestMethod -Uri "$base/api/user/login" -Method Post -ContentType "application/json" -Body (@{ username = "userA28243"; password = "123456" } | ConvertTo-Json) -WebSession $s2
Write-Output "A_LOGIN code=$($lg2.code)"
$r = Invoke-RestMethod -Uri "$base/api/message" -Method Post -ContentType "application/json" -Body (@{ toUserId = $uid; content = "Hi uitest1, this is from userA" } | ConvertTo-Json) -WebSession $s2
Write-Output "SEND code=$($r.code) id=$($r.data)"

# uitest1 未读数应为1
$cnt = Invoke-RestMethod -Uri "$base/api/message/unread-count" -Method Get -WebSession $s1
Write-Output "UITEST_UNREAD=$($cnt.data)"
