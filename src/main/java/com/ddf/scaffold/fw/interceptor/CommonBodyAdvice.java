package com.ddf.scaffold.fw.interceptor;

import com.ddf.scaffold.fw.exception.ErrorAttributesHandler;
import com.ddf.scaffold.fw.response.ResponseData;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 允许在执行一个@ResponseBody 或一个ResponseEntity控制器方法之后但在使用一个主体写入正文之前自定义响应HttpMessageConverter。
 *
 * @author dongfang.ding
 * @date 2019/6/27 11:15
 */
@RestControllerAdvice
@ControllerAdvice
public class CommonBodyAdvice implements ResponseBodyAdvice<Object> {


    /**
     * 如果出现了异常，先经过{@link ErrorAttributesHandler}处理之后依然会将请求拦截到这里，但是异常本身有自己的很多属性，与正常的ResponseData
     * 有诸多不通过，不需要data但却需要更多的错误消息，所以是将两个地方统一还是说异常和正常不太一样，看自己决定吧。我这边做了分开
     *
     * @param returnType
     * @param converterType
     * @return
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 当返回值已经是ResponseData了，就不在重复处理了
        // FIXME 根据实际项目中情况可能这里还需要增加判断
        if (returnType.getMethod() != null && returnType.getMethod().getReturnType() != null
                && (ResponseData.class.equals(returnType.getMethod().getReturnType())
                || BasicErrorController.class.equals(returnType.getMethod().getDeclaringClass()))) {
            return false;
        }
        return true;
    }

    @Override
    public ResponseData<Object> beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        return ResponseData.success(body);
    }
}
