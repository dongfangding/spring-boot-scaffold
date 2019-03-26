package com.ddf.scaffold.fw.exception;

/**
 * @author DDf on 2019/2/26
 * 如不想使用国际化异常，又想统一项目中定义的异常，可定义在该类中使用
 */
public enum CNMessage implements GlobalExceptionCodeResolver {
    /**
     *
     */
    TEMPLATE_FILE_NOT_EXIST("模板文件不存在"),

    ITEMPLATE_RECORD_NOT_EXISTS("导入模板记录不存在[%s]"),

    TEMPLATE_RECORD_NOT_EXISTS("导出模板记录不存在[%s]"),

    NO_FILE_TO_UPLOAD("没有需要上传的文件"),

    TEMPLATE_NOT_CONFIG("未配置导出模板"),

    USER_LOGIN_NAME_REPEAT("登录名存在重复，数据不合法！"),



    CAN_NOT_FIND_COMP("无法找到对应的客户信息！[%s]");


    private String message;

    CNMessage(String message) {
        this.message = message;
    }

    @Override
    public String get() {
        return this.message;
    }
}

