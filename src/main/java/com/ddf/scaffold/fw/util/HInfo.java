package com.ddf.scaffold.fw.util;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @Title: HInfo.java
 * @Package com.fw.utils
 * @Description: 自定义响应数据结构
 * 				succeed：表示成功
 * 				error：表示错误，错误信息在msg字段中
 * 				exception：异常抛出信息
 */
@XmlRootElement
public class HInfo<T> {
	// 响应业务状态
    private String code;
    // 响应消息
    private String message;
    // 响应中的数据
	@JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;
    
    
    public static<T> HInfo build(String code, String message, T data) {
        return new HInfo(code, message, data);
    }
    // 成功
    public static<T> HInfo ok(T data) {
        return new HInfo(data);
    }

    public static HInfo ok() {
        return new HInfo(null);
    }

    //错误
    public static HInfo errorMsg(String msg) {
        return new HInfo("error", msg, null);
    }
    
    public static HInfo errorException(String msg) {
        return new HInfo("exception", msg, null);
    }
    
    // 
    public HInfo(String code, String message, T data) {
    	this.code = code;
    	this.message = message;
	    this.data = data;
	}
    
    public HInfo(T data) {
    	this.code = "succeed";
        this.message = "成功";
	    this.data = data;
	}
    
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

	public Object getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}


    @Override
    public String toString() {
        return "HInfo{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}