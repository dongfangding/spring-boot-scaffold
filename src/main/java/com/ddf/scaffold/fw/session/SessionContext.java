package com.ddf.scaffold.fw.session;

import com.ddf.scaffold.fw.entity.BaseDomain;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashMap;

/**
 * @author DDf on 2018/12/31
 */
@Component
@SessionScope
public class SessionContext<T extends BaseDomain> extends HashMap implements Serializable {

	private static final long serialVersionUID = 3370995848343546285L;

	/**
	 * 用户唯一键（用户名/邮箱，按自己需要来）,登录后必须由编码者赋值，后面框架中会用到
	 */
	@Getter
	@Setter
	@NotNull private String uid;

	/**
	 * 用户信息对象,登录后编码者将用户信息设置设置到该属性中
	 */
	@Getter
	@Setter
	@NotNull private T user;
}
