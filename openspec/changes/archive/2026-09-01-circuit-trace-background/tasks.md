# Tasks: 纯紫色渐变背景(电路方案验收转向)

## 1. 清理电路资产

- [x] 1.1 `UiConstants`：移除 12 个 `CIRCUIT_*` 电路常量；`FRAME_DELAY_MS` 保留（DropFilePanel/FieldHint/GradientButton 仍使用）；验证：`mvn compile` 通过，`grep -rn "CIRCUIT_" src/` 无输出
- [x] 1.2 删除 `CircuitLayout.java`；验证：`grep -rn "CircuitLayout" src/` 无输出

## 2. 静态渐变背景

- [x] 2.1 新建 `GradientBackgroundPanel`：仅绘制紫色渐变（BG_TOP→BG_BOTTOM 垂直渐变，预渲染缓存、尺寸变化时重建），无 Timer、无动画、无鼠标监听；验证：代码无 Timer/repaint 循环，仅 paintComponent 按需重绘
- [x] 2.2 `EncryptDogFrame`：字段与构造引用替换为 `GradientBackgroundPanel`，移除 `stop()` 调用与 `forwardCardMouseToCircuit` 鼠标转发（`dispose()` 恢复直接 `super.dispose()`）；验证：`mvn compile` 通过
- [x] 2.3 删除 `CircuitPanel.java`；验证：`grep -rn "CircuitPanel" src/` 无输出

## 3. 验收

- [x] 3.1 `mvn clean package` 产出 `target/encryptdog-2.0.4.jar`；验证：构建成功
- [x] 3.2 截图验收：表单页与执行页背景为纯紫色渐变、与标题栏无缝衔接、无任何动画元素；标题栏为深蓝底、logo 与 "EncryptDog" 文字完整（用户上报的标题栏破坏重点复验）；验证：截图与 spec 场景比对
- [x] 3.3 性能实测：空闲 CPU 接近 0（无逐帧重绘层）；验证：ps 采样远低于电路版 67.6%
- [x] 3.4 GUI 冒烟：启动、表单填写、执行表格页往返、Back 重置均正常；验证：运行日志 0 异常
