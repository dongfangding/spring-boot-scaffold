package com.ddf.scaffold.fw.interceptor;

import com.ddf.scaffold.fw.session.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.Map;

/**
 * @author dongfang.ding on 2018/12/31
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 11)
public class RequestContextInterceptor extends HandlerInterceptorAdapter {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RequestContext requestContext;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        extractParam(request);

        if (request instanceof MultipartHttpServletRequest) {
            MultipartHttpServletRequest multiRequest = ((MultipartHttpServletRequest) request);
            requestContext.setFileItems(multiRequest.getFileMap().values());
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // HTTP 1.0 header
        response.setDateHeader("Expires", 0);
        response.addHeader("Pragma", "no-cache");
        // HTTP 1.1 header
        response.setHeader("Cache-Control", "no-cache");


    }


    private void extractParam(HttpServletRequest request) {
        Enumeration<String> paramEnum = request.getParameterNames();
        Map<String, Object> paramMap = requestContext.getParamMap();
        while (paramEnum.hasMoreElements()) {
            String paramName = paramEnum.nextElement();
            paramMap.put(paramName, request.getParameter(paramName));
        }
        logger.info("from request params interceptor \n{}", requestContext.getParamMap());
        logger.info("the request uri is \n{}?{}", request.getRequestURI(),
                request.getQueryString());
    }
}
