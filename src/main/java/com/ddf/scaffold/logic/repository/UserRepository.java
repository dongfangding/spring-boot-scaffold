package com.ddf.scaffold.logic.repository;

import com.ddf.scaffold.fw.exception.CNMessage;
import com.ddf.scaffold.fw.exception.GlobalCustomizeException;
import com.ddf.scaffold.logic.entity.User;
import com.ddf.scaffold.fw.jpa.JpaBaseDao;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author DDf on 2018/12/1
 */
@Repository
@Transactional(readOnly = true)
public interface UserRepository extends JpaBaseDao<User, Long> {

	/**
	 * 根据用户名和密码查询用户
	 * @param userName 用户名
	 * @param password 密码
	 * @return
	 */
	User getUserByUserNameAndPassword(String userName, String password);

	/**
	 * 根据用户名或邮箱查询用户是否存在
	 * @param userName 用户名
	 * @param email 邮箱
	 * @return
	 */
	User getUserByUserNameOrEmail(String userName, String email);

	/**
	 * 根据登录用户名查找用户
	 * @param loginName
	 * @return
	 */
	@Transactional(readOnly = true)
	default User getUserByLoginName(@NotNull String loginName) {
		Map<String, Object> propertiesMap = new HashMap<>(2);
		propertiesMap.put("loginName", loginName);
		List<User> userList = findByProperties(propertiesMap);
		if (userList != null && userList.size() > 0) {
			if (userList.size() == 1) {
				return userList.get(0);
			}
			throw new GlobalCustomizeException(CNMessage.USER_LOGIN_NAME_REPEAT, loginName);
		}
		return null;
	}
}
