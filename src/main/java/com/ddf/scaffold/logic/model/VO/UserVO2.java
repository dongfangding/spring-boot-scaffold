package com.ddf.scaffold.logic.model.VO;

import com.ddf.scaffold.logic.model.entity.User;
import com.ddf.scaffold.logic.model.entity.UserOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 *
 * 使用组合的方式来测试多表映射
 *
 * @author dongfang.ding
 * @date 2019/5/23 13:37
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class UserVO2 {

    private User user;

    private List<UserOrder> userOrderList;
}
