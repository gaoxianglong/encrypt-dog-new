## 1. Drop 协议修复

- [x] 1.1 重写 `DropFilePanel$DndListener.drop`:`acceptDrop()` 之前的 flavor 不支持分支保留 `rejectDrop()`;accept 之后取数、判空、`onFilesDropped` 回调与涟漪全部包进 try(成功置 `success=true`),`catch (Exception)` 中记录 WARN,`finally` 中调用 `dropComplete(success)`。验证:代码审查确认 `rejectDrop()` 仅出现在 accept 之前、`dropComplete` 位于 finally、try 内无异常可穿出 `drop()` 的路径
- [x] 1.2 为 `DropFilePanel` 添加 Lombok `@Slf4j`,失败时以 `log.warn` 记录异常与 `dtde.getCurrentDataFlavorsAsList()`(取数结果 null/空时同样记录)。验证:`mvn compile` 通过,无编译错误

## 2. 构建与回归验证

- [x] 2.1 执行 `mvn package` 构建打包。验证:BUILD SUCCESS,产物生成
- [x] 2.2 成功路径回归:以 `--gui` 启动,从 Finder 拖入文件与文件夹。验证:路径正常添加至列表、重复项不重复添加、放下涟漪动画正常、无任何异常输出
- [x] 2.3 失败路径验证:拖入不含文件数据的内容(如浏览器中的文本/图片)。验证:不打印 `InvalidDnDOperationException`、不添加任何路径、不弹错误提示,FILE 日志出现 WARN(含异常与可用 DataFlavor 列表)
- [x] 2.4 恢复能力验证:失败拖拽之后立即再次拖入真实文件。验证:列表正常添加,拖拽通道未失效,无需重启应用(对应原始"拖一次后整个 GUI 拖拽失效"的复现场景)
