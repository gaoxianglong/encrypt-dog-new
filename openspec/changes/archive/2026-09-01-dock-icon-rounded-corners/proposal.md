# Dock 图标圆角化（打包与运行时两处）

## Why

用户验收发现 .app 的 Dock 图标呈**方形**：`dock_logo.png` 为全幅方形 PNG，iconutil 生成的 icns 含完整 1024px 图层，macOS 对这类"自带完整高清图层的 icns"信任美术成品、不贴标准圆角遮罩——系统应用图标圆润是因为美术资源本身四角透明。jar 形态的运行时 Dock 图标同理（原图缩放无圆角）。需要把圆角做进图标本身：打包期（icns）与运行时（Taskbar 设置）两处统一处理。

## What Changes

- **打包期**（`build-mac.sh`）：icns 生成前增加 PIL 预处理——给 dock_logo.png 套圆角透明遮罩（圆角半径 = 边长 22%，与 macOS 标准 squircle 观感一致），随后走既有 sips 十档 + iconutil 流程。
- **运行时**（jar 形态）：`installDockIcon` 的 128/256 双档改用**圆角渲染**（Java2D 圆角裁剪 + 透明底，同一 22% 比例）。
- **启动预置**（`Starter.preinstallDockIcon`）：临时文件同样输出圆角版本（512px），保证启动期图标与运行时设置无缝衔接（不出现方形→圆角跳变）。
- 圆角渲染逻辑收敛为共享工具（新方法，挂 `LogoUtil`），打包期 PIL 与运行时 Java2D 使用相同半径比例。

## Capabilities

### New Capabilities

无。

### Modified Capabilities

- `swing-gui`: 「Dock 图标」需求扩展——Dock 图标 SHALL 以圆角呈现（约 22% 边长半径，与 macOS 标准图标观感一致），启动预置、运行时设置与 .app 打包产物三处一致。

## Impact

- 受影响代码：`LogoUtil`（新增圆角渲染方法）、`EncryptDogGui.installDockIcon`、`Starter.preinstallDockIcon`、`build-mac.sh`（PIL 预处理步骤）。
- 资源：`dock_logo.png` 原图不改（圆角在渲染/打包期套用）。
- 无 API/依赖变化；仅 macOS 相关视觉行为。
