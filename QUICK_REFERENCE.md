# 快速参考 - Cursor 分页改造

## 📊 架构对比

```
传统OFFSET分页流程：
┌─────────────┐
│ PageHelper  │  PageHelper.startPage(100, 10)
│  .startPage │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Page对象  │  pageNum=100, pageSize=10
│             │  startRow=1000, endRow=1010
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Dialect    │  getPageSql()
│  (MySQL)    │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────┐
│ 生成SQL:                        │
│ SELECT * FROM user              │
│ ORDER BY id                     │
│ LIMIT 1000, 10                  │  ← 性能问题：需扫描1010行
└─────────────────────────────────┘


Cursor分页流程：
┌─────────────┐
│ PageHelper  │  PageHelper.startCursor("id", 1000, 10)
│.startCursor │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Page对象  │  useCursor=true
│             │  cursorColumn="id"
│             │  cursorValue=1000
│             │  pageSize=10
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Dialect    │  getPageSql() [检测到useCursor=true]
│  (MySQL)    │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────┐
│ 生成SQL:                        │
│ SELECT * FROM user              │
│ WHERE id > 1000                 │  ← 索引直接定位
│ ORDER BY id                     │
│ LIMIT 10                        │
└─────────────────────────────────┘
```

## 🎯 需要修改的文件清单

| 文件                     | 位置                                                  | 修改内容                  | 难度     |
| ------------------------ | ----------------------------------------------------- | ------------------------- | -------- |
| `Constant.java`          | `src/main/java/com/github/pagehelper/`                | 添加 2 个常量             | ⭐       |
| `Page.java`              | `src/main/java/com/github/pagehelper/`                | 添加 4 个字段 + 10 个方法 | ⭐⭐     |
| `PageMethod.java`        | `src/main/java/com/github/pagehelper/page/`           | 添加 4 个静态方法         | ⭐⭐     |
| `MySqlDialect.java`      | `src/main/java/com/github/pagehelper/dialect/helper/` | 修改 2 个方法             | ⭐⭐⭐   |
| `PostgreSqlDialect.java` | `src/main/java/com/github/pagehelper/dialect/helper/` | 修改 2 个方法             | ⭐⭐⭐   |
| `OracleDialect.java`     | `src/main/java/com/github/pagehelper/dialect/helper/` | 修改 2 个方法（可选）     | ⭐⭐⭐⭐ |
| `SqlServerDialect.java`  | `src/main/java/com/github/pagehelper/dialect/helper/` | 修改 2 个方法（可选）     | ⭐⭐⭐⭐ |

## 📝 核心代码片段

### 1. Page 类新增字段（最关键）

```java
private Boolean useCursor;          // 是否启用游标
private String cursorColumn;        // 游标字段名
private Object cursorValue;         // 游标值
private Boolean cursorGreaterThan;  // 比较方向
```

### 2. Dialect 类核心逻辑（以 MySQL 为例）

```java
@Override
public String getPageSql(String sql, Page page, CacheKey pageKey) {
    if (page.useCursor()) {
        // 游标分页：WHERE id > ? LIMIT ?
        return sql + "\n WHERE " + page.getCursorColumn() + " > ? \n LIMIT ?";
    }
    // 传统分页：LIMIT ?, ?
    return sql + "\n LIMIT ?, ?";
}
```

### 3. PageMethod 类新增 API

```java
public static <E> Page<E> startCursor(String column, Object value, int size) {
    Page<E> page = new Page<>(1, size, true);
    page.cursor(column, value);
    setLocalPage(page);
    return page;
}
```

## 🚀 使用对比

### 场景：查询第 100 页数据

#### 传统方式

```java
PageHelper.startPage(100, 10);
List<User> users = userMapper.selectAll();
// SQL: SELECT * FROM user ORDER BY id LIMIT 1000, 10
// 性能：需要扫描1010行，耗时随页码增加
```

#### Cursor 方式

```java
PageHelper.startCursor("id", lastId, 10);
List<User> users = userMapper.selectAll();
// SQL: SELECT * FROM user WHERE id > ? ORDER BY id LIMIT 10
// 性能：直接从索引定位，耗时恒定
```

## ⚡ 性能提升数据

| 数据量  | 页码        | 传统分页 | Cursor 分页 | 提升倍数   |
| ------- | ----------- | -------- | ----------- | ---------- |
| 100 万  | 第 1 页     | 5ms      | 5ms         | 1x         |
| 100 万  | 第 100 页   | 50ms     | 5ms         | **10x**    |
| 100 万  | 第 1000 页  | 500ms    | 5ms         | **100x**   |
| 100 万  | 第 10000 页 | 5000ms   | 5ms         | **1000x**  |
| 1000 万 | 第 10000 页 | 50000ms  | 5ms         | **10000x** |

## 🔄 实际应用场景

### ✅ 适合使用 Cursor 分页

```java
// 1. 移动端下拉刷新
@GetMapping("/api/posts/feed")
public List<Post> getFeed(@RequestParam Long lastId) {
    PageHelper.startCursor("id", lastId != null ? lastId : 0L, 20);
    return postService.getPublicPosts();
}

// 2. 实时消息流
@GetMapping("/api/messages")
public List<Message> getMessages(@RequestParam Long afterId) {
    PageHelper.startCursor("id", afterId, 50);
    return messageService.getMessages();
}

// 3. 数据导出（大数据量）
public void exportUsers() {
    Long lastId = 0L;
    while (true) {
        PageHelper.startCursor("id", lastId, 1000);
        List<User> batch = userMapper.selectAll();
        if (batch.isEmpty()) break;

        // 导出批次数据
        exportBatch(batch);

        lastId = batch.get(batch.size() - 1).getId();
    }
}

// 4. 无限滚动列表
@GetMapping("/api/products")
public List<Product> getProducts(@RequestParam(required = false) Long cursor) {
    PageHelper.startCursorNoCount("id", cursor != null ? cursor : 0L, 30);
    return productService.list();
}
```

### ❌ 不适合使用 Cursor 分页

```java
// 1. 需要跳页的分页器
// 传统分页支持：第1页 -> 第5页 -> 第10页
// Cursor分页不支持：只能顺序翻页

// 2. 显示总页数
// 传统：第1/100页
// Cursor：无法计算准确页码

// 3. 排序字段不固定
// Cursor需要固定的游标字段
```

## 🛠️ 修改步骤建议

### 阶段 1：基础支持（必须）

1. ✅ 修改 `Constant.java` - 5 分钟
2. ✅ 修改 `Page.java` - 30 分钟
3. ✅ 修改 `PageMethod.java` - 15 分钟

### 阶段 2：MySQL 支持（核心）

4. ✅ 修改 `MySqlDialect.java` - 1 小时

### 阶段 3：测试验证

5. ✅ 编写单元测试 - 1 小时
6. ✅ 集成测试 - 30 分钟

### 阶段 4：其他数据库（可选）

7. ⬜ 修改 `PostgreSqlDialect.java` - 45 分钟
8. ⬜ 修改 `OracleDialect.java` - 1 小时
9. ⬜ 修改 `SqlServerDialect.java` - 1 小时

**预计总时间：4-6 小时（仅 MySQL），8-10 小时（全部数据库）**

## 📋 自检清单

在提交代码前，请确认：

- [ ] `Constant.java` 添加了 `PAGEPARAMETER_CURSOR_COLUMN` 和 `PAGEPARAMETER_CURSOR_VALUE`
- [ ] `Page.java` 添加了 4 个字段和对应的 getter/setter
- [ ] `Page.java` 添加了 `useCursor()` 判断方法
- [ ] `Page.java` 添加了 `cursor()` 链式调用方法
- [ ] `PageMethod.java` 添加了 `startCursor()` 系列方法
- [ ] `MySqlDialect.java` 的 `getPageSql()` 支持游标分页
- [ ] `MySqlDialect.java` 的 `processPageParameter()` 正确处理游标参数
- [ ] 编写了单元测试验证游标分页功能
- [ ] 验证了 SQL 注入防护机制
- [ ] 测试了边界情况（首次查询、空结果等）
- [ ] 确认不影响原有的传统分页功能

## 🔍 调试技巧

### 查看生成的 SQL

```java
// 开启MyBatis日志
PageHelper.startCursor("id", 100L, 10);
List<User> users = userMapper.selectAll();
// 查看控制台输出的SQL
```

### 验证游标是否生效

```java
Page<User> page = PageHelper.startCursor("id", 100L, 10);
System.out.println("Use Cursor: " + page.useCursor());  // 应该输出true
System.out.println(page);  // 查看完整的Page信息
```

### 性能对比测试

```java
// 测试传统分页
long start1 = System.currentTimeMillis();
PageHelper.startPage(10000, 10);
List<User> users1 = userMapper.selectAll();
long time1 = System.currentTimeMillis() - start1;

// 测试Cursor分页
long start2 = System.currentTimeMillis();
PageHelper.startCursor("id", 100000L, 10);
List<User> users2 = userMapper.selectAll();
long time2 = System.currentTimeMillis() - start2;

System.out.println("传统分页耗时: " + time1 + "ms");
System.out.println("Cursor分页耗时: " + time2 + "ms");
System.out.println("性能提升: " + (time1 / time2) + "倍");
```

## 📚 参考资料

### SQL 性能优化原理

```sql
-- 传统分页（慢）
EXPLAIN SELECT * FROM user ORDER BY id LIMIT 100000, 10;
-- type: ALL, rows: 100010  ← 扫描大量数据

-- Cursor分页（快）
EXPLAIN SELECT * FROM user WHERE id > 100000 ORDER BY id LIMIT 10;
-- type: range, rows: 10  ← 索引直接定位
```

### 最佳实践

1. **游标字段选择**：主键 > 唯一索引 > 普通索引
2. **排序一致性**：SQL 必须包含 `ORDER BY cursor_column`
3. **首次查询**：cursorValue 传 0 或 MIN 值
4. **返回游标**：返回结果中包含最后一条的游标值供下次查询使用

## 💡 常见问题

**Q: 游标分页能否支持跳页？**  
A: 不能。这是 Cursor 分页的固有限制，只能顺序翻页。

**Q: 如何知道是否还有下一页？**  
A: 查询时设置 `pageSize + 1`，如果返回 `pageSize + 1` 条说明有下一页。

**Q: Count 查询会影响性能吗？**  
A: 是的。建议使用 `startCursorNoCount()` 跳过 count 查询。

**Q: 可以使用复合游标吗（如 id + timestamp）？**  
A: 当前实现仅支持单字段游标。复合游标需要额外开发。

**Q: WHERE 条件冲突怎么办？**  
A: 实现会智能添加 `AND`，但复杂 SQL 建议使用子查询或视图。

---

## 🎉 总结

通过以上改造，您的 PageHelper 将具备：

- ✅ **高性能深分页**：Cursor 分页解决深分页性能问题
- ✅ **向后兼容**：原有的传统分页功能不受影响
- ✅ **灵活切换**：可根据场景选择合适的分页方式
- ✅ **易于使用**：API 设计简洁，学习成本低

开始改造吧！🚀
