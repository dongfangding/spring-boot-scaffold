package com.ddf.scaffold.fw.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 针对通用字段自动填充功能的实现
 *
 * 实现对通用实体字段的赋值，
 *
 * @see com.ddf.scaffold.fw.entity.BaseDomain
 * @author dongfang.ding
 * @date 2019/5/22 17:15
 */
@Slf4j
@Component
public class FillMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("start insert fill ....");
        // 切记切记，这里是filedName，是实体属性字段名，而不是数据库列名
        setInsertFieldValByName("createBy", "ddf_insert", metaObject);
        setInsertFieldValByName("createTime", new Date(), metaObject);
        setInsertFieldValByName("modifyBy", "ddf_insert", metaObject);
        setInsertFieldValByName("modifyTime", new Date(), metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 切记切记，这里是filedName，是实体属性字段名，而不是数据库列名
        setUpdateFieldValByName("modifyBy", "ddf_modify", metaObject);
        setUpdateFieldValByName("modifyTime", new Date(), metaObject);
    }
}
