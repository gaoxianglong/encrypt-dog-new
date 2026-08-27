## ADDED Requirements

### Requirement: 滚动条主题配色

GUI 中的滚动条（含下拉弹窗与文件列表滚动条）SHALL 使用主题强调色（ACCENT 紫）渲染，与整体视觉风格保持一致。

#### Scenario: 滚动条颜色

- **WHEN** 用户查看文件列表滚动条或下拉弹窗滚动条
- **THEN** 滚动条滑块以紫色渲染，与主题强调色一致

### Requirement: 文件列表选中态配色

文件列表中单击选中文件时，选中行背景 SHALL 使用淡紫色（与主题强调色 ACCENT 区分、更淡的紫色调），选中文字保持主题主文本色。

#### Scenario: 选中文件高亮

- **WHEN** 用户在文件列表单击选中某个文件
- **THEN** 该行以淡紫色背景高亮，与滚动条/分段选择器等 ACCENT 组件颜色区分，文字清晰可读

### Requirement: 窗口版权信息

GUI 窗口底部 SHALL 展示版权信息 "Copyright (c) 2021-2031 gaoxianglong"（与终端版本 footer 形式一致），位于卡片下方居中、次要文字色、不遮挡任何交互。

#### Scenario: 底部版权展示

- **WHEN** 用户查看 GUI 窗口
- **THEN** 窗口底部居中显示 "Copyright (c) 2021-2031 gaoxianglong"（次要文字色），不遮挡卡片与表单交互
