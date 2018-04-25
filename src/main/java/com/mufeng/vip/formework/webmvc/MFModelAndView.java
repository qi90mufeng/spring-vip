package com.mufeng.vip.formework.webmvc;

import java.util.Map;

public class MFModelAndView {

    private String viewName;

    private Map<String, Object> model;

    public MFModelAndView(String viewName, Map<String, Object> model) {
        this.viewName = viewName;
        this.model = model;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public Map<String, Object> getModel() {
        return model;
    }

    public void setModel(Map<String, Object> model) {
        this.model = model;
    }
}
