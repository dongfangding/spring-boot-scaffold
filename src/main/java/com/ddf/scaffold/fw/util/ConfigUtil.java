package com.ddf.scaffold.fw.util;

import com.ddf.scaffold.fw.security.SecurityUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import java.io.File;

@Component
@ConfigurationProperties(prefix = "customs.config-util")
public class ConfigUtil implements ServletContextAware {

	private String contextPath;

	@Getter
	@Setter
	private String dataUrl;

	@Getter
	@Setter
	private String dataRealDir;

	@Getter
	@Setter
	private String dataDir;

	private String templateDir;

	private String attachDir;

	private String tempDir;

	private String tempUrl;


	@Override
	public void setServletContext(ServletContext servletContext) {
		contextPath = servletContext.getRealPath("/");
	}
	
	public String getContextPath() {
		return contextPath;
	}
	
	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	
	public String getRealDataDir() {
		return contextPath + getDataDir();
	}

	public String getTempUrl() {
		return getDataUrl() + ConstUtil.DIR_SEP + SecurityUtils.getUserOrgCode() + ConstUtil.DIR_SEP + tempDir;
	}
	
	
	public String getTempDir() {
		return getDataRealDir() + File.separator + SecurityUtils.getUserOrgCode() + File.separator + tempDir;
	}

	public void setTempDir(String tempDir) {
		this.tempDir = tempDir;
	}

	public String getRealTempDir() {
		return getRealDataDir() + File.separator + File.separator + SecurityUtils.getUserOrgCode() + File.separator + tempDir;
	}
	
	public String getTemplateDir() {
		return getDataUrl() + File.separator + SecurityUtils.getUserOrgCode() + File.separator + templateDir;
	}

	public void setTemplateDir(String templateDir) {
		this.templateDir = templateDir;
	}
	
	public String getRealTemplateDir() {
		return getDataRealDir() + File.separator + SecurityUtils.getUserOrgCode() + File.separator + templateDir;
	}

	public String getAttachDir() {
		return getDataUrl() + File.separator + SecurityUtils.getUserOrgCode() + File.separator + attachDir;
	}

	public void setAttachDir(String attachDir) {
		this.attachDir = attachDir;
	}
	
	public String getRealAttachDir() {
		return getDataRealDir() + File.separator + SecurityUtils.getUserOrgCode() + File.separator + attachDir;
	}
}
