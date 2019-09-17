package com.ddf.scaffold.logic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ddf.scaffold.fw.exception.GlobalCustomizeException;
import com.ddf.scaffold.fw.exception.GlobalExceptionEnum;
import com.ddf.scaffold.fw.security.JwtProperties;
import com.ddf.scaffold.fw.security.JwtUtil;
import com.ddf.scaffold.fw.security.UserClaim;
import com.ddf.scaffold.fw.util.*;
import com.ddf.scaffold.logic.constant.LogicGlobalConstants;
import com.ddf.scaffold.logic.mapper.UserMapper;
import com.ddf.scaffold.logic.model.bo.UserRegistryBO;
import com.ddf.scaffold.logic.model.entity.BootUser;
import com.ddf.scaffold.logic.repository.UserRepository;
import com.ddf.scaffold.logic.service.UserService;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author dongfang.ding on 2018/12/1
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, BootUser> implements UserService {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private MailUtil mailUtil;
	@Autowired
	private JwtProperties jwtProperties;

	/**
	 * 登录
	 * @param userName 用户名
	 * @param password 密码
	 * @return
	 */
	@Override
	@Transactional(readOnly = true)
	public String login(@NotNull String userName, @NotNull String password) {
		Preconditions.checkArgument(StringUtils.isNotBlank(userName), "用户名不能为空!");
		Preconditions.checkArgument(StringUtils.isNotBlank(password), "密码不能为空!");
		BootUser bootUser = getByUserNameAndPassword(userName, password);
		if (bootUser == null) {
			throw new GlobalCustomizeException(GlobalExceptionEnum.USERNAME_OR_PASSWORD_INVALID);
		}
		if (LogicGlobalConstants.BYTE_FALSE.equals(bootUser.getIsEnable())) {
			throw new GlobalCustomizeException(GlobalExceptionEnum.ACCOUNT_NOT_ENABLE);
		}

		UserClaim userClaim = new UserClaim();
		userClaim.setUserId(bootUser.getId()).setUserName(bootUser.getUserName()).setLastModifyPasswordTime(
				// 默认注册时间
				bootUser.getLastModifyPassword()).setCredit(WebUtil.getHost());
		return JwtUtil.defaultJws(userClaim, jwtProperties.getExpiredMinute());
	}

	/**
	 * 注册用户，用户名或邮箱已存在，不能注册
	 * @param userRegistryBo 新增用户对象
	 * @return
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public BootUser registry(UserRegistryBO userRegistryBo) {
		Preconditions.checkNotNull(userRegistryBo);
		Preconditions.checkArgument(!StringUtils.isAnyBlank(userRegistryBo.getUserName(), userRegistryBo.getPassword(),
				userRegistryBo.getEmail()), "用户名、密码、邮箱都不能为空！");
		LambdaQueryWrapper<BootUser> queryWrapper = Wrappers.lambdaQuery();
		queryWrapper.eq(BootUser::getUserName, userRegistryBo.getUserName());
		if (count(queryWrapper) > 0) {
			throw new GlobalCustomizeException(GlobalExceptionEnum.USERNAME_EXIST);
		}
		queryWrapper = Wrappers.lambdaQuery();
		queryWrapper.eq(BootUser::getEmail, userRegistryBo.getEmail());
		if (count(queryWrapper) > 0) {
			throw new GlobalCustomizeException(GlobalExceptionEnum.EMAIL_HAD_REGISTERED);
		}
		BootUser bootUser = BeanUtil.copy(userRegistryBo, BootUser.class);
		if (bootUser == null) {
			throw new GlobalCustomizeException(GlobalExceptionEnum.LOGIN_ERROR);
		}
		bootUser.setLastModifyPassword(System.currentTimeMillis());
		save(bootUser);
		return bootUser;
	}


	/**
	 * 验证注册用户的邮箱,这里先粗略处理(规则以及时效有效性限制)
	 * @param email
	 */
	@Override
	public void validateEmail(String email) throws MessagingException {
		if (!StringUtils.isBlank(email)) {
			Map<String, Object> propertiesMap = new HashMap<>();
			propertiesMap.put("email", email);
			Long aLong = userRepository.querySize(propertiesMap, null);
			if (aLong.intValue() > 0) {
				// fixme 去除注释
				// throw new GlobalCustomizeException(GlobalExceptionEnum.EMAIL_HAD_REGISTRY);
			}
			Random random = new Random();
			StringBuilder str = new StringBuilder();
			for (int i = 0; i < 6; i ++) {
				str.append(random.nextInt(9));
			}
			String content = "您本次申请注册的验证码为： " + str.toString();
			mailUtil.sendMimeMail(new String[] {email}, "注册验证码", content);
		}
	}

	/**
	 * 根据用户名查找用户
	 *
	 * @param userName
	 * @return
	 */
	@Override
	public BootUser findByName(String userName) {
		if (org.apache.commons.lang3.StringUtils.isBlank(userName)) {
			return null;
		}
		LambdaQueryWrapper<BootUser> queryWrapper = Wrappers.lambdaQuery();
		queryWrapper.eq(BootUser::getUserName, userName);
		return getOne(queryWrapper);
	}

	/**
	 * 根据用户名和密码查找用户
	 *
	 * @param userName
	 * @param password
	 * @return
	 */
	@Override
	public BootUser getByUserNameAndPassword(@NotNull String userName, @NotNull String password) {
		Preconditions.checkArgument(StringUtils.isNotBlank(userName), "用户名不能为空!");
		Preconditions.checkArgument(StringUtils.isNotBlank(password), "密码不能为空!");
		LambdaQueryWrapper<BootUser> queryWrapper = Wrappers.lambdaQuery();
		queryWrapper.eq(BootUser::getUserName, userName);
		queryWrapper.eq(BootUser::getPassword, MD5Util.encodeSalt(password));
		return getOne(queryWrapper);
	}
}
