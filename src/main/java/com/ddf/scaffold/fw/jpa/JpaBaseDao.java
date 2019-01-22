package com.ddf.scaffold.fw.jpa;

import com.ddf.scaffold.fw.entity.BaseDomain;
import com.ddf.scaffold.fw.session.RequestContext;
import com.ddf.scaffold.fw.session.SessionContext;
import com.ddf.scaffold.fw.util.QueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import javax.persistence.EntityManager;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * @author DDf on 2018/12/11
 */
@NoRepositoryBean
public interface JpaBaseDao<T extends BaseDomain, S> extends JpaRepository<T, S> {

    /**
     * 向上暴露EntityManager对象
     * @return
     */
    EntityManager getEntityManager();

    /**
     * 设置RequestContext 的值，因为{@link JpaBaseDaoImpl}非容器管理类，获取不了，只能提供方法通过反射设置
     * @param requestContext
     * @see JpaBaseDaoAspect
     */
    void setRequestContext(ThreadLocal<RequestContext> requestContext);

    /**
     * 设置SessionContext 的值，因为{@link JpaBaseDaoImpl}非容器管理类，获取不了，只能提供方法通过反射设置
     * @param sessionContext
     * @see JpaBaseDaoAspect
     */
    void setSessionContext(ThreadLocal<SessionContext> sessionContext);

    /**
     * 获得RequestContext的值，必须调用{@link JpaBaseDao#setRequestContext(ThreadLocal)}，才会有值，用于使用完毕后释放对象
     * @see JpaBaseDaoAspect
     * @return
     */
    ThreadLocal<RequestContext> getRequestContext();

    /**
     * 获得SessionContext的值，必须调用{@link JpaBaseDao#setSessionContext(ThreadLocal)}，才会有值，用于使用完毕后释放对象
     * @see JpaBaseDaoAspect
     * @return
     */
    ThreadLocal<SessionContext> getSessionContext();

    /**
     * 根据条件更新个别字段的值
     *
     * @param fieldMap    需要更新的字段值
     * @param queryParams 更新的where条件
     * @return
     */
    Integer updateByMap(@NotNull Map<String, Object> fieldMap, @NotNull List<QueryParam> queryParams);

    /**
     * 单表根据字段值和value匹配查询返回一个对象，默认removed = 0
     *
     * @param propertiesMap field-value键值对
     * @return
     */
    T findOneByProperties(@NotNull Map<String, Object> propertiesMap);

    /**
     * 单表根据字段值和value匹配查询返回一个对象
     *
     * @param propertiesMap field-value键值对
     * @param isRemoved     是否包含removed=1的条件
     * @return
     */
    T findOneByProperties(@NotNull Map<String, Object> propertiesMap, boolean isRemoved);

    /**
     * 单表根据字段值和value匹配查询返回对象列表
     *
     * @param propertiesMap field-value键值对
     * @return
     */
    List<T> findByProperties(@NotNull Map<String, Object> propertiesMap);


    /**
     * 单表根据字段值和value匹配查询返回对象列表
     *
     * @param propertiesMap field-value键值对
     * @param isRemoved     是否包含removed=1的条件
     * @return
     */
    List<T> findByProperties(@NotNull Map<String, Object> propertiesMap, boolean isRemoved);

    /**
     * 分页根据查询键值对查询条件返回对象分页数据
     *
     * @param propertiesMap field-value键值对
     * @param pageable      分页对象
     * @return
     */
    Page<T> pageByProperties(@NotNull Map<String, Object> propertiesMap, @NotNull Pageable pageable);

    /**
     * 分页根据负责条件查询数据
     *
     * @param queryParams {@link QueryParam}
     * @param pageable    分页对象
     * @return
     */
    Page<T> pageByQueryParams(@NotNull List<QueryParam> queryParams, @NotNull Pageable pageable);


    /**
     * 单表根据QueryParam查询对象来匹配查询条件返回一条数据，如果查询到多条会报错
     *
     * @param queryParams 查询条件对象
     * @return
     */
    T findOneByQueryParams(@NotNull List<QueryParam> queryParams);


    /**
     * 单表根据QueryParam查询对象来匹配查询条件返回一条数据，如果查询到多条会报错
     *
     * @param queryParams 查询条件对象
     * @param isRemoved   是否包含removed=1的条件
     * @return
     */
    T findOneByQueryParams(@NotNull List<QueryParam> queryParams, boolean isRemoved);


    /**
     * 单表根据QueryParam查询对象来匹配查询条件返回数据列表
     *
     * @param queryParams 查询条件对象
     * @return
     */
    List<T> findByQueryParams(@NotNull List<QueryParam> queryParams);


    /**
     * 单表根据QueryParam查询对象来匹配查询条件返回数据列表
     *
     * @param queryParams 查询条件对象
     * @param isRemoved   是否包含removed=1的条件
     * @return
     */
    List<T> findByQueryParams(@NotNull List<QueryParam> queryParams, boolean isRemoved);


    /**
     * 复杂条件查询返回结果总条数
     *
     * @param queryParams 查询条件对象
     * @param countField  count的字段
     * @return
     */
    Long querySize(@NotNull List<QueryParam> queryParams, String countField);


    /**
     * 简单条件查询返回结果总条数
     *
     * @param propertiesMap 查询条件对象
     * @param countField    count的字段
     * @return
     */
    Long querySize(@NotNull Map<String, Object> propertiesMap, String countField);
}
