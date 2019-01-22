package com.ddf.scaffold.fw.log;

import lombok.Getter;
import lombok.Setter;

/**
 * @author DDf on 2018/11/7
 */
@Getter
@Setter
public class LogAspectConfiguration {
    public static final String BEAN_NAME = "logAspectConfiguration";

    private boolean enableLogAspect;
}
