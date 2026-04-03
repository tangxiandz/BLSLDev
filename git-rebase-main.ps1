# Git 主线切换脚本
# 以管理员身份运行 PowerShell 后执行此脚本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Git 主线切换工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 项目路径
$projectPath = "D:\ProjectCode\BLSLTemp\BLSLDev-master\BLSLDev-master"
$remoteUrl = "https://github.com/tangxiandz/BLSLDev.git"
$backupBranch = "server-backup-$(Get-Date -Format 'yyyyMMdd-HHmmss')"

Write-Host "项目路径: $projectPath" -ForegroundColor Yellow
Write-Host "远程仓库: $remoteUrl" -ForegroundColor Yellow
Write-Host "备份分支: $backupBranch" -ForegroundColor Yellow

# 进入项目目录
Set-Location $projectPath

# 1. 检查是否已初始化 Git 仓库
if (-not (Test-Path ".git")) {
    Write-Host "`n1. 初始化 Git 仓库..." -ForegroundColor Cyan
    & "C:\Program Files\Git\bin\git.exe" init
}

# 2. 添加远程仓库（如果不存在）
$remoteExists = & "C:\Program Files\Git\bin\git.exe" remote -v
if (-not ($remoteExists -like "*origin*$remoteUrl*")) {
    Write-Host "`n2. 添加远程仓库..." -ForegroundColor Cyan
    & "C:\Program Files\Git\bin\git.exe" remote add origin $remoteUrl
}

# 3. 获取远程仓库信息
Write-Host "`n3. 获取远程仓库信息..." -ForegroundColor Cyan
& "C:\Program Files\Git\bin\git.exe" fetch origin

# 4. 创建远程代码的备份分支
Write-Host "`n4. 创建远程代码的备份分支..." -ForegroundColor Cyan
& "C:\Program Files\Git\bin\git.exe" checkout -b $backupBranch origin/main
& "C:\Program Files\Git\bin\git.exe" push origin $backupBranch
Write-Host "已创建备份分支: $backupBranch" -ForegroundColor Green

# 5. 切换回主线分支
Write-Host "`n5. 切换回主线分支..." -ForegroundColor Cyan
& "C:\Program Files\Git\bin\git.exe" checkout -B main

# 6. 添加所有本地文件
Write-Host "`n6. 添加所有本地文件..." -ForegroundColor Cyan
& "C:\Program Files\Git\bin\git.exe" add .

# 7. 提交本地代码
Write-Host "`n7. 提交本地代码..." -ForegroundColor Cyan
& "C:\Program Files\Git\bin\git.exe" commit -m "本地最新代码为主线版本"

# 8. 强制推送到远程主线
Write-Host "`n8. 强制推送到远程主线..." -ForegroundColor Cyan
Write-Host "注意：这将覆盖远程仓库的主线分支！" -ForegroundColor Red
& "C:\Program Files\Git\bin\git.exe" push -f origin main

# 9. 查看最终状态
Write-Host "`n9. 查看最终状态..." -ForegroundColor Cyan
& "C:\Program Files\Git\bin\git.exe" branch -a
& "C:\Program Files\Git\bin\git.exe" status

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "操作完成！" -ForegroundColor Cyan
Write-Host "- 服务器代码已备份到分支: $backupBranch" -ForegroundColor Green
Write-Host "- 本地最新代码已改为主线并推送" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
