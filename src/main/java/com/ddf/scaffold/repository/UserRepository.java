package com.ddf.scaffold.repository;

import com.ddf.scaffold.entity.User;
import com.ddf.scaffold.fw.jpa.JpaBaseDao;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
	 * 通过关键字查询可以添加的伙伴列表，不能把自己查出来
	 * @param userKey 用户关键字
	 * @param userId 用户自己的id
	 * @return
	 */
	@Query("from User where (userName = :key or email =:key) and removed = 0 and id <> :userId")
	List<User> searchUserForPartner(@Param("key") String userKey, @Param("userId") Long userId);
}
