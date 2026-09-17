$ErrorActionPreference = "Continue"
$base = "http://localhost:8081"
$suffix = Get-Random -Minimum 10000 -Maximum 99999
$u = "img$suffix"
$p = "pass123"

# 1) 生成一张测试 PNG
Add-Type -AssemblyName System.Drawing
$png = "$env:TEMP\gd_test_$suffix.png"
$bmp = New-Object System.Drawing.Bitmap(600, 400)
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.Clear([System.Drawing.Color]::OrangeRed)
$g.Dispose()
$bmp.Save($png, [System.Drawing.Imaging.ImageFormat]::Png)
$bmp.Dispose()
Write-Output "PNG created: $png"

# 2) 注册并登录（WebSession 保存 Cookie）
$reg = Invoke-RestMethod -Uri "$base/api/user/register" -Method Post -ContentType "application/json" -Body (@{ username = $u; password = $p; email = "$u@test.com"; nickname = "imguser" } | ConvertTo-Json)
Write-Output "REGISTER code=$($reg.code) id=$($reg.data.id)"
$sess = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$lg = Invoke-RestMethod -Uri "$base/api/user/login" -Method Post -ContentType "application/json" -Body (@{ username = $u; password = $p } | ConvertTo-Json) -WebSession $sess
Write-Output "LOGIN code=$($lg.code)"
$sid = ($sess.Cookies.GetCookies((New-Object System.Uri $base)) | Where-Object { $_.Name -eq 'JSESSIONID' }).Value
Write-Output "JSESSIONID=$sid"

# 3) 用 curl 上传多图（multipart），手动带 Cookie
$upRaw = curl.exe -s -H "Cookie: JSESSIONID=$sid" -F "files=@$png;type=image/png" "$base/api/upload/image"
$arr = $upRaw | ConvertFrom-Json
Write-Output "UPLOAD code=$($arr.code) count=$($arr.data.Count)"
$orig = $arr.data[0].originalUrl
$thumb = $arr.data[0].thumbUrl
Write-Output "orig=$orig"
Write-Output "thumb=$thumb"

# 4) 建带图片的日记
$cr = Invoke-RestMethod -Uri "$base/api/diary" -Method Post -ContentType "application/json" -Body (@{ title = "imgdiary$suffix"; content = "with image"; isPublic = $true; images = @(@{ originalUrl = $orig; thumbUrl = $thumb }) } | ConvertTo-Json -Depth 5) -WebSession $sess
Write-Output "CREATE code=$($cr.code) id=$($cr.data)"
$did = $cr.data

# 5) 详情：应含 1 张图，cover 为缩略图
$det = Invoke-RestMethod -Uri "$base/api/diary/$did" -Method Get
Write-Output "DETAIL imgCnt=$($det.data.images.Count) cover=$($det.data.cover) imgThumb=$($det.data.images[0].thumbUrl)"

# 6) 静态访问缩略图与原图
foreach ($path in @($thumb, $orig)) {
    try {
        $r = Invoke-WebRequest -Uri ($base + $path) -UseBasicParsing -TimeoutSec 5
        Write-Output "STATIC $path => $($r.StatusCode) len=$($r.RawContentLength)"
    } catch {
        Write-Output "STATIC $path => ERR $([int]$_.Exception.Response.StatusCode)"
    }
}
