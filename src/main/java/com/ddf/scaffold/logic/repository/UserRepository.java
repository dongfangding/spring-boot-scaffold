package com.ddf.scaffold.logic.repository;

import com.ddf.scaffold.fw.exception.CNMessage;
import com.ddf.scaffold.fw.exception.GlobalCustomizeException;
import com.ddf.scaffold.fw.jpa.JpaBaseDao;
import com.ddf.scaffold.logic.model.entity.BootUser;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dongfang.ding on 2018/12/1
 */
@Repository
@Transactional(readOnly = true)
public interface UserRepository extends JpaBaseDao<BootUser, Long> {

	/**
	 * 根据用户名和密码查询用户
	 * @param userName 用户名
	 * @param password 密码
	 * @return
	 */
	BootUser getUserByUserNameAndPassword(String userName, String password);

	/**
	 * 根据登录用户名查找用户
	 * @param loginName
	 * @return
	 */
	@Transactional(readOnly = true)
	default BootUser getUserByLoginName(@NotNull String loginName) {
		Map<String, Object> propertiesMap = new HashMap<>(2);
		propertiesMap.put("loginName", loginName);
		List<BootUser> bootUserList = findByProperties(propertiesMap);
		if (bootUserList != null && bootUserList.size() > 0) {
			if (bootUserList.size() == 1) {
				return bootUserList.get(0);
			}
			throw new GlobalCustomizeException(CNMessage.USER_LOGIN_NAME_REPEAT, loginName);
		}
		return null;
	}
}
