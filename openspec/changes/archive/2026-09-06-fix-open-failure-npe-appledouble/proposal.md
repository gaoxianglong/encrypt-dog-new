## Why

解密时抛出 `NullPointerException: encryptContext is null`（`AbstractOperationTemplate.execute` 的 try-with-resources 打开流失败后，失败路径对 null 上下文调用 `onFailure`）。日志实证根因：exFAT 卷上删除主文件会连带删除其 `._` AppleDouble 伴生元数据文件，加/解密批次（带"删除源文件"）执行中这些 `._` 文件在"解析时存在、执行时消失"，其任务打开流时 FileNotFoundException → 失败路径 NPE 二次崩溃。后果：原始错误被掩盖、失败事件链不触发导致该行永远卡在 RUNNING、成功/失败计数漏算。同样的机制昨天加密批次（39 个 `._` 文件失败）与今天解密批次均复现。另，`._` 文件本就是 4KB 的 Finder 元数据（Finder 不可见），加/解密它们没有用户价值，却会混进操作列表制造垃圾产物与上述竞态，应在解析阶段过滤。

## What Changes

- 操作前置失败（打开源/目标文件失败，如文件不存在、权限变化、卷掉线）不再产生 NPE：补建最小执行上下文并走正常失败事件链，该文件行到达 FAILED 终态、失败计数正确、GUI 结果列显示失败图标且悬停可见原始错误原因（如"文件不存在"）。
- 解析阶段忽略 AppleDouble 元数据文件：文件名以 `._` 前缀开头的文件不进入操作列表（目录递归与显式指定均适用，加密/解密一致，终端与 GUI 一致），杜绝其制造的 parse/execute 竞态与垃圾产物。
- 附带修正：目标文件不存在时结果事件的目标大小显示 `-` 而非 `-0.00MB`。

## Capabilities

### New Capabilities

- `file-operation`: 文件操作管线的失败语义与解析过滤——前置失败必达失败终态且错误透明；AppleDouble 元数据文件不进入操作。

### Modified Capabilities

（无）

## Impact

- 代码：`core/operation/AbstractOperationTemplate.java`（execute 失败路径补建上下文）、`core/operation/EncryptContext.java`（无改动，仅复用）、`core/event/listener/impl/ResultListener.java`（无改动）、`core/parse/impl/FileNameParser.java`（`._` 过滤）、`core/state/impl/EncryptProcessStateImpl.java`（`buildResultEvent` 目标大小 -1 显示 `-`）。GUI 侧无改动（失败行展示沿用既有「完成回填」语义）。
- 行为：失败文件必现 FAILED 行（GUI 错误图标 + 悬停原因；终端 Dashboard 失败计数与状态一致）；`._` 文件不再出现在源文件确认列表（终端）与执行列表（GUI）。终端与 GUI 的 `._` 过滤行为一致（共享解析器）。
- 兼容性：显式指定 `._` 开头的真实文件也将被跳过（macOS 惯例，Finder 中本就不可见）；若过滤后无可用文件，维持既有 "No files available" 提示。
- 依赖：无新增依赖。
- 验证：复现环境（exFAT 卷 + `._` 伴生文件 + 删除源文件）下加/解密各一轮；单元测试覆盖失败路径与解析过滤。
