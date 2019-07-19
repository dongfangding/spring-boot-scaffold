[TOC]

## 一、概览  
### 1. 基于工具与版本
|  技术 | 版本   |
| ------------ | ------------ |
| JDK  | 1.8 +  |
|  Spring-boot |2.1.0.RELEASE   |
|  Hibernate | 5.3.7.Final  |
|  Spring-data-jpa | 2.1.0.RELEASE  |
|  druid |  1.1.12 |
|  lombok | 1.18.2  |
以上只简单罗列一下基本工具与版本，后续因项目改造而加入其它的技术框架，则直接基于`Spring-boot`改造即可；

### 2. 整体结构预览
1. 与框架相关的源码都放在`com.ddf.scaffold.fw`包下  

2. 框架的主配置类请参考`com.ddf.scaffold.fw.config.WebConfig`  

3. 项目入口即`spring-boot`的主程序，使用方自己决定路径即可，该脚手架项目的主入口为`com.ddf.scaffold.Application`，主入口应保证项目的实体与`Repository`能够被正确扫描

4. 项目的主配置文件请参考`src/main/resources/application.yml`

### 3. 问题
1. 当前源码只是一个初步的项目，以上选型可能实际上并不合适或者某些方面的处理是否会有隐藏问题，都需要实际情况的使用来检验；
2. 等待发现罗列未解决或已有疑问的问题

## 二、 使用

### 1. 数据源
该脚手架数据源的实现采用`com.alibaba.druid.pool.DruidDataSource`，与数据源相关的配置类请参考`com.ddf.scaffold.fw.druid.DruidConfig`，再细分相关属性通过内部类来封装`com.ddf.scaffold.fw.druid.DruidConfig.Config`；请参考源码再对比如下配置查看
#### 1.1 数据源配置参考
```
spring:
  datasource:
    initialization-mode: never
    type: com.alibaba.druid.pool.DruidDataSource
    druidProperties:
      username: root
      password: 123456
      url: jdbc:mysql://localhost:3306/honeytask?characterEncoding=utf8&useSSL=true&serverTimezone=GMT%2B8&zeroDateTimeBehavior=convertToNull
      name: druid
      initialSize: 5
      minIdle: 5
      maxActive: 20
      maxWait: 60000
      timeBetweenEvictionRunsMillis: 60000
      minEvictableIdleTimeMillis: 300000
      validationQuery: SELECT 1 FROM DUAL
      testWhileIdle: true
      testOnBorrow: true
      testOnReturn: false
      filters: stat,wall,slf4j
      logSlowSql: true
      poolPreparedStatements: true
      maxOpenPreparedStatements: 20
```

#### 1.2 SQL监控管理
配置类通过如下代码，注册了一个`Servlet`来对 SQL进行监控管理，登录用户名和密码默认为数据库连接的用户名和密码，访问请输入链接,其中用`{}`包括的属于变量信息，请根据自己项目信息进行替换，如没有可以直接省略；

`http://{ip}:{port}/{context-path}/druid/login.html`;
```java
@Bean
public ServletRegistrationBean druidServlet() {
    ServletRegistrationBean reg = new ServletRegistrationBean();
    reg.setServlet(new StatViewServlet());
    reg.addUrlMappings("/druid/*");
    reg.addInitParameter("loginUsername", druidProperties.getUsername());
    reg.addInitParameter("loginPassword", druidProperties.getPassword());
    reg.addInitParameter("logSlowSql", druidProperties.getLogSlowSql());
    return reg;
}
```


### 2. 实体类
#### 2.1. 实体基类
该脚手架实体类均使用`lombok`来简化实体类代码,实体基类参考`com.ddf.scaffold.fw.entity.BaseDomain`，**使用该工程的项目的实体必须继承该基类**，源码如下：
```java
package com.ddf.scaffold.fw.entity;

import com.ddf.scaffold.fw.jpa.AuditorAwareImpl;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

/**
 * &#064;MappedSuperclass 定义实体类所公用属性的超类，映射超类不会生成单独的表，它的映射信息作用于继承自它的实体类。
 * &#064;EntityListeners(AuditingEntityListener.class) 提供对{@code &#064;CreatedDate, &#064;LastModifiedDate}。等注解的支持，
 *      该功能需要依赖{@code spring-aspects}
 * &#064;CreatedBy与&#064;LastModifiedBy的支持请参见 {@link AuditorAwareImpl}
 * @author DDf on 2018/9/29
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class BaseDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(insertable = false, updatable = false)
    protected Long id;

    @CreatedBy
    @Column(name = "CREATE_BY")
    protected String createBy;

    @CreatedDate
    @Column(name = "CREATE_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    protected Date createTime;

    @LastModifiedBy
    @Column(name = "MODIFY_BY")
    protected String modifyBy;

    @LastModifiedDate
    @Column(name = "MODIFY_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    protected Date modifyTime;

    @Column(name = "REMOVED")
    protected Integer removed = 0;

    @Column(name = "VERSION")
    @Version
    protected Integer version = 0;

    @Transient
    protected String compCode;
}
```

#### 2.2 审计信息
审计信息，即一个实体的`createBy`, `createTime`, `modifyBy`, `modifyTime`四个信息，用来记录一个实体的新增和编辑所发生的时间与操作人，每当实体被创建或修改时这几个字段的值就要相应的更新，而不需要编码者去关心这一块；其中`createTime`与`modifyTime`这两个时间信息，在基类中通过`@EntityListeners(AuditingEntityListener.class)`已经能够处理，但与具体操作人相关的需要额外的实现，`SpringDataJap`提供了一个接口`org.springframework.data.domain.AuditorAware`，请实现该接口来完成用户信息的设置，该脚手架已提供实现，请参考`com.ddf.scaffold.fw.jpa.AuditorAwareImpl`，源码如下，有部分在这里未讲解的信息，会在后面补充；
```java
package com.ddf.scaffold.fw.jpa;

import com.ddf.scaffold.fw.entity.BaseDomain;
import com.ddf.scaffold.fw.session.SessionContext;
import com.ddf.scaffold.fw.util.ConstUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 *
 * 获得实体类保存的审计信息，针对{@code @CreatedBy}和{@code @LastModifiedBy}来捕捉用户信息
 *
 * @see BaseDomain#getCreateBy()
 * @see BaseDomain#getModifyBy()
 * @see SessionContext 开发人员在用户登录后，必须将用户相关的信息{@link SessionContext#setUid(String)}和{@link SessionContext#setUser(BaseDomain)}设置用户信息
 * @author DDf on 2019/1/2
 */
@Component
public class AuditorAwareImpl implements AuditorAware {

    @Autowired
    private SessionContext sessionContext;

    @Override
    public Optional getCurrentAuditor() {
        if (sessionContext == null || sessionContext.getUid() == null) {
            return Optional.of(ConstUtil.ANONYMOUS_NAME);
        }
        return Optional.of(sessionContext.getUid());
    }
}
```

还需要在配置类中开启相应的功能
```java
@EnableJpaAuditing
@EnableAspectJAutoProxy
@EntityScan("com.ddf.scaffold.fw.entity")
@Configuration
public class WebConfig implements WebMvcConfigurer {}
```

#### 2.3 版本控制
基类提供了一个属性，`version`，使用注解`@Version`修饰，表名实体的修改需要通过该字段的是值来进行控制，对实体进行修改时，如传入的`version`的值与数据库重不一致时，则不能正确保存

#### 2.4 主键
主键的属性为id, 主键生成策略可以根据项目使用情况进行自适应的修改

#### 2.5 数据有效性
基类通过属性`removed`来表名数据是否被删除，仅当`removed=0`时，代表数据是有效的，而对数据进行删除时，通过修改`removed=1`来对数据进行逻辑删除


#### 2.6 参考
提供一个实体以供参考，其中实体需要继承基类并且需要和数据库对应表进行映射,
对如下注解做简单介绍
|  注解 |含义   |
| ------------ | ------------ |
| @Entity  | 将当前类映射为数据库实体  |
| @Table  |  指定当前实体与数据库的哪个表进行映射 |
| @ToString  | lombok注解，生成toString方法，callSuper = true即父类属性也使用  |
| @NoArgsConstructor  | lombok注解，空参构造函数  |
| @AllArgsConstructor  | lombok注解，全参构造函数  |
| @EqualsAndHashCode  |  lombok注解，生成equals和hashcode方法 |
| @Getter  | lombok注解，对实体属性生成getter方法  |
| @Setter |  lombok注解，对实体属性生成setter方法 |

```java
package com.ddf.scaffold.logic.entity;

import com.ddf.scaffold.fw.entity.BaseDomain;
import lombok.*;

import javax.persistence.*;
import java.util.Date;

/**
 * @author DDf on 2018/12/1
 */
@Entity
@Table(name = "USER")
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
public class User extends BaseDomain {
    @Column(name = "USER_NAME")
    private String userName;
    @Column(name = "PASSWORD")
    private String password;
    @Column(name = "EMAIL")
    private String email;
    @Column(name = "BIRTHDAY")
    @Temporal(TemporalType.TIMESTAMP)
    private Date birthDay;
    @Transient
    private String validateEmail;
    @Transient
    private String confirmPassword;
    @Transient
    private String oldPassword;
}
```


### 3. Repository
`Repository`定义了实体类如何与数据库进行交互查询的接口，本脚手架基于`org.springframework.data.jpa.repository.JpaRepository`自定义了一些常用的查询方法以及通过`com.ddf.scaffold.fw.jpa.JpaBaseDaoImpl`的自定义实现新增和覆盖覆盖了一些`org.springframework.data.jpa.repository.support.SimpleJpaRepository`的默认实现，关于这一块下面简单介绍一下注意事项即可；

#### 3.1 配置
* @NoRepositoryBean
必须在自定义接口类上加上注解`@NoRepositoryBean`，来表明里面的方法或属性不能按照默认的实体属性去解析（SpringDataJap的特性，会将方法上的属性当成实体属性去解析，以完成查询功能），注解完成之后还需要在配置类中指定`Repository`的自定义超类，并且指定当前项目中所有`Repository`所在的包路径，这两个属性属于同一个注解

```java
@NoRepositoryBean
public interface JpaBaseDao<T extends BaseDomain, S> extends JpaRepository<T, S> {
    
}
```

* @EnableJpaRepositories
除了`@NoRepositoryBean`之外，还必须通过`@EnableJpaRepositories`注解在配置类上来指明刚才的自定义超类实现，以及指定当前项目所有的`Repository`所在的包路径，因为两个属性在同一个注解上，所以如果要保证项目路径和这里配置的扫描路径要保持一致；还有属性值必须和实体的类型一直，否则会报错类型不匹配；
```java
@EnableJpaRepositories(value = {"com.ddf"},
        repositoryBaseClass = JpaBaseDaoImpl.class)
@Configuration
public class WebConfig implements WebMvcConfigurer {}
```

#### 3.2 基本使用
编码者必须通过接口来声明`Repository`并继承自定义超类`com.ddf.scaffold.fw.jpa.JpaBaseDao`，这样自定义超类才能获取当前`Repository`操作的实体信息等，即可使用自定义实现的查询功能，同时`SpringDataJap`提供的功能依然可以正常使用,以下提供一个默认的参考
```java
package com.ddf.scaffold.logic.repository;

import User;
import com.ddf.scaffold.fw.jpa.JpaBaseDao;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author DDf on 2018/12/1
 */
@Repository
@Transactional(readOnly = true)
public interface UserRepository extends JpaBaseDao<User, Long> {

}
```

#### 3.3 自定义Repository超类
该接口类为自定义`Repository`超类，实现为`com.ddf.scaffold.fw.jpa.JpaBaseDaoImpl`，该实现用来解决封装常用的查询以及处理参数，避免在上述接口中声明过长的方法或不支持的语法，该接口提供了如下默认实现:，所有的用于查询的字段已在处理的时候做了验证，为了避免个人原因输入错误而导致程序没有异常而结果错误不易察觉，目前字段在实体中不存在，会抛出异常；
```java
package com.ddf.scaffold.fw.jpa;

import com.ddf.scaffold.fw.entity.BaseDomain;
import com.ddf.scaffold.fw.session.RequestContext;
import com.ddf.scaffold.fw.session.SessionContext;
import com.ddf.scaffold.fw.entity.QueryParam;
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

```

#### 3.4 自定义查询条件构造器
该类为了解决复杂查询条件而设置的用于拼接Sql条件的类,属性、操作符、属性值、关系符，可以根据实际情况进行匹配；该类型的查询匹配如下实现
```java
Integer updateByMap(@NotNull Map<String, Object> fieldMap, @NotNull List<QueryParam> queryParams);

Page<T> pageByQueryParams(@NotNull List<QueryParam> queryParams, @NotNull Pageable pageable);

T findOneByQueryParams(@NotNull List<QueryParam> queryParams);

T findOneByQueryParams(@NotNull List<QueryParam> queryParams, boolean isRemoved);

List<T> findByQueryParams(@NotNull List<QueryParam> queryParams);

List<T> findByQueryParams(@NotNull List<QueryParam> queryParams, boolean isRemoved);

Long querySize(@NotNull List<QueryParam> queryParams, String countField);
```

源码如下：
```java
package com.ddf.scaffold.fw.util;

import lombok.*;

import java.io.Serializable;

/**
 * @author DDf on 2018/12/16
 */
@Getter
@ToString
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class QueryParam<T> implements Serializable {

    private static final long serialVersionUID = -3340461020433145490L;

    /** 查询字段 */
    private String key;
    /** 字段与属性的关系 */
    private Op op;
    /** 字段的属性值 */
    private T value;
    /** 该字段查询条件与其它字段的关系 */
    private Relative relative;
    /** 分组名称，相同的分组名称的查询条件会放在一个()里处理，方便可以and or 一起使用，但又与其它条件是and */
    private String groupName;

    /**
     * 不需要分组使用的查询条件构造函数
     */
    public QueryParam(String key, Op op, T value, Relative relative) {
        this.key = key;
        this.op = op;
        this.value = value;
        this.relative = relative;
    }

    /**
     * 条件相等关系为AND的简写
     * @param key  字段
     * @param value 值
     */
    public QueryParam(String key, T value) {
        this.key = key;
        this.op = Op.EQ;
        this.value = value;
        this.relative = Relative.AND;
    }


    /**
     * 多个条件为AND关系的简写
     * @param key  字段
     * @param value 值
     */
    public QueryParam(String key, Op op, T value) {
        this.key = key;
        this.op = op;
        this.value = value;
        this.relative = Relative.AND;
    }

    public enum Relative {
        /** 与其它条件为and关系 */
        AND("AND"),
        /** 与其它关系为or关系 */
        OR("OR")
        ;
        private String value;
        Relative(String value) {
            this.value = value;
        }
        public String getValue() {
            return value;
        }
    }

    public enum Op {

        /** 相等 */
        EQ("="),
        /** 相似 */
        LIKE("LIKE"),
        /** 大于等于 */
        GE(">="),
        /** 大于 */
        GT(">"),
        /** 小于等于 */
        LE("<="),
        /** 小于 */
        LT("<"),
        /** IN */
        IN("IN"),
        /** 不相似 */
        NK("NOT LIKE"),
        /** 不等于 */
        NE("<>"),
        /** IS NOT NULL */
        NN("IS NOT NULL"),
        /** IS NULL */
        NI("IS NULL");

        private String value;
        Op(String value) {
            this.value = value;
        }
        public String getValue() {
            return value;
        }
    }
}
```

* userName = 'ddf' and password = '123456' and removed = 0

```java
@Autowired
private UserRepository userRepository;

public void test() {
    
    List<QueryParam> queryParams = new ArrayList<>();
    queryParams.add(new QueryParam<>("userName", "ddf"));
    queryParams.add(new QueryParam<>("password", "123456"));
    userRepository.findByQueryParams(queryParams);
    
    // 简单的=匹配可以使用Map
    Map<String, Object> propertiesMap = new HashMap<>();
    propertiesMap.put("userName", "ddf");
    propertiesMap.put("password", "123456");
    userRepository.findByProperties(propertiesMap);
}
```

* userName like '%d%' or version <= 10 
`like`操作符，如果属性值包含`%`，则使用传入的匹配符，如果属性值没有包含`%`，则默认匹配符为`%%`

```java
@Autowired
private UserRepository userRepository;

public void test() {
    List<QueryParam> queryParams = new ArrayList<>();
    queryParams.add(new QueryParam<>("userName", QueryParam.Op.LIKE, "d"));
    queryParams.add(new QueryParam<>("version", QueryParam.Op.LE, 10));
    userRepository.findByQueryParams(queryParams);
}
```

* (userName like '%d' or version <= 10 ) and (createTime > ? or createTime < ? )

时间类型的参数，value支持两种格式，一种是标准的Date对象，为方便前端传参处理，另外提供了毫秒值来标识的时间值，必须为Long类型，处理的时候会去格式化毫秒时间；

```java
@Autowired
private UserRepository userRepository;

public void test() {
    List<QueryParam> queryParams = new ArrayList<>();
    queryParams.add(new QueryParam<>("userName", QueryParam.Op.LIKE, "%dd", QueryParam.Relative.AND, "group1"));
    queryParams.add(new QueryParam<>("version", QueryParam.Op.LE, 10, QueryParam.Relative.OR, "group1"));
    queryParams.add(new QueryParam<>("createTime", QueryParam.Op.GT,  new Date(), QueryParam.Relative.OR, "createTime"));
    queryParams.add(new QueryParam<>("createTime", QueryParam.Op.LT,  new Date().getTime() + 3000000, QueryParam.Relative.OR, "createTime"));
    userRepository.findByQueryParams(queryParams);
}
```
#### 3.3 前端传参自定义查询实现
`com.ddf.scaffold.fw.entity.QueryParam`不仅支持后端开发人员对查询条件进行自定义，该脚手架工程也通过`com.ddf.scaffold.fw.resolver.QueryParamArgumentResolver`一个自定义参数解析器来将特定参数名的值解析成后端可以使用的`List<QueryParam>`对象，然后入参到`Controller`中，将值传入到查询方法中，即可作为一个通用的可由前端人员自定义改变查询条件，而不需要再额外的后端代码支持；
** 传参方法 **
前端传入必须使用`JSON`格式的数组对象，每个对象从内容应当遵循`com.ddf.scaffold.fw.entity.QueryParam`类的属性，如果某个属性不需要，可以不传；格式拼接好之后必须使用`queryParams`参数传递；
如
```
queryParams=[{"key": "version", "op": "GE", "value": 0}]
```
后端对应接收代码
```java
    @RequestMapping("/users")
    public List<User> users(List<QueryParam> queryParams) {
        return userRepository.findByQueryParams(queryParams);
    }
```



#### 3.4 Web增强模块
该脚手架工程已启用了`SpringDataJpa`提供的针对`Web`模块增强的一些功能，如分页，有关该章节详细信息请参考[https://docs.spring.io/spring-data/jpa/docs/2.0.10.RELEASE/reference/html/#core.web](https://docs.spring.io/spring-data/jpa/docs/2.0.10.RELEASE/reference/html/#core.web)

##### 3.4.1 分页实现

后端对应接收代码
```java
    @RequestMapping("/users")
    public Page<User> users(Pageable pageable, List<QueryParam> queryParams) {
        return userRepository.pageByQueryParams(queryParams, pageable);
    }
```

`Pageable`用于接收前端传入的分页参数，可接收值如下，
| 参数  | 含义  |
| ------------ | ------------ |
| page  | Page you want to retrieve. 0-indexed and defaults to 0.  |
| size  | Size of the page you want to retrieve. Defaults to 20.  |
| sort  |  Properties that should be sorted by in the format property,property(,ASC|DESC). Default sort direction is ascending. Use multiple sort parameters if you want to switch directions — for example, ?sort=firstname&sort=lastname,asc. |


`List<QueryParam> queryParam`可传参请参考`前端传参自定义查询实现`，则该方法就可以实现了由前端开发人员自定义任何支持的查询条件和分页控制，查询条件或分页即使各有不同，也不需要再增加额外的后端方法

#### 3.5 自定义超类常用功能测试类
此类基于JpaBaseDao的常用实现，提供了测试类，可以给使用者提供一个参考；
```java
package com.ddf.scaffold.fw.jpa;

import com.ddf.scaffold.ApplicationTest;
import User;
import com.ddf.scaffold.fw.entity.QueryParam;
import com.ddf.scaffold.logic.repository.UserRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.management.Query;
import javax.transaction.Transactional;
import java.util.*;

/**
 * @author DDf on 2019/1/3
 */
@Transactional
public class JpaBaseDaoTest extends ApplicationTest {
    @Autowired
    private UserRepository userRepository;


    /**
     * 单表根据Id查询
     * from User where id = ? and removed = 0
     */
    @Test
    public void testFindById() {
        Optional<User> user = userRepository.findById(1L);
        user.ifPresent(System.out::println);
    }

    /**
     * 单表根据id删除
     * update user set removed = 1, version = version + 1, modify_by = ?, modify_time = ? where id = ?
     */
    @Test
    public void testDeleteById() {
        userRepository.deleteById(1L);
    }

    /**
     * 单表直接删除一个对象
     * update user set removed = 1, version = version + 1, modify_by = ?, modify_time = ? where id = ?
     */
    @Test
    public void testDelete() {
        Optional<User> user = userRepository.findById(1L);
        user.ifPresent(user1 -> userRepository.delete(user1));
    }

    /**
     * 单表根据简单的匹配条件返回一条数据，value值必须与在实体里对应属性的类型相同,条件字段必须在实体类中存在，否则会抛出异常
     *
     * from User where removed = 0 and id = ? and userName = ?
     *
     */
    @Test
    public void testFindOneByProperties() {
        Map<String, Object> propertiesMap = new HashMap<>();
        propertiesMap.put("id", 1L);
        propertiesMap.put("userName", "ddf");

        // 带removed = 0
        userRepository.findOneByProperties(propertiesMap);

        // 不带removed = 0
        userRepository.findOneByProperties(propertiesMap, false);

    }


    /**
     * 单表根据简单的匹配条件返回结果集，value值必须与在实体里对应属性的类型相同,条件字段必须在实体类中存在，否则会抛出异常
     * from User where removed = 0 and createBy = ?
     */
    @Test
    public void findByProperties() {
        Map<String, Object> propertiesMap = new HashMap<>();
        propertiesMap.put("createBy", "ddf");
        // 带removed = 0
        userRepository.findByProperties(propertiesMap);

        // 不带removed = 0
        userRepository.findByProperties(propertiesMap, false);

    }


    /**
     * 单表复杂查询条件返回结果集，value值必须与在实体里对应属性的类型相同,条件字段必须在实体类中存在，否则会抛出异常
     * from User where removed = 0 and userName = 'ddf' and id > 0 and (createBy like '%d%') and removed <> 100 or version is not null
     * and (createTime < ? or createTime < ?) and (userName = 'ddd' or removed >= 0 )
     */
    @Test
    public void testFindByQueryParams() {
        List<QueryParam> queryParams = new ArrayList<>();
        queryParams.add(new QueryParam<>("userName", "ddf"));
        queryParams.add(new QueryParam<>("id", QueryParam.Op.GT, 0L));
        queryParams.add(new QueryParam<>("createBy", QueryParam.Op.LIKE, "%d%"));
        queryParams.add(new QueryParam<>("createTime", QueryParam.Op.LT,  new Date(), QueryParam.Relative.OR, "createTime"));
        queryParams.add(new QueryParam<>("createTime", QueryParam.Op.LT,  new Date().getTime() + 3000000, QueryParam.Relative.OR, "createTime"));
        queryParams.add(new QueryParam<>("removed", QueryParam.Op.NE, 100));
        queryParams.add(new QueryParam<>("version", QueryParam.Op.NN, "", QueryParam.Relative.OR));
        queryParams.add(new QueryParam("userName", QueryParam.Op.EQ, "ddd", QueryParam.Relative.AND, "userName"));
        queryParams.add(new QueryParam("removed", QueryParam.Op.GE, 0, QueryParam.Relative.OR, "userName"));


        userRepository.findByQueryParams(queryParams);
        userRepository.findByQueryParams(queryParams, false);
    }

    /**
     * 单表根据复杂条件更新部分字段值，version为可选项，在某些场景确定需要的情况下最好传入
     * update user set version=version+1, modify_by=?, modify_time=?, removed=?
     *     where removed=0 and user_name=? and id>? and ( create_by like ? ) and create_time > ?
     *     and removed<> ? or version is not null
     */
    @Test
    public void testUpdateByMap() {
        List<QueryParam> queryParams = new ArrayList<>();
        queryParams.add(new QueryParam<>("userName", "ddf"));
        queryParams.add(new QueryParam<>("id", QueryParam.Op.GT, 0L));
        queryParams.add(new QueryParam<>("createBy", QueryParam.Op.LIKE, "d"));
        queryParams.add(new QueryParam<>("createTime", QueryParam.Op.LT, new Date()));
        queryParams.add(new QueryParam<>("removed", QueryParam.Op.NE, 100));
        queryParams.add(new QueryParam<>("version", QueryParam.Op.NN, 5, QueryParam.Relative.OR));

        Map<String, Object> fieldMap = new HashMap<>();
        fieldMap.put("removed", 1);
        userRepository.updateByMap(fieldMap, queryParams);
    }

    /**
     * 单表保存或更新，当对象的id在数据库重存在时，则此时为更新，若id不存在，则为保存，如果为更新,则version必传，否则会报错
     */
    @Test
    public void testSave() {
        User user = new User();
        user.setId(1L);
        user.setUserName("ddf^");
        user.setVersion(111);
        userRepository.save(user);
    }


    /**
     * 单表根据条件进行分页和排序查询，支持简单和复杂条件
     */
    @Test
    public void testPageByProperties() {
        Map<String, Object> propertiesMap = new HashMap<>();
        propertiesMap.put("createBy", "ddf");

        /**
         * Pageable 该项目已开启了Web增强模块，controller层直接使用Pageable入参即可接收分页和排序参数，具体、
         * 可传参数请参考https://docs.spring.io/spring-data/jpa/docs/2.0.10.RELEASE/reference/html/#core.web
         */

        Pageable pageable = PageRequest.of(1, 2);
        Page<User> users = userRepository.pageByProperties(propertiesMap, pageable);
        System.out.println(users);

        /**
         * from User where removed=0 and version>=?
         *     order by createBy ASC, createTime DESC limit ?
         */
        List<QueryParam> queryParams = new ArrayList<>();
        queryParams.add(new QueryParam("version", QueryParam.Op.GE, 0));

        Sort sort = Sort.by(Sort.Order.asc("createBy"), Sort.Order.desc("createTime"));
        Pageable pageable1 = PageRequest.of(1, 2, sort);

        Page<User> users1 = userRepository.pageByQueryParams(queryParams, pageable1);
        System.out.println(users1);
    }

    /**
     * 单表根据复杂条件查询匹配结果大小
     */
    @Test
    public void testQuerySize() {
        List<QueryParam> queryParams = new ArrayList<>();
        queryParams.add(new QueryParam<>("userName", "ddf"));
        queryParams.add(new QueryParam<>("id", QueryParam.Op.GT, 0L));
        queryParams.add(new QueryParam<>("createBy", QueryParam.Op.LIKE, "d"));
        queryParams.add(new QueryParam<>("createTime", QueryParam.Op.LT, new Date()));
        queryParams.add(new QueryParam<>("removed", QueryParam.Op.NE, 100));
        Long count = userRepository.querySize(queryParams, null);
        System.out.println(count);


        Map<String, Object> propertiesMap = new HashMap<>();
        propertiesMap.put("version", 0);
        Long countId = userRepository.querySize(propertiesMap, "id");
        System.out.println(countId);

    }


    @Test
    public void test() {
        List<QueryParam> queryParams = new ArrayList<>();
        queryParams.add(new QueryParam<>("userName", QueryParam.Op.LIKE, "%dd", QueryParam.Relative.AND, "group1"));
        queryParams.add(new QueryParam<>("version", QueryParam.Op.LE, 10, QueryParam.Relative.OR, "group1"));
        queryParams.add(new QueryParam<>("createTime", QueryParam.Op.GT,  new Date(), QueryParam.Relative.OR, "createTime"));
        queryParams.add(new QueryParam<>("createTime", QueryParam.Op.LT,  new Date().getTime() + 3000000, QueryParam.Relative.OR, "createTime"));
        userRepository.findByQueryParams(queryParams);
    }
}
```

### 4. 拦截器与相关功能
该脚手架提供了两个拦截器，位于`com.ddf.scaffold.fw.interceptor`包下，通过主配置类注册；

#### 4.1 com.ddf.scaffold.fw.interceptor.LoginInterceptor
该拦截器就实现了一个功能，用来拦截请求并判断当前`session`中是否有用户登录，如果没有登录，则责任链终止，本次访问请求不会继续往下执行而到此结束；需要说明的是这个拦截器的优先级高于本脚手架的另外一个优先级，但为了预留空间，目前该拦截器的优先级为`@Order(Ordered.HIGHEST_PRECEDENCE + 10)`，如果项目中需要添加别的拦截器，请自行处理好各个拦截器之间的优先级；

如果有某些请求，如登录、注册等请求本身就没有登录但又不能让程序终止，那么就要对请求进行放行。该类提供了一个配置参数`ignoreFile`用来将需要放行的请求配置进去之后，一旦匹配即使没有登录，请求也会被放行；配置方式必须在`.yml`中配置，如下提供参考
```
custom:  # 自定义的属性最好都写在custom前缀下，方便辨认
  login-interceptor:
    ignoreFile: /druid,/user/login,/user/registry,/user/validateEmail   # 不需要校验登录的请求
```

#### 4.2 com.ddf.scaffold.fw.interceptor.RequestContextInterceptor
该拦截器目前的作用是将用户的请求参数信息封装到到`com.ddf.scaffold.fw.session.RequestContext`中，
请参考`RequestContext`类中的属性，所有请求参数都会被封装到`paramMap`属性中，而如果是上传文件请求，则文件信息会被封装到`fileItems`属性中，需要使用的时候，则直接注入`RequestContext`对象获取即可

### 5. 登录
登录部分本不属于脚手架相关的内容，各个系统登录的方式本就有所不同，但目前该脚手架工程有相当一些地方需要使用到用户信息来完成一些功能，所以登录成功后，需要编码者将用户信息赋值到容器中

#### 5.1 com.ddf.scaffold.fw.session.SessionContext
该类用来存储当前`Session`的用户信息，核心属性有两个，一个是当前用户信息类，一个是当前用户唯一标识，用于完成对审计信息的填写，因为使用id难以辨认，因此留了一个属性，让编码者自行决定往这个属性中放入用户的哪个属性来当作唯一标识符；该类结构如下
```java
package com.ddf.scaffold.fw.session;

import com.ddf.scaffold.fw.entity.BaseDomain;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashMap;

/**
 * @author DDf on 2018/12/31
 */
@Component
@SessionScope
public class SessionContext<T extends BaseDomain> extends HashMap implements Serializable {

    private static final long serialVersionUID = 3370995848343546285L;

    /**
     * 用户唯一键（用户名/邮箱，按自己需要来）,登录后必须由编码者赋值，后面框架中会用到
     */
    @Getter
    @Setter
    @NotNull private String uid;

    /**
     * 用户信息对象,登录后编码者将用户信息设置设置到该属性中
     */
    @Getter
    @Setter
    @NotNull private T user;
}
```

#### 5.2 设置用户信息
以下简单模拟用户登录后，如何设置用户信息.其中登录相关的逻辑自行编写即可，但是一旦验证成功，请务必注入`SessionContext`，然后给`uid`和`user`两个属性赋值；
```java
    @Transactional(readOnly = true)
    public User login(@NotNull String userName, @NotNull String password) {
        ConstUtil.fastFailureParamMission(userName, password);
        User user = userRepository.getUserByUserNameAndPassword(userName, password);
        if (user != null) {
            sessionContext.setUid(user.getUserName());
            sessionContext.setUser(user);
            return user;
        }
        throw new GlobalCustomizeException(GlobalExceptionEnum.LOGIN_ERROR);
    }
```

### 6. 异常及国际化
本脚手架提供了一个对异常国际化的支持,该功能有如下几个部分支持
1. 统一异常代码接口
2. 异常枚举类实现统一异常代码接口
3. 自定义异常类接收统一异常代码接口参数
4. 异常国家化资源文件
5. 异常处理器，用户格式化异常代码填充占位符以及国际化消息


### 7. 日志拦截及接口耗时统计与回调
文档待补充
提供一个注解可以将控制器的请求参数以及返回值打印，并提供一个毫秒值的属性，当接口耗时超过指定时间，则会触发一个回调接口，实现该接口可以针对延迟比较大的接口做一些统计或处理；

### 8. 加入mybatis-plus
文档待补充
包含通用字段填充、逻辑删除等基本通用功能组件的配置

### 9. 集成swagger2
文档待补充
一个API接口管理工具

### 10. 引入rabbit-mq
文档待补充
引入对rabbit-mq的支持，提供演示了几种不同交换器类型和收发消息以及死信队列；

### 11. 统一响应内容消息体
```java
public class ResponseData<T> {
    /** 返回状态码 */
    private String code;
    /** 返回消息 */
    private String message;
    /** 响应时间 */
    private long timestamp;
    /** 返回数据 */
    private T data;
}
```

### 12. TCP长连接
* 项目启动后启动长连接服务端
* 提供自定义报文来接收与反馈客户端传送的数据，必须满足服务端格式否则关闭对方连接
* 监控客户端连接状态（注册、在线、离线）并入库（连接在线不一定设备在线，如果不传送设备id，对后面业务无效）
* 针对本项目，客户端连接后必须发送自己的设备id,后面业务流程会取在线并且有设备号的连接才算有效
* 提供对客户端的心跳检测来保证连接的可用以及清除无用连接信息
* 服务端与客户端连接必须建立ssl连接，服务端会提供自己的公钥












