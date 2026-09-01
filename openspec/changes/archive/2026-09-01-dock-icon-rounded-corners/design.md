# 设计：Dock 图标圆角化（打包与运行时两处）

## Context

`dock_logo.png`（1254×1254 全幅方形、四角不透明）被三处使用：`.app` 打包期 icns（`build-mac.sh` iconutil 流程）、jar 形态运行时设置（`EncryptDogGui.installDockIcon` 的 128/256 双档）、启动期预置（`Starter.preinstallDockIcon` 拷贝原始字节到临时文件）。macOS 对含完整 1024px 图层的现代 icns 不贴标准圆角遮罩（信任美术成品），导致 Dock 图标呈方形。用户要求两处（打包与运行时）统一圆角。

动机与范围见 proposal.md；需求见 specs/swing-gui/spec.md。注意：本变更与 remove-menu-bar-tray 的 swing-gui delta 同触「Dock 图标」需求，**归档顺序须先 remove 后 rounded**（本 delta 基于 remove 后的文本编写）。

## Goals / Non-Goals

**Goals:**

- 三处（启动预置/运行时/打包 icns）Dock 图标统一圆角，半径 = 边长 22%
- 启动全程无方形→圆角跳变

**Non-Goals:**

- 不改 `dock_logo.png` 原图（圆角在渲染/打包期套用，源图保持方形成品以便其它用途）
- 不做圆形/胶囊形等其它轮廓（用户要的是 macOS 标准 squircle 观感）

## Decisions

### D1：82% 内框 + 圆角半径 = 内框边长 22%，Java 与 Python 同比例（视觉验收修正）

macOS 标准 squircle 观感对应约 22% 的圆角占比；且系统图标美术自带约 18% 透明边距（内容约 82%）——**首版满幅渲染（内容到画布边缘）被用户验收否决：Dock 中图标比其它应用大一圈**。修正：内容收进 82% 内框居中，圆角半径按内框边长的 22% 计算。Java2D 用 `RoundRectangle2D` 裁剪（inset + inner 参数），PIL 用 `rounded_rectangle` 同比例 + `ImageChops.multiply` 保留内部 alpha——两处视觉一致。

### D2：Java 侧共享工具 = `LogoUtil.loadRoundedImage(resource, size)`

新增方法：读原图 → 缩放到 size → 新建 TYPE_INT_ARGB 透明画布 → 圆角裁剪绘制。headless 安全（BufferedImage/Graphics2D 不初始化 AWT Toolkit），`preinstallDockIcon` 可在 AWT 初始化前调用。`installDockIcon` 用该方法生成 128/256 双档；`preinstallDockIcon` 改为：渲染圆角 512px → `ImageIO.write` 到临时文件 → 设置属性（原样拷贝字节的逻辑替换为渲染输出）。

### D3：打包期 PIL 预处理（build-mac.sh）

icns 生成前插入 Python3 步骤（系统自带，无需 pip）：PIL 打开 dock_logo.png → 缩放到 1024 → 圆角遮罩 → 另存临时 PNG → 后续 sips 十档与 iconutil 以该 PNG 为源。无 PIL 时（极罕见）降级为原图直走（方形，不中断打包）。

### D4：验证策略

- 单元级：渲染出的 PNG 四角 alpha=0（sips 检查角像素或直接目视）
- jar 形态：启动全程 Dock 圆角、无跳变（用户验收 + 截屏）
- .app 形态：重打包后 icns 圆角（用户验收）
- 回归：终端模式、GUI 功能不受影响

## Risks / Trade-offs

- [22% 与系统图标视觉仍有细微差异] → 验收微调比例即可（两处常量同步改）。
- [PIL 缺失导致打包降级为方形] → 降级路径明确（不中断打包），日志提示。
- [preinstall 渲染增加启动耗时] → 单次 512px 渲染 <50ms，在 AWT 初始化前完成，无感知。
- [headless 环境 ImageIO 写盘失败] → try-catch 静默降级为无预置图标（运行时 Taskbar 设置仍生效）。

## Migration Plan

无数据/持久化变更。旧版 .app 方形图标在重新打包后变为圆角；jar 形态即时生效。回滚 = 还原 `installDockIcon`/`preinstallDockIcon` 原实现与脚本步骤。
