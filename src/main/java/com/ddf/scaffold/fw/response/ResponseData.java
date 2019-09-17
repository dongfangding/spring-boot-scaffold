package com.ddf.scaffold.fw.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 统一响应内容类
 *
 * @author dongfang.ding
 * @date 2019/6/27 11:17
 */
@Data
@NoArgsConstructor
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

    /**
     * 构建鉴权失败响应类
     * @param message
     * @return
     */
    public static ResponseData<String> unauthorized(String message) {
        ResponseData<String> responseData = new ResponseData<>();
        responseData.setCode(HttpStatus.UNAUTHORIZED.value() + "");
        responseData.setMessage("授权失败: " + message);
        responseData.setTimestamp(System.currentTimeMillis());
        responseData.setData(null);
        return responseData;
    }
}
