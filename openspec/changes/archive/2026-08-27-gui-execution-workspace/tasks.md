## 1. 数据层

- [x] 1.1 `OperationProgressDTO.FileProgress` 增加 `targetFile`、`targetFileSize`;`EncryptCoreFacade` 提取层回填(目标文件解析阶段即有值,处理后大小/错误原因完成时回填);验证 `mvn -q compile` 编译通过,无头断言快照字段正确

## 2. 执行表格页

- [x] 2.1 重写 `ProgressPanel` 为三层布局:左上角返回图标按钮(home-fill.png,替代文字 Back)+ 操作标题(Processing…→Operation completed,始终白色、标题容器内垂直居中、左侧间距保持、字号与首页一致)+ 四个统计小窗(高度紧凑 58、横向等宽铺满)+ 9 列自绘列表(半透明圆角行条、无网格、数值列居中、表头带主题背景色、列宽等比缩放)+ 紫色滚动条(补 FlatLaf ScrollBar.thumbColor 键);验证编译通过
- [x] 2.2 `refresh(OperationProgressDTO)` 驱动行数据与统计小窗实时更新,`finish(OperationResultDTO)` 合并最终结果并播放完成提示音(音效由 core FinishedListener 播放,无需迁移);begin 先清空行数据(修复重复行 bug);失败行进度条停留在失败时刻进度不强制 100%;Result 列以状态图标替代文字(成功对勾/失败错误小图标,悬停展示失败原因);表头文字全部居中;行按状态排序(进行中>等待中>已完成,序号随排序重排);rowsPanel 显式 setPreferredSize 修复滚动失效(超过 10 行可下滑);验证无头断言:执行中快照→行状态/统计值正确,finish 后处理后大小/结果回填且失败行进度保持原值,排序正确,连续两次 begin 行数不翻倍
- [x] 2.3 进度列渲染:紫色渐变填充(ACCENT→ACCENT_BRIGHT)+ 半透明白轨道,无颗粒装饰;验证编译通过

## 3. 窗口形态切换

- [x] 3.1 `EncryptDogFrame` 新增 `switchWindowShape(boolean toWide)`:760×760 ↔ 1200×750 一次性直接切换(无逐帧动画),保持屏幕居中、圆角与分层容器同步;验证编译通过
- [x] 3.2 提交进入执行页时直接切换宽屏(与内容淡入并行),Back 直接切回方形;表单页保持 760×760;验证 GUI 实测切换无卡顿、窗口居中不漂移

## 4. 完成停留与清理

- [x] 4.1 `finishOperation`/`operationFailed` 改为停留表格页(合并结果/异常信息),删除 `ResultPanel` 类、粒子爆发调用、失败抖动路径及相关常量;验证编译通过
- [x] 4.2 GUI 实测:提交→拉伸→表格实时刷新→完成停留(音效+汇总)→Back 反向缩回→表单已重置;部分失败行红/绿颜色正确,异常场景停留表格页不抖动

## 5. 回归验证

- [x] 5.1 终端模式回归:不带 `--gui` 执行一次加密与一次解密;验证终端交互与变更前一致
