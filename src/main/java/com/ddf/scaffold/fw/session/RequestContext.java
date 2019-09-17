package com.ddf.scaffold.fw.session;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author dongfang.ding on 2018/12/31
 */
@Component
@RequestScope
public class RequestContext extends HashMap<String, Object>  implements Serializable {
	private static final long serialVersionUID = 611927845534787914L;

	@Getter
	private Map<String, Object> paramMap = new ConcurrentHashMap<>();

	@Getter
	@Setter
	private Collection<MultipartFile> fileItems;
}
