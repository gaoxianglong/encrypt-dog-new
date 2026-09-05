# 设计：.dog 文档图标更换为独立品牌图标

## Context

动机见 proposal.md。现状：`build-mac.sh` 将应用 icns（dock_logo.png 经 PIL 圆角化 + sips 十档 + iconutil）直接 `cp` 为 `dog-document.icns`，并注入 CFBundleTypeIconFile/UTTypeIconFile 两个图标键，构建断言校验 icns 与键存在。新图形 `file.png`：512×512 方画布，内容 alpha 包围盒 394×512（竖版、上下出血到画布边缘、四角透明），已自带轮廓——不能套现有圆角产线（22% 圆角遮罩会破坏图形形态），`sips -z` 强制方形会拉伸变形，但画布本身是方形、内容居其内，等比例缩放整张画布即可保持形态。

## Goals / Non-Goals

**Goals:**

- .dog 文档图标由独立品牌图形产线生成，与应用 Dock 图标区分
- 产线复用项目既有约定（82% 内框边距、sips 十档 + iconutil、PIL 缺失降级不中断打包）
- 防回归：文档图标产线被改回"拷贝应用 icns"时打包中止

**Non-Goals:**

- 不改应用 Dock 图标产线（dock_logo.png → app icns 保持不变）
- 不改 jar 形态与终端模式（不声明文档图标）
- 不改 Info.plist 键注入方式与文件关联行为

## Decisions

### D1：资源更名 `file.png` → `dog-document.png`

`src/main/resources/file.png` 更名为 `dog-document.png`（与 icns 名、图标键 dog-document 对应，语义清晰）。512×512 源图不裁剪不重绘，画布保持方形以保证 sips 等比缩放不拉伸。

### D2：独立 icns 产线（build-mac.sh）

替换 `cp app.icns → dog-document.icns` 为独立生成段落：

1. PIL 预处理（可用时）：读 dog-document.png → 画布整体 LANCZOS 缩放至 1024 画布的 82% 内框居中（画布 512→839，内容 bbox 394×512 → 约 646×839，纵向占满内框），**不套圆角遮罩**（保留自带轮廓）；输出临时 PNG 作为 iconset 源。PIL 缺失降级原图直走（上下出血但打包不中断，与 dock 产线降级策略一致）。
2. sips 十档（16/32/128/256/512 及 @2x）+ iconutil 输出 `dog-document.icns`，拷入 bundle Resources。512→1024 为 2x LANCZOS 放大：文档图标主要呈现 32–64px，1024 档仅预览用，可接受。

备选：原图直用不内框 → 否决：图形上下出血到画布边缘，Finder 中观感偏大，违背项目 82% 内框约定；套 22% 圆角遮罩 → 否决：竖版图形轮廓被裁掉；改源图美术重新出图 → 不必要：PIL 内框一步到位。

### D3：防回归断言

在既有"文档图标声明"断言后追加：`cmp -s` 应用 icns 与 `dog-document.icns`，字节相同则打包中止并输出独立错误信息（"文档图标与应用图标同源（产线被改回拷贝）"），与声明缺失断言区分。仅当产线被改回复制时触发，正常独立产线必不相同。

### D4：Finder 图标缓存

macOS 的 iconservices 会缓存文档图标：已安装设备上旧 .dog 文件可能继续显示旧图标。验证策略：**新建** .dog 文件观察图标（新文件通常立即采用新图标）；README 既有缓存刷新说明补充文档图标更换场景（killall Finder、必要时刷新 iconservices 缓存）。

## Risks / Trade-offs

- [Finder 缓存导致验收看到旧图标] → D4：新建 .dog 文件验证 + README 刷新步骤。
- [竖版图形在方形图标槽视觉偏小/偏大] → 82% 内框纵向占满；验收不通过仅调 PIL 内框比例（0.80–0.90），产线结构不变。
- [PIL 缺失降级原图观感差] → 降级不中断打包、日志提示，与 dock 产线同策略；正式打包环境均带系统 Python3+PIL。
- [断言误伤] → 断言仅字节比较且错误信息独立，正常产线必不相同，仅防回归。

## Migration Plan

无数据/持久化变更。旧安装包 .dog 继续显示旧图标（同源应用图标），重装新包 + 缓存刷新后生效。回滚 = 还原 build-mac.sh 的拷贝段落与资源更名。
