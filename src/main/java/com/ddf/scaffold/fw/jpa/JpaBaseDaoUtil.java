package com.ddf.scaffold.fw.jpa;

import com.ddf.scaffold.fw.entity.BaseDomain;
import com.ddf.scaffold.fw.util.QueryParam;

import java.util.List;

/**
 * @author DDf on 2019/2/28
 *
 * 用于快速拼接查询条件的辅助类
 */
public class JpaBaseDaoUtil {

    private JpaBaseDaoUtil() {}

    /**
     * 添加判断重复的主键条件，如果是新增数据，则不需要主键条件，
     * 如果是编辑的数据，则判断重复的时候要排除本条主键数据
     * @param queryParams 要加入的条件集合
     * @param jpaBaseDao  实体的Repository
     * @param entity 实体类
     */
    public static boolean addPK(List<QueryParam> queryParams, JpaBaseDao jpaBaseDao, BaseDomain entity) {
        if (!jpaBaseDao.isNew(entity)) {
            return queryParams.add(new QueryParam<>("id", QueryParam.Op.NE, jpaBaseDao.getId(entity)));
        }
        return false;
    }
}
