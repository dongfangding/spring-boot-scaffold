package com.ddf.scaffold.fw.util;


import com.ddf.scaffold.fw.exception.GlobalCustomizeException;

/**
 * @author DDf on 2018/12/1
 */
public class ConstUtil {

	private ConstUtil() {}

	public static final String ANONYMOUS_NAME = "SYSTEM";


	public static final String FALSE_STR = "0";
	public static final String TRUE_STR = "1";
	public static final String STRING_COLON = ":";


	public static boolean isNull(Object obj) {
		return !isNotNull(obj);
	}

	public static boolean isNotNull(Object obj) {
		return obj != null && !"".equals(obj.toString().trim());
	}


	/**
	 * TODO 是否做成每个请求自动验证？或者配置参数决定是否验证？
	 * @param params
	 */
	public static void fastFailureParamMission(String... params) {
		if (params.length > 0) {
			for (String param : params) {
				if (isNull(param)) {
					throw new GlobalCustomizeException("参数丢失.......");
				}
			}
		}
	}

	public static void fastFailureParamMission(Long... params) {
		if (params.length > 0) {
			for (Long param : params) {
				if (isNull(param)) {
					throw new GlobalCustomizeException("参数丢失.......");
				}
			}
		}
	}

	public static void fastFailureParamMission(Integer... params) {
		if (params.length > 0) {
			for (Integer param : params) {
				if (isNull(param)) {
					throw new GlobalCustomizeException("参数丢失.......");
				}
			}
		}
	}
}
