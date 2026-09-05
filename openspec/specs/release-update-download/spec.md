# release-update-download Specification

## Purpose

让 EncryptDog GUI 用户无需手动访问 GitHub 即可获知并获取新版本：启动时后台检查一次、托盘可手动检查，发现新版本弹窗确认后下载 DMG 并自动挂载打开，由用户拖拽完成安装。

## Requirements

### Requirement: 启动时后台检查与弹窗

GUI 启动完成后，系统 SHALL 在后台线程查询 GitHub 最新 release 一次，检查过程 SHALL NOT 阻塞界面或影响加解密操作。发现新版本时 SHALL 弹出确认框询问是否下载；无新版本或检查失败（网络不可用、接口限流、解析失败等）SHALL 静默结束，不弹任何错误。启动检查出结果时若任务执行中，弹窗 SHALL 推迟到任务结束（回到空闲态）后再展示。

#### Scenario: 发现新版本弹确认框

- **WHEN** 应用启动且 GitHub 最新 release 版本高于当前版本
- **THEN** 弹出确认框提示新版本号并询问是否下载，界面其它功能正常可用

#### Scenario: 无新版本静默

- **WHEN** 应用启动且 GitHub 最新 release 版本不高于当前版本
- **THEN** 不弹任何窗口与通知，应用正常运行

#### Scenario: 检查失败静默

- **WHEN** 应用启动时网络不可用或 GitHub 接口返回失败/限流
- **THEN** 系统静默结束本次检查，不弹错、不崩溃，应用正常运行

#### Scenario: 干活态出结果推迟弹窗

- **WHEN** 启动检查在任务执行中返回发现新版本
- **THEN** 不立即弹窗；任务结束后（回到空闲态）再弹确认框

### Requirement: 版本与资产判定

系统 SHALL 仅当满足以下全部条件时判定为新版本：GitHub 最新 release 的版本号高于当前运行时版本（版本号取自应用构建版本信息）；该 release 的资产中存在 .dmg 文件。版本号比较 SHALL 忽略 tag 的 v 前缀与 -RELEASE 后缀。版本相同、无 .dmg 资产、或资产列表不可解析时 SHALL 视为无新版本。

#### Scenario: 更高版本且有 dmg 资产

- **WHEN** 最新 release tag 为 v2.1.0（高于当前 2.0.7）且资产含 EncryptDog-2.1.0.dmg
- **THEN** 判定为新版本并进入弹窗确认流程

#### Scenario: 版本相同

- **WHEN** 最新 release tag 为 v2.0.7（与当前版本相同）
- **THEN** 判定为无新版本，静默结束

#### Scenario: 无 dmg 资产

- **WHEN** 最新 release 版本更高但资产中不含任何 .dmg 文件
- **THEN** 判定为无新版本，静默结束

### Requirement: 托盘手动检查

托盘菜单 SHALL 提供 "Check for updates" 项，用户点击 SHALL 触发与启动检查相同的检查流程。手动检查的结果 SHALL 始终即时反馈：发现新版本弹确认框；无新版本 SHALL 弹窗提示已是最新版本；检查失败（网络不可用、接口异常等）SHALL 通过系统通知提示检查失败并回到可再次检查状态。检查或下载进行中重复点击 SHALL 被忽略。

#### Scenario: 手动检查发现新版本

- **WHEN** 用户点击托盘菜单 Check for updates 且存在新版本
- **THEN** 弹出确认框询问是否下载

#### Scenario: 手动检查无新版本

- **WHEN** 用户点击托盘菜单 Check for updates 且不存在新版本
- **THEN** 系统弹窗提示当前已是最新版本，窗口隐藏时先恢复窗口保证弹窗可见

#### Scenario: 手动检查失败轻提示

- **WHEN** 用户点击托盘菜单 Check for updates 且检查因网络或接口原因失败
- **THEN** 系统弹出通知提示检查失败，托盘菜单回到 Check for updates 可再次检查，不崩溃

#### Scenario: 检查中重复触发被忽略

- **WHEN** 一次检查或下载正在进行中，用户再次点击 Check for updates
- **THEN** 不发起新请求、不弹窗，现有流程不受影响

### Requirement: 确认框语义与下载

确认框 SHALL 提供 Download 与 Later 两个选择：选择 Download 后系统 SHALL 在后台下载 .dmg 资产到本地缓存目录，下载过程 SHALL 不阻塞界面，进度 SHALL 展示在托盘菜单；选择 Later SHALL 关闭确认框且不下载，托盘菜单 SHALL 保留 "Update available {version}" 项可再次触发确认框。窗口隐藏时弹出确认框 SHALL 先恢复窗口保证可见。

#### Scenario: 确认下载后台进行

- **WHEN** 用户在确认框选择 Download
- **THEN** 确认框关闭，后台开始下载，加解密等界面功能正常可用

#### Scenario: Later 后可再次触发

- **WHEN** 用户在确认框选择 Later
- **THEN** 确认框关闭且不下载，托盘菜单出现 Update available {version} 项，点击该项重新弹出确认框

#### Scenario: 窗口隐藏时确认框可见

- **WHEN** 窗口隐藏（托盘态）且需要弹出更新确认框
- **THEN** 系统先恢复窗口再展示确认框

### Requirement: 下载完成挂载打开与缓存

下载完成后，系统 SHALL 自动挂载下载的 DMG 并打开 Finder 窗口展示其内容，SHALL 通过系统通知提示用户拖入 Applications 完成安装。下载的 DMG SHALL 保留在本地缓存目录，托盘菜单 SHALL 提供 "Open downloaded update" 项供用户重新挂载打开。下载失败 SHALL 通过系统通知轻提示，状态回到可重新检查。

#### Scenario: 下载完成自动挂载

- **WHEN** DMG 下载完成
- **THEN** 系统挂载该 DMG 并打开 Finder 窗口，弹出通知提示用户将应用拖入 Applications 完成更新

#### Scenario: 重新打开已下载的更新

- **WHEN** 用户关闭了挂载窗口后点击托盘菜单 Open downloaded update
- **THEN** 系统重新挂载并打开缓存目录中的 DMG

#### Scenario: 下载失败回到可检查

- **WHEN** 下载过程中断或失败
- **THEN** 系统弹出通知提示下载失败，托盘菜单回到 Check for updates 可再次检查，不崩溃

### Requirement: 更新能力不干扰既有功能

更新检查、弹窗、下载与挂载 SHALL NOT 改变加解密流程、终端模式、单例与退出语义等任何既有行为。终端模式 SHALL NOT 触发任何更新检查或网络请求。托盘不可用的降级场景下，弹窗确认与下载 SHALL 仍可用（仅托盘菜单与托盘通知缺失）。

#### Scenario: 终端模式不检查

- **WHEN** 用户以终端模式（不带 --gui）运行
- **THEN** 系统不发起任何更新相关网络请求，无任何更新提示

#### Scenario: 托盘降级下弹窗仍可用

- **WHEN** 托盘不可用（降级运行）且启动检查发现新版本
- **THEN** 确认框正常弹出，确认后下载与挂载流程正常完成

#### Scenario: 更新期间加解密正常

- **WHEN** 更新检查或下载进行中用户提交加解密任务
- **THEN** 任务执行与结果展示与本次变更前完全一致
