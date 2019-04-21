package com.ddf.scaffold.fw.interceptor;

import com.ddf.scaffold.fw.session.SessionContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

/**
 * @author DDf on 2018/12/31
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@ConfigurationProperties(
		prefix = "custom.login-interceptor"
)
public class LoginInterceptor extends HandlerInterceptorAdapter {
	@Getter
	@Setter
	private Set<String> ignoreFile;

	@Autowired
	private SessionContext sessionContext;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (sessionContext.getUser() == null || sessionContext.getUser().getId() == null) {
			String servletPath = request.getServletPath();
			boolean isMatch = false;
			if (ignoreFile != null && !ignoreFile.isEmpty()) {
				for (String v : ignoreFile) {
					if (servletPath.equals(v) || v.startsWith(servletPath)) {
						isMatch = true;
					}
				}
			}
			if (!isMatch && !"/error".equals(servletPath)) {
				// FIXME
//				throw new GlobalCustomizeException(GlobalExceptionEnum.USER_NOT_LOGIN);
			}
		}
		return true;
	}

}
