package com.ddf.scaffold.logic.VO;

import com.ddf.scaffold.logic.entity.User;
import com.ddf.scaffold.logic.entity.UserOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 用户展示对象
 *
 * @author dongfang.ding
 * @date 2019/5/23 10:45
 */
@Data
@ToString(exclude = {"userOrderList"}, callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class UserVO extends User {

    List<UserOrder> userOrderList;
}
