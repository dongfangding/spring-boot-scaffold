package com.ddf.scaffold.logic.controller;

import com.ddf.scaffold.logic.entity.User;
import com.ddf.scaffold.fw.session.RequestContext;
import com.ddf.scaffold.fw.session.SessionContext;
import com.ddf.scaffold.fw.entity.QueryParam;
import com.ddf.scaffold.logic.repository.UserRepository;
import com.ddf.scaffold.logic.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.util.List;

/**
 * @author DDf on 2018/12/2
 */
@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private RequestContext requestContext;
	@Autowired
	private SessionContext<User> sessionContext;


	/**
	 * 注册用户
	 * @param user 用户实体
	 * @return
	 */
	@PostMapping("registry")
	public User registry(@RequestBody User user) {
		return userService.registry(user);
	}

	@RequestMapping("validateEmail")
	public void validateEmail(@RequestParam("validateEmail") String email) throws MessagingException {
		userService.validateEmail(email);
	}


	/**
	 * 用户登录
	 * @param userName 用户名
	 * @param password 密码
	 * @return
	 */
	@RequestMapping("login")
	public User login(@RequestParam("userName") String userName, @RequestParam String password) {
		return userService.login(userName, password);
	}

    /**
     * 根据id查找用户
     * @param id
     * @return
     */
	@RequestMapping("user/{id}")
    public User uniqueUser(@PathVariable("id") Long id) {
        return userRepository.findById(id).orElse(null);
    }

	/**
	 * 分页查询用户列表
	 * @param pageable 分页参数
	 * @param queryParams 查询参数对象
	 * @return
	 */
	@RequestMapping("/users")
	public Page<User> users(Pageable pageable, List<QueryParam> queryParams) {
		return userRepository.pageByQueryParams(queryParams, pageable);
	}


	@RequestMapping("/delete/{id}")
	public void delete(@PathVariable Long id) {
		userRepository.deleteById(id);
	}

}
