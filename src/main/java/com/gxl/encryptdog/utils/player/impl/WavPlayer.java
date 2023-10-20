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

package com.gxl.encryptdog.utils.player.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Objects;

import javax.sound.sampled.*;

import com.gxl.encryptdog.base.error.ResourceException;
import com.gxl.encryptdog.utils.Utils;
import com.gxl.encryptdog.utils.player.Player;

/**
 * WAV播放器实现
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/9/26 14:53
 */
public class WavPlayer implements Player {

    /**
     * 加解密操作完成后播放提示音
     * @param path
     * @throws ResourceException
     */
    @Override
    public void play(String path) throws ResourceException {
        AudioInputStream audioStream = null;
        SourceDataLine sourceDataLine = null;
        try {
            // 设置缓冲区大小
            var buffer = new byte[DEFAULT_BUF_SIZE];
            // 获取音频输入流
            audioStream = AudioSystem.getAudioInputStream(new BufferedInputStream(Utils.getResourceStream(path)));
            // 获取音频的编码格式
            var format = audioStream.getFormat();
            var dataLineInfo = new DataLine.Info(SourceDataLine.class, format, AudioSystem.NOT_SPECIFIED);
            sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            sourceDataLine.open(format);
            sourceDataLine.start();
            int value;
            while ((value = audioStream.read(buffer)) != -1) {
                //播放音频
                sourceDataLine.write(buffer, 0, value);
            }

        } catch (Throwable e) {
            throw new ResourceException("Audio resource loading error", e);
        } finally {
            // 相关资源清理
            close(audioStream, sourceDataLine);
        }
    }

    /**
     * 相关资源清理
     * @param audioStream
     * @param sourceDataLine
     * @throws IOException
     */
    private void close(AudioInputStream audioStream, SourceDataLine sourceDataLine) {
        if (Objects.nonNull(sourceDataLine) && sourceDataLine.isActive()) {
            sourceDataLine.drain();
            sourceDataLine.close();
        }
        if (Objects.nonNull(audioStream)) {
            try {
                audioStream.close();
            } catch (IOException e) {
                //...
            }
        }
    }
}
