# Show Comment Plugin
IDEA 智能注释插件

[![Version](https://img.shields.io/jetbrains/plugin/v/io.github.linwancen.show-comment.svg)](https://plugins.jetbrains.com/plugin/18553-show-comment/versions)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/io.github.linwancen.show-comment.svg)](https://plugins.jetbrains.com/plugin/18553-show-comment)

Thanks JetBrains Licenses for Open Source.

## Notes 说明

Show doc comment at the Project view Tree, line End, json, other

在文件树、行末、JSON 等地方显示注释.

<h2>English Note</h2>
<ul>
<li>"xx-ClassNameOrSimpleName.json" and jump to field
<li>input `doc` `docc` -> /** */
<li>json doc from xxx.json.tsv
<li>json dict doc from (key).tsv
<li>from External Conf for folder, resources, COBOL etc.
<li>Config: settings -> Tools -> // Show Comment Global/Project
</ul>

<h3>External Comment</h3>
<a href="https://github.com/LinWanCen/show-comment/tree/main/src/test/java/io/github/linwancen/plugin/show/demo/ext">Demo(GitHub)</a>
<ul>
<li>Reload: Tools -> 🔄 // Reload External Comment
<li>path/[any][filename.]ext.tree.tsv // file and folder tree comment 📝 📁
<li>path/[any][filename.]ext.key.tsv  // line keywords to split and comment
<li>path/[any][filename.]ext.doc.tsv  // line words comment
<li>In path, "doc" can replace any, and can use % like in SQL, effect at previous layer when folder named -1
<li>The lines in key.tsv will be concatenated with `|` to regexp; longer str should in front; startWith `?` to exclude
<li>Chang tsv file in find pop window would not reload!
<li>The tsv conf file must could be search in "Go to File"(Ctrl + Shift + N)
</ul>

<h3>My Project</h3>
<ul>
<li>Show doc comment at the Project view Tree, line End, json etc.:
<a href="https://plugins.jetbrains.com/plugin/18553-show-comment">Show Comment</a>
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
<h2>中文说明</h2>
<ul>
<li>在结构树显示 文档注释
<li>在行末尾显示 文档注释
<li>输入 doc / docc 等生成 /** */
<li>json 字段注释从 xxx.json.tsv 读取
<li>json 字典注释从 键名.tsv 读取
<li>支持 "xx-类全名或简名.json" 文档注释与跳转到字段
<li>支持 从配置文件获取外部注释用于文件夹、资源、COBOL 等
<li>修改配置：设置 -> 工具 -> // Show Comment Global/Project
</ul>

<h3>外部注释</h3>
<a href="https://gitee.com/LinWanCen/show-comment/tree/main/src/test/java/io/github/linwancen/plugin/show/demo/ext">示例(Gitee)</a>
：比如你要给 .go 的文件配置文件注释可以放在相同目录或父目录的 xxx.go.tree.tsv 中
<ul>
<li>重新加载：工具 -> "🔄 // Reload External Comment"
<li>path/[any][filename.]ext.tree.tsv // 文件(夹)注释 📝 📁
<li>path/[any][filename.]ext.key.tsv  // 切割关键字与注释
<li>path/[any][filename.]ext.doc.tsv  // 词注释
<li>key.tsv 的每一行将会用`|`连接起来形成正则表达式，较长的关键字应该放在前面，用 `?` 开头排除
<li>doc 文件夹可以替换任何一层文件夹，可以像 SQL 那样用 % 模糊匹配，文件夹名为 -1 时在上一层文件夹生效
<li>在搜索弹出窗中修改 tsv 文件将不会被重加载
<li>tsv 配置文件必须能被搜索(Ctrl + Shift + N)
</ul>

<h3>我的项目</h3>
<ul>
<li>在文件树、行末、JSON 显示注释:
<a href="https://plugins.jetbrains.com/plugin/18553-show-comment">Show Comment</a>
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
<h2>找个女朋友</h2>
<ul>
<li>我的情况：1993-11-03，软件工程师，广发银行编制，宅，LOLm，NS，看书，写文章，KTV，爬山，逛街
<li>我的性格：ISFP(探险家 内向 实际 感性 随性)
<li>希望对方：广东省，与家人关系好，爱笑甜美大眼睛
<li>有兴趣请联系 1498425439@qq.com
</ul>

<hr>
<h2>支持</h2>
<ul>
<li>如果对你有所帮助，别忘了给本项目
<a href="https://github.com/LinWanCen/show-comment">GitHub</a>
主页一个 Star，您的支持是项目前进的动力。
</ul>
<hr>


## Change Notes 更新说明

<h2>English Change Notes:</h2>
<ul>
<li>2.09  Add line-end-comment  support Python doc strings
<li>2.08  Add                   i18n and chinese
<li>2.07  Add global-setting    reset default
<li>2.06  Add project-view-tree xx-abc.xxx from Abc.java doc
<li>2.05  Add line-end-comment  json doc from xxx.json.tsv
<li>2.04  Add line-end-comment  json dict doc from (key).tsv
<li>2.03  Add live-templates    input `doc` `docc` -> /** */
<li>2.02  Add line-end-comment  show before doc for `isA(xxx)` and `a.set(b.get)`
<li>2.01  Add line-end-comment  support SQL, JavaScript, Python, Golang, Kotlin
<li>2.00  ★★                   support all JetBrains IDE
<li>1.24  Add PopupMenu         Copy FileName:LineNumber
<li>1.23  Add project-view-tree setting for show when compact middle packages
<li>1.22  Add PopupMenu         Copy ClassName.MethodName
<li>1.21  Add line-end-comment  default skip only English when system lang is not `en`
<li>1.20  Add line-end-comment  get doc first sentence checkbox
<li>1.19  Add line-end-comment  supper doc at @Override, support doc tag like @author
<li>1.18  Add External Comment  effect at previous layer when folder named -1
<li>1.17  Add line-end-comment  skip doc text or class/member name by regexp
<li>1.16  Add line-end-comment  skip when comments have been added
<li>1.15  Add line-end-comment  support COBOL ext '', 'cbl', 'cob', 'cobol'
<li>1.14  Add line-end-comment  skip Annotation, skip only English (ASCII)
<li>1.13  Add line-end-comment  Copy With Line Comment & Add Line Comment
<li>1.12  ★   External Comment  for COBOL etc
<li>1.11  Add                   json key jump to field
<li>1.10  Add project-view-tree for package from parent or other project
<li>1.9   Add project-view-tree for "xx-ClassNameOrSimpleName.json" and SPI file
<li>1.8   ★   line-end-comment  for "xx-ClassNameOrSimpleName.json"
<li>1.7   Add line-end-comment  setting for prefix and count
<li>1.6   Add line-end-comment  independent switch for call, new, ref
<li>1.5   Add line-end-comment  find next loop when none
<li>1.4   Add line-end-comment  find element right to left
<li>1.3   ★   project-view-tree file and member comment
<li>1.2   Add line-end-comment  settings for class prefix filter
<li>1.1   Add line-end-comment  settings for text color
</ul>

<h2>中文更新说明:</h2>
<ul>
<li>2.09  增加 行末注释   支持 Python 文档字符串
<li>2.08  增加           多语言与中文支持
<li>2.07  增加 全局设置   复位默认值
<li>2.06  增加 文件树注释  xx-abc.xxx 来自 Abc.java 的文档注释
<li>2.05  增加 行末注释   json 字段注释从 xxx.json.tsv 读取
<li>2.04  增加 行末注释   json 字典注释从 键名.tsv 读取
<li>2.03  增加 活动模板   输入 doc / docc 等生成 /** */
<li>2.02  增加 行末注释   `isA(xxx)` 和 `a.set(b.get)` 显示前一个注释
<li>2.01  增加 行末注释   支持 SQL, JavaScript, Python, Golang, Kotlin
<li>2.00  ★★           支持所有 JetBrains 软件
<li>1.24  增加 右键菜单   复制 文件名:行号
<li>1.23  增加 文件树注释 折叠中间包时显示中间包注释设置
<li>1.22  增加 右键菜单   复制 类名.方法名
<li>1.21  增加 行末注释   系统语言非英文时 默认 忽略纯英文
<li>1.20  增加 行末注释   获取第一句注释选项
<li>1.19  增加 行末注释   @Override 显示父方法注释，支持 @author 等注释标签
<li>1.18  增加 tsv 注释  文件夹名为 -1 时配置在上一层文件夹生效
<li>1.17  增加 行末注释   根据正则表达式跳过指定注释文本或类成员名字的注释
<li>1.16  增加 行末注释   已经添加行末注释时跳过
<li>1.15  增加 行末注释   COBOL 拓展名支持 无拓展名、cbl、cob、cobol
<li>1.14  增加 行末注释   忽略注解 与 忽略纯英文
<li>1.13  增加 行末注释   带注释复制 和 添加注释
<li>1.12  ★   tsv 注释  用于 COBOL 等
<li>1.11  增加           json 跳转到字段
<li>1.10  增加 文件树注释 在父包和其他项目的包中获取
<li>1.9   增加 文件树注释 "xx-类全名或简名.json" 和 SPI
<li>1.8   ★   行末注释   "xx-类全名或简名.json"
<li>1.7   增加 行末注释   前缀和对象数设置
<li>1.6   增加 行末调用   new，引用注释独立开关
<li>1.5   增加 行末注释   没有注释时循环查找下一个对象
<li>1.4   增加 行末注释   从右往左查找行末注释对象
<li>1.3   ★   文件树注释 (左侧项目导航文件树)
<li>1.2   增加 行末注释   类前缀配置
<li>1.1   增加 行末注释   颜色配置
</ul>



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
