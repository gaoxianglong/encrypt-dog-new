## 1. 前置失败必达失败终态

- [x] 1.1 修改 `AbstractOperationTemplate.execute()` 的 catch 块：`encryptContext == null` 时补建最小上下文（resultContext/operationVO/defaultCapacity/sourceFileCapacity）、写入原始错误消息到该文件的 errorMsg、先迁移 WAITING→RUNNING 再调用 `onFailure`。验证：编译通过。
- [x] 1.2 编写单元测试：构造"解析后消失"的文件（先注册 viewState 再删除文件），执行任务后断言：无异常抛出、该文件 viewState 状态为 FINISHED、结果为 FAILED、失败计数 +1、errorMsg 为原始错误消息。验证：测试通过。
- [x] 1.3 真实场景回归：exFAT 卷上布置 `主文件 + ._伴生文件`，带"删除源文件"执行加密：全程无 NPE、无 `._` 垃圾产物、成功/失败计数正确（过滤生效后 `._` 不进入执行；前置失败兜底机制由 1.2 单测覆盖）。验证：无未捕获异常、计数正确。

## 2. AppleDouble 解析过滤

- [x] 2.1 修改 `FileNameParser`：显式文件分支与目录递归分支均跳过 basename 以 `._` 开头的文件。验证：编译通过。
- [x] 2.2 编写单元测试：临时目录构造 `a.txt`、`._a.txt`、`._meta`、子目录中的 `._b.txt` 与 `b.txt`，加密与解密两种模式解析后断言列表仅含正常文件；全部为 `._` 文件时断言抛出无可用文件异常。验证：测试通过。
- [x] 2.3 终端回归：终端模式对含 `._` 文件的目录执行一次加密，确认源文件确认列表不含 `._` 文件、无 `.dog` 垃圾产物。验证：确认列表与产物正确。

## 3. 结果大小显示修正与收尾

- [x] 3.1 修改 `ResultEvent` 构建处（`EncryptProcessStateImpl.buildResultEvent`）：目标容量 < 0 时目标大小取 "-" 而非 capacityFormat(-1)。验证：编译通过。
- [x] 3.2 运行全量构建与测试（`mvn test`），确认无回归。验证：全部测试通过。
- [x] 3.3 执行 `openspec validate --changes 2026-09-06-fix-open-failure-npe-appledouble`。验证：validate 通过。
