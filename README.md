# Show Comment Plugin
IDEA 智能注释插件

https://plugins.jetbrains.com/plugin/18553-show-comment



## Notes 说明

<h2>English Notes:</h2>
<ul>
<li>Show javadoc comments at the Project view Tree structure
<li>Show javadoc comments at the end-of-line
<li>Show javadoc comments at "xx ClassNameOrSimpleName.json" and jump to field
<li>Show comments from External Conf for folder, resources, COBOL etc.
<li>Config: settings -> Tools -> // Show Comment Global/Project
</ul>

<h3>External Comment:</h3>
<ul>
<li>Reload: Tools -> 🔄 // Reload External Comment
<li>path/[any][filename.]ext.tree.tsv // file and folder tree comment 📝 📁
<li>path/[any][filename.]ext.key.tsv  // line keywords to split and comment
<li>path/[any][filename.]ext.doc.tsv  // line words comment
<li>In path, "doc" can replace any, and can use % like in SQL
<li>The lines in key.tsv will be concatenated with `|` to regexp; longer str should in front; startWith `?` to exclude
<li>Chang tsv file in find pop window would not reload!
<li>The tsv conf file must could be search in "Go to File"(Ctrl + Shift + N)
</ul>


<h2>中文说明:</h2>
<ul>
<li>在结构树显示 文档注释
<li>在行末尾显示 文档注释
<li>支持 "xx 类全名或简名.json" 文档注释与跳转到字段
<li>支持 从配置文件获取外部注释用于文件夹、资源、COBOL 等
<li>修改配置：设置 -> 工具 -> // Show Comment Global/Project
</ul>

<h3>外部注释：</h3>
<ul>
<li>重新加载：工具 -> "🔄 // Reload External Comment"
<li>path/[any][filename.]ext.tree.tsv // 文件(夹)注释 📝 📁
<li>path/[any][filename.]ext.key.tsv  // 切割关键字与注释
<li>path/[any][filename.]ext.doc.tsv  // 词注释
<li>key.tsv 的每一行将会用`|`连接起来形成正则表达式，较长的关键字应该放在前面，用 `?` 开头排除
<li>doc 文件夹可以替换任何一层文件夹，可以像 SQL 那样用 % 模糊匹配
<li>在搜索弹出窗中修改 tsv 文件将不会被重加载
<li>tsv 配置文件必须能被搜索(Ctrl + Shift + N)
</ul>



## Change Notes 更新说明

<h2>English Change Notes:</h2>
<ul>
<li>1.15  Add line-end-comment  support COBOL ext '', 'cbl', 'cob', 'cobol'
<li>1.14  Add line-end-comment  skip Annotation, skip only English (ASCII)
<li>1.13  Add Copy With Line Comment & Add Line Comment
<li>1.12   ★  External Comment for COBOL etc
<li>1.11  Add json key jump to field
<li>1.10  Add project-view-tree-comment  for package from parent or other project
<li>1.9   Add project-view-tree-comment  for "xx ClassNameOrSimpleName.json" and SPI file
<li>1.8    ★  line-end-comment  for "xx ClassNameOrSimpleName.json"
<li>1.7   Add line-end-comment  setting for prefix and count
<li>1.6   Add line-end-comment  independent switch for call, new, ref
<li>1.5   Add line-end-comment  find next loop when none
<li>1.4   Add line-end-comment  find element right to left
<li>1.3    ★  project-view-tree-comment
<li>1.2   Add line-end-comment  settings fro class prefix filter
<li>1.1   Add line-end-comment  settings for text color
</ul>

<h2>中文更新说明:</h2>
<ul>
<li>1.15  增加 行末注释 COBOL 拓展名支持 无拓展名、cbl、cob、cobol
<li>1.14  增加 行末注释 忽略注解 与 忽略纯英文
<li>1.13  增加 带行末注释复制 和 添加行末注释
<li>1.12   ★  外部注释用于 COBOL 等
<li>1.11  增加 json 跳转到字段
<li>1.10  增加 在父包和其他项目的包中获取 项目导航栏注释
<li>1.9   增加 "xx 类全名或简名.json" 和 SPI 项目导航栏注释
<li>1.8    ★  "xx 类全名或简名.json" 行末注释
<li>1.7   增加 行末注释前缀和对象数设置
<li>1.6   增加 行末调用，new，引用注释独立开关
<li>1.5   增加 没有注释时循环查找下一个对象
<li>1.4   增加 从右往左查找行末注释对象
<li>1.3    ★  项目导航栏注释
<li>1.2   增加 行末注释类前缀配置
<li>1.1   增加 行末文本颜色配置
</ul>



### Demo 示例

See in IDEA with this plugin | 安装插件后用 IDEA 查看

- [Java Doc Comment Demo | Java 文档注释](src/test/java/io/github/linwancen/plugin/show/demo/java/Call.java)
- [JSON Doc Comment Demo | JSON 文档注释](src/test/java/io/github/linwancen/plugin/show/demo/json/base Pojo.json)
- [External Comment Demo For COBOL | 外部注释 Demo](src/test/java/io/github/linwancen/plugin/show/demo/ext/cobol/demo/BASE.cbl)  
  [COBOL Highlighting | COBOL 高亮配置](src/test/java/io/github/linwancen/plugin/show/demo/ext/cobol/COBOL_IDEA.md)