package com.ddf.scaffold.fw.jpa;

import com.ddf.scaffold.fw.entity.BaseDomain;
import com.ddf.scaffold.fw.entity.CompanyDomain;
import com.ddf.scaffold.fw.exception.GlobalCustomizeException;
import com.ddf.scaffold.fw.exception.GlobalExceptionEnum;
import com.ddf.scaffold.fw.session.RequestContext;
import com.ddf.scaffold.fw.session.SessionContext;
import com.ddf.scaffold.fw.util.ConstUtil;
import com.ddf.scaffold.fw.util.ContextKey;
import com.ddf.scaffold.fw.util.FieldExtend;
import com.ddf.scaffold.fw.util.QueryParam;
import org.hibernate.StaleObjectStateException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author DDf on 2018/12/11
 */
public class JpaBaseDaoImpl<T extends BaseDomain, S> extends SimpleJpaRepository<T, S> implements JpaBaseDao<T, S> {

    private static final String ID_MUST_NOT_BE_NULL = "The given id must not be null!";

    /**
     * @see JpaBaseDaoAspect
     */
    private ThreadLocal<RequestContext> localRequestContext;

    private ThreadLocal<SessionContext> localSessionContext;

    /**
     * JPA操作数据的原生对象
     */
    private EntityManager entityManager;

    /**
     * 包含实体类对象的信息，如类名
     */
    private JpaEntityInformation entityInformation;

    /**
     * 当前实体类名
     */
    private String entityName;

    /**
     * 缓存每次调用自定义查询的实体对象的属性信息，重复调用不会重复缓存，因此当系统启动后，
     * 修改实体信息需要重启服务，提供一个请求参数{@link ContextKey#flushCache}为1的情况下可强制重新缓存，慎用
     */
    private Map<String, Map<String, FieldExtend>> classMap = new ConcurrentHashMap<>();


    /**
     * 获得JPA操作数据的原生对象和当前的被操作的实体对象信息
     *
     * @param entityInformation 当前操作的实体类信息
     * @param entityManager     {@link EntityManager}
     */
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    JpaBaseDaoImpl(JpaEntityInformation entityInformation,
                   EntityManager entityManager) {
        super(entityInformation, entityManager);

        // Keep the EntityManager around to used from the newly introduced methods.
        this.entityManager = entityManager;
        this.entityInformation = entityInformation;
        this.entityName = entityInformation.getJavaType().getSimpleName();
    }

    /**
     * 将EntityManager对象暴露出去，以供自己的Repository可以使用原生对象进行某些操作
     *
     * @return
     */
    @Override
    public EntityManager getEntityManager() {
        return this.entityManager;
    }

    @Override
    public void setRequestContext(ThreadLocal<RequestContext> requestContext) {
        this.localRequestContext = requestContext;
    }

    @Override
    public ThreadLocal<RequestContext> getRequestContext() {
        return this.localRequestContext;
    }


    @Override
    public void setSessionContext(ThreadLocal<SessionContext> sessionContext) {
        this.localSessionContext = sessionContext;
    }

    @Override
    public ThreadLocal<SessionContext> getSessionContext() {
        return this.localSessionContext;
    }

    /**
     * 根据主键查找一条记录
     *
     * @param id 主键
     */
    @Override
    public Optional<T> findById(@NotNull S id) {
        Assert.notNull(id, ID_MUST_NOT_BE_NULL);
        String sql = " from " + entityName + " where uuid = :uuid and removed = 0 ";
        T singleResult;
        try {
            singleResult = (T) entityManager.createQuery(sql, entityInformation.getJavaType()).
                    setParameter("uuid", id).getSingleResult();
        } catch (EmptyResultDataAccessException | NoResultException e) {
            return Optional.empty();
        }
        if (singleResult != null) {
            return Optional.of(singleResult);
        }
        return Optional.empty();
    }


    /**
     * 复写SimpleJpaRepository的deleteById,将删除改为逻辑删除
     *
     * @param id
     */
    @Override
    @Transactional
    public void deleteById(@NotNull S id) {
        Assert.notNull(id, ID_MUST_NOT_BE_NULL);
        String uid = getUid();
        int num = entityManager.createQuery("update " + entityName + " t set t.removed = 1, t.version = t.version + 1, " +
                "t.modifyBy = :modifyBy, t.modifyTime = :modifyTime where t.uuid = :uuid").setParameter("uuid", id)
                .setParameter("modifyBy", uid).setParameter("modifyTime", new Date(), TemporalType.TIMESTAMP).executeUpdate();
        if (num == 0) {
            throw new GlobalCustomizeException(GlobalExceptionEnum.UPDATE_ERROR, num);
        }
    }


    /**
     * 复写SimpleJpaRepository的删除对象方法，获取对象的id,根据id逻辑删除数据，对象主键必须为id
     *
     * @param entity
     */
    @Override
    @Transactional
    public void delete(@NotNull T entity) {
        deleteById(getId(entity));
    }

    @Override
    public List<T> findAllById(Iterable<S> s) {
        return super.findAllById(s);
    }

    @Override
    public Page<T> findAll(@NotNull Pageable pageable) {
        return pageByQueryParams(null, pageable);
    }

    /**
     * 单表根据字段值和value匹配查询返回一个对象，默认removed = 0
     *
     * @param propertiesMap field-value键值对
     * @return
     */
    @Override
    public T findOneByProperties(@NotNull Map<String, Object> propertiesMap) {
        return findOneByProperties(propertiesMap, false);
    }


    /**
     * 根据条件返回一条记录，如果匹配多条户抛出异常
     *
     * @param propertiesMap field-value键值对
     * @param isRemoved     是否包含removed=1的条件
     */
    @Override
    public T findOneByProperties(@NotNull Map<String, Object> propertiesMap, boolean isRemoved) {
        StringBuffer sbl = new StringBuffer(commonHead(isRemoved));
        List<QueryParam> queryParams = transToQueryParams(propertiesMap);
        Query typeQuery = buildQueryParamsSql(sbl, queryParams, true, Sort.unsorted());
        try {
            return (T) typeQuery.getSingleResult();
        } catch (EmptyResultDataAccessException | NoResultException e) {
            return null;
        }
    }


    /**
     * 将简单的Map格式的条件转换为QueryParam格式，方便统一使用QueryParam处理
     *
     * @param propertiesMap
     * @return
     */
    private List<QueryParam> transToQueryParams(@NotNull Map<String, Object> propertiesMap) {
        List<QueryParam> queryParams = new ArrayList<>();
        propertiesMap.forEach((k, v) -> queryParams.add(new QueryParam<>(k, v)));
        return queryParams;
    }

    /**
     * 单表根据字段值和value匹配查询返回对象列表
     *
     * @param propertiesMap field-value键值对
     */
    @Override
    public List<T> findByProperties(@NotNull Map<String, Object> propertiesMap) {
        return findByProperties(propertiesMap, false);
    }


    /**
     * @param propertiesMap field-value键值对
     * @param isRemoved     是否包含removed=1的条件
     */
    @Override
    public List<T> findByProperties(@NotNull Map<String, Object> propertiesMap, boolean isRemoved) {
        StringBuffer sbl = new StringBuffer(commonHead(isRemoved));
        List<QueryParam> queryParams = transToQueryParams(propertiesMap);
        return buildQueryParamsSql(sbl, queryParams, true, Sort.unsorted()).getResultList();
    }

    /**
     * 分页根据查询键值对查询条件返回对象分页数据
     *
     * @param propertiesMap field-value键值对
     * @param pageable      分页对象
     */
    @Override
    public Page<T> pageByProperties(@NotNull Map<String, Object> propertiesMap, @NotNull Pageable pageable) {
        if (propertiesMap != null && !propertiesMap.isEmpty()) {
            StringBuffer sbl = new StringBuffer(commonHead(false));
            List<QueryParam> queryParams = transToQueryParams(propertiesMap);
            Query query = buildQueryParamsSql(sbl, queryParams, true, pageable.getSort());
            return responsePage(query, queryParams, pageable);
        }
        throw new GlobalCustomizeException(GlobalExceptionEnum.DB_ERROR);
    }

    /**
     * @param queryParams {@link QueryParam}
     * @param pageable    分页对象
     * @return
     */
    @Override
    public Page<T> pageByQueryParams(List<QueryParam> queryParams, @NotNull Pageable pageable) {
        StringBuffer sbl = new StringBuffer(commonHead(false));
        Query query = buildQueryParamsSql(sbl, queryParams, true, pageable.getSort());
        return responsePage(query, queryParams, pageable);
    }

    /**
     * 处理分页结果
     *
     * @param query
     * @param queryParams
     * @param pageable
     * @return
     */
    private Page<T> responsePage(@NotNull Query query, List<QueryParam> queryParams, @NotNull Pageable pageable) {
        query.setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
                .setMaxResults((pageable.getPageNumber()) * pageable.getPageSize() + pageable.getPageSize());
        List<T> result = query.getResultList();
        if (result == null) {
            result = new ArrayList<>();
        }
        return new PageImpl<>(result, pageable, querySize(queryParams, null));
    }


    /**
     * 单表根据QueryParam查询对象来匹配查询条件返回一条数据，如果查询到多条会报错
     *
     * @param queryParams 查询条件对象
     * @return
     */
    @Override
    public T findOneByQueryParams(@NotNull List<QueryParam> queryParams) {
        return findOneByQueryParams(queryParams, false);
    }

    /**
     * 单表根据QueryParam查询对象来匹配查询条件返回一条数据，如果查询到多条会报错
     *
     * @param queryParams 查询条件对象
     * @param isRemoved   是否包含removed=1的条件
     * @return
     */
    @Override
    public T findOneByQueryParams(@NotNull List<QueryParam> queryParams, boolean isRemoved) {
        StringBuffer sbl = new StringBuffer(commonHead(isRemoved));
        try {
            return (T) buildQueryParamsSql(sbl, queryParams, true, null).getSingleResult();
        } catch (EmptyResultDataAccessException | NoResultException e) {
            return null;
        }
    }

    /**
     * 单表根据QueryParam查询对象来匹配查询条件返回数据列表
     *
     * @param queryParams 查询条件对象
     * @return
     */
    @Override
    public List<T> findByQueryParams(@NotNull List<QueryParam> queryParams) {
        return findByQueryParams(queryParams, false);
    }

    /**
     * 单表根据QueryParam查询对象来匹配查询条件返回数据列表
     *
     * @param queryParams 查询条件对象
     * @param isRemoved   是否包含removed=1的条件
     * @return
     */
    @Override
    public List<T> findByQueryParams(@NotNull List<QueryParam> queryParams, boolean isRemoved) {
        StringBuffer sbl = new StringBuffer(commonHead(isRemoved));
        return buildQueryParamsSql(sbl, queryParams, true, null).getResultList();
    }

    /**
     * 查询返回结果总条数
     *
     * @param queryParams 查询条件对象
     * @param countField  count的字段
     * @return
     */
    @Override
    public Long querySize(@NotNull List<QueryParam> queryParams, String countField) {
        StringBuffer sbl = new StringBuffer();
        if (StringUtils.isEmpty(countField)) {
            countField = "*";
        }
        sbl.append("SELECT COUNT(").append(countField).append(") FROM ").append(entityName).append(" WHERE removed = 0 ");
        Query query = buildQueryParamsSql(sbl, queryParams, false, Sort.unsorted());
        Object singleResult;
        try {
            singleResult = query.getSingleResult();
        } catch (EmptyResultDataAccessException | NoResultException e) {
            return 0L;
        }
        return Long.valueOf(singleResult.toString());
    }

    @Override
    public Long querySize(@NotNull List<QueryParam> queryParams) {
        return querySize(queryParams, null);
    }


    /**
     * 根据简单条件查询匹配大小
     *
     * @param propertiesMap 查询条件对象
     * @param countField    count的字段
     * @return
     */
    @Override
    public Long querySize(@NotNull Map<String, Object> propertiesMap, String countField) {
        if (propertiesMap != null && !propertiesMap.isEmpty()) {
            List<QueryParam> queryParams = transToQueryParams(propertiesMap);
            return querySize(queryParams, countField);
        }
        return 0L;
    }

    @Override
    public Long querySize(@NotNull Map<String, Object> propertiesMap) {
        return querySize(propertiesMap, null);
    }

    /**
     * 根据主键集合删除多条数据,影响函数为0时抛出异常
     * @param iterable
     */
    @Override
    public void deleteByIds(@NotNull Iterable<S> iterable) {
        if (iterable == null) {
            throw new IllegalArgumentException("iterable must not be null!");
        }
        String uid = getUid();
        int num = entityManager.createQuery("update " + entityName + " t set t.removed = 1, t.version = t.version + 1, " +
                "t.modifyBy = :modifyBy, t.modifyTime = :modifyTime where t.uuid in (:uuid)").setParameter("uuid", iterable)
                .setParameter("modifyBy", uid).setParameter("modifyTime", new Date(), TemporalType.TIMESTAMP).executeUpdate();
        if (num == 0) {
            throw new GlobalCustomizeException(GlobalExceptionEnum.UPDATE_ERROR, num);
        }
    }

    @Override
    public List<T> findByRequestContext() {
        return null;
    }

    @Override
    public T findOneByRequestContext() {
        return null;
    }


    /**
     * 根据检查相等查询条件删除数据，影响函数为0不报错
     * @param properties
     * @return
     */
    @Override
    public Integer deleteByProperties(Map<String, Object> properties) {
        if (properties != null && !properties.isEmpty()) {
            List<QueryParam> queryParams = transToQueryParams(properties);
            return deleteByQueryParams(queryParams);
        }
        return 0;
    }

    /**
     * 根据复杂条件删除数据，，影响函数为0不报错
     * @param queryParams
     * @return
     */
    @Override
    public Integer deleteByQueryParams(List<QueryParam> queryParams) {
        if (queryParams != null && queryParams.size() > 0) {
            Map<String, Object> fieldMap = new HashMap<>(4);
            fieldMap.put("removed", 1);
            return updateByMap(fieldMap, queryParams);
        }
        return 0;
    }

    private String commonHead(boolean isRemoved) {
        String headSql = "FROM " + entityName + " ";
        if (isRemoved) {
            headSql += " WHERE removed = 1";
        } else {
            headSql += " WHERE removed = 0";
        }
        return headSql;
    }


    /**
     * 根据条件更新个别字段的值
     *
     * @param fieldMap    需要更新的字段值
     * @param queryParams 更新的where条件
     */
    @Override
    @Transactional
    public Integer updateByMap(@NotNull Map<String, Object> fieldMap, @NotNull List<QueryParam> queryParams) {
        StringBuffer sbl = new StringBuffer();
        sbl.append(" update ").append(entityName).append(" set version = version + 1, modifyBy = :modifyBy, ")
                .append("modifyTime = :modifyTime");
        if (fieldMap != null && !fieldMap.isEmpty()) {
            fieldMap.forEach((k, v) -> sbl.append(", ").append(k).append(" = :").append(k));
        }
        sbl.append(" where removed = 0");
        Query query;
        if (queryParams != null && !queryParams.isEmpty()) {
            query = buildQueryParamsSql(sbl, queryParams, false, Sort.unsorted());
        } else {
            throw new GlobalCustomizeException("该方法不允许全表更新！");
        }
        String uid = getUid();
        query.setParameter("modifyBy", uid)
                .setParameter("modifyTime", new Date(), TemporalType.TIMESTAMP);
        if (fieldMap != null && !fieldMap.isEmpty()) {
            fieldMap.forEach(query::setParameter);
        }
        return query.executeUpdate();
    }

    /**
     * 获得当前用户的唯一标识符，登陆后可将用户指定属性赋值给uid
     *
     * @return
     */
    private String getUid() {
        String uid;
        if (localSessionContext == null || localSessionContext.get().getUid() == null) {
             uid = ConstUtil.ANONYMOUS_NAME;
        } else if (localSessionContext.get().getUid() == null) {
            throw new GlobalCustomizeException(GlobalExceptionEnum.UID_NOT_EXIST);
        } else {
            uid = localSessionContext.get().getUid();
        }
        return uid;
    }


    /**
     * 根据查询对象构造HQL
     *
     * 为了避免同一个字段当作条件多次后续按照参数名称来设置实际参数值出现覆盖，因此参数的占位符做了特殊后缀处理
     *
     * @param sbl         预先定义的sql片段
     * @param queryParams {@link QueryParam}
     * @param typedQuery  当前sql返回结果是否是当前实体对象类型
     * @param sort        排序对象
     * @return
     */
    private Query buildQueryParamsSql(
            StringBuffer sbl, @NotNull List<QueryParam> queryParams, boolean typedQuery, Sort sort) {
        if (sbl == null) {
            sbl = new StringBuffer(100);
        }

        // 特殊类型处理
        castFieldValueByType(queryParams);

        // 处理单表复杂查询条件
        if (queryParams != null && !queryParams.isEmpty()) {
            Map<String, FieldExtend> fieldMap = getCachedFieldMap();

            // QueryParam如果groupName相同，则视为一个()内的条件
            Map<String, List<QueryParam>> groupMap = queryParams.stream().filter(qm -> !StringUtils.isEmpty(qm.getGroupName())).collect(Collectors
                    .groupingBy(QueryParam::getGroupName));

            // groupName为空，则采用默认的处理，每个条件都是独立的
            List<QueryParam> notGroupList = queryParams.stream().filter(qm -> StringUtils.isEmpty(qm.getGroupName())).collect(Collectors.toList());

            if (!notGroupList.isEmpty()) {
                dealGroupOrNotGroup(sbl, notGroupList, fieldMap, false);
            }

            if (groupMap != null && !groupMap.isEmpty()) {
                for (Map.Entry<String, List<QueryParam>> entry : groupMap.entrySet()) {
                    dealGroupOrNotGroup(sbl, entry.getValue(), fieldMap, true);
                }
            }
        }

        // 处理RequestContext中的paramMap的与实体属性匹配的参数
        /*boolean contextToField = false;
        if (localRequestContext != null && ConstUtil.TRUE_STR.equals(localRequestContext.get().get(ContextKey.contextToField.name()))) {
            Map<String, FieldExtend> fieldMap = getCachedFieldMap();
            for (Map.Entry<String, Object> entry : localRequestContext.get().entrySet()) {
                String k = entry.getKey();
                Object v = entry.getValue();
                if (fieldMap.containsKey(k) && !StringUtils.isEmpty(v)) {
                    contextToField = true;
                    sbl.append(" and ").append(k).append(" = :").append(k).append("_").append(Math.abs(v.hashCode()));
                }
            }
        }*/

        // 处理排序
        if (sort != null && sort != Sort.unsorted()) {
            Iterator<Sort.Order> iterator = sort.iterator();
            boolean appendOrderBy = false;
            while (iterator.hasNext()) {
                if (!appendOrderBy) {
                    sbl.append(" order by ");
                } else {
                    sbl.append(", ");
                }
                appendOrderBy = true;
                Sort.Order next = iterator.next();
                sbl.append(next.getProperty()).append(" ").append(next.getDirection()).append(" ");
            }
        }

        Query query;
        if (typedQuery) {
            query = entityManager.createQuery(sbl.toString(), entityInformation.getJavaType());
        } else {
            query = entityManager.createQuery(sbl.toString());
        }

        // 填充处理单表复杂查询条件的占位符参数
        if (queryParams != null && !queryParams.isEmpty()) {
            queryParams.forEach(queryParam -> {
                QueryParam.Op op = queryParam.getOp();
                if (queryParam.getValue() == null) {
                    return;
                }
                if (!QueryParam.Op.NN.equals(op) && !QueryParam.Op.NI.equals(op)) {
                    boolean isLike = false;
                    if (QueryParam.Op.LIKE.equals(queryParam.getOp())) {
                        if (!queryParam.getValue().toString().contains(ConstUtil.STRING_PERCENT)) {
                            isLike = true;
                        }
                    }
                    if (isLike) {
                        query.setParameter(queryParam.getKey() + "_" + Math.abs(queryParam.getValue().hashCode()), ConstUtil.STRING_PERCENT + queryParam.getValue() + ConstUtil.STRING_PERCENT);
                    } else {
                        query.setParameter(queryParam.getKey() + "_" + Math.abs(queryParam.getValue().hashCode()), queryParam.getValue());
                    }
                }
            });
        }

        // 填充处理RequestContext中的paramMap的与实体属性匹配的参数占位符
        /*if (contextToField) {
            Map<String, Object> paramMap = localRequestContext.get();
            Map<String, FieldExtend> fieldMap = getCachedFieldMap();
            paramMap.forEach((k, v) -> {
                if (fieldMap.containsKey(k) && !StringUtils.isEmpty(v)) {
                    FieldExtend fieldExtend = fieldMap.get(k);
                    Field field = fieldExtend.getField();
                    // 处理Date类型的处理,value支持Date对象和Long类型的时间毫秒值
                    if (field.getType().isAssignableFrom(Date.class)) {
                        if (v instanceof Long) {
                            v = (new Date((Long) v));
                        }
                    }
                    if (v.getClass().isAssignableFrom(field.getType())) {
                        query.setParameter(k + "_" + Math.abs(v.hashCode()), (field.getType()));
                    }
                }
            });
        }*/

        return query;
    }

    /**
     *   重要： 如果提示参数类型不匹配的时候，请查询该方法是否包含了实体中所使用的类型；以下方法并没有
     *   将全部数据类型都进行转换，而是几个常见的类型，如果不满足自己使用，请添加即可；
     *  目前默认只支持的参数类型只有Integer和String,其它参数都必须转换成字段类型对应的值才可以正常查询；
     *  如Integer类型的参数则传过来一定不能有引号，否则会无法查询；但如果是Byte类型，传过来的值是1，more
     *  也不能正常的和参数类型匹配；如果在后端则必须(byte) 1传过来，而从前端则无法传入明确的参数类型的值，
     *  如果不处理会出现类型不匹配的错误，因此在这里进行转换;
     *  第二个对于in的条件处理，前端传入特殊的字符串，这里要将字符串根据字段类型转换成对应类型的数组格式才能去处理In
     * @param queryParams
     */
    private void castFieldValueByType(List<QueryParam> queryParams) {
        if (queryParams != null && !queryParams.isEmpty()) {
            Map<String, FieldExtend> fieldMap = getCachedFieldMap();
            Iterator<QueryParam> iterator = queryParams.iterator();
            while (iterator.hasNext()) {
                QueryParam queryParam = iterator.next();
                QueryParam.Op op = queryParam.getOp();
                // 与值无关的操作符不需要转换
                if (QueryParam.Op.NN.equals(op) || QueryParam.Op.NI.equals(op)) {
                    continue;
                }

                if (queryParam.getValue() == null || "".equals(queryParam.getValue())) {
                    iterator.remove();
                }
                FieldExtend fieldExtend = fieldMap.get(queryParam.getKey());
                Field field = fieldExtend.getField();


                if (String.class.getName().equals(field.getType().getName()) && QueryParam.Op.IN.equals(op)) {
                    queryParam.setValue(Arrays.asList(String.valueOf(queryParam.getValue()).split(",")));
                }
                // 处理Date类型的处理,value支持Date对象和Long类型的时间毫秒值
                else if (field.getType().isAssignableFrom(Date.class)) {
                    if (queryParam.getValue() instanceof Long) {
                        queryParam.setValue(new Date((Long) queryParam.getValue()));
                    }
                } else if (Integer.class.getName().equals(field.getType().getName())) {
                    if (QueryParam.Op.IN.equals(op)) {
                        if (!(queryParam.getValue() instanceof List)) {
                            List<Integer> intList = new ArrayList<>();
                            String[] strArr = String.valueOf(queryParam.getValue()).split(",");
                            for (String str : strArr) {
                                intList.add(Integer.parseInt(str));
                            }
                            queryParam.setValue(intList);
                        }
                    } else {
                        queryParam.setValue(Integer.parseInt(String.valueOf(queryParam.getValue())));
                    }
                }  else if (Byte.class.getName().equals(field.getType().getName())) {
                    if (QueryParam.Op.IN.equals(op)) {
                        if (!(queryParam.getValue() instanceof List)) {
                            List<Byte> byteList = new ArrayList<>();
                            String[] strArr = String.valueOf(queryParam.getValue()).split(",");
                            for (String str : strArr) {
                                byteList.add(Byte.parseByte(str));
                            }
                            queryParam.setValue(byteList);
                        }
                    } else {
                        queryParam.setValue(Byte.parseByte(String.valueOf(queryParam.getValue())));
                    }
                } else if (BigDecimal.class.getName().equals(field.getType().getName())) {
                    if (QueryParam.Op.IN.equals(op)) {
                        if (!(queryParam.getValue() instanceof List)) {
                            List<BigDecimal> bigDecimalList = new ArrayList<>();
                            String[] bigDecimals = String.valueOf(queryParam.getValue()).split(",");
                            for (String bigDecimal : bigDecimals) {
                                bigDecimalList.add(new BigDecimal(bigDecimal));
                            }
                            queryParam.setValue(bigDecimalList);
                        }
                    } else {
                        queryParam.setValue(new BigDecimal(String.valueOf(queryParam.getValue())));
                    }
                }
            }
        }
    }

    /**
     * 处理多个条件之间的关系
     * 为了避免同一个字段当作条件多次后续按照参数名称来设置实际参数值出现覆盖，因此参数的占位符做了特殊后缀处理
     * @param sbl
     * @param queryParams 查询条件
     * @param fieldMap 当前实体类的属性信息
     * @param isGroup 是否需要分组处理
     */
    private void dealGroupOrNotGroup(
            StringBuffer sbl, @NotNull List<QueryParam> queryParams, Map<String, FieldExtend> fieldMap, boolean isGroup) {
        if (isGroup) {
            // sbl.append(" AND ( 1 = 1 ");
            sbl.append(" AND ( ");
        }
        boolean appendRelative = false;
        for (QueryParam param : queryParams) {
            defaultQueryParamField(param);
            String key = param.getKey();
            Object value = param.getValue();
            if (value == null) {
                continue;
            }
            QueryParam.Op op = param.getOp();
            if (!fieldMap.containsKey(key)) {
                throw new GlobalCustomizeException(GlobalExceptionEnum.FIELD_NOT_MATCH, key,
                        entityInformation.getJavaType().getName());
            }
            if (!isGroup || appendRelative) {
                sbl.append(" ").append(param.getRelative().getValue());
            }
            appendRelative = true;
            sbl.append(" ").append(key).append(" ").append(param.getOp().getValue());
            if (!QueryParam.Op.NN.equals(op) && !QueryParam.Op.NI.equals(op)) {
                sbl.append(" :").append(key).append("_").append(Math.abs(param.getValue().hashCode()));
            }
        }
        if (isGroup) {
            sbl.append(" ) ");
        }
    }

    /**
     * 给QueryParam的空字段的值赋值默认值，虽然构造函数中常用的几种方式都提供了默认值，但对于前端来说必须全部给与默认值，
     * 否则就要这里给默认值
     */
    private void defaultQueryParamField(@NotNull QueryParam queryParam) {
        if (queryParam.getOp() == null) {
            queryParam.setOp(QueryParam.Op.EQ);
        }
        if (queryParam.getRelative() == null) {
            queryParam.setRelative(QueryParam.Relative.AND);
        }
    }


    /**
     * 获得对象的字段，然后缓存起来，下次使用直接返回，
     * 存在问题，如果字段进行修改，但项目没有重启，会存在字段对应不上的问题，所以再修改字段后，最好重启服务;
     * 提供一个请求参数{@link ContextKey#flushCache}，如果为1则强制刷新缓存，一般最好不要使用；
     */
    private Map<String, FieldExtend> getCachedFieldMap() {
        Class clazz = entityInformation.getJavaType();
        String className = clazz.getSimpleName();
        Map<String, FieldExtend> fieldMap = classMap.get(className);
        if (fieldMap == null || (localRequestContext != null && ConstUtil.TRUE_STR
                .equals(localRequestContext.get().get(ContextKey.flushCache.name())))) {
            fieldMap = new HashMap<>(20);
            Field[] fields;
            // BaseDomain
            if (BaseDomain.class.isAssignableFrom(clazz)) {
                fields = BaseDomain.class.getDeclaredFields();
                for (Field field : fields) {
                    FieldExtend fieldExtend = new FieldExtend(field, field.getAnnotation(Column.class));
                    fieldMap.put(field.getName(), fieldExtend);
                }
            }
            // CompanyDomain
            if (CompanyDomain.class.isAssignableFrom(clazz)) {
                fields = CompanyDomain.class.getDeclaredFields();
                for (Field field : fields) {
                    FieldExtend fieldExtend = new FieldExtend(field, field.getAnnotation(Column.class));
                    fieldMap.put(field.getName(), fieldExtend);
                }
            }
            fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                FieldExtend fieldExtend = new FieldExtend(field, field.getAnnotation(Column.class));
                fieldMap.put(field.getName(), fieldExtend);
            }
            classMap.put(className, fieldMap);
        }
        return fieldMap;
    }

    /**
     * 返回指定对象的所有数据，已过滤removed = 1的数据
     *
     * @return
     */
    @Override
    public List<T> findAll() {
        Map<String, FieldExtend> cachedFieldMap = getCachedFieldMap();
        if (cachedFieldMap.containsKey("removed")) {
            String sql = " FROM " + entityName + " WHERE removed = 0 ";
            return entityManager.createQuery(sql, entityInformation.getJavaType()).getResultList();
        }
        return super.findAll();
    }

    @Override
    @Transactional
    public <S1 extends T> S1 save(@NotNull S1 entity) {
        if (getId(entity) != null) {
            if (entity.getVersion() == null) {
                throw new GlobalCustomizeException(GlobalExceptionEnum.VERSION_MISSION);
            }
        }
        try {
            if (this.entityInformation.isNew(entity)) {
                this.entityManager.persist(entity);
                return entity;
            } else {
                return this.entityManager.merge(entity);
            }
            // fixme catch不到
        } catch (ObjectOptimisticLockingFailureException | StaleObjectStateException exception) {
            throw new GlobalCustomizeException("数据已被修改，请刷新重试");
        }
    }


    @Override
    @Transactional
    public <S1 extends T> List<S1> saveAll(Iterable<S1> entities) {
        Assert.notNull(entities, "The given Iterable of entities not be null!");

        List<S1> result = new ArrayList<>();

        for (S1 entity : entities) {
            result.add(save(entity));
        }

        return result;
    }

    @Override
    public boolean existsById(S id) {
        Assert.notNull(id, ID_MUST_NOT_BE_NULL);
        return findById(id).isPresent();
    }

    private S getId(@NotNull T entity) {
        return (S) entity.getId();
    }
}
