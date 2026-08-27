## 1. 弹窗四角透明化

- [x] 1.1 `ThemedConfirmDialog` 构造中 `setBackground(new Color(0, 0, 0, 0))`,内容面板与 `getRootPane()` 置 `setOpaque(false)`,保留现有 `setShape` 圆角裁剪;验证 `mvn -q compile` 编译通过
- [x] 1.2 启动 GUI 勾选"删除源文件"提交,弹出弹窗后截图核对:四角为透明圆角、无灰色直角,渐变与描边贴合圆角;验证与主窗口圆角视觉一致
