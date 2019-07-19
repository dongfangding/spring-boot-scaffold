package com.ddf.scaffold.logic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ddf.scaffold.logic.model.VO.UserVO1;
import com.ddf.scaffold.logic.model.VO.UserVO2;
import com.ddf.scaffold.logic.model.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 用户映射类
 *
 * @author dongfang.ding
 * @date 2019/5/21 9:56
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * 关联查询USER和ORDER，使用继承的方式
     * @param userId
     * @return
     */
    List<UserVO1> selectUserVO(@Param("userId") Long userId);


    /**
     * 关联查询USER和ORDER，使用组合的形式
     * @param userId
     * @return
     */
    List<UserVO2> selectUserVO2(@Param("userId") Long userId);


    /**
     * 相同的XML映射查询，如果传入参数有分页对象，则自动分页，分页对象必须为第一个参数
     * @param page
     * @param userId
     * @return
     */
    IPage<UserVO2> selectUserVO2(Page<UserVO2> page, @Param("userId") Long userId);
}
