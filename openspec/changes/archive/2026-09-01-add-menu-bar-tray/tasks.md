# 实现任务：菜单栏常驻托盘（双进程）

## 1. 托盘守护进程

- [x] 1.1 `Starter` 增加 `--tray` 路由：进入托盘守护模式（不建窗口、不解析 picocli），并将该参数从 `--gui` 预填解析中排除。验证：`mvn compile` 通过。
- [x] 1.2 新增托盘守护类（建议 `gui/interfaces/swing/tray/` 包）：`SystemTray.isSupported()` 检查；`TrayIcon` 挂 logo（`BaseMultiResolutionImage` 装 22px/44px 双档，`setImageAutoSize(false)`）、tooltip "Encrypt Dog"、`PopupMenu` 两项（Open Encrypt Dog / Quit）；守护侧 ServerSocket 监听 127.0.0.1:32345 + 魔数握手；Quit 流程 = 通知 GUI（若在）→ removeIcon → `System.exit(0)`。验证：`java -jar dog.jar --tray` 菜单栏出现图标、菜单两项、Quit 后图标消失且进程退出。

## 2. 本地通信与单例

- [x] 2.1 新增本地 socket 小工具类：连接 + 魔数握手 + 发送短命令（show/quit），端口常量 32345（守护）/32346（GUI），仅 127.0.0.1。验证：`mvn compile` 通过。
- [x] 2.2 GUI 侧单例接线：`EncryptDogGui.launch` 启动序列改为——先 bind 32346；成功 → ensureDaemon（探测+握手 32345，缺则 spawn `java -Xmx64m -jar <self> --tray` 并轮询就绪 ≤3s，失败记日志继续）→ 显示窗口；bind 失败 → 连接发 "show" 后 `System.exit(0)`。GUI 侧 ServerSocket 接收 "show"（toFront+requestFocus，EDT）与 "quit"（frame.dispose）。验证：二次 `--gui` 启动旧窗口带前台、新实例退出；首次启动自动拉起守护。

## 3. 托盘交互闭环

- [x] 3.1 托盘 Open：连 GUI 端口成功发 "show"；失败 spawn `--gui` 进程（同 D2 的 self-jar 解析逻辑）。验证：GUI 未运行点 Open 拉起新窗口；GUI 已运行点 Open 只带前台不开新窗。
- [x] 3.2 端口冲突降级：守护端口被陌生进程占（握手失败）→ GUI 正常启动仅无托盘；GUI 端口被陌生进程占 → 正常启动（单例保护失效为已知限制）。验证：`nc -l 32345` 模拟后启动 GUI 不崩溃。

## 4. 验收与回归

- [x] 4.1 完整生命周期验收：首次 `--gui` 启动 → 菜单栏出现 logo 图标；✕ 关窗口 → 图标仍在；托盘 Open → GUI 重新出现；托盘 Quit → 窗口（若有）+ 图标全退、守护进程退出。验证：与 spec 场景「首次 GUI 启动拉起守护」「关闭窗口图标保留」「Open 拉起 GUI」「托盘 Quit 全退」一致。
- [x] 4.2 守护自愈：活动监视器强杀守护进程后再次 `--gui` 启动，图标恢复且唯一。验证：与「守护被杀后自愈」场景一致。
- [x] 4.3 图标视觉：深色菜单栏下 logo 清晰可辨（不模糊/不过暗），悬停 tooltip 正确。验证：与「图标展示」场景一致；若偏暗记录为后续亮色变体项。
- [x] 4.4 终端模式回归：不带 `--gui`/`--tray` 执行一次加/解密，无托盘图标、行为与改动前完全一致。
- [x] 4.5 GUI 常规功能回归：表单、执行页、状态徽章、拖拽、模式切换均不受托盘改动影响。
