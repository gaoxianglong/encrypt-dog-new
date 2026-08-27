# Tasks: Add Drag-and-Drop and XOR Warning

## 1. 源文件模块重组

- [x] 1.1 新建等宽分段选择器组件（Encrypt/Decrypt 各 210px，选中 ACCENT 填充、未选透明，复用 switchMode 状态机），替换原两个独立模式按钮，验证模式切换与确认密钥框显隐、主按钮文字联动正常
- [x] 1.2 新建 `DropFilePanel` 拖拽框组件（深色底 rgba(0,0,0,60) + BasicStroke 虚线描边 + 圆角；空态居中 fileEmpty 图标 48px + "Drag files or folders here" 提示；有文件时切换为滚动列表视图），验证空态/列表切换与选中状态保留
- [x] 1.3 按钮行布局：`+ Select files`/`+ Select directory` 左对齐、`Remove selected` 右对齐，与模块左右边缘及下方输入框边缘对齐，验证按钮功能不变
- [x] 1.4 模块几何整合：模块替换旧 y64..230 区域（分段选择器 64..100 / 拖拽框 104..196 / 按钮行 200..230），下方密钥等区域坐标不变，主按钮底部留白 36px，验证整体与卡片、输入框对齐统一（几何已由 5.2 修订：拖拽框加高至 112、下方区域 +14、留白 30px）

## 2. 拖拽与动画

- [ ] 2.1 拖拽框接入自定义 TransferHandler（接受 javaFileListFlavor，文件/目录路径入列表并去重），验证拖入文件与文件夹均加入列表、重复项不重复添加
- [ ] 2.2 拖入悬停呼吸脉冲（虚线边框颜色 ACCENT↔TEXT_SECONDARY 正弦振荡 Timer，dragEnter 启动 / dragExit 与 drop 完成复位），验证拖入时脉冲、移出后恢复
- [ ] 2.3 放下涟漪（DropFilePanel 内 GradientPaint 扫过约 350ms）+ 新行依次淡入（自定义 renderer 按行 alpha 40ms 步进），验证动画流畅、不影响选中与移除交互

## 3. XOR 安全提示

- [x] 3.1 新增 `xorWarningLabel`（TEXT_SECONDARY，文案 "Using this algorithm for encryption is insecure"），显隐收敛到 `updateXorWarning()`，分段选择器切换 / 下拉 ItemListener / prefill 三处联动，验证加密+XOR 显示、加密+其他、解密+XOR、解密+其他 四种组合显隐正确

## 4. 图标资产与打包

- [x] 4.1 `24gl-fileEmpty.png` 经 sips 缩 128px 入 `src/main/resources/file-empty.png`，`LogoUtil` 扩展通用 `loadImage(resource, size)`（按显示尺寸精确缩放，Icon 尺寸与显示一致），验证空态图标清晰完整显示
- [ ] 4.2 `mvn package` 重新打包，GUI 冒烟验证模块布局、拖拽动画、警告显示、表单既有交互无回归、终端模式不受影响

## 5. 实测反馈修复

- [x] 5.1 标题栏比例：`LOGO_TITLE_SIZE` 18→22、标题字体 22 加粗，验证 logo 与 EncryptionDog 同高平行
- [ ] 5.2 拖拽修复：acceptDrag/acceptDrop 显式 ACTION_COPY（macOS Finder dragEnter 报告 ACTION_NONE 导致失效）+ DropTarget 按组件分别绑定；拖拽框高度 92→112（y104..216），按钮行 220..246，下方区域按 D1 新坐标表整体 +14，验证真实拖拽可加入列表、几何无重叠
- [x] 5.3 XOR 警告：文案改为 "Use with caution"（bounds 322..554, 与下拉框同高 40 + JLabel.CENTER 垂直居中），验证四种组合显隐与对齐

- [x] 5.4 文件列表滚动修复：JList 包入透明 JScrollPane（viewport 透明、无边框、AS_NEEDED 滚动策略，FlatLaf 细滚动条），验证多文件时滚动条出现且可滚动到全部文件、少文件时滚动条隐藏、空态图标提示与拖拽/淡入不受影响

- [x] 5.5 表单字段间隙统一：四框间隙统一 6px（密钥 252 / 确认密钥 298 / 算法 344 / 目标目录 390），标签偏移统一 field+8 垂直居中，选项区 436/466、错误提示 498、主按钮 522，验证加密模式下四框间隙一致、底部留白 ≥30px

- [x] 5.6 宽度协调（方案B）：目标目录字段 420 全宽、Browse 按钮内嵌右端 98px（仿眼睛按钮的 BorderLayout 组合模式），验证密钥/确认密钥/目标目录三框同宽 420、算法行"短下拉+右侧警告"右缘对齐 554、四框间隙保持统一 6px
