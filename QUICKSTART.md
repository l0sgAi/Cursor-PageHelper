# ⚡ 快速开始 - 5 分钟上手指南

## 🎯 目标

5 分钟内完成 PageHelper Cursor 分页的打包和使用。

---

## 📋 前置要求

- ✅ JDK 8+
- ✅ Maven 3.x
- ✅ 已完成代码修改（参考其他文档）

---

## 🚀 三步上手

### 步骤 1：修改版本号（1 分钟）

编辑 `pom.xml` 第 31 行：

```xml
<version>6.1.1-cursor-1.0.0</version>
```

**建议版本格式**：

- `6.1.1-cursor-1.0.0` - 正式版
- `6.1.1-cursor-SNAPSHOT` - 开发版

### 步骤 2：打包安装（2 分钟）

**方式 A：使用脚本（推荐）**

Windows:

```bash
.\build-local.bat
```

Linux/Mac:

```bash
chmod +x build-local.sh
./build-local.sh
```

**方式 B：手动命令**

```bash
mvn clean install -DskipTests
```

### 步骤 3：在项目中使用（2 分钟）

在你的项目 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper</artifactId>
    <version>6.1.1-cursor-1.0.0</version>
</dependency>
```

---

## ✨ 立即使用

### 基本用法

```java
// 首次查询
PageHelper.startCursor("id", 0L, 10);
List<User> users = userMapper.selectAll();

// 获取最后一条记录的ID
Long lastId = users.get(users.size() - 1).getId();

// 下一页
PageHelper.startCursor("id", lastId, 10);
List<User> nextPage = userMapper.selectAll();
```

### 控制器示例

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @GetMapping
    public PageInfo<User> getUsers(
        @RequestParam(required = false, defaultValue = "0") Long cursor) {

        // 使用 Cursor 分页
        PageHelper.startCursor("id", cursor, 20);
        List<User> users = userMapper.selectAll();

        return new PageInfo<>(users);
    }
}
```

### API 参考

```java
// 基本用法
PageHelper.startCursor("id", lastId, 10);

// 不查询 count（性能更好）
PageHelper.startCursorNoCount("id", lastId, 10);

// 降序分页（使用 < 比较）
PageHelper.startCursor("id", lastId, 10, false);

// 链式调用
PageHelper.startPage(1, 10).cursor("id", lastId);
```

---

## 🔍 验证安装

### 方法 1：查看文件

Windows:

```bash
dir %USERPROFILE%\.m2\repository\com\github\pagehelper\pagehelper\6.1.1-cursor-1.0.0\
```

Linux/Mac:

```bash
ls ~/.m2/repository/com/github/pagehelper/pagehelper/6.1.1-cursor-1.0.0/
```

应该看到：

```
pagehelper-6.1.1-cursor-1.0.0.jar
pagehelper-6.1.1-cursor-1.0.0.pom
...
```

### 方法 2：测试代码

创建测试类：

```java
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;

public class QuickTest {
    public static void main(String[] args) {
        Page<Object> page = PageHelper.startCursor("id", 100L, 10);

        if (page.useCursor()) {
            System.out.println("✅ Cursor 分页功能已启用！");
            System.out.println("游标字段: " + page.getCursorColumn());
            System.out.println("游标值: " + page.getCursorValue());
        } else {
            System.out.println("❌ Cursor 分页未启用");
        }
    }
}
```

---

## 📊 生成的 SQL

### 传统分页

```sql
-- 第10000页，性能差
SELECT * FROM user ORDER BY id LIMIT 100000, 10;
```

### Cursor 分页

```sql
-- 任意深度，性能恒定
SELECT * FROM user WHERE id > 100000 ORDER BY id LIMIT 10;
```

---

## ⚠️ 注意事项

### ✅ 必须满足

1. **游标字段有索引**（最好是主键）
2. **SQL 包含 ORDER BY**（按游标字段排序）
3. **游标值是递增/递减的**

### ❌ 不适合的场景

- 需要跳页功能
- 需要显示总页数
- 需要随机访问页面

---

## 🎯 适用场景

### ✅ 非常适合

- 📱 移动端下拉加载
- 📜 无限滚动列表
- 💬 消息/评论流
- 📊 实时数据流
- 📁 大数据导出

### 示例：移动端 Feed 流

```java
@GetMapping("/feed")
public List<Post> getFeed(@RequestParam(required = false) Long lastId) {
    if (lastId == null) lastId = 0L;
    PageHelper.startCursorNoCount("id", lastId, 20);
    return postMapper.getPublicPosts();
}
```

---

## 🆘 常见问题

### Q: 找不到 startCursor 方法？

**A**: 确认已执行 `mvn install` 并刷新 Maven 项目。

### Q: 版本冲突？

**A**: 检查项目中是否已有 pagehelper 依赖，移除旧版本。

### Q: 性能没有提升？

**A**: 确认：

1. 游标字段是否有索引？
2. SQL 是否包含 ORDER BY？
3. 是否真的启用了 Cursor 模式？（检查 `page.useCursor()`）

### Q: 如何降序分页？

**A**:

```java
// DESC 排序使用 < 比较
PageHelper.startCursor("id", lastId, 10, false);
```

---

## 📚 深入学习

- 📖 [完整改造方案](./CURSOR_PAGINATION_GUIDE.md)
- 💻 [详细代码示例](./CURSOR_IMPLEMENTATION_EXAMPLES.md)
- 📦 [打包发布指南](./BUILD_AND_DEPLOY_GUIDE.md)
- ⚡ [快速参考](./QUICK_REFERENCE.md)

---

## 🎉 完成！

恭喜！你已经成功打包并使用 PageHelper Cursor 分页。

**性能提升**：深分页场景可达 **100-1000 倍**！

享受高性能分页吧！🚀
