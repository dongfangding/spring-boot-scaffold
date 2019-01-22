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
	UID_NOT_EXIST("UID_NOT_EXIST"),
	UPDATE_ERROR("UPDATE_ERROR"),
	VERSION_MISSION("VERSION_MISSION"),
	FIELD_NOT_MATCH("FIELD_NOT_MATCH"),
	OBJECT_OPTIMISTIC_LOCKING("OBJECT_OPTIMISTIC_LOCKING"),
	SERIAL_NO_GENERATE_FAILURE("SERIAL_NO_GENERATE_FAILURE"),
	USER_NOT_LOGIN("USER_NOT_LOGIN"),
	DB_ERROR("DB_ERROR"),
	USER_NOT_EXIST("USER_NOT_EXIST"),
	LOGIN_ERROR("LOGIN_ERROR"),
	VALIDATE_EMAIL_ERROR("VALIDATE_EMAIL_ERROR"),
	EMAIL_HAD_REGISTRY("EMAIL_HAD_REGISTRY"),
	PASSWORD_ERROR("PASSWORD_ERROR")

	;

	private String code;

	GlobalExceptionEnum (String code) {
		this.code = code;
	}

	@Override
	public String getCode() {
		return code;
	}
}