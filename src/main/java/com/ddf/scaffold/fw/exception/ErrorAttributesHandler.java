package com.ddf.scaffold.fw.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.context.MessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

/**
 * @author DDf on 2019/1/2
 * 处理全局异常，支持异常类占位符解析和国际化
 */
@Component
@Order(value = Ordered.HIGHEST_PRECEDENCE + 10)
public class ErrorAttributesHandler extends DefaultErrorAttributes {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private MessageSource messageSource;

	@Override
	public Map<String, Object> getErrorAttributes(WebRequest webRequest, boolean includeStackTrace) {
		Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, includeStackTrace);
		Throwable error = getError(webRequest);
		if (error == null) {
			return errorAttributes;
		}
		error.printStackTrace();
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		error.printStackTrace(pw);
		logger.error(sw.toString());

		GlobalCustomizeException exception;
		if (error instanceof GlobalCustomizeException) {
			exception = (GlobalCustomizeException) error;
		} else if (error instanceof ObjectOptimisticLockingFailureException) {
			exception = new GlobalCustomizeException(GlobalExceptionEnum.OBJECT_OPTIMISTIC_LOCKING);
		} else {
			exception = new GlobalCustomizeException(error.getMessage());
			exception.setCode(error.getMessage());
		}
		// 解析异常类消息代码，并根据当前Local格式化资源文件
		Locale locale = webRequest.getLocale();
		exception.setMessage(messageSource.getMessage(exception.getCode(), exception.getParams(),
				exception.getCode(), locale));

		errorAttributes.put("code", exception.getCode());
		errorAttributes.put("message", exception.getMessage());
		return errorAttributes;
	}
}
