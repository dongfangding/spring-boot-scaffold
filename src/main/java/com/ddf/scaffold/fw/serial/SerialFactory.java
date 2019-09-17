package com.ddf.scaffold.fw.serial;

import com.ddf.scaffold.fw.entity.PSerialRule;
import com.ddf.scaffold.fw.exception.GlobalCustomizeException;
import com.ddf.scaffold.fw.exception.GlobalExceptionEnum;
import com.ddf.scaffold.fw.security.SecurityUtils;
import com.ddf.scaffold.fw.serial.repository.SerialNoRepository;
import com.ddf.scaffold.fw.serial.repository.SerialRuleRepository;
import com.ddf.scaffold.fw.util.GlobalConfig;
import com.ddf.scaffold.fw.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 */
@Component
public class SerialFactory {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final Short LOOP_NONE = 0;
    private static final Byte LOOP_PER_DAY = 1;
    private static final Byte LOOP_PER_MONTH = 2;
    private static final Byte LOOP_PER_YEAR = 3;
    private static final String RULE_YEAR_FULL = "<YYYY>";
    private static final String RULE_YEAR_SHORT = "<YY>";
    private static final String RULE_MONTH = "<MM>";
    private static final String RULE_DAY = "<DD>";
    private static final String RULE_SERIAL = "<SN>";
    public static final String RULE_COMP = "<COMP>";
    public static final String RULE_USER = "<USER>";
    public static final String RULE_CURRENCY = "<CUR>";
    public static final String RULE_RP = "<RP>";
    public static final String RULE_PROJ_CODE = "<PROJ_CODE>";


    @Autowired
    private SerialNoRepository serialNoRepository;
    @Autowired
    private SerialRuleRepository serialRuleRepository;
    @Autowired
    private GlobalConfig globalConfig;

    /**
     * 获得平台标准的流水号规则，不再根据传参或者当前用户所在规则查找流水号规则
     * @param code
     * @return
     */
    public String getPlatformSerial(String code) {
        return getSerial(code, null, null, true);
    }


    public String getSerial(String code) {
        return getSerial(code, null, null, false);
    }

    /**
     * 获取序列号 code: 对应的序列号生成规则名称 paramMap: 提供的替换变量 公司简称: SerialFactory.RULE_COMP,
     * 如果不提供, 自动取当前公司 用户简称: SerialFactory.RULE_USER, 如果不提供, 自动取当前用户 币种简称:
     * SerialFactory.RULE_CURRENCY
     * * 1.获取需要的变量
     * * 2.根据rule code和company code找到对应的p_serial_rule
     * * 3.seru_uniq_suffix, 用对应的变量替换之后, 得到seno_suffix
     * * 4.根据seru_code,comp_code, seno_suffix三个参数, 从p_serial_no表获取流水号 1)获取到就+1 2)未获取到,
     * *   插入一条新记录
     * * 5.把取到的流水号, 根据设定的长度seru_sn_length, 补0
     * * 6.根据seru_rule, 替换每个变量,包括流水号
     * * 7.返回替换之后的最终serial
     * *
     *
     * @param code
     * @param paramMap
     * @return
     */
    public String getSerial(
            String code, Map<String, String> paramMap, Date serialDate, boolean isPlatform) {
        if (paramMap == null) {
            paramMap = new HashMap<>();
        }
        // get all parameter
        buildParamMap(paramMap, isPlatform);
        Map<String, Object> propertiesMap = new HashMap<>();
        propertiesMap.put("seruCode", code);
        propertiesMap.put("compCode", paramMap.get(RULE_COMP));

        List<PSerialRule> ruleList = serialRuleRepository.findByProperties(propertiesMap);
        if (ruleList == null || ruleList.isEmpty()) {
            throw new GlobalCustomizeException(GlobalExceptionEnum.SERIAL_RULE_NOT_EXISTS, propertiesMap.get("orgCode"));
        }
        if (ruleList.size() != 1) {
            throw new GlobalCustomizeException(GlobalExceptionEnum.REPEAT_SERIAL_RULE, propertiesMap.get("orgCode"),
                    propertiesMap.get("seruCode"));
        }
        PSerialRule rule = ruleList.get(0);
        // get unique suffix
        // use real value of variable to replace the variable symbol
        String senoSuffix = fillRuleByDate(rule.getSeruUniqSuffix(), paramMap, serialDate);

        // insert fields of serial no
        propertiesMap.put("seruId", "" + rule.getId());
        propertiesMap.put("senoSuffix", senoSuffix);
        propertiesMap.put("senoCurrentNo", "1");
        propertiesMap.put("seruCode", code);
        // expire time of serial no
        Byte seruLoopPeriod = rule.getSeruLoopPeriod();
        //Calendar expire = getExpireTime(seruLoopPeriod);
        Calendar expire = getExpireTimeByDate(seruLoopPeriod, serialDate);
        propertiesMap.put("senoExpire", StringUtil.date2String(expire.getTime()));

        // get the next NO using PSerialNoDAO
        Long sn = serialNoRepository.getNextSerialNo(propertiesMap);

        // format the serial no, add 0 to the leading
        String strSN = formatSN(sn, rule.getSeruSnLength());

        // get final serial no
        // use real value of variable to replace the variable symbol
        paramMap.put(RULE_SERIAL, strSN);
        String no = fillRuleByDate(rule.getSeruRule(), paramMap, serialDate);
        logger.info("{}: {}", Thread.currentThread().getName(), no);
        return no;
    }



    /**
     * 计算规则的过期时间
     *
     * @param seruLoopPeriod
     * @return
     */
    private static Calendar getExpireTimeByDate(Byte seruLoopPeriod, Date serialDate) {
        Calendar expire = Calendar.getInstance();
        if (serialDate != null) {
            expire.setTime(serialDate);
        }
        if (LOOP_NONE.byteValue() == seruLoopPeriod) {
            expire.set(expire.get(Calendar.YEAR) + 20, Calendar.JANUARY, 1, 0, 0, 0);
        } else if (LOOP_PER_YEAR.equals(seruLoopPeriod)) {
            expire.set(expire.get(Calendar.YEAR) + 1, Calendar.JANUARY, 1, 0, 0, 0);
        } else if (LOOP_PER_MONTH.equals(seruLoopPeriod)) {
            expire.add(Calendar.MONTH, 1);
            expire.set(expire.get(Calendar.YEAR), expire.get(Calendar.MONTH), 1, 0, 0, 0);
        } else if (LOOP_PER_DAY.equals(seruLoopPeriod)) {
            expire.add(Calendar.DAY_OF_MONTH, 1);
            expire.set(expire.get(Calendar.YEAR), expire.get(Calendar.MONTH), expire.get(Calendar.DAY_OF_MONTH), 0, 0,
                    0);
        }
        return expire;
    }


    /**
     * 查询条件
     *
     * @param propertyMap
     * @param isPlatform 是否平台标准
     */
    private void buildParamMap(Map<String, String> propertyMap, boolean isPlatform) {
        if (isPlatform) {
            propertyMap.put(RULE_COMP, globalConfig.getPlatformCompCode());
        } else {
            if (!propertyMap.containsKey(RULE_COMP)) {
                propertyMap.put(RULE_COMP, SecurityUtils.getUserOrgCode());
            }
        }

    }

    /**
     * 替换规则中的变量
     *
     * @param rule
     * @param paramMap
     * @return
     */
    private static String fillRuleByDate(@NotNull String rule, @NotNull Map<String, String> paramMap, Date serialDate) {
        Date now = new Date();
        if (serialDate != null) {
            now = serialDate;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String strNow = simpleDateFormat.format(now);

        // year month day
        rule = rule.replaceAll(RULE_YEAR_FULL, strNow.substring(0, 4));
        rule = rule.replaceAll(RULE_YEAR_SHORT, strNow.substring(2, 4));
        rule = rule.replaceAll(RULE_MONTH, strNow.substring(5, 7));
        rule = rule.replaceAll(RULE_DAY, strNow.substring(8, 10));

        // company code, bootUser code, currency code, serial no
        if (paramMap != null && !paramMap.isEmpty()) {
            for (String key : paramMap.keySet()) {
                rule = rule.replaceAll(key, paramMap.get(key));
            }
        }
        return rule;
    }

    /**
     * 流水号补0
     *
     * @param sn
     * @param length
     * @return
     */
    private String formatSN(Long sn, Integer length) {
        BigInteger i = new BigInteger("10");
        i = i.pow(length);
        String temp = Long.toString(i.longValue() + sn);
        return temp.substring(temp.length() - length);
    }

    /**
     * 初始化, 凌晨执行, 删除过期的号 delete the expired record
     */
    @Transactional
    public void initSerial() {
        serialNoRepository.init();
    }
}
