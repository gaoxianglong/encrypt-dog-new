# Tasks: 更新 README 以覆盖当前 GUI 功能与版本

## 1. 版本引用统一为 2.0.4

- [x] 1.1 顶部徽章 version-2.0.2--RELEASE 更新为 2.0.4-RELEASE；验证：徽章与实际 pom 版本一致（grep 确认）
- [x] 1.2 install 段：`git clone` 段与 wget 段中的 `dog-2.0.2.jar` 更新为 `dog-2.0.4.jar`，wget 链接 v2.0.2 → v2.0.4；验证：构建产物 dog-2.0.4.jar 与文档一致
- [x] 1.3 use 段输出横幅 `version: 2.0.3-RELEASE` 更新为 `version: 2.0.4-RELEASE`，gui mode 示例中的 `dog-2.0.3.jar` 更新为 `dog-2.0.4.jar`；验证：grep 全文无 2.0.2/2.0.3 残留

## 2. GUI 使用指南按现状扩充

- [x] 2.1 Source files 条目补充拖拽交互：拖拽文件/文件夹到拖拽区添加（呼吸边框/涟漪/淡入、重复项过滤、与按钮等价）；验证：与主 spec「拖拽添加源文件」一致
- [x] 2.2 Mode 条目补充解密模式隐藏项：Decrypt 下确认密钥行与「Local machine only」均隐藏；验证：与主 spec「模式切换布局联动」「GUI 参数收集」一致
- [x] 2.3 Algorithm 条目补充 XOR 安全提示：加密模式选中 XOR 时下拉框旁显示 "Use with caution"；验证：与主 spec「XOR 算法安全提示」一致
- [x] 2.4 新增校验说明：字段内联错误提示（出错字段内、无字段错误在主按钮上方、约 2 秒淡出）；验证：与主 spec「错误提示内联展示」一致
- [x] 2.5 Options 条目细化：删除源文件执行前二次确认弹窗（主题化圆角弹窗）、Local machine only 仅加密模式展示；验证：与主 spec「删除源文件确认」「主题化弹窗圆角渲染」一致
- [x] 2.6 执行流程描述更新：提交直接进入执行（仅勾选删除时先确认弹窗）、宽屏执行形态、统计小窗与 9 列表格实时刷新、完成后停留+提示音+异常红字顶部、返回箭头重置表单；验证：与主 spec「执行窗口形态切换」「执行表格视图」「执行完成停留」「返回表单重置」一致（并修正旧文「粒子爆发/卡片抖动/文件列表确认弹窗」等过时描述）

## 3. CLI 使用补充

- [x] 3.1 补充 `-k` 交互式输入说明与最小示例（加密输两次、解密输一次、Y/N 确认）；验证：本会话早前已按同样命令形式实际执行加密→解密回环成功（产物 .dog、解密 diff 一致）
- [x] 3.2 核对 use 段 usage 文本与 `dog -h` 实际输出一致：逐字节比对 1302 字符完全一致（并移除旧块中过时的 Missing required options 行）

## 4. 验收修订（GUI 为主 + 内存参数统一）

- [x] 4.1 全部 JVM 内存参数统一为 `-Xms1g -Xmx1g -Xmn384m`（install 两处 alias、gui 启动示例）；验证：grep 全文无 512m 残留
- [x] 4.2 文档结构 GUI 为主：`gui mode` 提升为安装后首个使用章节、终端用法降级为 `terminal mode` 章节（含 -h 输出与 -k 示例整体下移）；验证：章节顺序 install → gui mode → terminal mode → highest security → file structure
- [x] 4.3 规划工件同步：proposal 补充 JVM 统一与 GUI 为主两项范围说明；验证：工件与 README 实际内容一致

## 5. 验收

- [x] 5.1 全文检查：无残留 2.0.2/2.0.3 版本号与过期 jar 名（grep 通过）；logo 图片、「highest security」「file structure」章节内容未变；git diff 仅 README.md；另移除了 highest security 段尾的孤儿代码围栏，修复 file structure 表格被渲染为代码块的问题
- [x] 5.2 按文档启动 GUI（`java -Xms1g -Xmx1g -Xmn384m -jar dog-2.0.4.jar --gui`）抽查：拖拽添加、解密模式隐藏项、XOR 提示、执行表格与返回按钮均与文档一致；验证：界面行为与文档描述相符
