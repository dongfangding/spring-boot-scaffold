package com.ddf.scaffold.fw.jpa;

import com.ddf.scaffold.fw.entity.BaseDomain;
import com.ddf.scaffold.fw.security.UserToken;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 *
 * 获得实体类保存的审计信息，针对{@code @CreatedBy}和{@code @LastModifiedBy}来捕捉用户信息
 *
 * @see BaseDomain#getCreateBy()
 * @see BaseDomain#getModifyBy()
 * @author DDf on 2019/1/2
 */
@Component
public class AuditorAwareImpl implements AuditorAware<String> {

	@Override
	public Optional getCurrentAuditor() {
		return Optional.of(UserToken.getUserName());
	}
}
