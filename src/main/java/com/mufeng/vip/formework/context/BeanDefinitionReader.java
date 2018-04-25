package com.mufeng.vip.formework.context;

import com.mufeng.vip.formework.beans.BeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

//对配置文件进行查找，读取，解析
public class BeanDefinitionReader {

    private Properties config = new Properties();

    private List<String> registyBeanClasses = new ArrayList<String>();

    private final String SCAN_PACKAGE = "scanPackage";

    public BeanDefinitionReader(String... locations)  {
        InputStream is = null;
        try{
            is = this.getClass().getResourceAsStream(locations[0].replace("classpath:",""));

            config.load(is);
        }catch(IOException e){
            e.printStackTrace();
        }finally {
            try{
                if (null != is){
                    is.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        doScanner(config.getProperty(SCAN_PACKAGE));

    }

    public List<String> loadBeanDefinitions(){
        return null;
    }

    //
    public BeanDefinition registerBean(String className){
        if (this.registyBeanClasses.contains(className)){
            BeanDefinition beanDefinition = new BeanDefinition();
            beanDefinition.setBeanClassName(className);
            beanDefinition.setFactoryBeanName(lowerFirstCase(className.substring(className.lastIndexOf(""))));
            return beanDefinition;
        }
        return null;
    }

    //递归扫描
    private void doScanner(String packageName){
        URL url = this.getClass().getClassLoader().getResource("/" + packageName.replace(".","/"));

        File classDir = new File(url.getFile());

        for (File file: classDir.listFiles()){
            if (file.isDirectory()){
                doScanner(packageName + "." + file.getName());
            }else{
                registyBeanClasses.add(packageName + "." + file.getName().replace(".class", ""));
            }
        }
    }

    public Properties getConfig(){
        return config;
    }

    private String lowerFirstCase(String substring) {
        return null;
    }
}
