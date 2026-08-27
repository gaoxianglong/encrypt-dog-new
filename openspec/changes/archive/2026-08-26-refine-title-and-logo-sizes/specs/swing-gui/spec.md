## ADDED Requirements

### Requirement: 字号层级

内容卡片任务标题的字号 SHALL NOT 大于标题栏品牌名称的字号，保持"品牌—任务"层级协调；标题栏 logo 的显示尺寸 SHALL 与品牌名称视觉高度相当，不显过小。

#### Scenario: 任务标题不高于品牌名称

- **WHEN** 用户同时查看任务标题 "Encrypt files" 与标题栏品牌名称 "EncryptionDog"
- **THEN** 任务标题字号不高于品牌名称字号

#### Scenario: 标题栏 logo 与品牌名称协调

- **WHEN** 用户查看窗口标题栏
- **THEN** logo 与 "EncryptionDog" 垂直居中对齐，且 logo 视觉高度与品牌名称相当
