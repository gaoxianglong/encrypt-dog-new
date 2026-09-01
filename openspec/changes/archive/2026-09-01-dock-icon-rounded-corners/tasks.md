# 实现任务：Dock 图标圆角化（打包与运行时两处）

## 1. Java 侧圆角渲染

- [x] 1.1 `LogoUtil` 新增 `loadRoundedImage(resource, size)`：读原图 → 等比缩放到 size → TYPE_INT_ARGB 透明画布 + RoundRectangle2D 裁剪（半径 = size × 22%）绘制，返回 `BufferedImage`；资源缺失返回 null。验证：`mvn compile` 通过。
- [x] 1.2 `EncryptDogGui.installDockIcon` 改用 `loadRoundedImage` 生成 128/256 双档装 `BaseMultiResolutionImage`。验证：`mvn compile` 通过。
- [x] 1.3 `Starter.preinstallDockIcon` 改为渲染圆角 512px 后 `ImageIO.write` 到临时文件（deleteOnExit 保留），失败静默降级。验证：`mvn compile` 通过；渲染出的临时 PNG 四角 alpha=0（sips 采样验证）。
- [x] 1.4 82% 内框修正（视觉验收反馈：满幅图标比其它应用大一圈）：`loadRoundedImage` 与 PIL 预处理同步改为内容收进 82% 内框居中、圆角半径按内框 22% 计算。验证：`mvn compile` 通过；渲染 PNG 边距区 alpha=0、内框边缘 alpha=255（采样验证）。

## 2. 打包期 PIL 预处理

- [x] 2.1 `build-mac.sh` icns 生成前插入 python3 预处理：PIL 缩放 dock_logo.png 至 1024 → `rounded_rectangle` 圆角遮罩（半径 22%）→ 输出临时 PNG 作为 sips/iconutil 的源图；无 PIL 时降级直用原图并提示。验证：`bash -n` 通过；`./build-mac.sh` 跑通。

## 3. 验收与回归

- [x] 3.1 jar 形态：`java -jar dog.jar --gui` 启动全程 Dock 图标圆角、无方形到圆角跳变、无咖啡杯闪现。验证：与 spec 场景「运行期 Dock 展示应用图标」「图标圆角一致」「启动期无咖啡杯闪现」一致。
- [x] 3.2 .app 形态：`./build-mac.sh` 重打包后 Dock 图标圆角，与 jar 形态观感一致。验证：与「图标圆角一致」场景一致。
- [x] 3.3 终端模式回归：不带 `--gui` 执行一次加/解密，行为不变。
- [x] 3.4 GUI 常规功能回归：表单、执行页、状态徽章、拖拽不受影响。
