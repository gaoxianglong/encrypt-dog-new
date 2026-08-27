## ADDED Requirements

### Requirement: 信息层级

GUI SHALL 采用"应用品牌—当前任务—具体操作"三级信息层级：应用品牌（logo + EncryptionDog）仅出现在窗口标题栏；主内容区以任务标题 "Encrypt files" 与副标题 "Protect your files with local encryption" 说明当前任务，不重复展示品牌名称；Encrypt/Decrypt 模式切换紧接任务标题；版本号 SHALL 位于内容卡片右下角，以次要视觉样式展示。

#### Scenario: 品牌单一性

- **WHEN** 用户查看 GUI 窗口
- **THEN** logo 与 "EncryptionDog" 仅在窗口标题栏出现一次，内容卡片头部不重复展示品牌名称

#### Scenario: 任务标题与模式切换

- **WHEN** 用户查看内容卡片头部
- **THEN** 依次展示任务标题 "Encrypt files"、副标题 "Protect your files with local encryption"，Encrypt/Decrypt 分段选择器紧接其后

#### Scenario: 版本号位置

- **WHEN** 用户查看内容卡片
- **THEN** 版本号显示在卡片右下角，为小字号次要色，不处于视觉焦点位置
