package com.ddf.scaffold.fw.jpa;

import com.ddf.scaffold.fw.entity.BaseDomain;
import com.ddf.scaffold.fw.entity.QueryParam;
import com.ddf.scaffold.fw.exception.GlobalCustomizeException;
import com.ddf.scaffold.fw.exception.GlobalExceptionEnum;
import com.ddf.scaffold.fw.security.SecurityUtils;
import com.ddf.scaffold.fw.util.MethodUtil;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dongfang.ding on 2019/2/28
 *
 * 用于快速拼接查询条件的辅助类
 */
public class JpaBaseDaoUtil {

    private JpaBaseDaoUtil() {}

    /**
     * 判断是否是平台用户
     * @return 是返回true,否则返回false
     */
    public static boolean isPlatformUser() {
        return true;
    }


    /**
     * 非平台用户给通用查询条件增加当前用户所属公司代码条件过滤
     * @param queryParams 查询对象列表
     */
    public static void addCompCodeIfNotPlatformUser(@NotNull List<QueryParam> queryParams) {
        if (!isPlatformUser()) {
            queryParams.add(new QueryParam<>("orgCode", SecurityUtils.getUserOrgCode()));
        }
    }

    /**
     * 给通用查询条件增加当前用户所属公司代码条件过滤
     * @param queryParams 查询对象列表
     */
    public static void addCompCode(@NotNull List<QueryParam> queryParams) {
        queryParams.add(new QueryParam<>("orgCode", SecurityUtils.getUserOrgCode()));
    }

    /**
     * 添加判断重复的主键条件，如果是新增数据，则不需要主键条件，
     * 如果是编辑的数据，则判断重复的时候要排除本条主键数据
     * @param queryParams 要加入的条件集合
     * @param jpaBaseDao  实体的Repository
     * @param entity 实体类
     */
    public static boolean addCheckRepeatUuid(@NotNull List<QueryParam> queryParams, @NotNull JpaBaseDao jpaBaseDao,
                                             @NotNull BaseDomain entity) {
        boolean isNew = jpaBaseDao.isNew(entity);
        if (!isNew) {
            queryParams.add(new QueryParam<>("uuid", QueryParam.Op.NE, entity.getId()));
        }
        return isNew;
    }


    /**
     * 根据给定的条件来判断实体是否重复，不需要自己判断是否是新增还是编辑数据，只需要传入判断重复的关键字的条件即可
     * @param queryParams 查询对象列表
     * @param jpaBaseDao 实体的Repository
     * @param entity 实体数据
     */
    public static void checkRepeat(@NotNull List<QueryParam> queryParams, @NotNull JpaBaseDao jpaBaseDao,
            @NotNull BaseDomain entity, GlobalExceptionEnum message) {
        addCheckRepeatUuid(queryParams, jpaBaseDao, entity);
        Long size = jpaBaseDao.querySize(queryParams);
        if (size > 0) {
            if (message == null) {
                message = GlobalExceptionEnum.DATA_REPEAT;
            }
            throw new GlobalCustomizeException(message);
        }
    }

    /**
     * <P>简单条件的新增或编辑判断重复，只需要传入要判断的字段即可，如果是and的关系请使用notGroupList参数传递，
     * 如果是一个and表达式括号里的多个字段是or关系，则使用groupList参数传递字段名，可组合使用，如果是非以相等条件作为判断，请勿使用该方法</P>
     * <p>如果不使用该方法，那么去重的写法如下：</p>
     * <pre class="code">
     *      List&#60;QueryParam&#62; queryParams = new ArrayList&#60;&#62;();
     *      queryParams.add(new QueryParam&#60;&#62;("orgCode", UserToken.getOrgCode()));
     *      queryParams.add(new QueryParam&#60;&#62;("partyType", party.getPartyType()));
     *      if (ConstUtil.isNotNull(party.getPartyName())) {
     *          queryParams.add(new QueryParam&#60;&#62;("partyName", party.getPartyName()));
     *      }
     *      JpaBaseDaoUtil.addCheckRepeatUuid(queryParams, partyRepository, party);
     *      Long size = partyRepository.querySize(queryParams);
     *      if (size &#62; 0) {
     *          throw new GlobalCustomizeException(GlobalExceptionEnum.DATA_REPEAT);
     *      }
     * </pre>
     * 以上代码等同于以下
     * <pre class="code">
     *     List&#60;String&#62; notGroupList = Arrays.asList("orgCode", "partyType");
     *     List&#60;String&#62; groupList = Arrays.asList("partyName", "partyContact");
     *     JpaBaseDaoUtil.checkRepeat(partyRepository, party, notGroupList, groupList);
     * </pre>
     *
     * @param jpaBaseDao 实体的Repository
     * @param entity 实体对象
     * @param notGroupList 不分组的条件字段名，每个字段都是一个独立的条件，而且只能是EQ
     * @param groupList 分组的条件字段名，只支持一个分组条件，传入的字段名各个之间的关系为OR，与值的关系也是EQ
     * @param message 可自定义自己要抛出的异常，而不使用默认的{@link GlobalExceptionEnum#DATA_REPEAT}
     */
    public static void checkRepeat(@NotNull JpaBaseDao jpaBaseDao, @NotNull BaseDomain entity,
            @Nullable List<String> notGroupList, @Nullable List<String> groupList, GlobalExceptionEnum message) {
        List<QueryParam> queryParams = new ArrayList<>();
        Object value;
        if (notGroupList != null && notGroupList.size() > 0) {
            for (String fieldName : notGroupList) {
                value = MethodUtil.doGetMethod(entity, fieldName);
                if (value != null) {
                    queryParams.add(new QueryParam<>(fieldName, value));
                }
            }
        }
        if (groupList != null && groupList.size() > 0) {
            for (String fieldName : groupList) {
                value = MethodUtil.doGetMethod(entity, fieldName);
                if (value != null) {
                    queryParams.add(new QueryParam<>(fieldName, QueryParam.Op.EQ,
                            MethodUtil.doGetMethod(entity, fieldName), QueryParam.Relative.OR, "group1"));
                }
            }
        }
        checkRepeat(queryParams, jpaBaseDao, entity, message);
    }

    /**
     *
     * @param jpaBaseDao 实体的Repository
     * @param entity 实体对象
     * @param notGroupList 不分组的条件字段名，每个字段都是一个独立的条件，而且只能是EQ
     * @param groupList 分组的条件字段名，只支持一个分组条件，传入的字段名各个之间的关系为OR，与值的关系也是EQ
     */
    public static void checkRepeat(@NotNull JpaBaseDao jpaBaseDao, @NotNull BaseDomain entity,
                                   @Nullable List<String> notGroupList, @Nullable List<String> groupList) {
        checkRepeat(jpaBaseDao, entity, notGroupList, groupList, null);
    }

    /**
     * 批量删除增加公司代码条件，不能只使用主键删除了别的公司的，
     * 如果不需要公司代码限制，直接调用{@link JpaBaseDao#deleteByIds(Iterable)}即可
     * @param idList 主键列表
     * @return
     */
    public static List<QueryParam> multiDeleteAddCompCode(List<String> idList) {
        List<QueryParam> queryParams = new ArrayList<>();
        addCompCodeIfNotPlatformUser(queryParams);
        queryParams.add(new QueryParam<>("uuid", QueryParam.Op.IN, idList));
        return queryParams;
    }
}
