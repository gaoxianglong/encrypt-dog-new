package com.gxl.encryptdog.gui.infrastructure.update;

/**
 * 更新链路测试的版本构造辅助:测试用例自适应构建版本,
 * 不依赖pom中写死的版本号(支持降版本实验等场景下跑全量测试)
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/9/4 16:30
 */
final class UpdateTestVersions {
    private UpdateTestVersions() {
    }

    /**
     * 构造严格高于当前构建版本的测试tag(首段+1,如2.0.4->3.0.4),
     * 当前版本不可解析时退化为2.1.0
     * @return 测试tag(不含v前缀)
     */
    static String nextMajor() {
        var current = UpdateChecker.currentVersion();
        if (null == current) {
            return "2.1.0";
        }
        try {
            var segments = VersionComparator.normalize(current).split("\\.");
            var first = Integer.parseInt(segments[0]) + 1;
            var builder = new StringBuilder(String.valueOf(first));
            for (var i = 1; i < segments.length; i++) {
                builder.append('.').append(segments[i]);
            }
            return builder.toString();
        } catch (Throwable e) {
            return "2.1.0";
        }
    }
}
