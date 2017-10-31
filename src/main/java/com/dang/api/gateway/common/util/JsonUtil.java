package com.dang.api.gateway.common.util;

import java.util.Map;

import com.alibaba.fastjson.JSON;

/**
 * Description:
 *
 * @Author dangfugui dangfugui@163.com
 * @Date Create in 2017/10/31
 */
public class JsonUtil {

    public static Map<String,Object> toMap(String paramsJson) {
       return JsonUtil.toMap(paramsJson);
    }

    public static String toJson(Object object){
        return JSON.toJSONString(object);
    }
}
