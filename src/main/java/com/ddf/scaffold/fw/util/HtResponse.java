package com.ddf.scaffold.fw.util;

import lombok.Getter;
import lombok.Setter;

/**
 * @author DDf on 2019/1/23
 */
@Getter
@Setter
public class HtResponse<T> {
    private Long timestamp;
    private Integer status;
    private String path;
    private String code;
    private String message;
    private T data;
}
