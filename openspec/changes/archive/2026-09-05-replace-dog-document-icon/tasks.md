# 实现任务：.dog 文档图标更换为独立品牌图标

## 1. 资源准备

- [x] 1.1 `src/main/resources/file.png` 更名为 `dog-document.png`（保留 512×512 原图不裁剪不重绘）。验证：`file dog-document.png` 为 512×512 RGBA；`git status` 不再出现 file.png。

## 2. 打包产线改造

- [x] 2.1 `build-mac.sh` 文档图标段落：删除 `cp app.icns → dog-document.icns`，新增独立生成——PIL 预处理（内容缩放至 1024 画布 82% 内框居中、不套圆角遮罩，PIL 缺失降级原图）+ sips 十档 + iconutil 输出 `dog-document.icns` 拷入 bundle Resources。验证：`bash -n` 通过；产物 icns 512 档内容纵向占比 81.9%。
- [x] 2.2 防回归断言：文档图标声明断言后追加 `cmp -s` 应用 icns 与 dog-document.icns，相同则打包中止并输出独立错误信息。验证：`bash -n` 通过；`./build-mac.sh` 跑通且断言通过（"文档图标独立生成: 与应用图标不同源"）。

## 3. 文档与验收

- [x] 3.1 README 图标缓存刷新说明补充 .dog 文档图标更换场景（killall Finder / iconservices 缓存刷新步骤）。验证：README 相应段落提及文档图标。
- [x] 3.2 产物验收：重打包后安装，新建 .dog 文件在 Finder 显示新品牌图标（竖版图形、与应用 Dock 图标不同源）；应用 Dock 图标、文件关联双击唤起、加解密行为均不变。验证：与 spec 场景「新 DMG 产物含图标声明」「已安装环境下 Finder 展示文档图标」「图标声明不改变文件格式」一致（用户确认 Finder 新图标已生效）。
- [x] 3.3 jar 形态与终端模式回归：jar 运行不声明文档图标、终端模式行为不变（本变更零 Java 代码改动，全量测试随构建通过，jar 仅多一个资源文件）。
