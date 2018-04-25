package com.mufeng.vip.formework.webmvc.servlet;

import com.mufeng.vip.formework.annotation.MFController;
import com.mufeng.vip.formework.annotation.MFRequestParam;
import com.mufeng.vip.formework.context.MFApplicationContext;
import com.mufeng.vip.formework.webmvc.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

public class DispatcherServlet extends HttpServlet {

    private final String LOCATION = "contextConfigLocation";

    private Map<String, MFHandlerMapping> handlerMapping = new HashMap<>();

    //最核心的设计，最经典的
    //它牛b到直接干掉了struts、webwork等MVC框架
    private List<MFHandlerMapping> handlerMappings = new ArrayList<>();

    private Map<MFHandlerMapping, MFHandlerAdapter> handlerAdapters = new HashMap<>();

    private List<MFViewResolver> viewResolvers = new ArrayList<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replace("+/","/");
        MFHandlerMapping handler = handlerMapping.get(url);
        try {
            MFModelAndView mv = (MFModelAndView)handler.getMethod().invoke(handler.getController());
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            doDispatch(req, resp);
        }catch (Exception e){
            resp.getWriter().write("500 Exception Details:\r\n" + Arrays.toString(e.getStackTrace()).
                    replaceAll("\\[\\]", "").replaceAll("\\s","\r\n"));
        }

    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        MFHandlerMapping handler = getHandler(req);
        if (handler == null){
            resp.getWriter().write("404 Not Found");
        }
        MFHandlerAdapter ha = getHandlerAdapter(handler);

        MFModelAndView mv = ha.handle(req, resp, handler);

        processDispatchResult(resp, mv);
    }

    private void processDispatchResult(HttpServletResponse resp, MFModelAndView mv) {
        //
        if (null == mv){
            return;
        }
        if (this.viewResolvers.isEmpty()){
            return;
        }

        for (MFViewResolver view : this.viewResolvers){
            if (!mv.getViewName().equals(view.getViewName())){
               continue;
            }


        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //初始化容器
        MFApplicationContext context = new MFApplicationContext(null, null);

        initStrategies(context);

        super.init(config);
    }

    private void initStrategies(MFApplicationContext context) {
        //初始化九大组件
        //文件上传解析
        initMultiPartResolver(context);
        //本地化解析
        initLocaleResolver(context);
        //主题解析
        initThemeResolver(context);
        //用来保存Controller中配置的RequestMapping和Method的一一对应关系
        initHandlerMapping(context);

        initHandlerAdapters(context);

        initHandlerExceptionResolvers(context);

        //直接解析请求到视图
        initRequestToViewNameTranslator(context);

        //实现动态模板的解析
        initViewResolvers(context);

        initFlashMapManager(context);
    }

    private void initFlashMapManager(MFApplicationContext context) {
    }

    private void initViewResolvers(MFApplicationContext context) {
        //在页面敲一个 http://localhost/first.html
        //解决页面名字和模板文件关联的问题
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File f = new File(templateRootPath);

        for (File template : f.listFiles()){
            this.viewResolvers
        }
    }

    private void initRequestToViewNameTranslator(MFApplicationContext context) {
    }

    private void initHandlerExceptionResolvers(MFApplicationContext context) {
    }

    private void initHandlerAdapters(MFApplicationContext context) {
        //在初始化阶段我们能做的就是，将这些参数的名字或者类型按一定的顺序保存下来，
        //因为后面用反射调用的时候，传的形参是一个数组
        //可以通过记录这些参数的位置index，

        for (MFHandlerMapping handlerMapping: this.handlerMappings){
            Map<String, Integer> paramMapping = new HashMap<>();
            Annotation[][] pa = handlerMapping.getMethod().getParameterAnnotations();
            for (int i = 0; i < pa.length; i++){
                for (Annotation a: pa[i] ) {
                    if (a instanceof MFRequestParam){
                        String paramName = ((MFRequestParam)a).value();
                        if (!"".equals(paramName.trim())){
                            paramMapping.put(paramName, i);
                        }
                    }
                }
            }

            Class<?>[] paramTypes = handlerMapping.getMethod().getParameterTypes();
            for (int i = 0; i < paramTypes.length; i++){
                Class<?> type = paramTypes[i];
                if (type == HttpServletRequest.class
                        || type == HttpServletResponse.class){
                    paramMapping.put(type.getName(), i);
                }
            }

            this.handlerAdapters.put(handlerMapping, new MFHandlerAdapter(paramMapping));
        }

    }

    private void initHandlerMapping(MFApplicationContext context) {
        //用来保存Controller中配置的RequestMapping和Method的一一对应关系
        String[] beanNames = context.getBeanDefinitionNames();
        for (String beanName: beanNames){
            Object instance = context.getBean(beanName);

            Class<?> clazz = instance.getClass();

            if (clazz.isAnnotationPresent(MFController.class)){
                continue;
            }

            String baseUrl = "";

            if (clazz.isAnnotationPresent(MFRequestMapping.class)){
                MFRequestMapping requestMapping = clazz.getAnnotation(MFRequestMapping.class);

                baseUrl = requestMapping.value();
            }

            Method[] methods = clazz.getMethods();

            for (Method method:methods
                 ) {
                if (!method.isAnnotationPresent(MFRequestMapping.class)){
                    continue;
                }
                MFRequestMapping requestMapping = clazz.getAnnotation(MFRequestMapping.class);

                String regex = ("/" + requestMapping.value().repaceAll("/+", "/"));
                Pattern pattern = Pattern.compile(regex);
                this.handlerMapping.add(new MFHandlerMapping(pattern, controller, method));
            }

        }

    }

    private void initThemeResolver(MFApplicationContext context) {
    }

    private void initLocaleResolver(MFApplicationContext context) {
    }

    private void initMultiPartResolver(MFApplicationContext context) {
    }

    private MFHandlerAdapter getHandlerAdapter(MFHandlerMapping handler){

    }

    private MFHandlerMapping getHandler(HttpServletRequest req){
        if (this.handlerMappings.isEmpty()){
            return null;
        }
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();

        url = url.replace(contextPath, "").replaceAll("/+", "/");

        for (MFHandlerMapping handler : this.handlerMappings){

        }
    }
}
