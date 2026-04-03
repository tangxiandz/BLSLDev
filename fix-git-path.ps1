# 修复 Git 环境变量脚本
# 以管理员身份运行 PowerShell 后执行此脚本

Write-Host "正在查找 Git 安装路径..." -ForegroundColor Cyan

# 常见 Git 安装路径
$gitPaths = @(
    "C:\Program Files\Git\cmd",
    "C:\Program Files (x86)\Git\cmd",
    "C:\Users\$env:USERNAME\AppData\Local\Programs\Git\cmd"
)

$gitFound = $false
$gitInstallPath = $null

foreach ($path in $gitPaths) {
    if (Test-Path "$path\git.exe") {
        $gitInstallPath = $path
        $gitFound = $true
        Write-Host "找到 Git: $path" -ForegroundColor Green
        break
    }
}

if (-not $gitFound) {
    # 尝试从注册表查找
    $regPath = "HKLM:\SOFTWARE\GitForWindows"
    if (Test-Path $regPath) {
        $installPath = (Get-ItemProperty -Path $regPath -Name "InstallPath" -ErrorAction SilentlyContinue).InstallPath
        if ($installPath) {
            $gitInstallPath = Join-Path $installPath "cmd"
            $gitFound = $true
            Write-Host "从注册表找到 Git: $gitInstallPath" -ForegroundColor Green
        }
    }
}

if (-not $gitFound) {
    Write-Host "未找到 Git 安装路径，请手动指定 Git 的 cmd 文件夹路径" -ForegroundColor Red
    exit 1
}

# 获取当前用户 PATH
$userPath = [Environment]::GetEnvironmentVariable("Path", "User")
$machinePath = [Environment]::GetEnvironmentVariable("Path", "Machine")

Write-Host "`n当前用户 PATH: $userPath" -ForegroundColor Yellow
Write-Host "`n当前系统 PATH: $machinePath" -ForegroundColor Yellow

# 检查是否已存在
if ($userPath -like "*$gitInstallPath*" -or $machinePath -like "*$gitInstallPath*") {
    Write-Host "`nGit 路径已存在于环境变量中" -ForegroundColor Green
} else {
    Write-Host "`n正在添加 Git 到用户环境变量..." -ForegroundColor Cyan
    $newPath = $userPath + ";" + $gitInstallPath
    [Environment]::SetEnvironmentVariable("Path", $newPath, "User")
    Write-Host "Git 路径已添加: $gitInstallPath" -ForegroundColor Green
}

# 同时添加 Git bin 目录（包含 ssh 等工具）
$gitBinPath = $gitInstallPath -replace "\\cmd$", "\bin"
if ($userPath -notlike "*$gitBinPath*" -and $machinePath -notlike "*$gitBinPath*") {
    $newPath = [Environment]::GetEnvironmentVariable("Path", "User") + ";" + $gitBinPath
    [Environment]::SetEnvironmentVariable("Path", $newPath, "User")
    Write-Host "Git bin 路径已添加: $gitBinPath" -ForegroundColor Green
}

Write-Host "`n修复完成！请重新打开 VS Code 或终端以使更改生效。" -ForegroundColor Cyan
Write-Host "验证命令: git --version" -ForegroundColor Cyan
