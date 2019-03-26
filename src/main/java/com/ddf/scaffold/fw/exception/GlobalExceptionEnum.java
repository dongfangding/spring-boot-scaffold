/**
 * 
 */
package com.ddf.scaffold.fw.exception;

/**
 * 定义异常消息的代码，get方法返回实际值，这个值需要在exception.properties、exception_zh_CN.properties、
 * exception_en_US中配置，请根据实际情况在对应的Locale资源文件中配置，至少配置exception.properties
 * @author DDF 2017年12月1日
 *
 */
public enum GlobalExceptionEnum implements GlobalExceptionCodeResolver {
	/** 用户已经存在 */
	USER_EXIST("USER_EXIST"),
	/** 用户不存在 */
	UID_NOT_EXIST("UID_NOT_EXIST"),
	/** 帐号停用 */
	ACCOUNT_DISABLED("ACCOUNT_DISABLED"),
	PERMISSION_DENIED("PERMISSION_DENIED"),
	UPDATE_ERROR("UPDATE_ERROR"),
	VERSION_MISSION("VERSION_MISSION"),
	FIELD_NOT_MATCH("FIELD_NOT_MATCH"),
	LOGIN_EXPIRED("LOGIN_EXPIRED"),
	OBJECT_OPTIMISTIC_LOCKING("OBJECT_OPTIMISTIC_LOCKING"),
	SERIAL_NO_GENERATE_FAILURE("SERIAL_NO_GENERATE_FAILURE"),
	USER_NOT_LOGIN("USER_NOT_LOGIN"),
	DB_ERROR("DB_ERROR"),
	USER_NOT_EXIST("USER_NOT_EXIST"),
	LOGIN_ERROR("LOGIN_ERROR"),
	VALIDATE_EMAIL_ERROR("VALIDATE_EMAIL_ERROR"),
	EMAIL_HAD_REGISTRY("EMAIL_HAD_REGISTRY"),
	PASSWORD_ERROR("PASSWORD_ERROR"),

	/** 登录名已存在 */
	LOGIN_NAME_EXIST("LOGIN_NAME_EXIST"),

	/** 用户名已存在 */
	USER_NAME_EXIST("USER_NAME_EXIST"),

	/** 数据校验重复 */
	DATA_REPEAT("DATA_REPEAT"),

	/** 无效的用户激活标识 */
	USER_ACTIVE_FLAG_MUST_IN("USER_ACTIVE_FLAG_MUST_IN"),

	/** 无效的收发通类型 */
	INVALID_PARTY_TYPE("INVALID_PARTY_TYPE"),

	/** 无效的公司类型 */
	INVALID_COMP_TYPE("INVALID_COMP_TYPE"),

	/** 该公司没有配置流水号规则 */
	SERIAL_RULE_NOT_EXISTS("SERIAL_RULE_NOT_EXISTS"),

	/** 流水号规则重复 */
	REPEAT_SERIAL_RULE("REPEAT_SERIAL_RULE");

	private String code;

	GlobalExceptionEnum (String code) {
		this.code = code;
	}

	@Override
	public String get() {
		return code;
	}
}