# Tasks: Polish GUI Scrollbar and Animations

## 1. 滚动条紫色

- [x] 1.1 `EncryptDogGui.launch` 在 FlatDarkLaf.setup 后设置滚动条 UIManager 配色（thumb=ACCENT、hover=ACCENT_BRIGHT，键名按 FlatLaf 3.7 实测），验证文件列表滚动条与下拉弹窗滚动条滑块为紫色、hover 提亮

- [x] 1.2 文件列表选中态淡紫背景：JList selectionBackground 半透明 ACCENT(alpha≈80) + selectionForeground TEXT_PRIMARY，FadeCellRenderer 选中态显式 setOpaque(true) 绘制背景，验证单击选中显示淡紫、与 ACCENT 组件区分、文字可读、行淡入与移除交互不受影响

- [x] 1.3 窗口底部版权信息：EncryptDogFrame 粒子层上添加居中版权标签 "Copyright (c) 2021-2031 gaoxianglong"（TEXT_SECONDARY、SMALL_FONT、bounds(0,730,760,20)），验证底部居中显示、不遮挡卡片与按钮交互、粒子背景正常

## 2. 拖拽动画强化

- [ ] 2.1 `DropFilePanel` 动画参数调整（脉冲 1200ms、区间 ACCENT↔ACCENT_BRIGHT、涟漪 600ms/alpha 110/带宽 120、行淡入 80ms×5 步），验证拖入悬停脉冲明显且舒缓、放下涟漪清晰持久、新行淡入节奏可感知，选中/移除交互不受影响

## 3. 打包回归

- [ ] 3.1 `mvn package` 重新打包，GUI 冒烟验证滚动条紫色、文件列表选中态淡紫与动画强化生效、表单既有交互无回归、终端模式不受影响


