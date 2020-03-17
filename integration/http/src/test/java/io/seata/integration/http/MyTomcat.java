package io.seata.integration.http;

import com.alibaba.fastjson.JSONObject;
import io.seata.common.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : wangxb
 */
public class MyTomcat {

    private Map<String, String> urlServletMap = new HashMap<String, String>();


    public void start() {
//        初始化URL与对应处理的servlet的关系
        initServletMapping();

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(8081);
            System.out.println("MyTomcat is start...");

//            while (true) {

                Socket socket = serverSocket.accept();
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();

                MyRequest myRequest = new MyRequest(inputStream);
                MyResponse myResponse = new MyResponse(outputStream);

//                请求分发
                dispatch(myRequest, myResponse);
                socket.close();
//            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != serverSocket) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initServletMapping() {
        for (ServletMapping servletMapping : ServletMappingConfig.servletMappingList) {
            urlServletMap.put(servletMapping.getUrl(), servletMapping.getClazz());
        }
    }

    public void dispatch(MyRequest myRequest, MyResponse myResponse) {
        String clazz = urlServletMap.get(myRequest.getPath());

        //反射
        try {
            Class<IndexController> myServletClass = (Class<IndexController>) Class.forName(clazz);
            IndexController myServlet = myServletClass.newInstance();
            IndexController.Person person = boxing(myRequest);

            //mock request intercepter
            TransactionPropagationIntercepter intercepter = new TransactionPropagationIntercepter();
            HttpServletRequest request = new MockHttpServletRequest(myRequest);
            intercepter.preHandle(request,null,null);
            myResponse.write(myServlet.testPost(person));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private IndexController.Person boxing(MyRequest myRequest) {

        Map<String, Object> params = getUrlParams(myRequest.getUrl());
        return JSONObject.parseObject(JSONObject.toJSONString(params), IndexController.Person.class);

    }

    public static void main(String[] args) {
        new MyTomcat().start();
//        String url = "http://127.0.0.1:8081/index?name=zhangsan&&age=15";
//        System.out.println(getUrlParams(url));
    }

    public static Map<String, Object> getUrlParams(String param) {
        Map<String, Object> map = new HashMap<String, Object>(0);
        if (StringUtils.isBlank(param)) {
            return map;
        }
        String[] urlPath = param.split("\\?");
        if (urlPath.length < 2) {
            return map;
        }

        String[] params = urlPath[1].split("&");
        for (int i = 0; i < params.length; i++) {
            String[] p = params[i].split("=");
            if (p.length == 2) {
                map.put(p[0], p[1]);
            }
        }
        return map;

    }
}