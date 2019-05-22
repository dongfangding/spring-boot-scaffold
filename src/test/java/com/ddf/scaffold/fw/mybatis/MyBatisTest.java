package com.ddf.scaffold.fw.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.ddf.scaffold.ApplicationTest;
import com.ddf.scaffold.logic.entity.User;
import com.ddf.scaffold.logic.mapper.UserMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Mybatis的方法测试
 *
 * @author dongfang.ding
 * @date 2019/5/21 9:54
 */
public class MyBatisTest extends ApplicationTest {

    @Autowired
    private UserMapper userMapper;


    /**
     * mybatis的条件构造器针对的是column_name而不是filed_name，这一点一定要注意；也是用的很不爽的一点，不知道有没有办法解决；
     */
    @Test
    public void testSelect() {
        // 标准list查询
        List<User> users = userMapper.selectList(null);
        users.forEach(System.out::println);

        // 条件构造器
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        // where and 查询
        queryWrapper.eq("removed", 0);
        queryWrapper.likeLeft("email", "test");
        List<User> users1 = userMapper.selectList(queryWrapper);
        System.out.println("users1 = " + users1);

        // and 和 or 混合查询
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("removed", 0).likeLeft("email", "test").or().like("user_name", "J").isNotNull("user_name");
        List<User> users2 = userMapper.selectList(queryWrapper);
        System.out.println("users2 = " + users2);

        // and 和嵌套or 查询
        queryWrapper = new QueryWrapper<>();
        queryWrapper.in("removed", Arrays.asList(0, 1, 2, 3)).ge("version", 0).and(userQueryWrapper -> userQueryWrapper.likeLeft("email", "test").or().likeLeft("user_name", "J"));
        List<User> users3 = userMapper.selectList(queryWrapper);
        System.out.println("users3 = " + users3);

        // order by 排序
        queryWrapper.orderByDesc("id").orderByAsc("user_name");
        List<User> users4 = userMapper.selectList(queryWrapper);
        System.out.println("users4 = " + users4);
    }


    @Test
    public void testUpdate() {
        User user = new User();
        user.setId(100L);
        user.setUserName("testUpdate");
        user.setPassword("123456");
        user.setEmail("testUpdate@163.com");
        user.setBirthDay(new Date());
        userMapper.insert(user);


        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("user_name", user.getUserName() + new Random().nextInt(10)).eq("id", 100);
        // UPDATE USER SET user_name=? WHERE id = ?
        userMapper.update(null, updateWrapper);

        // 等同于以下写法,注意看源码的注释，第一个参数是决定要修改哪些字段，是决定set部分，有哪些属性哪些都会被set,所以如果直接使用查出来的对象修改，
        // 那就是全字段更新了，第二个参数决定where部分
        User setUser = new User();
        setUser.setUserName(user.getUserName() + new Random().nextInt(10));
        userMapper.update(setUser, updateWrapper);

        // 如果弃用了逻辑删除功能，这里会使用update来代替delete
        userMapper.deleteById(100L);


    }
}
