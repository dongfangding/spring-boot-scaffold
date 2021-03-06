package com.ddf.scaffold.logic.service;

import com.ddf.scaffold.ApplicationTest;
import com.ddf.scaffold.logic.model.VO.BootUserVo;
import com.ddf.scaffold.logic.model.bo.UserRegistryBO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * @author dongfang.ding on 2019/4/17
 */
public class BootUserServiceTest extends ApplicationTest {
	@Autowired
	private UserService userService;

	/**
	 * 测试注册用户
	 */
	@Test
	public void testRegistry() {
		UserRegistryBO UserRegistryBo = new UserRegistryBO();
		UserRegistryBo.setUserName("testRegistry" + UUID.randomUUID().toString().substring(8));
		UserRegistryBo.setPassword("123456");
		UserRegistryBo.setEmail("testRegistry@qq.com" + UUID.randomUUID().toString().substring(8));
		UserRegistryBo.setBirthday(new Date());
		BootUserVo newBootUser = userService.registry(UserRegistryBo);
		Assert.notNull(newBootUser, "注册成功");
	}

	@Test
	public void testConcurrentRegistry () throws InterruptedException {
		CountDownLatch countDownLatch = new CountDownLatch(100000);
		for (int i = 0; i < 100000; i++) {
			new Thread(() -> {
				try {
					testRegistry();
				} finally {
					countDownLatch.countDown();
				}
			}).start();
		}
		countDownLatch.await();
		System.out.println("任务执行完成。。。。。。。。。。。");
	}
}
