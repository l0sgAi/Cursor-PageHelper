#!/bin/bash

####################################
# PageHelper Cursor - 本地打包脚本
####################################

echo ""
echo "========================================"
echo " PageHelper Cursor 本地打包工具"
echo "========================================"
echo ""

# 检查是否有 Maven
if ! command -v mvn &> /dev/null; then
    echo "[错误] 未找到 Maven，请先安装 Maven"
    exit 1
fi

# 清理
echo "[1/4] 清理旧的编译文件..."
mvn clean
if [ $? -ne 0 ]; then
    echo "[错误] 清理失败"
    exit 1
fi

# 编译
echo ""
echo "[2/4] 编译项目..."
mvn compile
if [ $? -ne 0 ]; then
    echo "[错误] 编译失败"
    exit 1
fi

# 打包
echo ""
echo "[3/4] 打包 JAR 文件..."
mvn package -DskipTests
if [ $? -ne 0 ]; then
    echo "[错误] 打包失败"
    exit 1
fi

# 安装
echo ""
echo "[4/4] 安装到本地 Maven 仓库..."
mvn install -DskipTests
if [ $? -ne 0 ]; then
    echo "[错误] 安装失败"
    exit 1
fi

echo ""
echo "========================================"
echo " 打包成功！"
echo "========================================"
echo ""
echo "安装位置: ~/.m2/repository/com/github/pagehelper/pagehelper/"
echo ""
echo "在项目中添加以下依赖即可使用："
echo ""
echo "<dependency>"
echo "    <groupId>com.github.pagehelper</groupId>"
echo "    <artifactId>pagehelper</artifactId>"
echo "    <version>6.1.1-cursor-SNAPSHOT</version>"
echo "</dependency>"
echo ""
echo "========================================"

