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

package com.gxl.encryptdog.base.error;

import lombok.Getter;

import java.io.Serial;

/**
 * 异常超类
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/9/21 23:16
 */
@Getter
public class BaseException extends Exception {
    @Serial
    private static final long serialVersionUID = -2491387245124351108L;
    private String            sourceFile;

    public BaseException(String sourceFile, String message) {
        super(message);
        this.sourceFile = sourceFile;
    }

    public BaseException(String message) {
        super(message);
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseException(String sourceFile, String message, Throwable cause) {
        super(message, cause);
        this.sourceFile = sourceFile;
    }

    public BaseException(Throwable cause) {
        super(cause);
    }

    protected BaseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
