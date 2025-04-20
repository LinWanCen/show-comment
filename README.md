# 智能注释插件 Show Comment Plugin IDEA

[![Version](https://img.shields.io/jetbrains/plugin/v/io.github.linwancen.show-comment.svg)](https://plugins.jetbrains.com/plugin/18553-show-comment/versions)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/io.github.linwancen.show-comment.svg)](https://plugins.jetbrains.com/plugin/18553-show-comment)

Thanks to JetBrains Licenses for Open Source.

## Notes 说明

Show doc comment in the Project view Tree, line End, json, other

在文件树、行末、JSON 等地方显示注释.

<ul>
  <li>Java, Kotlin, Groovy, Scala
  <li>C/C++/OC, Python, Go, Rust, Ruby
  <li>HTML(Vue), JS/TS, PHP, SQL
  <li>YAML/yml
</ul>

<h4>English Note</h4>
<ul>
<li>tree doc from lang or README.md pom .gradle etc. by settings
<li>input `doc` `docc` -> /** */
<li>json doc from xxx.json.tsv
<li>json dict doc from (key).tsv
<li>"xx-ClassNameOrSimpleName.json" and jump to field
<li>from External Conf for folder, resources, COBOL etc.
<li>Config: settings -> Tools -> // Show Comment App/Global/Project
</ul>

<h5>External Comment</h5>
<a href="https://github.com/LinWanCen/show-comment/tree/main/src/test/java/io/github/linwancen/plugin/show/demo/ext">Demo(GitHub)</a>
<ul>
<li>Reload: Tools -> 🔄 // Reload External Comment
<li>path/[any][filename.]ext.tree.tsv // file and folder tree comment 📝 📁
<li>path/[any][filename.]ext.key.regexp  // line keywords to split and comment
<li>path/[any][filename.]ext.doc.tsv  // line words comment
<li>In path, "doc" can replace any, and can use % like in SQL, effect at previous layer when folder named -1
<li>The lines in key.regexp will be concatenated with `|` to regexp; longer str should in front; startWith `?` to exclude
<li>Chang tsv file in find pop window would not reload!
<li>The tsv conf file must could be search in "Go to File" (Ctrl + Shift + N)
</ul>

<h5>My Project</h5>
<ul>
<li>Show doc comment at the Project view Tree, line End, json etc.:
<a href="https://plugins.jetbrains.com/plugin/18553-show-comment">Show Comment</a>
</li>
<li>show line count for file / method, show children count for dir in project view (tree on left):
<a href="https://plugins.jetbrains.com/plugin/23300-line-num">Line Num</a>
</li>
<li>Method call usage graph and maven dependency graph:
<a href="https://plugins.jetbrains.com/plugin/21242-draw-graph">Draw Graph</a>
</li>
<li>Find author/comment of multiple files or lines and export Find:
<a href="https://plugins.jetbrains.com/plugin/20557-find-author">Find Author</a>
<li>Auto sync coverage and capture coverage during debug:
<a href="https://plugins.jetbrains.com/plugin/20780-sync-coverage">Sync Coverage</a>
</ul>

<hr>
<h4>中文说明</h4>
<ul>
<li>在结构树显示 文件注释 或 项目说明 (README.md pom.xml 等，可设置)
<li>在行末尾显示 引用对象的文档注释，欢迎反馈您想要支持的语言，欢迎 C# 大神研究 CsLineEnd.java
<li>输入 doc / docc 等生成 /** */
<li>json 字段注释从 xxx.json.tsv 读取
<li>json 字典注释从 键名.tsv 读取
<li>支持 "xx-类全名或简名.json" 读取字段注释和跳转
<li>支持 从配置文件获取外部注释用于文件夹、资源、COBOL 等
<li>修改配置：设置 -> 工具 -> // Show Comment App/Global/Project
</ul>

<h5>外部注释</h5>
<a href="https://gitee.com/LinWanCen/show-comment/tree/main/src/test/java/io/github/linwancen/plugin/show/demo/ext">示例(Gitee)</a>
：比如你要给 .go 的文件配置文件注释可以放在相同目录或父目录的 xxx.go.tree.tsv 中
<ul>
<li>重新加载：工具 -> "🔄 // Reload External Comment"
<li>path/[any][filename.]ext.tree.tsv // 文件(夹)注释 📝 📁
<li>path/[any][filename.]ext.key.regexp  // 切割关键字与注释
<li>path/[any][filename.]ext.doc.tsv  // 词注释
<li>key.regexp 的每一行将会用`|`连接起来形成正则表达式，较长的关键字应该放在前面，用 `?` 开头排除
<li>doc 文件夹可以替换任何一层文件夹，可以像 SQL 那样用 % 模糊匹配，文件夹名为 -1 时在上一层文件夹生效
<li>在搜索弹出窗中修改 tsv 文件将不会被重加载
<li>tsv 配置文件必须能被搜索(Ctrl + Shift + N)
</ul>

<h5>我的项目</h5>
<ul>
<li>在文件树、行末、JSON 显示注释:
<a href="https://plugins.jetbrains.com/plugin/18553-show-comment">Show Comment</a>
</li>
<li>在文件树显示行数、文件数:
<a href="https://plugins.jetbrains.com/plugin/23300-line-num">Line Num</a>
</li>
<li>生成 方法调用图 和 Maven 依赖图:
<a href="https://plugins.jetbrains.com/plugin/21242-draw-graph">Draw Graph</a>
</li>
<li>查找多个文件或行的作者 与 导出搜索：
<a href="https://plugins.jetbrains.com/plugin/20557-find-author">Find Author</a>
<li>自动同步覆盖率 和 调试中抓取覆盖率：
<a href="https://plugins.jetbrains.com/plugin/20780-sync-coverage">Sync Coverage</a>
</ul>

<hr>
<h4>反馈问题和需求</h4>
<ul>
<a href="https://github.com/LinWanCen/show-comment/issue">GitHub issues</a>
<li>微信 LinWanCen
<li>邮箱 1498425439@qq.com
</ul>

<hr>
<h4>支持</h4>
<ul>
<li>如果对你有所帮助，可以通过群或文章等形式分享给大家，在插件市场好评，或者给本项目
<a href="https://github.com/LinWanCen/show-comment">GitHub</a>
主页一个 Star，您的支持是项目前进的动力。
</ul>
<hr>


## Change Notes 更新说明

[build.gradle](build.gradle)

### Demo 示例

See in IDEA with this plugin | 安装插件后用 IDEA 查看

- [Java Doc Comment Demo | Java 文档注释](src/test/java/io/github/linwancen/plugin/show/demo/java/Call.java)
- [JSON Doc Comment Demo | JSON 文档注释](src/test/java/io/github/linwancen/plugin/show/demo/json/base-Pojo.json)
- [External Comment Demo For COBOL | 外部注释 Demo](src/test/java/io/github/linwancen/plugin/show/demo/ext/cobol/demo/BASE.cbl)  
- [COBOL Highlighting | COBOL 高亮配置](src/test/java/io/github/linwancen/plugin/show/demo/ext/cobol/cobol/COBOL_IDEA.md)


#### Maven down source jar 自动下载带注释的源码
```xml
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-dependency-plugin</artifactId>
        <version>3.1.2</version>
        <executions>
          <execution>
            <id>down_source_jar</id>
            <phase>validate</phase>
            <goals>
              <goal>sources</goal>
            </goals>
            <configuration>
              <includeArtifactIds>
                <!-- 公司统一出入参基类 -->
                com.company.common.base,
                <!-- 公司统一错误码 -->
                com.company.common.errcode
              </includeArtifactIds>
            </configuration>
          </execution>
        </executions>
      </plugin>
```
