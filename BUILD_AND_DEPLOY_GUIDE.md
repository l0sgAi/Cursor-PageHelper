# 打包与发布指南 - PageHelper Cursor 分页版

## 📦 打包方式概览

修改完成后，有以下几种使用方式：

1. **本地打包安装** - 用于本地项目（最简单）⭐ 推荐
2. **发布到私有 Maven 仓库** - 用于团队内部使用
3. **发布到 Maven 中央仓库** - 用于开源分享

---

## 🚀 方式一：本地打包安装（推荐）

### 适用场景

- 自己或团队内部使用
- 快速测试验证
- 不需要公开发布

### 步骤 1：修改版本号

编辑 `pom.xml`，修改版本号以区分原版：

```xml
<groupId>com.github.pagehelper</groupId>
<artifactId>pagehelper</artifactId>
<version>6.1.1-cursor-SNAPSHOT</version>  <!-- 改成自定义版本 -->
<packaging>jar</packaging>

<name>pagehelper 6 - Cursor Pagination</name>
<description>Mybatis Pagination Plugin with Cursor Support</description>
```

**版本号建议**：

- `6.1.1-cursor-SNAPSHOT` - 开发版本
- `6.1.1-cursor-1.0.0` - 正式版本
- `6.1.1-cursor-beta` - 测试版本

### 步骤 2：编译打包

```bash
# 进入项目目录
cd D:\JavaPjs\cursorPH

# 清理旧的编译文件
mvn clean

# 编译并打包（跳过测试，加快速度）
mvn package -DskipTests

# 如果需要运行测试
mvn package
```

**打包结果**：

```
target/pagehelper-6.1.1-cursor-SNAPSHOT.jar
```

### 步骤 3：安装到本地 Maven 仓库

```bash
# 安装到本地 ~/.m2/repository
mvn clean install -DskipTests
```

**安装位置**（Windows）：

```
C:\Users\你的用户名\.m2\repository\com\github\pagehelper\pagehelper\6.1.1-cursor-SNAPSHOT\
```

### 步骤 4：在项目中使用

在你的项目 `pom.xml` 中引入：

```xml
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper</artifactId>
    <version>6.1.1-cursor-SNAPSHOT</version>
</dependency>
```

**完成！** 现在可以在项目中使用 Cursor 分页功能了。

---

## 🏢 方式二：发布到私有 Maven 仓库

### 适用场景

- 团队内部共享
- 统一版本管理
- 需要多个项目使用

### 前置要求

- 已搭建私有 Maven 仓库（如 Nexus、Artifactory）
- 拥有仓库的上传权限

### 步骤 1：配置仓库信息

编辑 `pom.xml`，添加或修改 `<distributionManagement>` 部分：

```xml
<distributionManagement>
    <repository>
        <id>company-releases</id>
        <name>Company Release Repository</name>
        <url>http://nexus.yourcompany.com/repository/maven-releases/</url>
    </repository>
    <snapshotRepository>
        <id>company-snapshots</id>
        <name>Company Snapshot Repository</name>
        <url>http://nexus.yourcompany.com/repository/maven-snapshots/</url>
    </snapshotRepository>
</distributionManagement>
```

### 步骤 2：配置认证信息

编辑 Maven 的 `settings.xml` 文件（位于 `~/.m2/settings.xml` 或 `Maven安装目录/conf/settings.xml`）：

```xml
<settings>
    <servers>
        <server>
            <id>company-releases</id>
            <username>your-username</username>
            <password>your-password</password>
        </server>
        <server>
            <id>company-snapshots</id>
            <username>your-username</username>
            <password>your-password</password>
        </server>
    </servers>
</settings>
```

**注意**：`<server>` 的 `id` 必须与 `pom.xml` 中的 `<repository>` 的 `id` 一致。

### 步骤 3：部署到私有仓库

```bash
# 部署 SNAPSHOT 版本
mvn clean deploy -DskipTests

# 部署 Release 版本（确保 pom.xml 中版本号不含 -SNAPSHOT）
mvn clean deploy -DskipTests
```

### 步骤 4：团队成员使用

**1. 配置私有仓库地址**（在项目 `pom.xml` 或 `settings.xml` 中）：

```xml
<repositories>
    <repository>
        <id>company-releases</id>
        <url>http://nexus.yourcompany.com/repository/maven-releases/</url>
        <releases>
            <enabled>true</enabled>
        </releases>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
    </repository>
    <repository>
        <id>company-snapshots</id>
        <url>http://nexus.yourcompany.com/repository/maven-snapshots/</url>
        <releases>
            <enabled>false</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```

**2. 添加依赖**：

```xml
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper</artifactId>
    <version>6.1.1-cursor-SNAPSHOT</version>
</dependency>
```

---

## 🌍 方式三：发布到 Maven 中央仓库

### 适用场景

- 开源项目
- 希望全球开发者都能使用
- 贡献社区

### 注意事项

⚠️ **建议做法**：

1. 不要直接发布到原 `com.github.pagehelper` groupId（会冲突）
2. 使用自己的 groupId，如 `io.github.yourname.pagehelper`
3. 或者向 PageHelper 官方提交 Pull Request

### 前置要求

1. **注册 Sonatype 账号**

   - 访问：https://issues.sonatype.org
   - 注册账号并创建 Issue 申请 groupId

2. **拥有域名**（用于验证 groupId）

   - 如果使用 `io.github.yourname`，需要验证 GitHub 账号
   - 如果使用自己的域名 `com.yourdomain`，需要证明域名所有权

3. **生成 GPG 密钥**（用于签名）

### 步骤 1：生成 GPG 密钥

```bash
# 安装 GPG（Windows 用户安装 Gpg4win）
# 下载地址：https://www.gpg4win.org/

# 生成密钥对
gpg --gen-key
# 按提示输入姓名、邮箱等信息

# 查看密钥
gpg --list-keys

# 发布公钥到服务器
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
```

### 步骤 2：修改 pom.xml

```xml
<project>
    <!-- 修改 groupId 为你的 -->
    <groupId>io.github.yourname</groupId>
    <artifactId>pagehelper-cursor</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <name>PageHelper with Cursor Pagination</name>
    <description>MyBatis pagination plugin with high-performance cursor pagination support</description>
    <url>https://github.com/yourname/pagehelper-cursor</url>

    <licenses>
        <license>
            <name>MIT License</name>
            <url>https://opensource.org/licenses/MIT</url>
        </license>
    </licenses>

    <developers>
        <developer>
            <id>yourname</id>
            <name>Your Name</name>
            <email>your.email@example.com</email>
        </developer>
    </developers>

    <scm>
        <connection>scm:git:git://github.com/yourname/pagehelper-cursor.git</connection>
        <developerConnection>scm:git:ssh://github.com:yourname/pagehelper-cursor.git</developerConnection>
        <url>https://github.com/yourname/pagehelper-cursor/tree/master</url>
    </scm>

    <distributionManagement>
        <snapshotRepository>
            <id>ossrh</id>
            <url>https://s01.oss.sonatype.org/content/repositories/snapshots</url>
        </snapshotRepository>
        <repository>
            <id>ossrh</id>
            <url>https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/</url>
        </repository>
    </distributionManagement>

    <build>
        <plugins>
            <!-- Source -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-source-plugin</artifactId>
                <version>3.2.1</version>
                <executions>
                    <execution>
                        <id>attach-sources</id>
                        <goals>
                            <goal>jar-no-fork</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

            <!-- Javadoc -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-javadoc-plugin</artifactId>
                <version>3.4.1</version>
                <configuration>
                    <encoding>UTF-8</encoding>
                    <charset>UTF-8</charset>
                    <doclint>none</doclint>
                </configuration>
                <executions>
                    <execution>
                        <id>attach-javadocs</id>
                        <goals>
                            <goal>jar</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

            <!-- GPG 签名 -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-gpg-plugin</artifactId>
                <version>3.0.1</version>
                <executions>
                    <execution>
                        <id>sign-artifacts</id>
                        <phase>verify</phase>
                        <goals>
                            <goal>sign</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

            <!-- Nexus Staging -->
            <plugin>
                <groupId>org.sonatype.plugins</groupId>
                <artifactId>nexus-staging-maven-plugin</artifactId>
                <version>1.6.13</version>
                <extensions>true</extensions>
                <configuration>
                    <serverId>ossrh</serverId>
                    <nexusUrl>https://s01.oss.sonatype.org/</nexusUrl>
                    <autoReleaseAfterClose>true</autoReleaseAfterClose>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 步骤 3：配置 settings.xml

编辑 `~/.m2/settings.xml`：

```xml
<settings>
    <servers>
        <server>
            <id>ossrh</id>
            <username>your-sonatype-username</username>
            <password>your-sonatype-password</password>
        </server>
    </servers>

    <profiles>
        <profile>
            <id>ossrh</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <gpg.executable>gpg</gpg.executable>
                <gpg.passphrase>your-gpg-passphrase</gpg.passphrase>
            </properties>
        </profile>
    </profiles>
</settings>
```

### 步骤 4：部署到中央仓库

```bash
# 部署 SNAPSHOT 版本（测试用）
mvn clean deploy

# 部署 Release 版本
mvn clean deploy -P release

# 或者使用
mvn clean deploy -Dgpg.passphrase=your-gpg-passphrase
```

### 步骤 5：发布构件

1. 登录 Sonatype Nexus：https://s01.oss.sonatype.org/
2. 进入 "Staging Repositories"
3. 找到你的构件，选择 "Close"
4. 等待验证通过后，选择 "Release"
5. 2-4 小时后同步到 Maven 中央仓库

### 步骤 6：全球开发者使用

发布成功后，任何人都可以这样使用：

```xml
<dependency>
    <groupId>io.github.yourname</groupId>
    <artifactId>pagehelper-cursor</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## 🔄 方式四：向官方提交贡献（推荐开源做法）

### 最佳实践

如果你的修改非常有价值，建议：

1. **Fork 官方仓库**

   ```bash
   # 访问 https://github.com/pagehelper/Mybatis-PageHelper
   # 点击 Fork 按钮
   ```

2. **创建功能分支**

   ```bash
   git checkout -b feature/cursor-pagination
   ```

3. **提交你的修改**

   ```bash
   git add .
   git commit -m "feat: add cursor pagination support for deep paging optimization"
   git push origin feature/cursor-pagination
   ```

4. **创建 Pull Request**

   - 访问你的 Fork 仓库
   - 点击 "New Pull Request"
   - 详细描述你的修改和优势
   - 附上性能测试数据

5. **等待官方审核**
   - 官方通过后，会发布新版本
   - 你的贡献将被全球开发者使用！

---

## 📋 完整打包流程示例

### 本地使用完整流程

```bash
# 1. 进入项目目录
cd D:\JavaPjs\cursorPH

# 2. 修改版本号
# 编辑 pom.xml，改为 <version>6.1.1-cursor-1.0.0</version>

# 3. 编译测试
mvn clean compile
mvn test

# 4. 打包
mvn clean package -DskipTests

# 5. 安装到本地仓库
mvn clean install -DskipTests

# 6. 在其他项目中使用
# 添加依赖：
# <dependency>
#     <groupId>com.github.pagehelper</groupId>
#     <artifactId>pagehelper</artifactId>
#     <version>6.1.1-cursor-1.0.0</version>
# </dependency>
```

### 验证安装

```bash
# 查看本地仓库是否安装成功
ls ~/.m2/repository/com/github/pagehelper/pagehelper/6.1.1-cursor-1.0.0/

# Windows 查看
dir C:\Users\你的用户名\.m2\repository\com\github\pagehelper\pagehelper\6.1.1-cursor-1.0.0\
```

---

## 🧪 测试打包结果

创建一个测试项目验证：

### 1. 创建测试项目

```xml
<!-- test-project/pom.xml -->
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>pagehelper-cursor-test</artifactId>
    <version>1.0.0</version>

    <dependencies>
        <!-- 使用你打包的版本 -->
        <dependency>
            <groupId>com.github.pagehelper</groupId>
            <artifactId>pagehelper</artifactId>
            <version>6.1.1-cursor-1.0.0</version>
        </dependency>

        <dependency>
            <groupId>org.mybatis</groupId>
            <artifactId>mybatis</artifactId>
            <version>3.5.13</version>
        </dependency>
    </dependencies>
</project>
```

### 2. 测试代码

```java
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;

public class CursorPaginationTest {
    public static void main(String[] args) {
        // 测试 Cursor 分页 API 是否存在
        Page<Object> page = PageHelper.startCursor("id", 100L, 10);

        System.out.println("Cursor 分页支持已启用！");
        System.out.println("Use Cursor: " + page.useCursor());
        System.out.println("Cursor Column: " + page.getCursorColumn());
        System.out.println("Cursor Value: " + page.getCursorValue());
    }
}
```

### 3. 运行测试

```bash
mvn clean compile exec:java -Dexec.mainClass="CursorPaginationTest"
```

---

## 📊 版本管理建议

### 语义化版本

```
主版本号.次版本号.修订号-标识符

例如：
6.1.1-cursor-1.0.0
│   │   │     │ │ │
│   │   │     │ │ └─ 修订号（bug修复）
│   │   │     │ └─── 次版本号（新功能）
│   │   │     └───── 主版本号
│   │   └─────────── 原版本修订号
│   └─────────────── 原版本次版本号
└─────────────────── 原版本主版本号
```

### 版本号示例

- `6.1.1-cursor-SNAPSHOT` - 开发中
- `6.1.1-cursor-1.0.0-beta` - 测试版
- `6.1.1-cursor-1.0.0-RC1` - 候选版本
- `6.1.1-cursor-1.0.0` - 正式版
- `6.1.1-cursor-1.0.1` - bug 修复版
- `6.1.1-cursor-1.1.0` - 新功能版
- `6.1.1-cursor-2.0.0` - 重大更新版

---

## 🎉 总结

| 方式         | 适用场景        | 难度       | 时间    |
| ------------ | --------------- | ---------- | ------- |
| **本地安装** | 个人/小团队使用 | ⭐         | 5 分钟  |
| **私有仓库** | 企业团队共享    | ⭐⭐       | 30 分钟 |
| **中央仓库** | 开源分享        | ⭐⭐⭐⭐⭐ | 数天    |
| **提交 PR**  | 贡献社区        | ⭐⭐⭐     | 看审核  |

**推荐做法**：

1. 🥇 先本地安装测试
2. 🥈 团队使用则发布到私有仓库
3. 🥉 成熟后再考虑中央仓库或官方贡献

Good luck! 🚀
