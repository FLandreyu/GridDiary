# 由 sql/schema.sql 生成「幂等建表脚本」backend/src/main/resources/db/init.sql
# 差异：去掉 DROP TABLE / CREATE DATABASE / USE，所有建表改为 IF NOT EXISTS
# 目的：打包成 exe 后首次启动能自动补齐缺失的数据表，且重复执行不会清空已有数据
# 注意：本文件含中文，必须以 UTF-8 BOM 保存（Windows PowerShell 5.1 无 BOM 会按 GBK 解析）

param(
    [string]$SchemaPath = (Join-Path $PSScriptRoot '..\sql\schema.sql'),
    [string]$OutputPath = (Join-Path $PSScriptRoot '..\backend\src\main\resources\db\init.sql')
)

$ErrorActionPreference = 'Stop'

$schema = (Resolve-Path $SchemaPath).Path
$output = [System.IO.Path]::GetFullPath($OutputPath)

$sql = [System.IO.File]::ReadAllText($schema, [System.Text.Encoding]::UTF8)

# 1) 删掉所有 DROP TABLE 语句
$sql = [regex]::Replace($sql, '(?m)^\s*DROP TABLE IF EXISTS[^\r\n]*[\r\n]+', '')
# 2) 删掉建库 / 切库语句（程序通过 JDBC 的 createDatabaseIfNotExist 建库）
$sql = [regex]::Replace($sql, '(?im)^\s*CREATE DATABASE[\s\S]*?;[\r\n]*', '')
$sql = [regex]::Replace($sql, '(?im)^\s*USE[^\r\n]*[\r\n]*', '')
# 3) 建表改为幂等
$sql = $sql -replace 'CREATE TABLE `', 'CREATE TABLE IF NOT EXISTS `'

$header = @'
-- =============================================================
-- GridDiary 幂等建表脚本（打包运行 / 首次启动时由 Spring 自动执行）
-- 由 sql/schema.sql 自动生成，请勿手工修改
-- 全部为 IF NOT EXISTS：重复执行安全，不会清空已有数据
-- =============================================================


'@

$dir = Split-Path $output
if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Force -Path $dir | Out-Null }

# 不带 BOM 写出（BOM 会让第一条 SQL 语句解析失败）
[System.IO.File]::WriteAllText($output, $header + $sql, (New-Object System.Text.UTF8Encoding($false)))

Write-Host "[gen-init-sql] $schema -> $output"
