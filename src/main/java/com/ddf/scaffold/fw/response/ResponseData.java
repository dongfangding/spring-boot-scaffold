package com.ddf.scaffold.fw.response;

import lombok.Data;

/**
 * 统一响应内容类
 *
 * @author dongfang.ding
 * @date 2019/6/27 11:17
 */
@Data
public class ResponseData<T> {
    /** 返回状态码 */
    private String code;
    /** 返回消息 */
    private String message;
    /** 响应时间 */
    private long timestamp;
    /** 返回数据 */
    private T data;


    public ResponseData(String code, String message, long timestamp, T data) {
        this.code = code;
        this.message = message;
        this.timestamp = timestamp;
        this.data = data;
    }

    public static <T> ResponseData<T> success(T data) {
        return new ResponseData<>("200", "success", System.currentTimeMillis(), data);
    }
}
