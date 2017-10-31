package com.dang.api.gateway.common;

/**
 * Description:
 *
 * @Author dangfugui dangfugui@163.com
 * @Date Create in 2017/10/31
 */
public class ApiException extends RuntimeException {
    public ApiException(String message){
        super(message);
    }
}
