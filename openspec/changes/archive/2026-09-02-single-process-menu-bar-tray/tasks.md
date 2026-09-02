## 1. 托盘挂载与二态菜单

- [x] 1.1 新增 `gui/interfaces/swing/tray/TrayManager`：`SystemTray` 支持检测、`logo.png` 经 `LogoUtil.loadImage` 缩放 22/44 双档 `BaseMultiResolutionImage` 图标（`setImageAutoSize(false)`）、tooltip "EncryptDog"、IDLE/WORKING 状态字段与 EDT 统一更新入口（`SwingUtilities.invokeLater`）。验证：`--gui` 启动后菜单栏出现品牌标志图标、tooltip 为 EncryptDog，深/浅色菜单栏下均清晰不模糊
- [x] 1.2 实现二态菜单：空闲态 Show / Reveal last output / Quit；干活态 Show / 禁用状态行 / Quit，状态行文案 `Encrypting {done}/{total} · {pct}%`，tooltip 与状态行同步；Show 菜单项点击在 EDT 上调用 `restoreWindow()`（setVisible + toFront + requestFocus）。验证：触发 `onOperationStart` 后菜单变干活态、状态行禁用且文案正确，结束后回空闲态；隐藏窗口后点击 Show 窗口恢复前台并保持原页面
- [x] 1.3 进度计算与节流：done=FileProgress.state 为 FINISHED 的文件数（含失败）；pct=按 `progress` 百分比字符串与 `sourceFileSize` 人类可读大小做字节加权（解析失败按文件数均摊）；tooltip/菜单更新 500ms 节流。验证：构造多文件 FileProgress 列表连续调用 `onProgress`，状态行数值符合口径，500ms 内多次调用仅更新一次

## 2. GUI 接线与关窗语义

- [x] 2.1 `EncryptDogGui.launch` 集成 `TrayManager.install(frame)`：install 成功才把 `setDefaultCloseOperation` 切换为 `HIDE_ON_CLOSE`，失败保持 `EXIT_ON_CLOSE`（降级矩阵）；注册 `com.apple.eawt` `AppReOpenedListener` 指向 `restoreWindow()`。验证：`--gui` 启动后点 ✕ 窗口隐藏、进程与图标仍在；点 Dock 图标窗口恢复前台且停留在关闭前页面
- [x] 2.2 `EncryptDogFrame` 三个钩子：`startOperation` → `tray.onOperationStart()`，`finishOperation` → `tray.onOperationFinished(result)`，`operationFailed` → `tray.onOperationFinished(异常)`。验证：提交任务后托盘立即切干活态，完成（含部分失败）与异常路径均回空闲态
- [x] 2.3 进度转发：Frame 现有 `OperationListener` 同时调用 `tray.onProgress(dto)`（调度线程进入，内部切 EDT）。验证：执行多文件任务，状态行 done/total 与执行表格一致递增

## 3. 单例锁

- [x] 3.1 实现 localhost:32346 端口单例锁：bind 成功即唯一实例并起 accept 线程（收到魔数 "ENCRYPTDOG1" + "show" 回 "ok" 并在 EDT 上 `restoreWindow()`）；bind 失败则连接发魔数+show，收到 "ok" 后 `System.exit(0)`，超时/魔数不符视为陌生占用降级正常启动并记日志；`acquireSingleton()` 在窗口创建前执行。验证：先后两次 `java -jar --gui`，第二个进程自行退出且第一个窗口（含隐藏态）被带到前台；用 `nc -l 32346` 占住端口后 GUI 仍正常启动

## 4. 退出语义

- [x] 4.1 注册 `com.apple.eawt` quit handler 拦截 Cmd+Q，与托盘 Quit 菜单项共用 `requestQuit()`：空闲直接 `System.exit(0)`；干活中 `ThemedConfirmDialog.show` 弹确认浮层（"Quit EncryptDog" / 提示任务进行中与文件可能不完整 / Quit anyway / Keep working），确认退出、取消或 ESC 继续；窗口隐藏时先 `restoreWindow()` 再弹浮层。验证：空闲 Cmd+Q 与托盘 Quit 均直接退出进程；干活中两者均弹浮层，取消后任务继续、确认后进程立即退出；隐藏窗口干活中点 Quit 浮层正常可见

## 5. Reveal 与完成通知

- [x] 5.1 `lastOutputFile` 维护：`onOperationFinished` 从 `fileResults` 取最后一个成功（result 为成功且 targetFile 非空）的 `targetFile`，刷新 Reveal 菜单项 `setEnabled`；点击用 `Desktop.browseFileDirectory(file)` 打开目录并选中，异常静默。验证：跑一轮多文件任务后点 Reveal last output，Finder 打开最后一个成功输出目录并选中该文件；启动后未跑任务时菜单项置灰；手动删除输出文件后点击静默无异常
- [x] 5.2 `onOperationFinished` 中窗口不可见时 `tray.displayMessage("EncryptDog", 成功/失败摘要)`（音效由 core `FinishedListener` 播放，不重复实现）。验证：干活中 ✕ 隐藏窗口，完成后系统通知弹出并展示成功/失败数量，托盘回到空闲态

## 6. 文档与验收

- [x] 6.1 README 补充托盘使用说明：✕ 仅隐藏、Quit/Cmd+Q 才是退出、干活态进度状态行、Reveal last output、二次启动唤醒。验证：README 无旧行为（✕ 退出进程）描述残留，品牌名均为 EncryptDog
- [x] 6.2 `openspec validate --strict` 通过；终端模式全流程回归（不带 `--gui` 的加解密、banner、Dashboard 与引入前一致）。验证：validate 无错误；终端加解密一次全流程对比托盘引入前行为一致
- [x] 6.3 `build-mac.sh` 构建 .app/dmg 后真机验收：双形态（jar + .app）托盘挂载、✕ 隐藏、Dock 点击唤回、干活态进度与 tooltip、完成通知、退出确认、单例唤醒（jar 形态双开）、菜单栏图标深/浅色辨识度、隐藏后台 Activity Monitor 无持续 CPU。验证：按 specs/menu-bar-tray 与 specs/swing-gui 场景清单逐条通过
