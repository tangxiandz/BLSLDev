# Git 代码比对脚本
# 以管理员身份运行 PowerShell 后执行此脚本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Git 代码比对工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 项目路径
$projectPath = "D:\ProjectCode\BLSLTemp\BLSLDev-master\BLSLDev-master"
$remoteUrl = "https://github.com/tangxiandz/BLSLDev.git"

Write-Host "项目路径: $projectPath" -ForegroundColor Yellow
Write-Host "远程仓库: $remoteUrl" -ForegroundColor Yellow

# 进入项目目录
Set-Location $projectPath

# 1. 初始化 Git 仓库
Write-Host "`n1. 初始化 Git 仓库..." -ForegroundColor Cyan
& "C:\Program Files\Git\bin\git.exe" init

# 2. 添加远程仓库
Write-Host "`n2. 添加远程仓库..." -ForegroundColor Cyan
& "C:\Program Files\Git\bin\git.exe" remote add origin $remoteUrl

# 3. 获取远程仓库信息
Write-Host "`n3. 获取远程仓库信息..." -ForegroundColor Cyan
& "C:\Program Files\Git\bin\git.exe" fetch origin

# 4. 查看远程分支
Write-Host "`n4. 查看远程分支..." -ForegroundColor Cyan
& "C:\Program Files\Git\bin\git.exe" branch -r

# 5. 查看本地与远程的差异
Write-Host "`n5. 查看本地与远程的差异..." -ForegroundColor Cyan
Write-Host "文件差异列表:" -ForegroundColor Green
& "C:\Program Files\Git\bin\git.exe" diff origin/main --name-only

# 6. 查看详细差异
Write-Host "`n6. 查看详细差异内容..." -ForegroundColor Cyan
Write-Host "详细差异 (前 100 行):" -ForegroundColor Green
& "C:\Program Files\Git\bin\git.exe" diff origin/main | Select-Object -First 100

# 7. 查看本地未提交的更改
Write-Host "`n7. 查看本地未提交的更改..." -ForegroundColor Cyan
& "C:\Program Files\Git\bin\git.exe" status

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "比对完成！" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
