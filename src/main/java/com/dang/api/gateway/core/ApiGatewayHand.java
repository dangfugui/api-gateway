package com.dang.api.gateway.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dang.api.gateway.common.ApiException;
import com.dang.api.gateway.common.util.JsonUtil;

/**
 * Description:
 *
 * @Author dangfugui  dangfugui@163.cm
 * @Date Create in 2017/10/31
 */
@Component
public class ApiGatewayHand implements InitializingBean,ApplicationContextAware{

    private Logger logger = LoggerFactory.getLogger(ApiGatewayHand.class);
    private ApiStore apiStore  = null; // TODO
    private ParameterNameDiscoverer parameteUtil;
    private static String METHOD = "method";
    private static String PARAMS = "params";

    public ApiGatewayHand(){
        parameteUtil = new LocalVariableTableParameterNameDiscoverer(); // spring 工具 能拿到method 中的参数名称
    }

    public void handle(HttpServletRequest request, HttpServletResponse response) {
        String params = request.getParameter(PARAMS);
        String method = request.getParameter(METHOD);
        Object result ;
        ApiStore.ApiRunable apiRun = null;
        try{
            apiRun = sysParamsValdate(request);
            logger.info(String.format("请求接口={%s} 参数={%s}",method,params));
            Object[] args = buildParams(apiRun,params,request,response);
            result = apiRun.run(args);
        } catch (ApiException e) {
            response.setStatus(500);
            logger.error(String.format("调用接口={%s}异常  参数={%s}",method,params),e);
            result = handleError(e);
        } catch (InvocationTargetException e) {
            response.setStatus(500);
            logger.error(String.format("调用接口={%s}异常  参数={%s}",method,params),e.getTargetException());
            result = handleError(e.getTargetException());
        }catch (Exception e){
            response.setStatus(500);
            logger.error("其他异常",e);
            result = handleError(e);
        }
        // 统一返回结果
        returnResult(result, response);
    }

    private void returnResult(Object result, HttpServletResponse response) {
        String resultJson = JsonUtil.toJson(result);
        try {
            response.addHeader("Content-Type","application/json;charset=UTF-8");
            response.getOutputStream().write(resultJson.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String , Object> handleError(Throwable throwable ) {
        String code = "";
        String message = "";
        if(throwable instanceof ApiException){
            code = "001";
            message = throwable.getMessage();
        }else {
            code = "500";
            message = throwable.getMessage();
        }
        Map<String , Object> result = new HashMap<>();
        result.put("code",code);
        result.put("message",message);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(outputStream);
        throwable.printStackTrace(stream);
        result.put("stack",outputStream.toString());
        return result;
    }

    // 验证 系统参数
    private ApiStore.ApiRunable sysParamsValdate(HttpServletRequest request) {
        String apiName = request.getParameter(METHOD);
        String json = request.getParameter(PARAMS);
        ApiStore.ApiRunable api;
        if(apiName == null || apiName.trim().equals("")){
            throw new ApiException("调用失败：参数'method'为空");
        }else if( json == null){
            throw new ApiException("调用失败：参数'params'为空");
        }else if((api = apiStore.findApiRunable(apiName)) == null ){
            throw new ApiException("调用失败：指定api不存在，API："+ apiName);
        }
        return api;
    }

    private Object[] buildParams(ApiStore.ApiRunable run, String paramsJson, HttpServletRequest request,
                             HttpServletResponse response) {
        Map<String, Object> map = null;
        try {
            map = JsonUtil.toMap(paramsJson);
        }catch (IllegalArgumentException e){
            throw new ApiException("调用失败：json字符串格式异常,请检查params参数");
        }
        if(map == null){
            map = new HashMap<>();
        }
        Method method = run.targetMethod;
        List<String> paramNames = Arrays.asList(parameteUtil.getParameterNames(method));
        Class<?>[] paramTypes = method.getParameterTypes();
        for(Map.Entry<String,Object> m : map.entrySet()){
            if(!paramNames.contains(m.getKey())){
                throw new ApiException("调用失败：接口不存在‘"+m.getKey()+"'参数");
            }
        }
        Object[] args = new Object[paramTypes.length];
        for(int i = 0;i<paramTypes.length ; i++){
            if(paramTypes[i].isAssignableFrom(HttpServletRequest.class)){
                args[i] = request;
            }else if (map.containsKey(paramNames.get(i))){
                try {
                    args[i] = converJsonToBean(paramNames.get(i), paramTypes[i]);
                }catch (Exception e){
                    throw new ApiException(String.format("调用失败：指定参数'{%s}'格式错误或值错误：",paramNames.get(i))+e.getMessage());
                }
            }else {
                args[i] = null;
            }
            return args;
        }

        return null; //TODO
    }

    private Object converJsonToBean(String json, Class<?> paramType) {
        JSONObject jsonObject = new JSONObject();
        return jsonObject.getObject(json,paramType);
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        apiStore.loadApiFromSpringBeans();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        apiStore = new ApiStore(applicationContext);
    }
}
