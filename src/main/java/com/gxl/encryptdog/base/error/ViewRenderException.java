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

import java.io.Serial;

/**
 * 视图渲染异常类
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/2 11:49
 */
public class ViewRenderException extends BaseException {
    @Serial
    private static final long serialVersionUID = 4400833406287500226L;

    public ViewRenderException(String sourceFile, String message) {
        super(sourceFile, message);
    }

    public ViewRenderException(String message) {
        super(message);
    }

    public ViewRenderException(String message, Throwable cause) {
        super(message, cause);
    }

    public ViewRenderException(String sourceFile, String message, Throwable cause) {
        super(sourceFile, message, cause);
    }

    public ViewRenderException(Throwable cause) {
        super(cause);
    }

    protected ViewRenderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
