## ADDED Requirements

### Requirement: 模式切换布局联动

切换加/解密模式时，确认密钥行（标签与输入框）SHALL 仅在加密模式展示；解密模式下该行隐藏后，其下方模块（Algorithm、Target directory、两个选项复选框、错误提示、主操作按钮）SHALL 整体上移一个确认密钥行行高，使 Secret key 与 Algorithm 之间保持与其他相邻字段一致的间距，不出现空洞；加密模式下各模块位置与变更前完全一致；卡片右下角版本号 SHALL 在两种模式下均保持原位。

#### Scenario: 解密模式布局紧凑

- **WHEN** 用户切换到 Decrypt 模式
- **THEN** 确认密钥标签与输入框隐藏，Algorithm、Target directory、两个选项复选框、错误提示与主按钮整体上移一个行高，Secret key 与 Algorithm 的间距与其他相邻字段间距一致，无空洞

#### Scenario: 加密模式布局不变

- **WHEN** 用户处于 Encrypt 模式（含从 Decrypt 切回）
- **THEN** 确认密钥行展示，全部模块位于与变更前一致的位置

#### Scenario: 版本号位置稳定

- **WHEN** 用户在 Encrypt 与 Decrypt 模式之间切换
- **THEN** 卡片右下角版本号位置始终不变

## MODIFIED Requirements

### Requirement: 信息层级

GUI SHALL 采用"应用品牌—当前任务—具体操作"三级信息层级：应用品牌（logo + EncryptionDog）仅出现在窗口标题栏；主内容区以与当前模式对应的任务标题与副标题说明当前任务——加密模式为 "Encrypt files" / "Protect your files with local encryption"，解密模式为 "Decrypt files" / "Restore your files with local decryption"——不重复展示品牌名称；Encrypt/Decrypt 模式切换紧接任务标题；版本号 SHALL 位于内容卡片右下角，以次要视觉样式展示。

#### Scenario: 品牌单一性

- **WHEN** 用户查看 GUI 窗口
- **THEN** logo 与 "EncryptionDog" 仅在窗口标题栏出现一次，内容卡片头部不重复展示品牌名称

#### Scenario: 任务标题与模式切换

- **WHEN** 用户查看内容卡片头部并切换 Encrypt/Decrypt 模式
- **THEN** 加密模式展示任务标题 "Encrypt files" 与副标题 "Protect your files with local encryption"；解密模式展示 "Decrypt files" 与 "Restore your files with local decryption"；分段选择器紧接任务标题且标题块位置不变

#### Scenario: 版本号位置

- **WHEN** 用户查看内容卡片
- **THEN** 版本号显示在卡片右下角，为小字号次要色，不处于视觉焦点位置

### Requirement: 头部排版

内容卡片头部的主标题与副标题 SHALL 作为一个整体排版单元，在头部容器（卡片顶部至模式切换栏顶部之间）内垂直居中；副标题底边与模式切换栏顶边之间 SHALL 保持明显可见的间距；头部排版单元的任何调整 SHALL NOT 改变模式切换栏的位置；模式切换栏下方模块的位置仅由"模式切换布局联动"要求驱动，不受头部排版影响。

#### Scenario: 头部块垂直居中

- **WHEN** 用户查看内容卡片头部
- **THEN** 主标题与副标题（文案随模式联动）作为一个整体在头部容器内垂直居中，上下留白均衡

#### Scenario: 与模式切换栏的间距

- **WHEN** 用户查看头部与模式切换栏的衔接处
- **THEN** 副标题底边与 Encrypt/Decrypt 分段选择器顶边之间存在明显可见的间距，且分段选择器位置保持不变
