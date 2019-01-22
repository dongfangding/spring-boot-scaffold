package com.ddf.scaffold.fw.util;


import com.ddf.scaffold.fw.session.*;

/**
 * @author DDf on 2018/12/31
 */
public enum ContextKey {

	/** 是否强制刷新查询对象表结构缓存 */
	flushCache,

	/** 本次请求包含的{@link QueryParam}数组对象信息，应保持使用JSON数组对象形式 */
	queryParams,

	/** paramMap的属性如果匹配当前对象，是否默认加入到查询条件中，为方便前端传个别字段查询，为了防止与后端自己写的查询重复处理，
	 * 默认按false来处理，如果需要，可以将{@link RequestContext}的paramMap的当前key的值设置为1 */
	contextToField
}
