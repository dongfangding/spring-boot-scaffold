package com.ddf.scaffold.fw.mybatis;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.parser.ISqlParser;
import com.baomidou.mybatisplus.extension.parsers.BlockAttackSqlParser;
import com.baomidou.mybatisplus.extension.plugins.OptimisticLockerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PerformanceInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.List;

/**
 * mybatis-plus的配置类,
 *
 *
 * 关于mybatis-plus的关键点总结
 * <ul>
 *     <li>
 *         mybatis的条件构造器针对的是column_name而不是filed_name，这一点一定要注意；也是用的很不爽的一点，不知道有没有办法解决；
 *     </li>
 *     <li>
 *         数据库表和实体映射是通过{@code @TableName}来完成的，数据库字段和实体字段映射是用过{@code @TableField}来映射的，与JPA不同的是，
 *         数据库中不存在的字段，mybatis是通过{@code @TableField(exist = false)}来完成的；另外mybatis和数据库映射可以采用驼峰命名规则与经典的数据库下划线
 *         命名规则来匹配默进行映射，mybatis-plus默认开启,原生mybatis默认false,即实体字段userName对应数据库的字段未USER_NAME, password对应的为PASSWORD
 *     </li>
 *     <li>
 *         mybatis-plus使用{@link com.baomidou.mybatisplus.annotation.TableId}来标识数据库主键，主键生成策略通过该注解的{@link TableId#type()}属性来指定；
 *         需要注意的是及时设置了如自增策略{@code @TableId(type = IdType.AUTO)},数据库设计主键字段也要设计为自增；否则无效，反之数据库自增，但字段没有映射默认会是
 *         {@link com.baomidou.mybatisplus.annotation.IdType.NONE}，测试后发现是个很大的数;
 *         详细可用注解请<a href="https://mybatis.plus/guide/annotation.html">参考</a>
 *     </li>
 *     <li>
 *         项目中定义的继承{@link com.baomidou.mybatisplus.core.mapper.BaseMapper}的mapper，如果要注入使用，必须在配置类中通过{@code @MapperScan}注解来扫描
 *     </li>
 *     <li>
 *         想要打印mybatis的查询sql，可以通过logger.level.${mapper所在的包}配置为debug
 *     </li>
 *     <li>
 *         想要使用逻辑删除，在实体类字段上加上@TableLogic注解，查询时会过滤该字段代表删除的值，删除时会update而不是delete;仅支持mybatis-plus提供的功能；详见<a href="https://mp.baomidou.com/guide/logic-delete.html">请参考</a>
 *     </li>
 *     <li>
 *         <a href="https://mp.baomidou.com/config/#%E5%9F%BA%E6%9C%AC%E9%85%8D%E7%BD%AE">配置参考</a>
 *     </li>
 *     <li>
 *         <a href="https://mp.baomidou.com/guide/auto-fill-metainfo.html">自动填充功能</a>,实现见{@link FillMetaObjectHandler}
 *     </li>
 *     <li>
 *         <a href="https://mp.baomidou.com/guide/block-attack-sql-parser.html">攻击 SQL 阻断解析器</a>,本系统实现见{@link MyBatisConfig#paginationInterceptor()}
 *     </li>
 *     <li>
 *         <a href="https://mp.baomidou.com/guide/performance-analysis-plugin.html">性能分析插件，开发用</a>
 *     </li>
 *     <li>
 *         关于mybatis-plus的结构,封装的CRUD方法，自定义的mapper需要继承{@link com.baomidou.mybatisplus.core.mapper.BaseMapper}，但其实
 *         mybatis-plus还针对service层也做了一层接口封装，接口层继承{@link com.baomidou.mybatisplus.extension.service.IService}，实现
 *         类继承{@link com.baomidou.mybatisplus.extension.service.impl.ServiceImpl}，这样service层不需要注入mapper也获得了CRUD的功能；
 *         区别暂时只看到了service层的方法牵扯到对数据库的修改已经默认加了事务，所以如果是别的类想要使用另一个类的CURD方法，是注入service还是
 *         mapper呢？
 *     </li>
 *     <li>
 *         关于mapper中的方法传参，最好使用{@link org.apache.ibatis.annotations.Param}注解来标识一下参数名称，即使参数名称和方法中定义的形参名称
 *         一样，也要加这个注解指定，在传入基本包装类型的对象时，会无法使用正确使用动态sql解析参数
 *     </li>
 * </ul>
 *
 * @author dongfang.ding
 * @date 2019/5/22 17:14
 */
@Configuration
@MapperScan(basePackages = {"com.ddf.scaffold.logic.mapper"})
public class MyBatisConfig {

    /**
     * 分页与攻击 SQL 阻断解析器
     * @return
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        List<ISqlParser> sqlParserList = new ArrayList<>();
        // 攻击 SQL 阻断解析器、加入解析链
        sqlParserList.add(new BlockAttackSqlParser());
        paginationInterceptor.setSqlParserList(sqlParserList);
        return paginationInterceptor;
    }

    /**
     * 乐观锁支持
     * 仅支持 updateById(id) 与 update(entity, wrapper) 方法
     * @return
     */
    @Bean
    public OptimisticLockerInterceptor optimisticLockerInterceptor() {
        return new OptimisticLockerInterceptor();
    }

    /**
     * SQL执行效率插件
     * 参数：maxTime SQL 执行最大时长，超过自动停止运行，有助于发现问题。
     * 参数：format SQL SQL是否格式化，默认false。
     * 该插件只用于开发环境，不建议生产环境使用。
     */
    @Bean
    @Profile({"default", "dev","test"})
    public PerformanceInterceptor performanceInterceptor() {
        PerformanceInterceptor performanceInterceptor = new PerformanceInterceptor();
        performanceInterceptor.setMaxTime(200);
        performanceInterceptor.setFormat(true);
        return performanceInterceptor;
    }
}

