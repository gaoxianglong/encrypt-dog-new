# Proposal: 更新 README 以覆盖当前 GUI 功能与版本

## Why

GUI 已上线并经历多轮迭代（拖拽添加、解密模式布局、执行表格、内联错误提示等），但 README 仍停留在 2.0.2/2.0.3 时代：版本引用过期（徽章 2.0.2、示例 jar 名 2.0.2/2.0.3，实际构建产物为 dog-2.0.4.jar），GUI 使用指南只覆盖按钮添加文件、确认弹窗等旧形态，新用户按文档操作与真实界面不符。

## What Changes

- 版本引用统一为 2.0.4：顶部徽章、install 的 jar 名与 wget 下载链接、use 输出横幅（2.0.3-RELEASE → 2.0.4-RELEASE）、gui mode 启动示例
- GUI 使用指南按现状扩充（Interface guide 各条目 + 执行流程描述）：
  - 源文件：补充拖拽文件/文件夹到拖拽区（与 `+ Select files` / `+ Select directory` 按钮并列的交互）
  - 模式：解密模式下确认密钥行与「Local machine only」选项均隐藏（原文档只提到确认密钥）
  - 算法：XOR 选中时下拉框旁显示安全提示
  - 校验：字段内联错误提示（出错字段内红字、约 2 秒淡出）
  - 删除源文件：执行前二次确认弹窗（主题化圆角弹窗）
  - 执行阶段：窗口切换宽屏形态、逐文件执行表格（状态图标、进度、失败原因悬停）、预估剩余时间、执行完成后停留、左上角返回箭头重置表单回首页
- CLI 使用补充说明与示例：`-k` 为交互式输入（掩码回显，加密时要求输入两次），补充一次加密与一次解密的最小命令示例
- JVM 内存参数全文统一为 `-Xms1g -Xmx1g -Xmn384m`：install 两处 alias 与 gui 启动示例一致，不再出现 `-Xms512m -Xmx512m`
- 文档结构以 GUI 为主：`gui mode` 章节提升为安装后首个使用章节，终端用法降级为 `terminal mode` 章节（保留完整 `-h` 输出与 `-k` 交互示例）
- 除上述章节顺序调整外保持 README 现有排版风格；logo 图片与「highest security」「file structure」章节内容不动
- 无 BREAKING 变更；不触碰任何代码

## Capabilities

### New Capabilities

<!-- 无 -->

### Modified Capabilities

<!-- 无：纯文档变更，已声明 skip_specs: true（无 spec 级行为变化） -->

## Impact

- **文件**：仅 `README.md`
- **验收**：按文档启动命令与示例操作一次 GUI，界面行为与文档描述一致；jar 名与版本号与实际构建产物 dog-2.0.4.jar 一致
- **不涉及**：源代码、specs、构建配置
