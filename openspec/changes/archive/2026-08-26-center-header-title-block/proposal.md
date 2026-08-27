# Proposal: 头部标题块垂直居中

## Why

当前卡片头部的主标题 "Encrypt files"（y14）与副标题 "Protect your files with local encryption"（y48，底边 64）贴底排布——副标题底边与 Encrypt/Decrypt 分段选择器顶边完全齐平，视觉上头重脚轻。需要将主标题与副标题作为一个整体，在头部容器内垂直居中，并与下方模式切换栏保持适当间距。

## What Changes

- `EncryptFormPanel` 头部两个标签的 bounds 调整：主标题 (30,14,400,34) → (30,7,400,34)，副标题 (30,48,420,16) → (30,41,420,16)；标题+副标题整体（y7..57）在头部容器（卡片顶 0 至模式切换栏顶 64）内垂直居中，与切换栏之间留出 7px 间距。
- 模式切换栏（y64）及其下方全部模块几何零变化。
- 字号、颜色、文案均不变。

## Capabilities

### New Capabilities

（无）

### Modified Capabilities

- `swing-gui`: 新增"头部排版"需求——头部标题块垂直居中与下方间距的排版契约（头部标题块在此之前尚无独立排版需求，主 spec 中"信息层级"需求尚未随 restructure-gui-info-hierarchy 归档同步）。

## Impact

- 仅改动 `gui/interfaces/swing/EncryptFormPanel.java` 中两个 JLabel 的 bounds 常量值。
- 无 API、依赖、数据格式变化；终端模式零影响。
