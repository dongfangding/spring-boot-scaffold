package com.ddf.scaffold.logic.model.VO;

import com.ddf.scaffold.logic.model.entity.User;
import com.ddf.scaffold.logic.model.entity.UserOrder;
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
public class UserVO1 extends User {

    List<UserOrder> userOrderList;
}
