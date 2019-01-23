package com.ddf.scaffold.fw.session;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author DDf on 2018/12/31
 */
@Component
@RequestScope
public class RequestContext implements Serializable {
	private static final long serialVersionUID = 611927845534787914L;

	@Getter
	private Map<String, Object> paramMap = new ConcurrentHashMap<>();

	@Getter
	@Setter
	private Collection<MultipartFile> fileItems;
}
