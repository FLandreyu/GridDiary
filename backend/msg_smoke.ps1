$ErrorActionPreference = "Continue"
$base = "http://localhost:8081"
$sfx = Get-Random -Minimum 10000 -Maximum 99999
$uA = "msgA$sfx"; $uB = "msgB$sfx"; $pw = "123456"

function Reg($u) {
    Invoke-RestMethod -Uri "$base/api/user/register" -Method Post -ContentType "application/json" -Body (@{ username = $u; password = $pw; email = "$u@test.com"; nickname = "nick-$u" } | ConvertTo-Json)
}
function Login($u) {
    $s = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $r = Invoke-RestMethod -Uri "$base/api/user/login" -Method Post -ContentType "application/json" -Body (@{ username = $u; password = $pw } | ConvertTo-Json) -WebSession $s
    if ($r.code -ne 200) { throw "login failed $u" }
    return $s
}

Reg $uA | Out-Null; Reg $uB | Out-Null
$sA = Login $uA; $sB = Login $uB
Write-Output "USERS A=$uA B=$uB"

# A 初始无会话无未读
$ua0 = Invoke-RestMethod -Uri "$base/api/message/unread-count" -Method Get -WebSession $sA
Write-Output "A_UNREAD_INIT=$($ua0.data)"

# 取 A/B 的 id
$meB = Invoke-RestMethod -Uri "$base/api/user/me" -Method Get -WebSession $sB
$bid = $meB.data.id
$meA = Invoke-RestMethod -Uri "$base/api/user/me" -Method Get -WebSession $sA
$aid = $meA.data.id
Write-Output "A_ID=$aid B_ID=$bid"

$s1 = Invoke-RestMethod -Uri "$base/api/message" -Method Post -ContentType "application/json" -Body (@{ toUserId = $bid; content = "msg1 from A" } | ConvertTo-Json) -WebSession $sA
Write-Output "SEND_1 id=$($s1.data)"
$s2 = Invoke-RestMethod -Uri "$base/api/message" -Method Post -ContentType "application/json" -Body (@{ toUserId = $bid; content = "msg2 from A" } | ConvertTo-Json) -WebSession $sA
Write-Output "SEND_2 id=$($s2.data)"

# B 未读应为 2
$ub = Invoke-RestMethod -Uri "$base/api/message/unread-count" -Method Get -WebSession $sB
Write-Output "B_UNREAD_AFTER2=$($ub.data)"

# B 会话列表：应 1 条，peer=A，未读2
$cv = Invoke-RestMethod -Uri "$base/api/message/conversations" -Method Get -WebSession $sB
$c = $cv.data[0]
Write-Output "B_CONV count=$($cv.data.Count) peer=$($c.peerNickname) unread=$($c.unread) last=$($c.lastContent) lastFromMe=$($c.lastFromUserId -eq $aid)"

# B 打开与 A 聊天：messages 正序 2 条；之后未读归 0
$dt = Invoke-RestMethod -Uri "$base/api/message/with/$aid" -Method Get -WebSession $sB
Write-Output "B_CHAT peer=$($dt.data.peerNickname) msgCount=$($dt.data.messages.Count) order=$($dt.data.messages.content -join '|')"
$ub2 = Invoke-RestMethod -Uri "$base/api/message/unread-count" -Method Get -WebSession $sB
Write-Output "B_UNREAD_AFTER_OPEN=$($ub2.data)"

# 再发一条后未读回到 1，再开聊天看消息为3条正序
Invoke-RestMethod -Uri "$base/api/message" -Method Post -ContentType "application/json" -Body (@{ toUserId = $bid; content = "msg3 from A" } | ConvertTo-Json) -WebSession $sA | Out-Null
$ub3 = Invoke-RestMethod -Uri "$base/api/message/unread-count" -Method Get -WebSession $sB
Write-Output "B_UNREAD_AFTER3=$($ub3.data)"
$dt2 = Invoke-RestMethod -Uri "$base/api/message/with/$aid" -Method Get -WebSession $sB
Write-Output "B_CHAT2 msgCount=$($dt2.data.messages.Count) order=$($dt2.data.messages.content -join '|')"

# A 的会话列表（B 已读 A 的消息，未读应为0；last msg 为 msg3）
$cva = Invoke-RestMethod -Uri "$base/api/message/conversations" -Method Get -WebSession $sA
Write-Output "A_CONV count=$($cva.data.Count) peer=$($cva.data[0].peerNickname) unread=$($cva.data[0].unread) last=$($cva.data[0].lastContent)"

# 给自己发 -> 应 400
try { Invoke-RestMethod -Uri "$base/api/message" -Method Post -ContentType "application/json" -Body (@{ toUserId = $aid; content = "self" } | ConvertTo-Json) -WebSession $sA | Out-Null; Write-Output "SELF unexpected200" }
catch { Write-Output "SELF_SEND status=$([int]$_.Exception.Response.StatusCode)" }

# 给不存在用户发 -> 404
try { Invoke-RestMethod -Uri "$base/api/message" -Method Post -ContentType "application/json" -Body (@{ toUserId = 999999; content = "ghost" } | ConvertTo-Json) -WebSession $sA | Out-Null; Write-Output "GHOST unexpected200" }
catch { Write-Output "GHOST_SEND status=$([int]$_.Exception.Response.StatusCode)" }
