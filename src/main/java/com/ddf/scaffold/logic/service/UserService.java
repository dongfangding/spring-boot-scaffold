package com.ddf.scaffold.logic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ddf.scaffold.logic.entity.User;

import javax.mail.MessagingException;

/**
 * @author DDf on 2018/12/1
 */
public interface UserService extends IService<User> {
	/**
	 * 登录
	 * @param userName 用户名
	 * @param password 密码
	 * @return
	 */
	User login(String userName, String password);

	/**
	 * 用户注册
	 * @param user 新增用户对象
	 * @return
	 */
	User registry(User user);

	/**
	 * 验证注册用户邮箱
	 * @param email
	 */
	void validateEmail(String email) throws MessagingException;


}
