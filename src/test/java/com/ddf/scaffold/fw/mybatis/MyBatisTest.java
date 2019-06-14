package com.ddf.scaffold.fw.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ddf.scaffold.ApplicationTest;
import com.ddf.scaffold.logic.VO.UserVO;
import com.ddf.scaffold.logic.VO.UserVO2;
import com.ddf.scaffold.logic.entity.User;
import com.ddf.scaffold.logic.entity.UserOrder;
import com.ddf.scaffold.logic.mapper.UserMapper;
import com.ddf.scaffold.logic.mapper.UserOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

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
@Slf4j
@Transactional
public class MyBatisTest extends ApplicationTest {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserOrderMapper userOrderMapper;


    /**
     * 以下写法为3.x以后的版本，可以使用lambda表达式而不是直接使用列名来硬编码
     */
    @Test
    public void testSelect() {
        // 标准list查询
        List<User> users = userMapper.selectList(null);
        users.forEach(System.out::println);

        // 条件构造器，3.x以后的版本可以支持lamda表达式，避免直接使用表名硬编码，
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(User::getRemoved, 0);

        // where and 查询
        queryWrapper.likeLeft(User::getEmail, "test");
        List<User> users1 = userMapper.selectList(queryWrapper);
        System.out.println("users1 = " + users1);

        // and 和 or 混合查询
        queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(User::getRemoved, 0).likeLeft(User::getEmail, "test").or().like(User::getUserName, "J").isNotNull(User::getUserName);
        List<User> users2 = userMapper.selectList(queryWrapper);
        System.out.println("users2 = " + users2);

        // and 和嵌套or 查询
        queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.in(User::getRemoved, Arrays.asList(0, 1, 2, 3)).ge(User::getVersion, 0).and(userQueryWrapper -> userQueryWrapper.likeLeft(User::getEmail, "test").or().likeLeft(User::getUserName, "J"));
        List<User> users3 = userMapper.selectList(queryWrapper);
        System.out.println("users3 = " + users3);

        // order by 排序
        queryWrapper.orderByDesc(User::getId).orderByAsc(User::getUserName);
        List<User> users4 = userMapper.selectList(queryWrapper);
        System.out.println("users4 = " + users4);
    }


    @Test
    public void testUpdate() {
        User user = new User();
        user.setUserName("testUpdate");
        user.setPassword("123456");
        user.setEmail("testUpdate@163.com");
        user.setBirthday(new Date());
        userMapper.insert(user);
        log.info("insert user, id = {}", user.getId());

        LambdaUpdateWrapper<User> updateWrapper = Wrappers.lambdaUpdate();

        // UPDATE USER SET user_name=? WHERE id = ?,这种写法通用填充会失效
        updateWrapper.set(User::getUserName, user.getUserName() + new Random().nextInt(1000)).eq(User::getId, user.getId());
        userMapper.update(null, updateWrapper);
        // 等同于以下写法,注意看源码的注释，第一个参数是决定要修改哪些字段，是决定set部分，有哪些属性哪些都会被set,所以如果直接使用查出来的对象修改，
        // 那就是全字段更新了，第二个参数决定where部分，这种写法可以出发通用字段的填充
        User setUser = new User();
        setUser.setUserName(user.getUserName() + new Random().nextInt(10));
        userMapper.update(setUser, updateWrapper);

        // 如果启用了逻辑删除功能，这里会使用update来代替delete
        userMapper.deleteById(100L);


        // 启用了乐观锁注解，则更新的时候可以再where条件中带上version，必须where条件中带有version，否则乐观锁就不会生效
        updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(User::getPassword, "123456").eq(User::getVersion, user.getVersion()).eq(User::getId, user.getId());
        userMapper.update(null, updateWrapper);
        // 等同于以下写法
        User versionUser = new User();
        versionUser.setId(user.getId());
        versionUser.setVersion(user.getVersion());
        versionUser.setPassword("123456");
        userMapper.updateById(versionUser);
    }


    /**
     * 测试自定义XML来映射查询结果
     */
    @Test
    public void testMapperXML() {
        // 先造几条数据
        User user = new User();
        user.setUserName("testMapperXML" + new Random().nextInt(1000));
        user.setPassword("123456");
        user.setEmail(user.getUserName() + "@qq.com");
        user.setBirthday(new Date());
        userMapper.insert(user);

        user = userMapper.selectById(user.getId());

        userOrderMapper.insert(new UserOrder().setUserId(user.getId()).setName("书本").setNum(1).setPrice(1000d));
        userOrderMapper.insert(new UserOrder().setUserId(user.getId()).setName("手机").setNum(1).setPrice(5444d));

        // 通过继承的方式来映射结果
        List<UserVO> userVOList = userMapper.selectUserVO(user.getId());
        if (null != userVOList && !userVOList.isEmpty()) {
            for (UserVO userVO : userVOList) {
                // toString()方法故意排除了userOrderList属性，方便这里打印查看
                log.info("user: {}", userVO);
                log.info("userOrderList: {}", userVO.getUserOrderList());
            }
        }

        System.out.println("-------------------------------------------------------------------");

        // 通过组合的方式类映射查询结果
        List<UserVO2> userVOList2 = userMapper.selectUserVO2(user.getId());
        if (null != userVOList2 && !userVOList2.isEmpty()) {
            for (UserVO2 userVO : userVOList2) {
                // toString()方法故意排除了userOrderList属性，方便这里打印查看
                log.info("user: {}", userVO.getUser());
                log.info("userOrderList: {}", userVO.getUserOrderList());
            }
        }

        System.out.println("--------------------------------------------------------------------");
        // 分页查询，自定义的XML分页查询是根据调用时的传参决定的，如果定义了一个查询，传参的时候允许传入分页对象，则自动分页，
        // XML中的查询无需更改，比如我现在还调用selectUserVO2，但是重载了一个方法可以传入分页对象，但调用的还是selectUserVO2的XML映射
        // 如果count是0，则不会分页
        Page<UserVO2> page = new Page<>(1, 10);
        userMapper.selectUserVO2(page, null);
    }
}
