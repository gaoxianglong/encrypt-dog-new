## 1. 粒子背景渲染优化

- [x] 1.1 基准测量:用 FrameBench 无头渲染当前 jar 的 ParticlePanel 120 帧,记录平均单帧耗时(优化前基线);另记录空闲 CPU 采样(供对比)
- [x] 1.2 `ParticlePanel`:背景渐变预渲染缓存(尺寸变化重建),paintBackground 改为 drawImage;验证 `mvn -q compile` 编译通过
- [x] 1.3 `ParticlePanel`:连线检测网格化(单元边长=LINK_DISTANCE,8 邻域,平方距离预判),连线结果与全对检测一致;验证 `mvn -q compile` 编译通过
- [x] 1.4 `ParticlePanel`:调色板(Color[64] 亮度阶梯 + Color[LINK_MAX_ALPHA+1] alpha 阶梯)与粒子精灵缓存(半径桶×亮度桶预渲染AA椭圆,逐帧贴绘),消除每帧对象分配与AA路径填充;网格桶复用固定数组;`LogoUtil` 增加图标缓存(paint路径不再每帧解码PNG);验证 `mvn -q compile` 编译通过
- [x] 1.5 无头断言:优化后 FrameBench 平均单帧耗时较基线下降(7.93→5.58ms,-30%);新旧渲染像素级对比一致(固定粒子坐标+闪烁周期对齐,±2px窗口感知对比硬差异0.0017%);PanelVerify7 回归通过
- [x] 1.6 GUI 实测:动画效果(渐变/粒子漂浮闪烁/星座连线/鼠标微扰/聚集爆发)与交互响应与优化前一致(60fps 不变);空闲 CPU 低于优化前(基线 117.9%/用户实测约 140%,固定管线开销经用户确认保留)

## 2. 回归验证

- [x] 2.1 终端模式回归:不带 `--gui` 执行一次加密与一次解密;验证终端交互与变更前一致
