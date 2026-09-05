# menu-bar-tray Specification

## Purpose

规定 EncryptDog GUI 模式在 macOS 菜单栏的单进程常驻托盘能力：图标挂载与降级、空闲/干活二态菜单与 tooltip、窗口关闭仅隐藏与恢复、托盘 Quit/Cmd+Q 退出语义、完成通知、最近输出定位与二次启动单例，托盘不可用时优雅降级且不影响其余 GUI 功能。

## Requirements

### Requirement: 单进程托盘挂载与降级

GUI 模式启动时，系统 SHALL 以单进程方式在 macOS 菜单栏挂载应用图标（`logo.png` 品牌标志资源，与标题栏同源，22pt 与 44pt@2x 双档多分辨率呈现，等比缩放不拉伸变形，图标内容以约 82% 内框渲染、在槽位内保留约 18% 的视觉留白与 macOS 菜单栏原生图标惯例一致，槽位尺寸不变），不引入独立启动参数、守护进程或第二个 JVM；终端模式 SHALL NOT 挂载菜单栏图标。系统托盘不可用或图标资源缺失时 SHALL 静默降级为无托盘运行，GUI 其余功能正常，且该场景下窗口 ✕ SHALL 保持退出进程语义。

#### Scenario: GUI 模式启动挂载图标

- **WHEN** 用户以 `--gui` 方式启动加密狗
- **THEN** macOS 菜单栏出现应用图标（清晰不模糊、与 Dock 图标同源、视觉大小与相邻菜单栏原生图标相当不偏大），tooltip 显示 "EncryptDog"，进程为单进程形态

#### Scenario: 终端模式不挂载图标

- **WHEN** 用户以终端模式（不带 `--gui`）运行
- **THEN** 菜单栏不出现任何图标，行为与托盘引入前完全一致

#### Scenario: 托盘图标视觉留白

- **WHEN** 用户对比菜单栏中的加密狗图标与相邻原生图标
- **THEN** 加密狗图标内容不顶满 22pt 槽位、四周保留约 18% 视觉留白，视觉大小与 macOS 菜单栏图标惯例一致

#### Scenario: 托盘不可用静默降级

- **WHEN** 运行环境不支持系统托盘（`SystemTray.isSupported()` 为 false）或图标资源缺失
- **THEN** 系统不挂载托盘、不崩溃、不弹错，GUI 其余功能正常，窗口 ✕ 关闭退出进程

### Requirement: 托盘二态菜单与 tooltip

托盘菜单 SHALL 在空闲与干活中两个状态呈现不同内容，菜单与 tooltip 文案 SHALL 全部为英文、SHALL NOT 使用 emoji 或装饰符号。空闲态菜单项依次为 Show、Reveal last output、更新检查项、Quit，tooltip 为 "EncryptDog"。干活态菜单项依次为 Show、状态行、更新检查项、Quit——状态行为禁用（不可点击）的菜单项，文案格式 "Encrypting {done}/{total} · {pct}%"，其中 done 为已完成处理文件数（含失败文件）、total 为总文件数、pct 为按字节加权的整体进度百分比；tooltip 与状态行文案同步。更新检查项恰好一项、按更新状态切换：常态为 Check for updates；检查进行中为禁用的 Checking for updates...；已发现新版本且未下载为 Update available {version}；下载进行中为禁用的 Downloading {version} · {pct}%；下载完成且缓存存在为 Open downloaded update。执行开始 SHALL 立即切入干活态，执行结束（含全部成功、部分失败与执行异常）SHALL 立即切回空闲态。

#### Scenario: 空闲态菜单

- **WHEN** 未在执行任务时用户点击菜单栏图标
- **THEN** 弹出菜单依次为 Show、Reveal last output、Check for updates、Quit 四项，tooltip 为 "EncryptDog"

#### Scenario: 干活态菜单与进度状态行

- **WHEN** 任务执行中用户点击菜单栏图标
- **THEN** 弹出菜单为 Show、状态行、Check for updates、Quit；状态行禁用并以 "Encrypting 3/12 · 41%" 形式展示当前进度，tooltip 同步展示同一进度文案

#### Scenario: 更新可用状态项

- **WHEN** 已发现新版本且用户尚未下载（含点击 Later 之后）
- **THEN** 托盘菜单以 Update available {version} 替代 Check for updates，点击重新弹出更新确认框

#### Scenario: 下载进度状态项

- **WHEN** 新版本下载进行中用户点击菜单栏图标
- **THEN** 托盘菜单包含禁用的 Downloading {version} · {pct}% 项，不出现可点击的检查更新项

#### Scenario: 已下载状态项

- **WHEN** 新版本下载完成且缓存 DMG 存在
- **THEN** 托盘菜单包含 Open downloaded update 项，点击重新挂载并打开缓存 DMG

#### Scenario: 状态随执行切换

- **WHEN** 用户提交执行任务、随后任务完成（任意结果）
- **THEN** 托盘在提交后立即切入干活态并持续更新进度，完成后立即回到空闲态

#### Scenario: 菜单文案语言与符号

- **WHEN** 用户查看托盘菜单与 tooltip
- **THEN** 所有文案均为英文，不出现 emoji 或装饰符号

### Requirement: 窗口关闭仅隐藏与恢复

GUI 窗口点击 ✕ 关闭按钮时 SHALL 仅隐藏窗口，进程 SHALL NOT 退出；正在执行的任务 SHALL 继续后台执行不受影响。隐藏的窗口 SHALL 可通过托盘菜单 Show 或点击 Dock 图标恢复前台显示，恢复后停留在关闭前的页面状态，不重复创建窗口。

#### Scenario: 空闲 ✕ 仅隐藏

- **WHEN** 空闲状态下用户点击窗口 ✕ 关闭按钮
- **THEN** 窗口隐藏，进程继续运行，菜单栏图标仍在，Dock 图标仍在

#### Scenario: 干活中 ✕ 任务继续

- **WHEN** 执行过程中用户点击窗口 ✕ 关闭按钮
- **THEN** 窗口隐藏，加/解密任务继续执行，托盘保持干活态并持续更新进度

#### Scenario: 托盘 Show 恢复窗口

- **WHEN** 窗口隐藏期间用户点击托盘菜单的 Show
- **THEN** 既有窗口恢复前台显示（不新开窗口），停留在关闭前的页面

#### Scenario: Dock 点击恢复窗口

- **WHEN** 窗口隐藏期间用户点击 Dock 中的 EncryptDog 图标
- **THEN** 既有窗口恢复前台显示（不新开窗口），停留在关闭前的页面

### Requirement: 退出语义与干活中确认

系统在托盘已挂载的运行状态下 SHALL 仅通过托盘菜单 Quit 与 macOS 快捷键 Cmd+Q 退出进程（托盘不可用降级场景除外，见「单进程托盘挂载与降级」）。空闲状态下，二者 SHALL 直接退出：窗口与菜单栏图标消失、进程结束。干活中状态下，二者 SHALL 弹出主题化确认浮层：用户选择取消则任务继续执行，选择确认退出则进程立即退出（正在处理中的文件可能留下不完整产物）。窗口隐藏时触发退出 SHALL 确保确认浮层可见。

#### Scenario: 空闲托盘 Quit 直接退出

- **WHEN** 空闲状态下用户点击托盘菜单 Quit
- **THEN** 系统直接退出：窗口、菜单栏图标与 Dock 图标消失，进程结束

#### Scenario: 空闲 Cmd+Q 直接退出

- **WHEN** 空闲状态下用户按下 Cmd+Q
- **THEN** 系统直接退出：窗口、菜单栏图标与 Dock 图标消失，进程结束

#### Scenario: 干活中 Quit 弹出确认浮层

- **WHEN** 任务执行中用户点击托盘菜单 Quit 或按下 Cmd+Q
- **THEN** 系统弹出主题化确认浮层提示任务进行中，不立即退出

#### Scenario: 确认退出

- **WHEN** 用户在确认浮层中选择确认退出
- **THEN** 系统立即退出进程，窗口、菜单栏图标与 Dock 图标消失

#### Scenario: 取消继续执行

- **WHEN** 用户在确认浮层中选择取消（或按 ESC）
- **THEN** 浮层关闭，任务继续执行，托盘保持干活态

#### Scenario: 隐藏窗口下退出确认可见

- **WHEN** 窗口隐藏且任务执行中，用户点击托盘菜单 Quit
- **THEN** 确认浮层正常弹出可见（必要时先恢复窗口再展示浮层）

### Requirement: 执行完成通知

任务执行完成（含全部成功、部分失败与执行异常）时，托盘 SHALL 从干活态回到空闲态。窗口隐藏期间任务完成时，系统 SHALL 弹出系统通知展示结果摘要（成功/失败数量）；全部成功与部分失败时 SHALL 播放完成提示音（沿用既有完成音效资源，不新增音频）。

#### Scenario: 完成后回到空闲态

- **WHEN** 任务执行完成（任意结果）
- **THEN** 托盘菜单与 tooltip 回到空闲态内容

#### Scenario: 窗口隐藏时完成弹系统通知

- **WHEN** 窗口隐藏期间任务执行完成
- **THEN** 系统弹出通知，摘要展示本次成功与失败文件数量

#### Scenario: 完成提示音

- **WHEN** 任务全部成功或部分失败完成
- **THEN** 系统播放完成提示音（与窗口可见时一致的既有音效）

### Requirement: Reveal last output

空闲态托盘菜单的 Reveal last output SHALL 打开最近一次成功输出文件所在的目录并选中该文件。从未产生过成功输出时该菜单项 SHALL 置灰禁用。目标文件已被移动或删除导致无法定位时 SHALL 静默失败，不影响程序运行。

#### Scenario: 打开最近成功输出目录

- **WHEN** 至少存在一次成功输出的操作结果，用户点击托盘菜单 Reveal last output
- **THEN** Finder 打开最近一次成功输出文件所在目录并选中该文件

#### Scenario: 无成功输出时禁用

- **WHEN** 从未产生过成功输出（尚未执行过或全部失败）
- **THEN** Reveal last output 菜单项置灰禁用，点击无效果

#### Scenario: 目标文件已不存在

- **WHEN** 最近成功输出文件已被移动或删除，用户点击 Reveal last output
- **THEN** 系统静默失败，不崩溃、不弹错

### Requirement: 二次启动单例唤醒

jar 形态重复以 `--gui` 启动时，新进程 SHALL 检测已有实例：存在则向已有实例发送唤醒命令并自行退出，菜单栏 SHALL NOT 出现第二个图标，已有实例窗口被带回前台（含隐藏态恢复）；不存在则正常启动。检测端口被陌生进程占用时 SHALL 降级为正常启动（可能短暂出现第二个图标，不崩溃、不阻塞）。

#### Scenario: 重复启动唤醒已有实例

- **WHEN** 已有 GUI 实例正在运行（窗口可见或隐藏），用户再次以 `--gui` 启动（jar 形态）
- **THEN** 新进程不打开第二个窗口、不挂载第二个菜单栏图标，已有实例窗口被带回前台后新进程自行退出

#### Scenario: 陌生进程占用检测端口

- **WHEN** 单例检测端口被陌生程序占用导致无法确认已有实例
- **THEN** 新进程降级为正常启动，GUI 功能正常，不崩溃、不阻塞
