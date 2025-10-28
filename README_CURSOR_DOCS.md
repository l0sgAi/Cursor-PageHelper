# 📚 PageHelper Cursor 分页 - 文档总览

欢迎使用 PageHelper Cursor 分页改造方案！这是一套完整的文档体系，帮助你快速实现高性能的游标分页。

---

## 🗂️ 文档导航

### 🚀 快速上手（5 分钟）

**[QUICKSTART.md](./QUICKSTART.md)** - 新手必读

- ⚡ 3 步完成打包和使用
- ✨ 立即开始使用示例
- 🔍 安装验证方法
- 🆘 常见问题解答

👉 **建议新手从这里开始！**

---

### 📖 总览文档

**[CURSOR_PAGINATION_README.md](./CURSOR_PAGINATION_README.md)** - 项目概述

- 🎯 改造目标说明
- 📊 性能提升效果
- ⚡ 实现原理对比
- ✅ 适用场景分析
- 📝 代码示例速查

---

### ⚡ 快速参考

**[QUICK_REFERENCE.md](./QUICK_REFERENCE.md)** - 速查手册

- 📊 架构对比图
- 🎯 需要修改的文件清单
- 📝 核心代码片段
- 🚀 使用对比示例
- ⚡ 性能提升数据
- 🛠️ 修改步骤建议
- 📋 自检清单
- 🔍 调试技巧

---

### 📚 完整指南

**[CURSOR_PAGINATION_GUIDE.md](./CURSOR_PAGINATION_GUIDE.md)** - 详细改造方案

- 🎯 为什么需要 Cursor 分页？
- 📋 5 个核心修改点详解
- 🔧 完整使用示例
- ⚠️ 注意事项和限制
- 🔍 其他数据库支持
- 📊 性能对比数据

---

### 💻 代码示例

**[CURSOR_IMPLEMENTATION_EXAMPLES.md](./CURSOR_IMPLEMENTATION_EXAMPLES.md)** - 实现代码大全

- 📝 所有文件的完整代码
- 💡 逐行注释说明
- 🧪 完整测试用例
- 🎯 最佳实践建议
- 🔧 各数据库实现对比

---

### 📦 打包发布

**[BUILD_AND_DEPLOY_GUIDE.md](./BUILD_AND_DEPLOY_GUIDE.md)** - 打包部署指南

- 📦 本地打包使用（5 分钟）
- 🏢 发布到私有 Maven 仓库
- 🌍 发布到 Maven 中央仓库
- 🤝 向官方提交贡献
- 📋 完整打包流程示例
- 🧪 测试打包结果

---

### 🛠️ 打包脚本

- **build-local.bat** - Windows 自动打包脚本
- **build-local.sh** - Linux/Mac 自动打包脚本

---

## 📊 文档体系图

```
PageHelper Cursor 分页文档
│
├─ 🚀 QUICKSTART.md ⭐ 从这里开始
│   └─ 5分钟快速上手
│
├─ 📖 CURSOR_PAGINATION_README.md
│   └─ 项目总览和概述
│
├─ ⚡ QUICK_REFERENCE.md
│   └─ 速查手册和清单
│
├─ 📚 CURSOR_PAGINATION_GUIDE.md
│   └─ 完整改造方案
│
├─ 💻 CURSOR_IMPLEMENTATION_EXAMPLES.md
│   └─ 详细代码示例
│
├─ 📦 BUILD_AND_DEPLOY_GUIDE.md
│   └─ 打包发布指南
│
└─ 🛠️ build-local.*
    └─ 自动打包脚本
```

---

## 🎯 根据你的需求选择文档

### 我是新手，第一次使用

👉 阅读：**[QUICKSTART.md](./QUICKSTART.md)**

### 我想了解整体方案

👉 阅读：**[CURSOR_PAGINATION_README.md](./CURSOR_PAGINATION_README.md)**

### 我想快速查找信息

👉 阅读：**[QUICK_REFERENCE.md](./QUICK_REFERENCE.md)**

### 我需要详细的改造步骤

👉 阅读：**[CURSOR_PAGINATION_GUIDE.md](./CURSOR_PAGINATION_GUIDE.md)**

### 我需要完整的代码示例

👉 阅读：**[CURSOR_IMPLEMENTATION_EXAMPLES.md](./CURSOR_IMPLEMENTATION_EXAMPLES.md)**

### 我想打包发布

👉 阅读：**[BUILD_AND_DEPLOY_GUIDE.md](./BUILD_AND_DEPLOY_GUIDE.md)**

---

## 🔥 推荐阅读顺序

### 初学者路线

```
1. QUICKSTART.md (5分钟)
   ↓
2. CURSOR_PAGINATION_README.md (10分钟)
   ↓
3. QUICK_REFERENCE.md (按需查阅)
```

### 开发者路线

```
1. QUICK_REFERENCE.md (了解架构)
   ↓
2. CURSOR_PAGINATION_GUIDE.md (详细方案)
   ↓
3. CURSOR_IMPLEMENTATION_EXAMPLES.md (代码实现)
   ↓
4. BUILD_AND_DEPLOY_GUIDE.md (打包发布)
```

### 高级用户路线

```
1. CURSOR_IMPLEMENTATION_EXAMPLES.md (直接看代码)
   ↓
2. BUILD_AND_DEPLOY_GUIDE.md (快速部署)
```

---

## 📈 学习路径

```
第1步：快速了解
├─ 阅读 QUICKSTART.md
└─ 运行打包脚本

第2步：深入理解
├─ 阅读 CURSOR_PAGINATION_GUIDE.md
└─ 理解实现原理

第3步：动手实践
├─ 参考 CURSOR_IMPLEMENTATION_EXAMPLES.md
└─ 逐个修改文件

第4步：测试验证
├─ 编写单元测试
└─ 性能对比测试

第5步：打包使用
├─ 参考 BUILD_AND_DEPLOY_GUIDE.md
└─ 在项目中使用
```

---

## 🎓 核心知识点

### 1. 什么是 Cursor 分页？

基于索引字段（如 ID）的值进行分页，而不是使用 OFFSET。

**传统分页**：

```sql
SELECT * FROM user ORDER BY id LIMIT 10000, 10;
```

**Cursor 分页**：

```sql
SELECT * FROM user WHERE id > 10000 ORDER BY id LIMIT 10;
```

### 2. 为什么性能更好？

- ✅ 利用索引直接定位，不需要扫描跳过的数据
- ✅ 性能恒定，不受页码影响
- ✅ 深分页场景性能提升 100-1000 倍

### 3. 核心修改点

1. **Page.java** - 添加游标字段
2. **MySqlDialect.java** - 修改 SQL 生成逻辑
3. **PageMethod.java** - 提供 Cursor API
4. **Constant.java** - 添加参数常量

### 4. 适用场景

- ✅ 移动端下拉加载
- ✅ 无限滚动列表
- ✅ 实时数据流
- ❌ 需要跳页的分页器

---

## 🔧 快速命令

### 打包安装

```bash
# Windows
.\build-local.bat

# Linux/Mac
./build-local.sh

# 手动
mvn clean install -DskipTests
```

### 在项目中使用

```xml
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper</artifactId>
    <version>6.1.1-cursor-1.0.0</version>
</dependency>
```

### 代码示例

```java
// 基本用法
PageHelper.startCursor("id", lastId, 10);
List<User> users = userMapper.selectAll();

// 不查询count
PageHelper.startCursorNoCount("id", lastId, 10);

// 降序分页
PageHelper.startCursor("id", lastId, 10, false);
```

---

## 📊 性能对比

| 页码        | 传统分页 | Cursor 分页 | 提升        |
| ----------- | -------- | ----------- | ----------- |
| 第 1 页     | 5ms      | 5ms         | -           |
| 第 100 页   | 50ms     | 5ms         | **10 倍**   |
| 第 1000 页  | 500ms    | 5ms         | **100 倍**  |
| 第 10000 页 | 5000ms   | 5ms         | **1000 倍** |

---

## 🆘 获取帮助

### 常见问题

查看各文档中的 "常见问题" 章节

### 调试技巧

参考 **[QUICK_REFERENCE.md](./QUICK_REFERENCE.md)** 中的 "调试技巧"

### 测试用例

参考 **[CURSOR_IMPLEMENTATION_EXAMPLES.md](./CURSOR_IMPLEMENTATION_EXAMPLES.md)** 中的测试代码

---

## 🤝 贡献

如果你有任何建议或发现问题：

- 提出 Issue
- 提交 Pull Request
- 分享使用经验

---

## 📄 许可证

遵循 PageHelper 的 MIT 许可证

---

## 🎉 开始吧！

选择适合你的文档开始阅读：

- 🚀 新手入门 → [QUICKSTART.md](./QUICKSTART.md)
- 📖 了解概述 → [CURSOR_PAGINATION_README.md](./CURSOR_PAGINATION_README.md)
- ⚡ 快速查阅 → [QUICK_REFERENCE.md](./QUICK_REFERENCE.md)
- 📚 详细方案 → [CURSOR_PAGINATION_GUIDE.md](./CURSOR_PAGINATION_GUIDE.md)
- 💻 代码示例 → [CURSOR_IMPLEMENTATION_EXAMPLES.md](./CURSOR_IMPLEMENTATION_EXAMPLES.md)
- 📦 打包部署 → [BUILD_AND_DEPLOY_GUIDE.md](./BUILD_AND_DEPLOY_GUIDE.md)

**祝你使用愉快！🚀**
