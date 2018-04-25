package com.mufeng.vip.formework.beans;

public class BeanWrapper {

    //还会用到 观察者模式
    //1、支持事件相应，会有一个监听
    private BeanPostProcessor postProcessor;
    //
    private Object wrapperInstance;
    //原始的通过反射new出来，要包装起来，存下来
    private Object originalInstance;

    public BeanWrapper(Object instance){
        this.originalInstance = instance;
        this.wrapperInstance = instance;
    }

    public Object getWrapperInstance() {
        return wrapperInstance;
    }

    //返回代理之后的class
    //可能会是这个$Proxy0
    Class<?> getWrapperClass(){
        return this.wrapperInstance.getClass();
    }


}
