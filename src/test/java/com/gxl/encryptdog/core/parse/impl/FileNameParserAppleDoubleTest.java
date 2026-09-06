/*
 *
 *  * Copyright 2019-2119 gao_xianglong@sina.com
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.gxl.encryptdog.core.parse.impl;

import com.gxl.encryptdog.base.error.ParseException;
import com.gxl.encryptdog.core.shell.request.ConsoleRequest;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * AppleDouble元数据文件解析过滤测试(任务2.2)
 * <p>
 * 目录递归与显式路径下._前缀文件均被忽略;非._前缀的点开头文件(.env)不受影响;
 * 加密与解密模式行为一致;全部为._文件时提示无可用文件。
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/9/6 14:30
 */
public class FileNameParserAppleDoubleTest {
    @Test
    public void appleDoubleFilesAreFiltered() throws Exception {
        var dir = Files.createTempDirectory("encryptdog-parse").toFile();
        // 顶层:a.txt(保留) ._a.txt(过滤) ._meta(过滤) .env(保留,非._前缀)
        writeFile(new File(dir, "a.txt"));
        writeFile(new File(dir, "._a.txt"));
        writeFile(new File(dir, "._meta"));
        writeFile(new File(dir, ".env"));
        // 子目录:b.txt(保留) ._b.txt(过滤)
        var sub = new File(dir, "sub");
        sub.mkdirs();
        writeFile(new File(sub, "b.txt"));
        writeFile(new File(sub, "._b.txt"));

        // 加密模式:目录递归解析,仅a.txt/.env/b.txt
        var encryptRequest = new ConsoleRequest();
        encryptRequest.setSourceFiles(new ArrayList<>() { { add(dir.getPath()); } });
        encryptRequest.setEncrypt(true);
        var encryptResult = new FileNameParser().parse(encryptRequest);
        assertEquals("加密模式文件列表数量不对", 3, encryptResult.size());
        assertTrue("._前缀文件混入加密列表", encryptResult.stream().noneMatch(p -> new File(p).getName().startsWith("._")));

        // 解密模式:同上,且后缀校验只保留.dog(过滤与后缀校验互不干扰)
        writeFile(new File(dir, "normal.dog"));
        writeFile(new File(dir, "._x.dog"));
        var decryptRequest = new ConsoleRequest();
        decryptRequest.setSourceFiles(new ArrayList<>() { { add(dir.getPath()); } });
        decryptRequest.setEncrypt(false);
        var decryptResult = new FileNameParser().parse(decryptRequest);
        assertEquals("解密模式文件列表数量不对", 1, decryptResult.size());
        assertEquals("normal.dog", new File(decryptResult.get(0)).getName());

        // 显式指定._文件:被忽略,过滤后无可用文件时报错
        var allDir = Files.createTempDirectory("encryptdog-parse-all").toFile();
        var onlyMeta = new File(allDir, "._only.txt");
        writeFile(onlyMeta);
        var explicitRequest = new ConsoleRequest();
        explicitRequest.setSourceFiles(new ArrayList<>() { { add(onlyMeta.getPath()); } });
        explicitRequest.setEncrypt(true);
        try {
            new FileNameParser().parse(explicitRequest);
            fail("全部被过滤时应报无可用文件");
        } catch (ParseException expected) {
            // 无可用文件
        }
    }

    /**
     * 写入带内容的文件(容量校验要求非空)
     * @param file 目标文件
     * @throws IOException
     */
    private static void writeFile(File file) throws IOException {
        Files.write(file.toPath(), "hello".getBytes());
    }
}
