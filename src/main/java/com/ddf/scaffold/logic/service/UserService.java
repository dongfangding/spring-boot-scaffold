package com.ddf.scaffold.logic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ddf.scaffold.logic.model.VO.BootUserVo;
import com.ddf.scaffold.logic.model.bo.UserRegistryBO;
import com.ddf.scaffold.logic.model.entity.BootUser;

import javax.mail.MessagingException;
import javax.validation.constraints.NotNull;

/**
 * @author dongfang.ding on 2018/12/1
 */
public interface UserService extends IService<BootUser> {
	/**
	 * 登录
	 * @param userName 用户名
	 * @param password 密码
	 * @return
	 */
	String login(String userName, String password);

	/**
	 * 用户注册
	 * @param userRegistryBo 新增用户对象
	 * @return
	 */
	BootUserVo registry(UserRegistryBO userRegistryBo);

	/**
	 * 验证注册用户邮箱
	 * @param email
	 * @throws MessagingException
	 */
	void validateEmail(String email) throws MessagingException;


	/**
	 * 根据用户名查找用户
	 * @param userName
	 * @return
	 */
	BootUser findByName(String userName);


	/**
	 * 根据用户名和密码查找用户
	 * @param userName
	 * @param password
	 * @return
	 */
	BootUser getByUserNameAndPassword(@NotNull String userName, @NotNull String password);

}
