# .dog 文档系统图标更换为独立品牌图标

## Why

用户提供新的品牌图形（`file.png`，512×512 方画布、竖版图形、四角透明），要求作为 .dog 文件的系统（Finder）图标。现状：`build-mac.sh` 将应用 icns（dock_logo.png 圆角化产物）直接拷贝为 `dog-document.icns`，.dog 文档图标与 Dock 图标同源——新需求要求 .dog 文件展示独立的品牌文档图标，与应用图标区分。

## What Changes

- 资源：`src/main/resources/file.png` 更名为 `dog-document.png`（语义命名，与 icns 名对应）。
- 打包产线（`build-mac.sh`）：`dog-document.icns` 不再拷贝应用 icns，改为从 `dog-document.png` 独立生成 iconset 十档 + iconutil；PIL 预处理仅做 82% 内框收边（与项目图标边距约定一致），**不套圆角遮罩**（新图形自带轮廓，套 22% 圆角会破坏形态）；PIL 缺失时降级原图直走 sips。
- 构建断言：新增防回归断言——`dog-document.icns` 与应用 icns 字节相同则打包中止（防有人改回拷贝产线）。
- README：图标缓存刷新说明补充 .dog 文档图标更换后的 Finder 缓存刷新（killall Finder / iconservices 缓存）。
- 应用图标（dock_logo.png 产线）、jar 形态与终端模式行为均不变。

## Capabilities

### New Capabilities

（无）

### Modified Capabilities

- `dog-file-association`: 「文档图标声明」需求修改——.dog 文档图标 SHALL 使用独立品牌文档图标资源（dog-document.png 生成，竖版品牌图形），SHALL NOT 与应用 Dock 图标同源；图标键注入、jar/终端不声明图标、文件格式与加密行为不变等既有约束保持。

## Impact

- 受影响代码：`build-mac.sh`（文档图标生成段落与断言）、资源文件更名。
- 无 Java 代码变化；无 API/依赖变化；不影响加解密行为与 .dog 文件格式。
- Finder 图标缓存：更换图标后已安装设备可能需要刷新图标服务缓存（README 已有刷新说明，本次补充文档图标场景）。
