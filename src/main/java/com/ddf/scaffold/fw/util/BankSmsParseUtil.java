package com.ddf.scaffold.fw.util;

import com.ddf.scaffold.fw.tcp.model.bo.BankTemplateParse;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 银行短信解析工具类
 *
 * @author dongfang.ding
 * @date 2019/8/6 12:32
 */
public class BankSmsParseUtil {
    /**
     * 短信模板左边的标识位
     */
    private static final String LEFT = "{";
    /**
     * 短信模板右边的标识位
     */
    private static final String RIGHT = "}";

    /**
     * 解析短信模板数据
     * <p>
     * String content = "账户*0936于07月29日09:54存入￥0.01元，可用余额5.01元。银联入账。【民生银行】";
     * String template = "账户*{}于{}存入￥{}元，可用余额{}元。银联入账。【民生银行】";
     *
     * @param content
     * @param template
     * @return
     */
    public static List<String> parse(String content, String template) {
        if (StringUtils.isAnyBlank(content, template)) {
            throw new RuntimeException("template不能为空!");
        }
        List<String> result = new ArrayList<>();
        int pointLeft, pointRight;
        String leftVal, rightVal;
        int contentLeft, contentRight;
        String tempStr, parseVal;
        // 一直找到字符串中没有需要解析的标识位数据
        while (template.contains(LEFT) && template.contains(RIGHT)) {
            // 获取第一个左标识位左边第一个元素的角标，即第一个{左边的第一个元素的位置
            pointLeft = template.indexOf(LEFT) - 1;
            // 获取第一个右标识位右边第一个元素的角标，即第一个}右边的第一个元素的位置
            pointRight = template.indexOf(RIGHT) + 1;

            // 获取左边第一个元素的值
            leftVal = template.substring(0, pointLeft + 1);
            // 舍弃右边元素左边的所有数据
            tempStr = template.substring(pointRight);

            // 判断}右边元素的内容，一直到下个{，如果没有，则右边元素的内容为剩余所有字符
            if (tempStr.contains(LEFT)) {
                rightVal = template.substring(pointRight, pointRight + tempStr.indexOf(LEFT));
            } else {
                rightVal = template.substring(pointRight);
            }

            // 获取模板中左边元素内容对应在原始数据中的角标
            contentLeft = content.indexOf(leftVal) + leftVal.length();
            // 获取模板中右边元素内容对应在原始数据中的角标
            contentRight = content.indexOf(rightVal);
            // 截取左右两个临界点，获取中间值，即为解析内容
            parseVal = content.substring(contentLeft, contentRight);
            result.add(parseVal);
            // 每次解析之后，要把之前已经解析过字符串从原始字符中截取掉，否则indexOf无法处理
            content = content.substring(contentRight);
            template = template.substring(pointRight);
        }
        return result;
    }


    /**
     * 将解析后的短信内容转换为对象，固定格式
     *
     * @param content
     * @param template
     * @return
     */
    public static BankTemplateParse parseToObj(String content, String template) {
        List<String> list = parse(content, template);
        if (list.size() != 3) {
            throw new RuntimeException("模板数据有误!");
        }
        BankTemplateParse bankTemplateParse = new BankTemplateParse();
        bankTemplateParse.setCardLast4Num(list.get(0));
        DateTimeFormatter formatterYear = DateTimeFormatter.ofPattern("yyyy年");
        LocalDate localDate = LocalDate.now();
        String format = formatterYear.format(localDate);
        String timeStr = format + list.get(1);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日hh:mm");
        Date date;
        try {
            date = simpleDateFormat.parse(timeStr);
        } catch (ParseException e) {
            throw new RuntimeException("时间解析失败");
        }
        bankTemplateParse.setInTime(date.getTime());
        bankTemplateParse.setInAmount(new BigDecimal(list.get(2)));
        return bankTemplateParse;
    }

    public static void main(String[] args) {
        String content = "账户*0936于07月29日09:54存入￥0.01元，可用余额5.01元。银联入账。【民生银行】";
        String template = "账户*{}于{}存入￥{}元，可用余额5.01元。银联入账。【民生银行】";
        BankTemplateParse parseToObj = parseToObj(content, template);
        System.out.println(parseToObj);
    }
}
