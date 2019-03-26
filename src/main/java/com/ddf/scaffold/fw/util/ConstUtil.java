package com.ddf.scaffold.fw.util;


import com.ddf.scaffold.fw.exception.GlobalCustomizeException;

import java.util.Locale;

/**
 * @author DDf on 2018/12/1
 */
public class ConstUtil {

	private ConstUtil() {}

	public static final String ANONYMOUS_NAME = "SYSTEM";

	public static final String FALSE_STR = "0";
	public static final String TRUE_STR = "1";
	public static final Byte FALSE_BYTE = new Byte(FALSE_STR);
	public static final Byte TRUE_BYTE = new Byte(TRUE_STR);

	public static final Integer NOT_REMOVED = 0;
	public static final Integer TRUE_REMOVED = 1;

	public static final String STRING_COLON = ":";
	public static final String STRING_SHARP = "#";
	public static final String STRING_DOT = ".";
	public static final String STRING_PERCENT = "%";
	public static final String STRING_UNDERLINE = "_";
	public static final String STRING_DASH = "-";
	public static final String STRING_LEFT_BRACE = "{";
	public static final String STRING_RIGHT_BRACE = "}";
	public static final String STRING_RETURN_WIN = "\r\n";
	public static final String STRING_RETURN_UNIX = "\n";

	public static final String CELL_TYPE_NUMBER = "isNumber";
	public static final String CELL_TYPE_STRING = "isString";
	public static final String CELL_TYPE_BOOL = "isBool";
	public static final String CELL_TYPE_BLANK = "isBlank";
	public static final String CELL_TYPE_ERROR = "isError";
	public static final String CELL_TYPE_DATE = "isDate";


	public static final String COMMA = ",";
	public static final String DIR_SEP ="/";
	public static final String Removed = "removed";
	public static final String CompCode = "compCode";
	public static final String XML = "xml";
	public static final String JSON = "json";

	public static final String ENCODING_UTF8 = "UTF-8";
	public static final Locale LOCALE_DEFAULT = Locale.CHINA;
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");

	// 打印模板
	public static final String TEMP_SUFFIX_EXCEL = "xls";
	public static final String TEMP_SUFFIX_EXCEL_XLSX="xlsx";
	public static final String TEMP_SUFFIX_WORD = "doc";
	public static final String TEMP_PARAM_CODE = "tetyCode";
	public static final String TEMP_PARAM_TYPE = "type";
	public static final String TEMP_PARAM_TYPE_CODE = "C";


	public static boolean isNull(Object obj) {
		return !isNotNull(obj);
	}

	public static boolean isNotNull(Object obj) {
		return obj != null && !"".equals(obj.toString().trim());
	}

	/**
	 * @param params
	 */
	public static void fastFailureParamMission(Object... params) {
		if (params.length > 0) {
			for (Object param : params) {
				if (isNull(param)) {
					throw new GlobalCustomizeException("请输入必填项.......");
				}
			}
		}
	}

	public static String appendLeftLike(String sourceStr) {
		if (!sourceStr.contains(STRING_PERCENT)) {
			sourceStr = STRING_PERCENT + sourceStr;
		}
		return sourceStr;
	}

	public static String appendRightLike(String sourceStr) {
		if (!sourceStr.contains(STRING_PERCENT)) {
			sourceStr = sourceStr + STRING_PERCENT;
		}
		return sourceStr;
	}

	public static String appendLike(String sourceStr) {
		if (!sourceStr.contains(STRING_PERCENT)) {
			sourceStr =  STRING_PERCENT + sourceStr + STRING_PERCENT;
		}
		return sourceStr;
	}
}
