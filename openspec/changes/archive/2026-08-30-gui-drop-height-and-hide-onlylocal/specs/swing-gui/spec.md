# swing-gui Specification (delta)

## MODIFIED Requirements

### Requirement: GUI 参数收集

GUI SHALL 提供表单收集与终端参数一一对应的操作参数：源文件（多选，支持选择目录递归解析）、密钥与确认密钥、加密/解密模式、加密算法（AES/3DES/XOR）、是否删除源文件、是否仅本机操作、目标输出目录。解密模式下 SHALL NOT 展示「是否仅本机操作」选项（解密路径不读取该标志，文件头自描述决定设备绑定语义），其余参数收集不变。

#### Scenario: 完整填写表单

- **WHEN** 用户在表单中选择文件、输入两次一致的密钥、选择算法与模式并点击执行
- **THEN** 系统按表单参数执行与终端等价语义的加/解密操作

#### Scenario: 收集缺省值

- **WHEN** 用户未选择算法、未填写目标目录、未勾选选项
- **THEN** 系统按终端缺省语义处理：算法缺省 AES、目标目录缺省为源文件目录、选项缺省关闭

#### Scenario: 解密模式隐藏仅本机选项

- **WHEN** 用户切换到 Decrypt 模式
- **THEN** 表单不展示「仅本机（Local machine only）」选项；用户切换到 Encrypt 模式后该选项重新展示

### Requirement: 模式切换布局联动

切换加/解密模式时，确认密钥行（标签与输入框）与「仅本机」选项 SHALL 仅在加密模式展示；解密模式下两行隐藏后，「删除源文件」选项 SHALL 上移一个行高、主操作按钮 SHALL 上移两个行高（对应两个隐藏行），使 Secret key 与 Algorithm 之间、删除选项与目标目录行之间保持与其他相邻字段一致的间距，不出现空洞；加密模式下各模块位置与变更前完全一致；蒙层顶部主副标题取消后，模式切换栏 SHALL 位于蒙层顶部首行，其下方模块 SHALL 整体上移至卡片顶部，卡片顶部 SHALL NOT 出现空洞；卡片右下角版本号 SHALL 在两种模式下均保持原位。

#### Scenario: 解密模式布局紧凑

- **WHEN** 用户切换到 Decrypt 模式
- **THEN** 确认密钥标签与输入框、「仅本机」选项隐藏，Algorithm、Target directory 与「删除源文件」选项上移一个行高，主按钮上移两个行高，Secret key 与 Algorithm 的间距与其他相邻字段间距一致，无空洞

#### Scenario: 加密模式布局不变

- **WHEN** 用户处于 Encrypt 模式（含从 Decrypt 切回）
- **THEN** 确认密钥行与「仅本机」选项展示，全部模块位于与变更前一致的位置

#### Scenario: 版本号位置稳定

- **WHEN** 用户在 Encrypt 与 Decrypt 模式之间切换
- **THEN** 卡片右下角版本号位置始终不变
