package com.ddf.scaffold.logic.service.impl;

import com.ddf.scaffold.logic.entity.User;
import com.ddf.scaffold.fw.exception.GlobalCustomizeException;
import com.ddf.scaffold.fw.exception.GlobalExceptionEnum;
import com.ddf.scaffold.fw.session.SessionContext;
import com.ddf.scaffold.fw.util.ConstUtil;
import com.ddf.scaffold.fw.util.MailUtil;
import com.ddf.scaffold.fw.entity.QueryParam;
import com.ddf.scaffold.logic.repository.UserRepository;
import com.ddf.scaffold.logic.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.mail.MessagingException;
import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * @author DDf on 2018/12/1
 */
@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private SessionContext sessionContext;
	@Autowired
	private MailUtil mailUtil;

	/**
	 * 登录
	 * @param userName 用户名
	 * @param password 密码
	 * @return
	 */
	@Override
	@Transactional(readOnly = true)
	public User login(@NotNull String userName, @NotNull String password) {
		ConstUtil.fastFailureParamMission(userName, password);
		User user = userRepository.getUserByUserNameAndPassword(userName, password);
		if (user != null) {
			sessionContext.setUid(user.getUserName());
			sessionContext.setUser(user);
			return user;
		}
		throw new GlobalCustomizeException(GlobalExceptionEnum.LOGIN_ERROR);
	}

	/**
	 * 注册用户，用户名或邮箱已存在，不能注册
	 * @param user 新增用户对象
	 * @return
	 */
	@Override
	@Transactional
	public User registry(User user) {
		if (user != null) {
			String userName = user.getUserName();
			String email = user.getUserEmail();

			String sessionValidateEmail = String.valueOf(sessionContext.get("validateEmail"));
			if (!sessionValidateEmail.equals(user.getValidateEmail())) {
				throw new GlobalCustomizeException(GlobalExceptionEnum.VALIDATE_EMAIL_ERROR);
			}

			ConstUtil.fastFailureParamMission(userName, user.getPassword(), user.getUserEmail());
			User exist = userRepository.getUserByUserNameOrEmail(userName, email);
			if (exist != null) {
				throw new GlobalCustomizeException(GlobalExceptionEnum.USER_EXIST, exist.getUserName());
			}
			sessionContext.setUid(user.getUserName());
			sessionContext.setUser(user);
			user = userRepository.save(user);
			try {
				mailUtil.sendMimeMail(new String[] {"dongfang.ding@hitisoft.com"}, "注册成功", "恭喜您注册成功");
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			return user;
		}
		return null;
	}


	/**
	 * 验证注册用户的邮箱,这里先粗略处理(规则以及时效有效性限制)
	 * @param email
	 */
	@Override
	public void validateEmail(String email) throws MessagingException {
		if (!StringUtils.isEmpty(email)) {
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
			sessionContext.put("validateEmail", str.toString());
			mailUtil.sendMimeMail(new String[] {email}, "注册验证码", content);
		}
	}


	/**
	 * 修改密码
	 * @return
	 */
	@Override
	@Transactional
	public User updatePassword(User user) {
		ConstUtil.fastFailureParamMission(user.getOldPassword(), user.getPassword(), user.getConfirmPassword());
		User currUser = (User) sessionContext.getUser();
		String currPassword = currUser.getPassword();
		if (!user.getOldPassword().equals(currPassword)) {
			throw new GlobalCustomizeException(GlobalExceptionEnum.PASSWORD_ERROR);
		}
		currUser.setVersion(user.getVersion());
		currUser.setPassword(user.getPassword());
		currUser = userRepository.save(currUser);
		sessionContext.setUser(currUser);
		return currUser;
	}
}
