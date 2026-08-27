## ADDED Requirements

### Requirement: 标题栏 logo 资源

标题栏 SHALL 加载应用 logo 资源文件（logo.png）展示，SHALL NOT 误加载其他界面图标资源；当 logo 原始宽高比与显示区域不一致时，SHALL 按原图比例缩放并居中适配显示区域，不拉伸变形。

#### Scenario: 加载正确的 logo 资源

- **WHEN** 用户启动 GUI 查看窗口标题栏
- **THEN** 标题栏展示应用 logo（logo.png），不展示文件空态图标等其他界面资源

#### Scenario: 非方形 logo 等比缩放

- **WHEN** logo 资源原始宽高比与方形显示区域不一致
- **THEN** logo 按原图比例缩放并居中于显示区域，图形不被拉伸变形
