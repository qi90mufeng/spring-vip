package com.mufeng.vip.formework.context;

import com.mufeng.vip.formework.annotation.MFAutowired;
import com.mufeng.vip.formework.annotation.MFController;
import com.mufeng.vip.formework.annotation.MFService;
import com.mufeng.vip.formework.beans.BeanDefinition;
import com.mufeng.vip.formework.beans.BeanPostProcessor;
import com.mufeng.vip.formework.beans.BeanWrapper;
import com.mufeng.vip.formework.core.BeanFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MFApplicationContext implements BeanFactory {

    private String[] configLoactions;
    private BeanDefinitionReader reader;
    //用来保存配置信息
    private Map<String,BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    //用来保证注册式单例的容器
    private Map<String, Object> beanCacheMap = new HashMap<>();
    //用来存储所有被代理过的对象
    private Map<String, BeanWrapper> beanWrapperMap = new ConcurrentHashMap<>();
    public MFApplicationContext(String[] configLoactions, BeanDefinitionReader reader) {
        this.configLoactions = configLoactions;
        this.reader = reader;

        refresh();
    }

    public void refresh(){
        //定位
        this.reader = new BeanDefinitionReader(configLoactions);
        //加载
        List<String> beanDefinitions = reader.loadBeanDefinitions();
        //注册
        doRegisty(beanDefinitions);

        //依赖注入（lazy-init=false）
        doAutowired();
    }

    private void doAutowired() {
        for(Map.Entry<String, BeanDefinition> entry : this.beanDefinitionMap.entrySet()){
            String beanName = entry.getKey();
            if (!entry.getValue().isLazyInit()){
                getBean(beanName);
            }
        }

        for (Map.Entry<String, BeanWrapper> entry : this.beanWrapperMap.entrySet()){
            populateBean(entry.getKey(), entry.getValue());
        }
    }

    public void populateBean(String beanName, Object instance){
        Class clazz = instance.getClass();

        if (!clazz.isAnnotationPresent(MFController.class) ||
            clazz.isAnnotationPresent(MFService.class)){
            return;
        }

        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (!field.isAnnotationPresent(MFAutowired.class)){
                continue;
            }
            MFAutowired autowired = field.getAnnotation(MFAutowired.class);

            String autowiredBeanName = autowired.value().trim();

            if ("".equals(autowiredBeanName)){
                autowiredBeanName = field.getType().getName();
            }
            field.setAccessible(true);

            try{
                field.set(instance, this.beanWrapperMap.get(autowiredBeanName).getWrapperInstance());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void doRegisty(List<String> beanDefinitions){
        try{
            for (String className : beanDefinitions) {
                Class<?> beanClass = Class.forName(className);
                //BeanName有三种情况
                //1、默认是类名首字母小写
                //2、自定义名字
                //3、接口注入

                //接口无法实例化
                if (beanClass.isInterface()){
                    continue;
                }
                BeanDefinition beanDefinition = reader.registerBean(className);
                if (beanDefinition != null){
                    this.beanDefinitionMap.put(beanDefinition.getBeanClassName(),beanDefinition);
                }

                Class<?>[] interfaces = beanClass.getInterfaces();
                for (Class<?> i: interfaces) {
                    //如果是多个实现类，只能覆盖
                    //可以自定义名字
                    this.beanDefinitionMap.put(i.getName(), beanDefinition);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //依赖注入： 通过读取beanDefinition中的信息，通过反射机制创建一个实例并返回
    //spring：不会把最原始的对象放出去，会用一个BeanWrapper来进行一次包装
    //装饰器模式
    //1、保留原来的oop关系
    //2、我需要对它进行扩展，增强（为以后AOP打基础）
    public Object getBean(String beanName){
        BeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);

        String className = beanDefinition.getBeanClassName();

        try{
            //生成通知事件
            BeanPostProcessor postProcessor = new BeanPostProcessor();

           Object instance =  instantionBean(beanDefinition);

           if (null == instance){
               return null;
           }
            //在实例化前调用一次
            postProcessor.postProcessBeforeInitialization(instance, beanName);

           BeanWrapper beanWrapper = new BeanWrapper(instance);
           this.beanWrapperMap.put(beanName, beanWrapper);
            //在实例化后调用一次
           postProcessor.postProcessAfterInitialization(instance, beanName);

            //populateBean(beanName, instance);
            //相当于给我们自己留有了可操作空间
           return this.beanWrapperMap.get(beanName).getWrapperInstance();
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    //传一个BeanDefinition，返回一个实例Bean
    private Object instantionBean(BeanDefinition beanDefinition){
        Object instance = null;
        String className = beanDefinition.getBeanClassName();
        try{
            if (this.beanCacheMap.containsKey(className)){
                instance = this.beanCacheMap.get(className);
            }else{
                Class<?> clazz = Class.forName(className);
                instance = clazz.newInstance();
                this.beanCacheMap.put(className, instance);
            }
            return instance;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public String[] getBeanDefinitionNames(){
        return this.beanDefinitionMap.keySet().toArray(new String[]{this.beanDefinitionMap});
    }

    public int getBeanDefinitionCount(){
        return this.beanDefinitionMap.size();
    }

    public Property getConfig() {
        return "";
    }
}
