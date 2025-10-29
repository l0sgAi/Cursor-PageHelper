# PageHelper-Cursor

## 支持 [MyBatis 3.1.0+](https://github.com/mybatis/mybatis-3)

**(PageHelper的cursor 修改版，支持游标分页，按照一个有序索引字段解决深分页问题)**

**目前是完全的测试版，可靠性未经完整测试，请勿用于生产环境。**

## 介绍
`PageHelper-Cursor` 是基于`PageHelper6.1.1`的`cursor`分页实现，支持按照一个有序索引字段解决深分页问题。

### 主要特性

1. 支持按照一个有序索引字段解决深分页问题
2. 目前只支持MySQL和PostgreSQL

### 注意：
> 所使用的排序字段必须有索引，否则深分页的性能优化将是无效的。

### 使用方法

需要`Mybatis3.1.0+`环境，如果您在SpringBoot上开发应用，请同时引入：
```xml
<!--SpringBoot分页插件整合-->
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper-spring-boot-starter</artifactId>
    <version>${pagehelper-spring-boot-starter.version}</version>
</dependency>

<!--pagehelper-cursor分页插件整合-->
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper</artifactId>
    <version>6.1.1-cursor-SNAPSHOT</version>
</dependency>
```

**例1：使用有序自增id分页**
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
**例2：使用`create_time`字段分页**
```java
    @GetMapping("/listWithCursor")
    @Tag(name = "获取所有评论信息", description = "管理员分页获取当前所有评论信息列表")
    public Result<List<TopCommentVo>> queryWithCursor(
            @RequestParam(required = false) String keyWord,
            @RequestParam(defaultValue = "0") String lastDate,
            @RequestParam(defaultValue = "10") int pageSize) {
        if(lastDate.equals("0")){
            lastDate = null;
        }
        // 开启分页
        PageHelper.startCursor("c.create_time",lastDate,pageSize,false);
        // 执行查询
        List<TopCommentVo> list = commentService.query(keyWord);
        // 获取分页信息
        PageInfo<TopCommentVo> pageInfo = new PageInfo<>(list);
        // 清理分页
        PageHelper.clearPage();
        // 使用自定义分页返回方法
        return Result.page(list, pageInfo.getTotal());
    }
```

# 📁 文档导航

本改造方案包含以下文档：

1. **[QUICK_REFERENCE.md](./QUICK_REFERENCE.md)** ⭐ **推荐先看**

    - 架构对比图
    - 需要修改的文件清单
    - 核心代码片段
    - 使用对比示例
    - 性能提升数据
    - 修改步骤建议

2. **[CURSOR_PAGINATION_GUIDE.md](./CURSOR_PAGINATION_GUIDE.md)**

    - 完整的改造方案说明
    - 为什么需要 Cursor 分页
    - 详细的修改指南
    - 使用示例
    - 注意事项
    - 性能对比

3. **[CURSOR_IMPLEMENTATION_EXAMPLES.md](./CURSOR_IMPLEMENTATION_EXAMPLES.md)**
    - 所有文件的详细代码示例
    - 逐行注释的实现代码
    - 完整的测试用例
    - 最佳实践建议

## 🔑 核心修改文件

### 已经修改

| 文件                | 修改内容                | 重要性     |
| ------------------- | ----------------------- | ---------- |
| `Constant.java`     | 添加 2 个游标参数常量   | ⭐⭐⭐     |
| `Page.java`         | 添加游标字段和方法      | ⭐⭐⭐⭐⭐ |
| `PageMethod.java`   | 添加 Cursor 分页 API    | ⭐⭐⭐⭐   |
| `MySqlDialect.java` | 实现 MySQL 游标分页 SQL | ⭐⭐⭐⭐⭐ |

### 未来可能的支持项

| 文件                     | 说明            |
| ------------------------ | --------------- |
| `PostgreSqlDialect.java` | PostgreSQL 支持 |
| `OracleDialect.java`     | Oracle 支持     |
| `SqlServerDialect.java`  | SQL Server 支持 |
| 其他 Dialect...          | 按需修改        |

## 📚 进一步阅读

- [MySQL LIMIT 优化](https://dev.mysql.com/doc/refman/8.0/en/limit-optimization.html)
- [PostgreSQL OFFSET 性能问题](https://www.postgresql.org/docs/current/queries-limit.html)
- [Seek Method 分页](https://use-the-index-luke.com/no-offset)

## 🤝 贡献

如果您对改造方案有任何建议或发现问题，欢迎：

- 提出 Issue
- 提交 Pull Request
- 分享使用经验

## 📄 许可

本改造方案遵循 PageHelper 的 MIT 许可证。

---

## 🎉 开始改造

1. **阅读** [QUICK_REFERENCE.md](./QUICK_REFERENCE.md) 了解整体方案
2. **参考** [CURSOR_IMPLEMENTATION_EXAMPLES.md](./CURSOR_IMPLEMENTATION_EXAMPLES.md) 获取详细代码
3. **执行** 按照步骤逐步修改
4. **测试** 验证功能和性能
5. **打包** 参考 [BUILD_AND_DEPLOY_GUIDE.md](./BUILD_AND_DEPLOY_GUIDE.md) 打包发布
6. **部署** 享受高性能分页！

## 📦 打包与发布

修改完成后，查看 **[BUILD_AND_DEPLOY_GUIDE.md](./BUILD_AND_DEPLOY_GUIDE.md)** 了解如何打包使用。

### 快速打包（推荐）

**Windows 用户**：

```bash
# 双击运行
build-local.bat

# 或命令行运行
.\build-local.bat
```

**Linux/Mac 用户**：

```bash
# 添加执行权限
chmod +x build-local.sh

# 运行
./build-local.sh
```

### 手动打包

```bash
# 1. 修改版本号（pom.xml）
<version>6.1.1-cursor-1.0.0</version>

# 2. 编译打包
mvn clean package -DskipTests

# 3. 安装到本地
mvn clean install -DskipTests

# 4. 在项目中使用
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper</artifactId>
    <version>6.1.1-cursor-1.0.0</version>
</dependency>
```

### 更多选项

- 📦 **本地使用** - 5 分钟快速打包（推荐）
- 🏢 **私有仓库** - 团队内部共享
- 🌍 **Maven 中央仓库** - 开源分享
- 🤝 **向官方贡献** - 提交 Pull Request

详见：[BUILD_AND_DEPLOY_GUIDE.md](./BUILD_AND_DEPLOY_GUIDE.md)

---

Good luck! 🚀


