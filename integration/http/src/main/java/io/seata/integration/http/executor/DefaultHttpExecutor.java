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
package io.seata.integration.http.executor;

import com.google.common.collect.Maps;
import io.seata.integration.http.context.BaseHttpContext;
import io.seata.integration.http.context.GetContextBase;
import io.seata.integration.http.context.PostContextBase;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import java.util.Map;

/**
 * Default http executor.
 *
 * @author wangxb
 */
public class DefaultHttpExecutor extends AbstractHttpExecutor {

    private static DefaultHttpExecutor instance = new DefaultHttpExecutor();

    private DefaultHttpExecutor() {
    }

    public static DefaultHttpExecutor getInstance() {
        return instance;
    }


    @Override
    protected void buildClientEntity(CloseableHttpClient httpClient, BaseHttpContext httpContext) {

    }

    @Override
    public Map<String, String> buildGetCustomizeHeaders(Map<String, String> headers, GetContextBase getContext) {

        return headers;
    }


    @Override
    protected <T> Map<String, String> buildPostCustomizeHeaders(PostContextBase<T> postContext) {
        return Maps.newHashMap();
    }


    @Override
    public StringEntity buildPostEntity(StringEntity entity, PostContextBase postContext) {
        return entity;
    }

    @Override
    public <K> K convertResult(HttpResponse response, Class<K> clazz) {


        if (clazz == HttpResponse.class) {
            return (K) response;
        }
        return null;
    }

}
