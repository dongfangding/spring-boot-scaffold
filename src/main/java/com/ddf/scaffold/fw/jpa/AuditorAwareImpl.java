package com.ddf.scaffold.fw.jpa;

import com.ddf.scaffold.fw.entity.BaseDomain;
import com.ddf.scaffold.fw.session.SessionContext;
import com.ddf.scaffold.fw.util.ConstUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 *
 * 获得实体类保存的审计信息，针对{@code @CreatedBy}和{@code @LastModifiedDate}来捕捉用户信息
 *
 * @see BaseDomain#getCreateBy()
 * @see BaseDomain#getModifyBy()
 * @see SessionContext 开发人员在用户登录后，必须将用户相关的信息{@link SessionContext#setUid(String)}和{@link SessionContext#setUser(BaseDomain)}设置用户信息
 * @author DDf on 2019/1/2
 */
@Component
public class AuditorAwareImpl implements AuditorAware {

	@Autowired
	private SessionContext sessionContext;

	@Override
	public Optional getCurrentAuditor() {
		if (sessionContext == null || sessionContext.getUid() == null) {
			return Optional.of(ConstUtil.ANONYMOUS_NAME);
		}
		return Optional.of(sessionContext.getUid());
	}
}
