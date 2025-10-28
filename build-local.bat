@echo off
REM ====================================
REM PageHelper Cursor - 本地打包脚本
REM ====================================

echo.
echo ========================================
echo  PageHelper Cursor 本地打包工具
echo ========================================
echo.

REM 检查是否有 Maven
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [错误] 未找到 Maven，请先安装 Maven 并配置环境变量
    pause
    exit /b 1
)

echo [1/4] 清理旧的编译文件...
call mvn clean
if %ERRORLEVEL% NEQ 0 (
    echo [错误] 清理失败
    pause
    exit /b 1
)

echo.
echo [2/4] 编译项目...
call mvn compile
if %ERRORLEVEL% NEQ 0 (
    echo [错误] 编译失败
    pause
    exit /b 1
)

echo.
echo [3/4] 打包 JAR 文件...
call mvn package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo [错误] 打包失败
    pause
    exit /b 1
)

echo.
echo [4/4] 安装到本地 Maven 仓库...
call mvn install -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo [错误] 安装失败
    pause
    exit /b 1
)

echo.
echo ========================================
echo  打包成功！
echo ========================================
echo.
echo 安装位置: %USERPROFILE%\.m2\repository\com\github\pagehelper\pagehelper\
echo.
echo 在项目中添加以下依赖即可使用：
echo.
echo ^<dependency^>
echo     ^<groupId^>com.github.pagehelper^</groupId^>
echo     ^<artifactId^>pagehelper^</artifactId^>
echo     ^<version^>6.1.1-cursor-SNAPSHOT^</version^>
echo ^</dependency^>
echo.
echo ========================================

pause

