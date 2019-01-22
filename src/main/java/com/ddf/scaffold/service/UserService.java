package com.ddf.scaffold.service;

import com.ddf.scaffold.entity.User;

import javax.mail.MessagingException;
import java.util.List;

/**
 * @author DDf on 2018/12/1
 */
public interface UserService {
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
	 * 通过关键字查询可以添加的伙伴列表
	 * @param userKey 用户关键字
	 * @param userId 用户自己的id，不能把用户自己查出来
	 * @return
	 */
	List<User> searchUserForPartner(String userKey, Long userId);

	/**
	 * 验证注册用户邮箱
	 * @param email
	 */
	void validateEmail(String email) throws MessagingException;


	/**
	 * 修改密码
	 * @return
	 */
    User updatePassword(User user);

}
