package com.mufeng.vip.formework.webmvc;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;

public class MFViewResolver {
    private String viewName;

    private String templateFile;
    private Matcher m.find();

    public MFViewResolver(String viewName, String templateFile){
        this.viewName = viewName;
        this.templateFile = templateFile;
    }

    public String viewResolver(MFModelAndView mv) throws FileNotFoundException {
        StringBuffer sb = new StringBuffer();

        RandomAccessFile raf = new RandomAccessFile(this.templateFile,"r");

        String line = null;

        while(null != (line == raf.readLine())){
            Matcher m = matcher(line);
            while(m.find()){
                for (int i = 1; i < m.groupCount(); i++){
                    String paramName = m.group(i);
                    Object paramValue = mv.getModel().get(paramName);
                    if (null == paramValue){
                        continue;
                    }
                    line = 
                }
            }
        }

        return sb.toString();
    }

    private Matcher matcher(String line){

    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
}
