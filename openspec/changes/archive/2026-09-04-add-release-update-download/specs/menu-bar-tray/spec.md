## MODIFIED Requirements

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
