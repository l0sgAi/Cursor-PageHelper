# Cursor 分页 - 具体实现代码示例

本文档提供所有需要修改文件的完整代码示例。

---

## 1. Constant.java - 添加游标参数常量

**文件路径**: `src/main/java/com/github/pagehelper/Constant.java`

### 在第 38 行后添加：

```java
    //第二个分页参数
    String PAGEPARAMETER_SECOND = "Second" + SUFFIX_PAGE;

    // ========== 新增 ==========
    //游标字段参数名
    String PAGEPARAMETER_CURSOR_COLUMN = "CursorColumn" + SUFFIX_PAGE;
    //游标值参数名
    String PAGEPARAMETER_CURSOR_VALUE = "CursorValue" + SUFFIX_PAGE;
}
```

---

## 2. Page.java - 添加游标字段和方法

**文件路径**: `src/main/java/com/github/pagehelper/Page.java`

### A. 在第 120 行（asyncCount 字段后）添加字段：

```java
    /**
     * 异步count查询
     */
    private Boolean asyncCount;

    // ========== Cursor分页支持 ==========
    /**
     * 是否启用游标分页
     */
    private Boolean useCursor;

    /**
     * 游标字段名（例如: "id", "created_at"等）
     * 必须是有索引的字段，建议使用主键
     */
    private String cursorColumn;

    /**
     * 游标值（上次查询最后一条记录的游标字段值）
     * 首次查询时可以为null
     */
    private Object cursorValue;

    /**
     * 游标比较方向
     * true: 使用 > 比较（配合 ASC 排序）
     * false: 使用 < 比较（配合 DESC 排序）
     */
    private Boolean cursorGreaterThan = true;
```

### B. 在第 342 行（setAsyncCount 方法后）添加 Getter/Setter：

```java
    public void setAsyncCount(Boolean asyncCount) {
        this.asyncCount = asyncCount;
    }

    // ========== Cursor分页方法 ==========

    public Boolean getUseCursor() {
        return useCursor;
    }

    public Page<E> setUseCursor(Boolean useCursor) {
        this.useCursor = useCursor;
        return this;
    }

    public String getCursorColumn() {
        return cursorColumn;
    }

    /**
     * 设置游标字段名，包含SQL注入检查
     */
    public Page<E> setCursorColumn(String cursorColumn) {
        if (cursorColumn != null && SqlSafeUtil.check(cursorColumn)) {
            throw new PageException("cursor column [" + cursorColumn + "] has a risk of SQL injection");
        }
        this.cursorColumn = cursorColumn;
        return this;
    }

    public Object getCursorValue() {
        return cursorValue;
    }

    public Page<E> setCursorValue(Object cursorValue) {
        this.cursorValue = cursorValue;
        return this;
    }

    public Boolean getCursorGreaterThan() {
        return cursorGreaterThan;
    }

    public Page<E> setCursorGreaterThan(Boolean cursorGreaterThan) {
        this.cursorGreaterThan = cursorGreaterThan;
        return this;
    }

    /**
     * 判断是否真正启用游标分页
     * 需要同时满足：useCursor=true 且 cursorColumn和cursorValue都不为null
     */
    public boolean useCursor() {
        return this.useCursor != null
            && this.useCursor
            && this.cursorColumn != null
            && this.cursorValue != null;
    }
```

### C. 在第 350 行附近添加链式调用方法：

```java
    /**
     * 指定使用的分页实现，如果自己使用的很频繁，建议自己增加一层封装再使用
     *
     * @param dialect 分页实现类，可以使用 {@link com.github.pagehelper.page.PageAutoDialect} 类中注册的别名，例如 "mysql", "oracle"
     * @return
     */
    public Page<E> using(String dialect) {
        this.dialectClass = dialect;
        return this;
    }

    // ========== 新增：Cursor分页链式调用方法 ==========

    /**
     * 启用游标分页（ASC排序）
     *
     * @param cursorColumn 游标字段名（如 "id", "created_at"）
     * @param cursorValue  游标值（上次查询最后一条记录的游标值）
     * @return Page对象
     */
    public Page<E> cursor(String cursorColumn, Object cursorValue) {
        this.useCursor = true;
        setCursorColumn(cursorColumn);
        this.cursorValue = cursorValue;
        this.cursorGreaterThan = true; // 默认使用 >
        return this;
    }

    /**
     * 启用游标分页（指定排序方向）
     *
     * @param cursorColumn  游标字段名
     * @param cursorValue   游标值
     * @param greaterThan   true: 使用 > (ASC排序), false: 使用 < (DESC排序)
     * @return Page对象
     */
    public Page<E> cursor(String cursorColumn, Object cursorValue, boolean greaterThan) {
        this.useCursor = true;
        setCursorColumn(cursorColumn);
        this.cursorValue = cursorValue;
        this.cursorGreaterThan = greaterThan;
        return this;
    }

    /**
     * 启用游标分页
     *
     * @return Page对象
     */
    public Page<E> enableCursor() {
        this.useCursor = true;
        return this;
    }

    /**
     * 禁用游标分页
     *
     * @return Page对象
     */
    public Page<E> disableCursor() {
        this.useCursor = false;
        return this;
    }
```

### D. 修改 toString 方法（第 613 行）：

```java
    @Override
    public String toString() {
        return "Page{" +
                "count=" + count +
                ", pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", startRow=" + startRow +
                ", endRow=" + endRow +
                ", total=" + total +
                ", pages=" + pages +
                ", reasonable=" + reasonable +
                ", pageSizeZero=" + pageSizeZero +
                ", useCursor=" + useCursor +
                ", cursorColumn='" + cursorColumn + '\'' +
                ", cursorValue=" + cursorValue +
                ", cursorGreaterThan=" + cursorGreaterThan +
                '}' + super.toString();
    }
```

---

## 3. MySqlDialect.java - 实现 Cursor 分页 SQL

**文件路径**: `src/main/java/com/github/pagehelper/dialect/helper/MySqlDialect.java`

### 完整替换文件内容：

```java
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2023 abel533@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.github.pagehelper.dialect.helper;

import com.github.pagehelper.Page;
import com.github.pagehelper.dialect.AbstractHelperDialect;
import com.github.pagehelper.util.MetaObjectUtil;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author liuzh
 */
public class MySqlDialect extends AbstractHelperDialect {

    @Override
    public Object processPageParameter(MappedStatement ms, Map<String, Object> paramMap, Page page, BoundSql boundSql, CacheKey pageKey) {
        // ========== Cursor分页参数处理 ==========
        if (page.useCursor()) {
            paramMap.put(PAGEPARAMETER_CURSOR_COLUMN, page.getCursorColumn());
            paramMap.put(PAGEPARAMETER_CURSOR_VALUE, page.getCursorValue());
            paramMap.put(PAGEPARAMETER_SECOND, page.getPageSize());

            // 更新缓存键
            pageKey.update(page.getCursorColumn());
            pageKey.update(page.getCursorValue());
            pageKey.update(page.getPageSize());
            pageKey.update(page.getCursorGreaterThan());

            // 处理参数映射
            if (boundSql.getParameterMappings() != null) {
                List<ParameterMapping> newParameterMappings = new ArrayList<>(boundSql.getParameterMappings());

                // 添加游标值参数
                Class<?> cursorValueClass = page.getCursorValue().getClass();
                newParameterMappings.add(
                    new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_CURSOR_VALUE, cursorValueClass).build()
                );

                // 添加pageSize参数
                newParameterMappings.add(
                    new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_SECOND, int.class).build()
                );

                MetaObject metaObject = MetaObjectUtil.forObject(boundSql);
                metaObject.setValue("parameterMappings", newParameterMappings);
            }
            return paramMap;
        }

        // ========== 传统分页参数处理（保持原有逻辑） ==========
        paramMap.put(PAGEPARAMETER_FIRST, page.getStartRow());
        paramMap.put(PAGEPARAMETER_SECOND, page.getPageSize());
        pageKey.update(page.getStartRow());
        pageKey.update(page.getPageSize());

        if (boundSql.getParameterMappings() != null) {
            List<ParameterMapping> newParameterMappings = new ArrayList<>(boundSql.getParameterMappings());
            if (page.getStartRow() == 0) {
                newParameterMappings.add(
                    new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_SECOND, int.class).build()
                );
            } else {
                newParameterMappings.add(
                    new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_FIRST, long.class).build()
                );
                newParameterMappings.add(
                    new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_SECOND, int.class).build()
                );
            }
            MetaObject metaObject = MetaObjectUtil.forObject(boundSql);
            metaObject.setValue("parameterMappings", newParameterMappings);
        }
        return paramMap;
    }

    @Override
    public String getPageSql(String sql, Page page, CacheKey pageKey) {
        // ========== Cursor分页SQL生成 ==========
        if (page.useCursor()) {
            StringBuilder sqlBuilder = new StringBuilder(sql.length() + 100);
            sqlBuilder.append(sql);

            // 智能添加WHERE或AND
            String upperSql = sql.toUpperCase().trim();
            if (containsWhereClauseInMainQuery(upperSql)) {
                sqlBuilder.append("\n AND ");
            } else {
                sqlBuilder.append("\n WHERE ");
            }

            // 添加游标条件
            sqlBuilder.append(page.getCursorColumn());
            if (page.getCursorGreaterThan() != null && page.getCursorGreaterThan()) {
                sqlBuilder.append(" > ?");
            } else {
                sqlBuilder.append(" < ?");
            }

            sqlBuilder.append("\n LIMIT ?");
            return sqlBuilder.toString();
        }

        // ========== 传统分页SQL生成（保持原有逻辑） ==========
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 14);
        sqlBuilder.append(sql);
        if (page.getStartRow() == 0) {
            sqlBuilder.append("\n LIMIT ? ");
        } else {
            sqlBuilder.append("\n LIMIT ?, ? ");
        }
        return sqlBuilder.toString();
    }

    /**
     * 检查主查询是否包含WHERE子句
     * 需要排除子查询中的WHERE
     */
    private boolean containsWhereClauseInMainQuery(String upperSql) {
        // 简单判断：查找最后一个WHERE的位置
        int lastWhereIndex = upperSql.lastIndexOf("WHERE");
        if (lastWhereIndex == -1) {
            return false;
        }

        // 查找最后一个FROM的位置（主查询的FROM）
        int lastFromIndex = upperSql.lastIndexOf("FROM");

        // 如果WHERE在FROM之后，说明是主查询的WHERE
        return lastWhereIndex > lastFromIndex;
    }

}
```

---

## 4. PageMethod.java - 添加 Cursor 分页 API

**文件路径**: `src/main/java/com/github/pagehelper/page/PageMethod.java`

### 在第 199 行（orderBy 方法后）添加：

```java
    /**
     * 排序
     *
     * @param orderBy
     */
    public static void orderBy(String orderBy) {
        Page<?> page = getLocalPage();
        if (page != null) {
            page.setOrderBy(orderBy);
            if (page.getPageSizeZero() != null && page.getPageSizeZero() && page.getPageSize() == 0) {
                page.setOrderByOnly(true);
            }
        } else {
            page = new Page();
            page.setOrderBy(orderBy);
            page.setOrderByOnly(true);
            setLocalPage(page);
        }
    }

    // ========== 新增：Cursor分页方法 ==========

    /**
     * 开启游标分页
     *
     * @param cursorColumn 游标字段名（如 "id"）
     * @param cursorValue  游标值（上次查询最后一条记录的游标值，首次查询传0或null）
     * @param pageSize     每页大小
     * @return Page对象
     */
    public static <E> Page<E> startCursor(String cursorColumn, Object cursorValue, int pageSize) {
        return startCursor(cursorColumn, cursorValue, pageSize, true, true);
    }

    /**
     * 开启游标分页（不查询count）
     *
     * @param cursorColumn 游标字段名
     * @param cursorValue  游标值
     * @param pageSize     每页大小
     * @return Page对象
     */
    public static <E> Page<E> startCursorNoCount(String cursorColumn, Object cursorValue, int pageSize) {
        return startCursor(cursorColumn, cursorValue, pageSize, false, true);
    }

    /**
     * 开启游标分页（指定排序方向）
     *
     * @param cursorColumn 游标字段名
     * @param cursorValue  游标值
     * @param pageSize     每页大小
     * @param greaterThan  true: 使用 > (配合ASC), false: 使用 < (配合DESC)
     * @return Page对象
     */
    public static <E> Page<E> startCursor(String cursorColumn, Object cursorValue,
                                           int pageSize, boolean greaterThan) {
        return startCursor(cursorColumn, cursorValue, pageSize, DEFAULT_COUNT, greaterThan);
    }

    /**
     * 开启游标分页（完整参数）
     *
     * @param cursorColumn 游标字段名
     * @param cursorValue  游标值
     * @param pageSize     每页大小
     * @param count        是否查询总数
     * @param greaterThan  比较方向
     * @return Page对象
     */
    public static <E> Page<E> startCursor(String cursorColumn, Object cursorValue,
                                           int pageSize, boolean count, boolean greaterThan) {
        Page<E> page = new Page<>(1, pageSize, count);
        page.setUseCursor(true);
        page.setCursorColumn(cursorColumn);
        page.setCursorValue(cursorValue);
        page.setCursorGreaterThan(greaterThan);

        // 保留已有的orderBy设置
        Page<E> oldPage = getLocalPage();
        if (oldPage != null && oldPage.isOrderByOnly()) {
            page.setOrderBy(oldPage.getOrderBy());
        }

        setLocalPage(page);
        return page;
    }

    /**
     * 设置参数
     *
     * @param properties 插件属性
     */
    protected static void setStaticProperties(Properties properties) {
        //defaultCount，这是一个全局生效的参数，多数据源时也是统一的行为
        if (properties != null) {
            DEFAULT_COUNT = Boolean.valueOf(properties.getProperty("defaultCount", "true"));
        }
    }

}
```

---

## 5. PostgreSqlDialect.java - Cursor 分页实现

**文件路径**: `src/main/java/com/github/pagehelper/dialect/helper/PostgreSqlDialect.java`

### 修改 processPageParameter 方法和 getPageSql 方法：

参照 MySqlDialect 的实现，主要区别：

- PostgreSQL 使用 `LIMIT ? OFFSET ?`
- 游标分页时使用 `WHERE column > ? LIMIT ?`（与 MySQL 相同）

```java
@Override
public String getPageSql(String sql, Page page, CacheKey pageKey) {
    // ========== Cursor分页SQL生成 ==========
    if (page.useCursor()) {
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 100);
        sqlBuilder.append(sql);

        // 智能添加WHERE或AND
        String upperSql = sql.toUpperCase().trim();
        if (containsWhereClauseInMainQuery(upperSql)) {
            sqlBuilder.append("\n AND ");
        } else {
            sqlBuilder.append("\n WHERE ");
        }

        // 添加游标条件
        sqlBuilder.append(page.getCursorColumn());
        if (page.getCursorGreaterThan() != null && page.getCursorGreaterThan()) {
            sqlBuilder.append(" > ?");
        } else {
            sqlBuilder.append(" < ?");
        }

        sqlBuilder.append("\n LIMIT ?");
        return sqlBuilder.toString();
    }

    // ========== 传统分页SQL（原有逻辑） ==========
    StringBuilder sqlStr = new StringBuilder(sql.length() + 17);
    sqlStr.append(sql);
    if (page.getStartRow() == 0) {
        sqlStr.append(" LIMIT ?");
    } else {
        sqlStr.append(" LIMIT ? OFFSET ?");
    }
    return sqlStr.toString();
}

private boolean containsWhereClauseInMainQuery(String upperSql) {
    int lastWhereIndex = upperSql.lastIndexOf("WHERE");
    if (lastWhereIndex == -1) {
        return false;
    }
    int lastFromIndex = upperSql.lastIndexOf("FROM");
    return lastWhereIndex > lastFromIndex;
}
```

---

## 6. 使用示例代码

### 示例 1：基本使用

```java
// Controller层
@GetMapping("/users")
public PageInfo<User> getUserList(@RequestParam(required = false) Long lastId) {
    // 使用游标分页
    if (lastId == null) {
        lastId = 0L; // 首次查询
    }
    PageHelper.startCursor("id", lastId, 10);
    List<User> users = userService.selectAll();
    return new PageInfo<>(users);
}

// Mapper
List<User> selectAll();

// XML (无需修改，PageHelper会自动注入WHERE和LIMIT)
<select id="selectAll" resultType="User">
    SELECT * FROM user ORDER BY id ASC
</select>
```

### 示例 2：降序分页

```java
// DESC排序时，使用 < 比较
Long lastId = 1000L;
PageHelper.startCursor("id", lastId, 10, false); // false表示使用 <
List<User> users = userMapper.selectAll();

// 生成SQL: SELECT * FROM user WHERE id < ? ORDER BY id DESC LIMIT ?
```

### 示例 3：基于时间戳的游标

```java
// 使用创建时间作为游标
Date lastCreateTime = lastRecord.getCreateTime();
PageHelper.startCursor("created_at", lastCreateTime, 20);
List<Order> orders = orderMapper.selectAll();

// 生成SQL: SELECT * FROM orders WHERE created_at > ? ORDER BY created_at LIMIT ?
```

### 示例 4：不查询总数（性能更好）

```java
// 无限滚动场景，不需要总数
PageHelper.startCursorNoCount("id", lastId, 10);
List<User> users = userMapper.selectAll();
```

### 示例 5：链式调用

```java
Page<User> page = PageHelper.startPage(1, 10)
    .cursor("id", lastId)           // 启用游标
    .setCount(false)                // 不查询总数
    .setOrderBy("id ASC");          // 设置排序

List<User> users = userMapper.selectAll();
```

---

## 7. 生成的 SQL 对比

### 传统分页

```sql
-- 第1页
SELECT * FROM user ORDER BY id LIMIT 0, 10;

-- 第100页（性能差）
SELECT * FROM user ORDER BY id LIMIT 1000, 10;

-- 第10000页（性能很差）
SELECT * FROM user ORDER BY id LIMIT 100000, 10;
```

### Cursor 分页

```sql
-- 首次查询
SELECT * FROM user WHERE id > 0 ORDER BY id LIMIT 10;

-- 下一页（lastId=10）
SELECT * FROM user WHERE id > 10 ORDER BY id LIMIT 10;

-- 再下一页（lastId=20）
SELECT * FROM user WHERE id > 20 ORDER BY id LIMIT 10;

-- 无论查询多少页，性能都是一致的！
```

---

## 8. 注意事项

### ✅ 适合的场景

- 移动端下拉加载
- 无限滚动列表
- 实时数据流
- 数据导出（顺序遍历）

### ❌ 不适合的场景

- 需要跳页功能
- 需要显示总页数
- 需要随机访问任意页
- 游标字段值会频繁变化

### ⚠️ 最佳实践

1. **游标字段选择**：使用主键或唯一索引字段
2. **排序必须一致**：SQL 必须按游标字段排序
3. **SQL 注入防护**：已内置 SqlSafeUtil 检查
4. **WHERE 条件复杂时**：建议使用子查询或视图简化

---

## 9. 测试建议

```java
@Test
public void testCursorPagination() {
    // 首次查询
    PageHelper.startCursor("id", 0L, 10);
    List<User> page1 = userMapper.selectAll();
    assertEquals(10, page1.size());

    // 获取最后一条记录的ID
    Long lastId = page1.get(page1.size() - 1).getId();

    // 下一页
    PageHelper.startCursor("id", lastId, 10);
    List<User> page2 = userMapper.selectAll();
    assertEquals(10, page2.size());

    // 验证：第二页的第一条ID应该大于第一页的最后一条ID
    assertTrue(page2.get(0).getId() > lastId);
}
```

---

## 总结

通过以上修改，PageHelper 将同时支持：

- ✅ 传统的 OFFSET 分页（向后兼容）
- ✅ 高性能的 Cursor 分页（新增功能）

开发者可以根据实际场景选择合适的分页方式！
