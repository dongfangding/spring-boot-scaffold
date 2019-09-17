package com.ddf.scaffold.fw.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ddf.scaffold.ApplicationTest;
import com.ddf.scaffold.logic.mapper.UserMapper;
import com.ddf.scaffold.logic.mapper.UserOrderMapper;
import com.ddf.scaffold.logic.model.VO.BootUserVO1;
import com.ddf.scaffold.logic.model.VO.UserVO2;
import com.ddf.scaffold.logic.model.entity.BootUser;
import com.ddf.scaffold.logic.model.entity.UserOrder;
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
        List<BootUser> bootUsers = userMapper.selectList(null);
        bootUsers.forEach(System.out::println);

        // 条件构造器，3.x以后的版本可以支持lamda表达式，避免直接使用表名硬编码，
        LambdaQueryWrapper<BootUser> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(BootUser::getRemoved, 0);

        // where and 查询
        queryWrapper.likeLeft(BootUser::getEmail, "test");
        List<BootUser> users1 = userMapper.selectList(queryWrapper);
        System.out.println("users1 = " + users1);

        // and 和 or 混合查询
        queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(BootUser::getRemoved, 0).likeLeft(BootUser::getEmail, "test").or().like(BootUser::getUserName, "J").isNotNull(BootUser::getUserName);
        List<BootUser> users2 = userMapper.selectList(queryWrapper);
        System.out.println("users2 = " + users2);

        // and 和嵌套or 查询
        queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.in(BootUser::getRemoved, Arrays.asList(0, 1, 2, 3)).ge(BootUser::getVersion, 0).and(userQueryWrapper -> userQueryWrapper.likeLeft(BootUser::getEmail, "test").or().likeLeft(BootUser::getUserName, "J"));
        List<BootUser> users3 = userMapper.selectList(queryWrapper);
        System.out.println("users3 = " + users3);

        // order by 排序
        queryWrapper.orderByDesc(BootUser::getId).orderByAsc(BootUser::getUserName);
        List<BootUser> users4 = userMapper.selectList(queryWrapper);
        System.out.println("users4 = " + users4);
    }


    @Test
    public void testUpdate() {
        BootUser bootUser = new BootUser();
        bootUser.setUserName("testUpdate");
        bootUser.setPassword("123456");
        bootUser.setEmail("testUpdate@163.com");
        bootUser.setBirthday(new Date());
        userMapper.insert(bootUser);
        log.info("insert bootUser, id = {}", bootUser.getId());

        LambdaUpdateWrapper<BootUser> updateWrapper = Wrappers.lambdaUpdate();

        // UPDATE USER SET user_name=? WHERE id = ?,这种写法通用填充会失效
        updateWrapper.set(BootUser::getUserName, bootUser.getUserName() + new Random().nextInt(1000)).eq(BootUser::getId, bootUser.getId());
        userMapper.update(null, updateWrapper);
        // 等同于以下写法,注意看源码的注释，第一个参数是决定要修改哪些字段，是决定set部分，有哪些属性哪些都会被set,所以如果直接使用查出来的对象修改，
        // 那就是全字段更新了，第二个参数决定where部分，这种写法可以出发通用字段的填充
        BootUser setBootUser = new BootUser();
        setBootUser.setUserName(bootUser.getUserName() + new Random().nextInt(10));
        userMapper.update(setBootUser, updateWrapper);

        // 如果启用了逻辑删除功能，这里会使用update来代替delete
        userMapper.deleteById(100L);


        // 启用了乐观锁注解，则更新的时候可以再where条件中带上version，必须where条件中带有version，否则乐观锁就不会生效
        updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(BootUser::getPassword, "123456").eq(BootUser::getVersion, bootUser.getVersion()).eq(BootUser::getId, bootUser.getId());
        userMapper.update(null, updateWrapper);
        // 等同于以下写法
        BootUser versionBootUser = new BootUser();
        versionBootUser.setId(bootUser.getId());
        versionBootUser.setVersion(bootUser.getVersion());
        versionBootUser.setPassword("123456");
        userMapper.updateById(versionBootUser);
    }


    /**
     * 测试自定义XML来映射查询结果
     */
    @Test
    public void testMapperXML() {
        // 先造几条数据
        BootUser bootUser = new BootUser();
        bootUser.setUserName("testMapperXML" + new Random().nextInt(1000));
        bootUser.setPassword("123456");
        bootUser.setEmail(bootUser.getUserName() + "@qq.com");
        bootUser.setBirthday(new Date());
        userMapper.insert(bootUser);

        bootUser = userMapper.selectById(bootUser.getId());

        userOrderMapper.insert(new UserOrder().setUserId(bootUser.getId()).setName("书本").setNum(1).setPrice(1000d));
        userOrderMapper.insert(new UserOrder().setUserId(bootUser.getId()).setName("手机").setNum(1).setPrice(5444d));

        // 通过继承的方式来映射结果
        List<BootUserVO1> userVOList = userMapper.selectUserVO(bootUser.getId());
        if (null != userVOList && !userVOList.isEmpty()) {
            for (BootUserVO1 userVO : userVOList) {
                // toString()方法故意排除了userOrderList属性，方便这里打印查看
                log.info("bootUser: {}", userVO);
                log.info("userOrderList: {}", userVO.getUserOrderList());
            }
        }

        System.out.println("-------------------------------------------------------------------");

        // 通过组合的方式类映射查询结果
        List<UserVO2> userVOList2 = userMapper.selectUserVO2(bootUser.getId());
        if (null != userVOList2 && !userVOList2.isEmpty()) {
            for (UserVO2 userVO : userVOList2) {
                // toString()方法故意排除了userOrderList属性，方便这里打印查看
                log.info("bootUser: {}", userVO.getBootUser());
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
