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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Maps;
import io.seata.core.context.RootContext;
import io.seata.integration.http.HttpUriRequestFactory;
import io.seata.integration.http.context.BaseHttpContext;
import io.seata.integration.http.context.GetContext;
import io.seata.integration.http.context.PostContext;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Abstract http executor.
 *
 * @author wangxb
 */
public abstract class AbstractHttpExecutor implements HttpExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractHttpExecutor.class);


    @Override
    public <K> K executeGet(String host, String path, Class<K> returnType) throws IOException {

        return executeGet(host, path, null, returnType);
    }

    @Override
    public <K> K executeGet(String host, String path, Map<String, String> paramMap, Class<K> returnType) throws IOException {

        Args.notNull(returnType, "returnType");
        Args.notNull(host, "host");
        Args.notNull(path, "path");

        GetContext getContext = new GetContext.Builder(host, path).param(paramMap).build();

        CloseableHttpClient httpClient = initClientInstance(getContext);
        return wrapHttpExecute(returnType, httpClient, HttpUriRequestFactory.createHttpUriRequest("get", initGetUrl(getContext)), buildGetCustomizeHeaders(setGlobalTransactionXidToHead(), getContext));
    }

    @Override
    public <T, K> K executePost(String host, String path, T param, Class<K> returnType) throws IOException {

        Args.notNull(returnType, "returnType");
        Args.notNull(host, "host");
        Args.notNull(path, "path");

        PostContext<T> postContext = new PostContext<>(host, path);
        postContext.setParam(param);

        /**
         * Set httpClient properties.
         */
        CloseableHttpClient httpClient = initClientInstance(postContext);
        HttpPost httpPost = (HttpPost) HttpUriRequestFactory.createHttpUriRequest("put", host + path);
        /**
         * Set body entity.
         */
        StringEntity entity = getStringEntity(postContext);
        if (entity != null) {
            httpPost.setEntity(entity);
        }
        Map<String, String> headers = buildPostCustomizeHeaders(postContext);
        return wrapHttpExecute(returnType, httpClient, httpPost, headers);
    }

    private <T> StringEntity getStringEntity(PostContext<T> postContext) {

        T param = postContext.getParam();
        StringEntity entity = null;
        if (param != null) {
            String content;
            if (param instanceof String) {
                String sParam = (String) param;
                JSONObject jsonObject = null;
                try {
                    jsonObject = JSON.parseObject(sParam);
                    content = jsonObject.toJSONString();
                } catch (JSONException e) {
                    //Interface provider process parse exception
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn(e.getMessage());
                    }
                    content = sParam;
                }

            } else {
                content = JSON.toJSONString(param);
            }
            entity = new StringEntity(content, ContentType.APPLICATION_JSON);
        }

        return buildPostEntity(entity, postContext);
    }


    private Map<String, String> setGlobalTransactionXidToHead() {
        Map<String, String> headers = Maps.newHashMap();
        String xid = RootContext.getXID();
        if (xid != null) {
            headers.put(RootContext.KEY_XID, xid);
        }
        return headers;
    }

    private CloseableHttpClient initClientInstance(BaseHttpContext context) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        buildClientEntity(httpClient, context);
        return httpClient;
    }

    protected abstract void buildClientEntity(CloseableHttpClient httpClient, BaseHttpContext httpContext);


    private <K> K wrapHttpExecute(Class<K> returnType, CloseableHttpClient httpClient, HttpUriRequest httpUriRequest, Map<String, String> headers) throws IOException {
        CloseableHttpResponse response;

        if (!headers.isEmpty()) {
            headers.keySet().forEach(key -> httpUriRequest.addHeader(key, headers.get(key)));
        }
        response = httpClient.execute(httpUriRequest);
        int statusCode = response.getStatusLine().getStatusCode();
        /** 2xx is success. */
        if (statusCode < HttpStatus.SC_OK || statusCode > HttpStatus.SC_MULTI_STATUS) {
            throw new RuntimeException("Failed to invoke the http method "
                    + httpUriRequest.getURI() + " in the service "
                    + ". return status by: " + response.getStatusLine().getStatusCode());
        }

        return convertResult(response, returnType);
    }

    protected abstract Map<String, String> buildGetCustomizeHeaders(Map<String, String> headers, GetContext getContext);

    private String initGetUrl(GetContext getContext) {

        String host = getContext.getHost();
        String path = getContext.getPath();

        if (getContext.getParamMap() == null || getContext.getParamMap().isEmpty()) {
            return host + path;
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(host + path);

        Map<String, String> trimParamMap = trim(getContext.getParamMap());
        trimParamMap.entrySet().stream().forEach((o) -> {
            builder.queryParam(o.getKey(), new Object[]{o.getValue()});
        });

        return builder.build().toUriString();

    }

    /*
     * Remove Null val
     * */
    private static Map<String, String> trim(Map<String, String> param) {
        Map<String, String> toParamMap = Maps.newHashMap();
        param.keySet().forEach((key) -> {
            if (param.get(key) != null && !StringUtils.isEmpty(param.get(key))) {
                toParamMap.put(key, param.get(key));
            }

        });
        return toParamMap;
    }


    protected abstract <T> Map<String, String> buildPostCustomizeHeaders(PostContext<T> postContext);

    protected abstract <T> StringEntity buildPostEntity(StringEntity entity, PostContext<T> postContext);

    protected abstract <K> K convertResult(HttpResponse response, Class<K> clazz);


    public static Map<String, String> convertParamOfBean(Object sourceParam) {
        return convert(JSON.parseObject(JSON.toJSONString(sourceParam, SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteMapNullValue), Map.class));
    }

    public static <T> Map<String, String> convertParamOfJsonString(String jsonstr, Class<T> returnType) {
        return convertParamOfBean(JSON.parseObject(jsonstr, returnType));
    }

    public static Map<String, String> convert(Map<String, Object> param) {
        return param.keySet().stream().filter(key -> param.get(key) != null && param.get(key) != null).collect(Collectors.toMap(key -> key, key -> param.get(key).toString()));
    }
}
