package com.dang.api.gateway.core;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

/**
 * Description: api注册中心  IOC 大仓库
 *
 * @Author dangfugui  dangfugui@163.cm
 * @Date Create in 2017/10/31
 */
public class ApiStore {

    private ApplicationContext applicationContext;
    // api接口容器
    private Map<String, ApiRunable> apiMap = new HashMap<>();

    public ApiStore(ApplicationContext applicationContext){
        Assert.notNull(applicationContext,"");
        this.applicationContext = applicationContext;
    }

    public void loadApiFromSpringBeans(){
        String[] names = applicationContext.getBeanDefinitionNames();
        Class<?> type;
        for(String name : names){   // 遍历所有的class
            type = applicationContext.getType(name);
            for(Method m : type.getDeclaredMethods() ){  // 遍历所有的 方法
                APIMapping apiMapping = m.getAnnotation(APIMapping.class);
                if (apiMapping != null){
                    addApiItem(apiMapping,name,m);
                }
            }
        }
    }

    /**
     *  添加 api
     * @param apiMapping   api 配置
     * @param name          bean 在spring context 中的名称
     * @param method    方法
     */
    private void addApiItem(APIMapping apiMapping, String name, Method method) {
        // 验证接口规范
        for (Field field : method.getReturnType().getDeclaredFields()) {
            if(field.getDeclaringClass().equals(Object.class)){
                throw new RuntimeException(String.format("%s,%s 不符合接口规范",method.getDeclaringClass(),method.getName()));
            }
        }

        // 执行器
        ApiRunable apiRunable = new ApiRunable(apiMapping.value(),name, method);
        apiMap.put(apiMapping.value(), apiRunable);
    }

    public ApiRunable findApiRunable(String apiName ,String version){
        return apiMap.get(apiName+"_"+version);
    }

    public ApiRunable findApiRunable(String apiName){
        Assert.notEmpty(apiMap,"API name nust note null !");
        List<ApiRunable> list = new ArrayList<>(20);
        for(ApiRunable api : apiMap.values()){
            if(api.getApiName().equals(apiName)){
                return api;
            }
        }
        return null;
    }


    public class ApiRunable{
        String apiName;     // api 名称
        String targetName;  // ioc bean 名称
        Object target;      // 接口示例
        Method targetMethod;    // 目标方法

        public ApiRunable(String apiName, String beanName, Method method) {
            this.apiName = apiName;
            this.targetName = beanName;
            this.targetMethod = method;
        }

        // 杜鳌线程安全问题
        public Object run(Object... args) throws InvocationTargetException, IllegalAccessException {
            if(target == null){
                target = applicationContext.getBean(targetName);
            }
            return targetMethod.invoke(target,args);
        }

        public Class<?>[] getParamTypes(){
            return targetMethod.getParameterTypes();
        }

        public String getApiName() {
            return apiName;
        }

        public String getTargetName() {
            return targetName;
        }

        public Object getTarget() {
            return target;
        }
    }
}
