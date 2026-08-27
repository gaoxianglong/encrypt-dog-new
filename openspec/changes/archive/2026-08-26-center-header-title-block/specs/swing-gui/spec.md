## ADDED Requirements

### Requirement: 头部排版

内容卡片头部的主标题与副标题 SHALL 作为一个整体排版单元，在头部容器（卡片顶部至模式切换栏顶部之间）内垂直居中；副标题底边与模式切换栏顶边之间 SHALL 保持明显可见的间距；模式切换栏及其下方全部模块的位置 SHALL NOT 因此改变。

#### Scenario: 头部块垂直居中

- **WHEN** 用户查看内容卡片头部
- **THEN** 主标题 "Encrypt files" 与副标题 "Protect your files with local encryption" 作为一个整体在头部容器内垂直居中，上下留白均衡

#### Scenario: 与模式切换栏的间距

- **WHEN** 用户查看头部与模式切换栏的衔接处
- **THEN** 副标题底边与 Encrypt/Decrypt 分段选择器顶边之间存在明显可见的间距，且分段选择器及其下方模块位置保持不变
