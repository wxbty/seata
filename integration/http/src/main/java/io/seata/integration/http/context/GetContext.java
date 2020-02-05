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

import java.util.Map;

/**
 * GetRequest context.
 *
 * @author wangxb
 */
public class GetContext extends BaseHttpContext {

    private Map<String, String> paramMap;

    private GetContext(Builder builder) {
        super(builder.host, builder.path);
        this.paramMap = builder.paramMap;
    }


    public Map<String, String> getParamMap() {
        return paramMap;
    }

    public static class Builder {
        private final String host;
        private final String path;
        private Map<String, String> paramMap;

        public Builder(String host, String path) {
            this.host = host;
            this.path = path;
        }

        public Builder param(Map<String, String> paramMap) {
            if (paramMap != null) {
                this.paramMap = paramMap;
            }
            return this;
        }

        public GetContext build() {
            return new GetContext(this);
        }
    }

    @Override
    public String toString() {
        return "GetContext{" +
                "host='" + host + '\'' +
                ", path='" + path + '\'' +
                ", paramMap=" + paramMap +
                '}';
    }

}
