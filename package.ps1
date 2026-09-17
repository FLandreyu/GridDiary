<#
.SYNOPSIS
    把 GridDiary 打包成「双击即用」的 Windows 程序（免安装绿色版）。

.DESCRIPTION
    打包流程：
      1. 构建前端：npm run build  ->  frontend/dist
      2. 前端产物复制进后端静态资源 backend/src/main/resources/static
      3. 由 sql/schema.sql 生成幂等建表脚本（打进 jar，首次启动自动建表）
      4. Maven 打出 Spring Boot 可执行 jar
      5. 生成应用图标 tools/griddiary.ico
      6. jpackage 生成内置 JRE 的应用镜像 dist/GridDiary/GridDiary.exe
      7. 补齐 config/ 配置、upload/ 图片、README.txt

    产物目录 dist/GridDiary 可以整体拷贝到任意 Windows 电脑，
    目标机只需装好 MySQL 8，双击 exe 即可（无需安装 Java）。

.EXAMPLE
    powershell -ExecutionPolicy Bypass -File package.ps1

.EXAMPLE
    # 只改了后端 Java 代码，跳过前端构建，并额外压缩成 zip
    powershell -ExecutionPolicy Bypass -File package.ps1 -SkipFrontend -Zip
#>
[CmdletBinding()]
param(
    [string]$AppName = 'GridDiary',
    [string]$AppVersion = '1.0.0',
    [string]$JarName = 'backend-0.0.1-SNAPSHOT.jar',
    [string]$JavaHome = $env:JAVA_HOME,
    [switch]$SkipFrontend,   # 跳过 npm 构建（前端没改动时提速）
    [switch]$SkipBackend,    # 跳过 Maven 构建（jar 已是最新时提速）
    [switch]$ForceInstall,   # 强制重装前端依赖
    [switch]$Zip             # 打包完成后额外压缩为 dist/GridDiary-<版本>-win-x64.zip
)

$ErrorActionPreference = 'Continue'
$root = $PSScriptRoot
Set-Location $root

function Write-Step([string]$Message) { Write-Host "`n==> $Message" -ForegroundColor Cyan }
function Write-Ok([string]$Message) { Write-Host "    $Message" -ForegroundColor Green }

# 调用外部程序（npm / mvnw / jpackage …），非 0 退出码直接抛错
function Invoke-External {
    param([string]$Exe, [string[]]$Arguments, [string]$What)
    Write-Host "    > $Exe $($Arguments -join ' ')" -ForegroundColor DarkGray
    $saved = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    & $Exe @Arguments | Out-Host
    $code = $LASTEXITCODE
    $ErrorActionPreference = $saved
    if ($code -ne 0) { throw "$What 失败（退出码 $code）" }
}

# 读取外部程序的输出（java -version 之类写到 stderr 的也不能当错误）
function Get-NativeText {
    param([string]$Exe, [string[]]$Arguments)
    $saved = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    $text = (& $Exe @Arguments 2>&1 | Out-String).Trim()
    $ErrorActionPreference = $saved
    return $text
}

$totalWatch = [System.Diagnostics.Stopwatch]::StartNew()

try {
    # ---------------------------------------------------------------
    # 0. 环境检查
    # ---------------------------------------------------------------
    Write-Step '检查构建环境'

    if (-not $JavaHome) {
        $javaCmd = Get-Command java -ErrorAction SilentlyContinue
        if ($javaCmd) { $JavaHome = Split-Path (Split-Path $javaCmd.Source -Parent) -Parent }
    }
    if (-not $JavaHome) {
        throw '找不到 Java：请设置 JAVA_HOME 环境变量，或用 -JavaHome 指定 JDK 目录'
    }
    $javaExe = Join-Path $JavaHome 'bin\java.exe'
    $jpackageExe = Join-Path $JavaHome 'bin\jpackage.exe'
    if (-not (Test-Path $jpackageExe)) {
        throw "找不到 jpackage.exe：请确认 $JavaHome 是完整 JDK（不是 JRE）。jpackage 需要 JDK 17 及以上"
    }
    $env:JAVA_HOME = $JavaHome
    Write-Ok "Java  : $JavaHome"
    Write-Ok "        $((Get-NativeText $javaExe @('-version')).Split("`n")[0].Trim())"

    if (-not $SkipFrontend) {
        foreach ($tool in @('node', 'npm')) {
            if (-not (Get-Command $tool -ErrorAction SilentlyContinue)) {
                throw "找不到 $tool：请先安装 Node.js（https://nodejs.org）"
            }
        }
        Write-Ok "Node  : $(Get-NativeText node @('-v'))  /  npm: $(Get-NativeText npm @('-v'))"
    }

    # ---------------------------------------------------------------
    # 1. 构建前端
    # ---------------------------------------------------------------
    $frontendDir = Join-Path $root 'frontend'
    $distDir = Join-Path $root 'dist'
    if (-not $SkipFrontend) {
        Write-Step '构建前端（Vite）'
        Push-Location $frontendDir
        try {
            if ($ForceInstall -or -not (Test-Path (Join-Path $frontendDir 'node_modules'))) {
                if (Test-Path (Join-Path $frontendDir 'package-lock.json')) {
                    Invoke-External 'npm' @('ci') '安装前端依赖'
                } else {
                    Invoke-External 'npm' @('install') '安装前端依赖'
                }
            } else {
                Write-Ok '已存在 node_modules，跳过安装（需要重装请加 -ForceInstall）'
            }
            Invoke-External 'npm' @('run', 'build') '前端构建'
        } finally {
            Pop-Location
        }
        if (-not (Test-Path (Join-Path $frontendDir 'dist\index.html'))) {
            throw "前端构建产物不完整：找不到 frontend\dist\index.html"
        }
    }

    # ---------------------------------------------------------------
    # 2. 前端产物 -> 后端静态资源
    #     放进 src/main/resources/static，Maven 打包时会自动收进 jar
    #     该目录已加入 .gitignore，属于构建产物
    # ---------------------------------------------------------------
    $staticDir = Join-Path $root 'backend\src\main\resources\static'
    if (Test-Path (Join-Path $frontendDir 'dist')) {
        Write-Step '把前端产物复制进后端静态资源'
        if (Test-Path $staticDir) { Remove-Item $staticDir -Recurse -Force }
        New-Item -ItemType Directory -Path $staticDir -Force | Out-Null
        Copy-Item (Join-Path $frontendDir 'dist\*') $staticDir -Recurse -Force
        $count = (Get-ChildItem $staticDir -Recurse -File).Count
        Write-Ok "$staticDir （$count 个文件）"
    } elseif (-not (Test-Path (Join-Path $staticDir 'index.html'))) {
        throw '既没有 frontend\dist，也没有已复制的前端产物；请先执行一次完整打包'
    }

    # ---------------------------------------------------------------
    # 3. 生成幂等建表脚本（首次启动自动建库建表）
    # ---------------------------------------------------------------
    Write-Step '生成幂等建表脚本 db/init.sql'
    & (Join-Path $root 'tools\gen-init-sql.ps1') -SchemaPath (Join-Path $root 'sql\schema.sql') -OutputPath (Join-Path $root 'backend\src\main\resources\db\init.sql')
    Write-Ok 'backend\src\main\resources\db\init.sql'

    # ---------------------------------------------------------------
    # 4. 打包后端 jar
    # ---------------------------------------------------------------
    $jarPath = Join-Path $root "backend\target\$JarName"
    if (-not $SkipBackend) {
        Write-Step '打包后端（Maven）'
        Push-Location (Join-Path $root 'backend')
        try {
            Invoke-External (Join-Path $root 'backend\mvnw.cmd') @('-B', '-q', '-DskipTests', 'package') '后端打包'
        } finally {
            Pop-Location
        }
    }
    if (-not (Test-Path $jarPath)) { throw "找不到构建产物：$jarPath" }
    Write-Ok ("$JarName  ({0:N1} MB)" -f ((Get-Item $jarPath).Length / 1MB))

    # 校验前端产物确实进了 jar
    $jarList = Get-NativeText (Join-Path $JavaHome 'bin\jar.exe') @('tf', $jarPath)
    if ($jarList -notmatch 'BOOT-INF/classes/static/index.html') {
        throw 'jar 里没有前端页面（BOOT-INF/classes/static/index.html），请检查第 2 步是否成功'
    }
    Write-Ok '已确认前端页面打进 jar'

    # ---------------------------------------------------------------
    # 5. 应用图标
    # ---------------------------------------------------------------
    $iconPath = Join-Path $root 'tools\griddiary.ico'
    if (-not (Test-Path $iconPath)) {
        Write-Step '生成应用图标'
        Invoke-External $javaExe @((Join-Path $root 'tools\MakeIcon.java'), $iconPath) '生成图标'
    }

    # ---------------------------------------------------------------
    # 6. jpackage 生成应用镜像（内置 JRE，双击即用）
    # ---------------------------------------------------------------
    Write-Step 'jpackage 生成应用镜像（内置 Java 运行环境）'
    $appDir = Join-Path $distDir $AppName
    if (Test-Path $appDir) { Remove-Item $appDir -Recurse -Force }
    $stage = Join-Path $distDir '_stage'
    if (Test-Path $stage) { Remove-Item $stage -Recurse -Force }
    New-Item -ItemType Directory -Path $stage -Force | Out-Null
    Copy-Item $jarPath $stage -Force  # jpackage 会把 --input 下所有文件塞进 app 目录，只放 jar

    # 中文 Windows 上必须显式带上这些模块：jdk.charsets(GBK) / jdk.localedata(区域数据)
    # / java.desktop(ImageIO 生成缩略图) 包含在 java.se 里
    $modules = 'java.se,jdk.unsupported,jdk.crypto.ec,jdk.crypto.cryptoki,jdk.localedata,jdk.charsets,jdk.zipfs,jdk.jfr,jdk.httpserver,jdk.management.agent,jdk.attach,jdk.naming.dns'
    $debugLauncher = "$($AppName)Debug=$(Join-Path $root 'tools\launcher-debug.properties')"

    $jpackageArgs = @(
        '--type', 'app-image',
        '--name', $AppName,
        '--app-version', $AppVersion,
        '--description', 'GridDiary 九宫格记忆网 - 个人日记与社交平台',
        '--vendor', 'FLandreyu',
        '--input', $stage,
        '--main-jar', $JarName,
        '--dest', $distDir,
        '--icon', $iconPath,
        '--java-options', '-Dspring.profiles.active=prod',
        '--java-options', '-Dfile.encoding=UTF-8',
        '--java-options', '-Duser.language=zh',
        '--java-options', '-Duser.country=CN',
        '--java-options', '-Xmx512m',
        '--add-modules', $modules,
        '--add-launcher', $debugLauncher
    )
    Invoke-External $jpackageExe $jpackageArgs 'jpackage 打包'
    if (Test-Path $stage) { Remove-Item $stage -Recurse -Force }

    # ---------------------------------------------------------------
    # 7. 补齐运行目录
    # ---------------------------------------------------------------
    Write-Step '补齐运行目录（config / upload / 说明文档）'
    $configTarget = Join-Path $appDir 'config'
    New-Item -ItemType Directory -Path $configTarget -Force | Out-Null
    Copy-Item (Join-Path $root 'packaging\config\application.yaml') (Join-Path $configTarget 'application.yaml') -Force
    # 说明文档用 ASCII 文件名，避免 Windows 自带解压工具把 zip 里的中文文件名解析成乱码
    Copy-Item (Join-Path $root 'packaging\使用说明.txt') (Join-Path $appDir 'README.txt') -Force

    # 历史上后端在不同工作目录启动过，图片可能同时落在 <项目根>/upload 与 backend/upload
    # 两处都带上（数据库里存的是 /upload/xxx 路径，缺文件页面就会显示不出图）
    $uploadSources = @((Join-Path $root 'upload'), (Join-Path $root 'backend\upload')) |
        Where-Object { Test-Path $_ }
    if ($uploadSources) {
        $uploadTarget = Join-Path $appDir 'upload'
        New-Item -ItemType Directory -Path (Join-Path $uploadTarget 'original') -Force | Out-Null
        New-Item -ItemType Directory -Path (Join-Path $uploadTarget 'thumb') -Force | Out-Null
        foreach ($source in $uploadSources) {
            Copy-Item (Join-Path $source '*') $uploadTarget -Recurse -Force
        }
        Write-Ok "已带上现有图片：$((Get-ChildItem $uploadTarget -Recurse -File).Count) 个文件"
    }

    $sizeMb = (Get-ChildItem $appDir -Recurse -File | Measure-Object -Property Length -Sum).Sum / 1MB
    Write-Ok ("应用目录大小：{0:N1} MB" -f $sizeMb)

    # ---------------------------------------------------------------
    # 8. 可选：压缩成 zip（便于拷贝给其他人）
    # ---------------------------------------------------------------
    $zipPath = $null
    if ($Zip) {
        Write-Step '压缩成 zip'
        $zipPath = Join-Path $distDir "$AppName-$AppVersion-win-x64.zip"
        if (Test-Path $zipPath) { Remove-Item $zipPath -Force }
        Compress-Archive -Path $appDir -DestinationPath $zipPath -CompressionLevel Optimal
        Write-Ok ("$zipPath  ({0:N1} MB)" -f ((Get-Item $zipPath).Length / 1MB))
    }

    $totalWatch.Stop()
    Write-Host ''
    Write-Host '=====================================================' -ForegroundColor Green
    Write-Host ' 打包完成' -ForegroundColor Green
    Write-Host '=====================================================' -ForegroundColor Green
    Write-Host "  双击运行： $(Join-Path $appDir "$AppName.exe")"
    Write-Host "  调试运行： $(Join-Path $appDir "$($AppName)Debug.exe")  （带控制台，排错用）"
    if ($zipPath) { Write-Host "  压缩包：   $zipPath" }
    Write-Host ("  总耗时：   {0:N1} 秒" -f $totalWatch.Elapsed.TotalSeconds)
    Write-Host ''
    exit 0
} catch {
    if (Test-Path (Join-Path $root 'dist\_stage')) { Remove-Item (Join-Path $root 'dist\_stage') -Recurse -Force -ErrorAction SilentlyContinue }
    Write-Host ''
    Write-Host "打包失败：$($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
