$ErrorActionPreference = "Continue"
$base = "http://localhost:8081"
$sfx = Get-Random -Minimum 10000 -Maximum 99999
$uA = "userA$sfx"; $uB = "userB$sfx"; $pw = "123456"

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

# A 建一篇公开日记
$cr = Invoke-RestMethod -Uri "$base/api/diary" -Method Post -ContentType "application/json" -Body (@{ title = "social$sfx"; content = "social body"; isPublic = $true; images = @() } | ConvertTo-Json) -WebSession $sA
$did = $cr.data
Write-Output "CREATE diary=$did"

# ---- Like ----
$lk1 = Invoke-RestMethod -Uri "$base/api/diary/$did/like" -Method Post -WebSession $sB
Write-Output "B_LIKE liked=$($lk1.data.liked) count=$($lk1.data.likeCount)"
$lk2 = Invoke-RestMethod -Uri "$base/api/diary/$did/like" -Method Post -WebSession $sB
Write-Output "B_LIKE_AGAIN liked=$($lk2.data.liked) count=$($lk2.data.likeCount)"
$ul = Invoke-RestMethod -Uri "$base/api/diary/$did/like" -Method Delete -WebSession $sB
Write-Output "B_UNLIKE liked=$($ul.data.liked) count=$($ul.data.likeCount)"
$lk3 = Invoke-RestMethod -Uri "$base/api/diary/$did/like" -Method Post -WebSession $sB
Write-Output "B_LIKE2 liked=$($lk3.data.liked) count=$($lk3.data.likeCount)"
$lk4 = Invoke-RestMethod -Uri "$base/api/diary/$did/like" -Method Post -WebSession $sA
Write-Output "A_LIKE liked=$($lk4.data.liked) count=$($lk4.data.likeCount)"
$st = Invoke-RestMethod -Uri "$base/api/diary/$did/like/status" -Method Get -WebSession $sB
Write-Output "B_STATUS liked=$($st.data.liked) count=$($st.data.likeCount)"

# ---- Comments (two-level) ----
$c1 = Invoke-RestMethod -Uri "$base/api/diary/$did/comments" -Method Post -ContentType "application/json" -Body (@{ content = "root comment from A"; parentId = $null } | ConvertTo-Json) -WebSession $sA
$rootId = $c1.data.id
Write-Output "A_ROOT_COMMENT id=$rootId author=$($c1.data.authorNickname)"

$c2 = Invoke-RestMethod -Uri "$base/api/diary/$did/comments" -Method Post -ContentType "application/json" -Body (@{ content = "reply1 from B"; parentId = $rootId } | ConvertTo-Json) -WebSession $sB
$r1Id = $c2.data.id
Write-Output "B_REPLY id=$r1Id parent=$($c2.data.parentId)"

# A replies to the reply -> should be hoisted to root (two-level only)
$c3 = Invoke-RestMethod -Uri "$base/api/diary/$did/comments" -Method Post -ContentType "application/json" -Body (@{ content = "reply2 from A (to reply)"; parentId = $r1Id } | ConvertTo-Json) -WebSession $sA
Write-Output "A_REPLY_REPLY id=$($c3.data.id) parent=$($c3.data.parentId) expectedRoot=$rootId"

$tree = Invoke-RestMethod -Uri "$base/api/diary/$did/comments" -Method Get
$root = $tree.data | Where-Object { $_.id -eq $rootId }
Write-Output "TREE roots=$($tree.data.Count) rootReplies=$($root.replies.Count)"

$del1 = Invoke-RestMethod -Uri "$base/api/diary/$did/comments/$r1Id" -Method Delete -WebSession $sB
Write-Output "B_DEL_REPLY code=$($del1.code)"
$tree2 = Invoke-RestMethod -Uri "$base/api/diary/$did/comments" -Method Get
Write-Output "TREE_AFTER_DEL_REPLY rootReplies=$($tree2.data[0].replies.Count)"

$del2 = Invoke-RestMethod -Uri "$base/api/diary/$did/comments/$rootId" -Method Delete -WebSession $sA
Write-Output "A_DEL_ROOT code=$($del2.code)"
$tree3 = Invoke-RestMethod -Uri "$base/api/diary/$did/comments" -Method Get
Write-Output "TREE_AFTER_DEL_ROOT total=$($tree3.data.Count)"

$det = Invoke-RestMethod -Uri "$base/api/diary/$did" -Method Get
Write-Output "DETAIL_AFTER likeCount=$($det.data.likeCount)"
