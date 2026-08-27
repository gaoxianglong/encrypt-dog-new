## ADDED Requirements

### Requirement: 标题栏 logo 与品牌名称协调

标题栏 logo 的显示尺寸 SHALL 与品牌名称视觉高度相当，不显过小。

#### Scenario: 标题栏 logo 与品牌名称协调

- **WHEN** 用户查看窗口标题栏
- **THEN** logo 与 "EncryptionDog" 垂直居中对齐，且 logo 视觉高度与品牌名称相当

## MODIFIED Requirements

### Requirement: 信息层级

GUI SHALL 采用"应用品牌—任务操作"两级信息层级：应用品牌（logo + EncryptionDog）仅出现在窗口标题栏；主内容区 SHALL 为干净统一的半透明毛玻璃蒙层，SHALL NOT 绘制顶部高光带（不出现把蒙层分成上下两部分的横向分割线），蒙层上 SHALL NOT 展示任务主标题与副标题，首行即 Encrypt/Decrypt 模式切换，不重复展示品牌名称；版本号 SHALL 位于内容卡片右下角，以次要视觉样式展示。

#### Scenario: 品牌单一性

- **WHEN** 用户查看 GUI 窗口
- **THEN** logo 与 "EncryptionDog" 仅在窗口标题栏出现一次，内容卡片不重复展示品牌名称

#### Scenario: 任务标题与模式切换

- **WHEN** 用户查看内容卡片并切换 Encrypt/Decrypt 模式
- **THEN** 卡片顶部不再展示任务主标题与副标题，首行直接为 Encrypt/Decrypt 分段选择器，模式切换仅联动确认密钥行与模式文案

#### Scenario: 版本号位置

- **WHEN** 用户查看内容卡片
- **THEN** 版本号显示在卡片右下角，为小字号次要色，不处于视觉焦点位置

### Requirement: 模式切换布局联动

切换加/解密模式时，确认密钥行（标签与输入框）SHALL 仅在加密模式展示；解密模式下该行隐藏后，其下方模块（Algorithm、Target directory、两个选项复选框、错误提示、主操作按钮）SHALL 整体上移一个确认密钥行行高，使 Secret key 与 Algorithm 之间保持与其他相邻字段一致的间距，不出现空洞；加密模式下各模块位置与变更前完全一致；蒙层顶部主副标题取消后，模式切换栏 SHALL 位于蒙层顶部首行，其下方模块 SHALL 整体上移至卡片顶部，卡片顶部 SHALL NOT 出现空洞；卡片右下角版本号 SHALL 在两种模式下均保持原位。

#### Scenario: 解密模式布局紧凑

- **WHEN** 用户切换到 Decrypt 模式
- **THEN** 确认密钥标签与输入框隐藏，Algorithm、Target directory、两个选项复选框、错误提示与主按钮整体上移一个行高，Secret key 与 Algorithm 的间距与其他相邻字段间距一致，无空洞

#### Scenario: 加密模式布局不变

- **WHEN** 用户处于 Encrypt 模式（含从 Decrypt 切回）
- **THEN** 确认密钥行展示，全部模块位于与变更前一致的位置

#### Scenario: 版本号位置稳定

- **WHEN** 用户在 Encrypt 与 Decrypt 模式之间切换
- **THEN** 卡片右下角版本号位置始终不变

## REMOVED Requirements

### Requirement: 头部排版

**Reason**: 内容卡片的主标题与副标题已取消，该排版单元不复存在。

**Migration**: 无需迁移；模式切换栏上移至卡片顶部，其余模块布局由「模式切换布局联动」要求驱动。

### Requirement: 字号层级

**Reason**: 任务标题已取消，任务标题字号层级失去约束对象。

**Migration**: 标题栏 logo 与品牌名称协调条款由新增要求「标题栏 logo 与品牌名称协调」承接。
