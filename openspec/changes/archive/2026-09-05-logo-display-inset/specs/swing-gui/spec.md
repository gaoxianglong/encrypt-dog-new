# swing-gui Spec Delta

## MODIFIED Requirements

### Requirement: 标题栏 logo 资源

标题栏 SHALL 加载应用 logo 资源文件（logo.png）展示，SHALL NOT 误加载其他界面图标资源；当 logo 原始宽高比与显示区域不一致时，SHALL 按原图比例缩放并居中适配显示区域，不拉伸变形。logo 内容 SHALL 以约 64% 内框渲染——在显示槽位内保留约 36% 的视觉留白，图形约 18px 与 22pt 品牌文字视觉平衡（验收迭代定稿），槽位尺寸不变，logo 视觉大小不显过大。

#### Scenario: 加载正确的 logo 资源

- **WHEN** 用户启动 GUI 查看窗口标题栏
- **THEN** 标题栏展示应用 logo（logo.png），不展示文件空态图标等其他界面资源

#### Scenario: 非方形 logo 等比缩放

- **WHEN** logo 资源原始宽高比与方形显示区域不一致
- **THEN** logo 按原图比例缩放并居中于显示区域，图形不被拉伸变形

#### Scenario: 标题栏 logo 视觉留白

- **WHEN** 用户查看窗口标题栏的 logo
- **THEN** logo 内容四周保留约 36% 的视觉留白，图形不顶满显示槽位，视觉大小与 "EncryptDog" 品牌文字平衡、不偏大

### Requirement: 标题栏 logo 与品牌名称协调

标题栏 logo 的显示尺寸 SHALL 与品牌名称视觉高度相当，不显过小，也不显过大。

#### Scenario: 标题栏 logo 与品牌名称协调

- **WHEN** 用户查看窗口标题栏
- **THEN** logo 与 "EncryptDog" 垂直居中对齐，且 logo 视觉高度与品牌名称相当（不明显小于或大于品牌文字）
