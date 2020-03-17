package io.seata.integration.http;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author : sp0108
 * @Description: TODO
 * @date Date : 2020年03月17日 15:31
 */
public class MyRequest {
    private String url;

    public String getParam() {
        return param;
    }


    public String getPath() {
        return path;
    }

    private String param;
    private String path;

    public String getXid() {
        return xid;
    }

    private String xid;

    public MyRequest(InputStream inputStream) throws IOException {
        String httpRequest = "";
        byte[] httpRequestBytes = new byte[2048];
        int length = 0;
        if ((length = inputStream.read(httpRequestBytes)) > 0) {
            httpRequest = new String(httpRequestBytes, 0, length);
        }

        String httpHead = httpRequest.split("\n")[0];
        url = httpHead.split("\\s")[1];
        xid = httpRequest.split("\\n")[1];
        if (xid.startsWith("Xid:")) {
            xid = xid.split("Xid:")[1].trim();
        }
        System.out.println("xid=" + xid);
        path = url.split("\\?")[0];
        if (url.contains("\\?")) {
            param = url.split("\\?")[1];
        }
        System.out.println(this);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }



}
