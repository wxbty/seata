package io.seata.integration.http;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : sp0108
 * @Description: TODO
 * @date Date : 2020年03月17日 15:33
 */
public class ServletMappingConfig {

    public static List<ServletMapping> servletMappingList =new ArrayList<>();
    //制定哪个URL交给哪个servlet来处理
    static{
        servletMappingList.add(new ServletMapping("testPost","/testPost","io.seata.integration.http.IndexController"));
    }
}
