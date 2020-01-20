/*
 *  Copyright 1999-2019 Seata.io Group.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.seata.integration.http.context;

/**
 * PostRequest context.
 *
 * @author wangxb
 */
public class PostContext<T> extends BaseHttpContext {

    private T param;

    public PostContext(String host, String path) {
        super(host, path);
    }


    public T getParam() {
        return param;
    }

    public void setParam(T param) {
        this.param = param;
    }

    @Override
    public String toString() {
        return "PostContext{" +
                "param=" + param +
                ", host='" + host + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
