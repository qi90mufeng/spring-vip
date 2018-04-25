package com.mufeng.vip.formework.webmvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Map;

public class MFHandlerAdapter {

    private Map<String, Integer> paramMapping;

    public MFHandlerAdapter(Map<String, Integer> paramMapping) {
        this.paramMapping = paramMapping;
    }

    /**
     *
     * @param req
     * @param resp
     * @param handler
     * @return
     */
    public MFModelAndView handle(HttpServletRequest req, HttpServletResponse resp, MFHandlerMapping handler) {
        //根据用户请求的参数信息，跟Method中的参数信息进行动态匹配
        //resp 传递来的目的只有一个，只是为了将其赋值给方法参数

        //只有当用户传过来的ModelAndView为空的时候，才会new一个默认的

        //1、要准备好这个方法的形参列表
        Class<?>[] paramTypes = handler.getMethod().getParameterTypes();

        //2、拿到自定义命名参数所在的位置
        //用户通过url传过来的参数列表
        Map<String,String[]> map = req.getParameterMap();

        //3、构造实参列表
        Object[] paramValues = new Object[paramTypes.length];
        for (Map.Entry<String, String[]> entry : map.entrySet()){
            String value = Arrays.toString(entry.getValue()).replaceAll("\\[|\\]","");

            if (!this.paramMapping.containsKey(entry.getKey())){
                continue;
            }

            int index = this.paramMapping.get(entry.getKey());
            paramValues[index] = caseStringValue(value, paramTypes[index]);
        }

        int reqIndex = this.paramMapping.get(HttpServletRequest.class.getName());
        paramValues[reqIndex] = req;

        int respIndex = this.paramMapping.get(HttpServletResponse.class.getName());
        paramValues[respIndex] = resp;

        Object result = handler.getMethod().invoke(handler.getController().paramValues);
        boolean isModelAndView = handler.getMethod().getReturnType() == MFModelAndView.class;

        if (!isModelAndView){
            return (MFModelAndView)result;
        }
        return result;
    }

    private Object caseStringValue(String value, Class<?> clazz){
        if (clazz == String.class){
            return value;
        }else if (clazz == Integer.class){
            return Integer.valueOf(value);
        }else if (clazz == int.class){
            return Integer.valueOf(value);
        }else{
            return null;
        }
    }
}
