package com.ddf.scaffold.logic.controller;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.IdUtil;
import com.ddf.scaffold.fw.entity.QueryParam;
import com.ddf.scaffold.fw.util.VerifyCodeUtils;
import com.ddf.scaffold.logic.model.VO.BootUserVo;
import com.ddf.scaffold.logic.model.VO.VerifyCode;
import com.ddf.scaffold.logic.model.bo.UserRegistryBO;
import com.ddf.scaffold.logic.model.entity.BootUser;
import com.ddf.scaffold.logic.repository.UserRepository;
import com.ddf.scaffold.logic.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * @author dongfang.ding on 2018/12/2
 */
@RestController
@RequestMapping("/user")
@Api(description = "用户控制器")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    /**
     * 注册用户
     *
     * @param userRegistryBo 用户实体
     * @return
     */
    @PostMapping("registry")
    @ApiOperation("注册用户")
    public BootUserVo registry(@RequestBody UserRegistryBO userRegistryBo) {
        return userService.registry(userRegistryBo);
    }

    @GetMapping("validateEmail")
    @ApiOperation("验证邮箱")
    public void validateEmail(@RequestParam("validateEmail") @ApiParam(value = "邮箱") String email) throws MessagingException {
        userService.validateEmail(email);
    }


    /**
     * 用户登录
     *
     * @param userName 用户名
     * @param password 密码
     * @return
     */
    @PostMapping("login")
    @ApiOperation("用户登录")
    public String login(@RequestParam("userName") @ApiParam(value = "登录名") String userName,
                        @RequestParam @ApiParam(value = "密码") String password) {
        return userService.login(userName, password);
    }

    /**
     * 根据id查找用户
     *
     * @param id
     * @return
     */
    @GetMapping("user/{id}")
    @ApiOperation("根据主键查询用户")
    public BootUser uniqueUser(@PathVariable("id") Long id) {
        return userRepository.findById(id).orElse(null);
    }

    /**
     * 分页查询用户列表
     *
     * @param pageable    分页参数
     * @param queryParams 查询参数对象
     * @return
     */
    @GetMapping("/users")
    @ApiOperation("分页查询用户列表")
    public Page<BootUser> users(Pageable pageable, List<QueryParam> queryParams) {
        return userRepository.pageByQueryParams(queryParams, pageable);
    }


    @PostMapping("/delete/{id}")
    @ApiOperation("根据主键删除用户")
    public void delete(@PathVariable @ApiParam(value = "主键") Long id) {
        userRepository.deleteById(id);
    }


    /**
     * 获取验证码
     */
    @GetMapping(value = "verifyCode")
    @ApiOperation(value = "获取验证码")
    public VerifyCode getCode() {

        //生成随机字串
        String verifyCode = VerifyCodeUtils.generateVerifyCode(4);
        String uuid = IdUtil.simpleUUID();
        // TODO 存取到与用户相关的作用域中
        // 生成图片
        int w = 111, h = 36;
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            VerifyCodeUtils.outputImage(w, h, stream, verifyCode);
            return new VerifyCode(Base64.encode(stream.toByteArray()), uuid);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
