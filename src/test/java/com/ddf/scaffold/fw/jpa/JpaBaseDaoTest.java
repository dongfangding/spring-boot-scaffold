package com.ddf.scaffold.fw.jpa;

import com.ddf.scaffold.ApplicationTest;
import com.ddf.scaffold.entity.User;
import com.ddf.scaffold.fw.util.QueryParam;
import com.ddf.scaffold.repository.UserRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.transaction.Transactional;
import java.util.*;

/**
 * @author DDf on 2019/1/3
 */
@Transactional
public class JpaBaseDaoTest extends ApplicationTest {
	@Autowired
	private UserRepository userRepository;


	/**
	 * 单表根据Id查询
	 */
	@Test
	public void testFindById() {
		Optional<User> user = userRepository.findById(1L);
		user.ifPresent(System.out::println);
	}

	/**
	 * 单表根据id删除
	 */
	@Test
	public void testDeleteById() {
		userRepository.deleteById(1L);
	}

	/**
	 * 单表直接删除一个对象
	 */
	@Test
	public void testDelete() {
		Optional<User> user = userRepository.findById(1L);
		user.ifPresent(user1 -> userRepository.delete(user1));
	}

	/**
	 * 单表根据一组id查询数据，暂未封装加上removed = 0条件
	 */
	@Test
	public void testFindAllById() {
		List<User> allById = userRepository.findAllById(Arrays.asList(1L, 2L, 3L));
		if (allById != null && !allById.isEmpty()) {
			System.out.println(allById);
		}
	}

	/**
	 * 单表根据简单的匹配条件返回一条数据，value值必须与在实体里对应属性的类型相同,条件字段必须在实体类中存在，否则会抛出异常
	 */
	@Test
	public void testFindOneByProperties() {
		Map<String, Object> propertiesMap = new HashMap<>();
		propertiesMap.put("id", 1L);
		propertiesMap.put("userName", "ddf");

		// 带removed = 0
		userRepository.findOneByProperties(propertiesMap);

		// 不带removed = 0
		userRepository.findOneByProperties(propertiesMap, false);

	}


	/**
	 * 单表根据简单的匹配条件返回结果集，value值必须与在实体里对应属性的类型相同,条件字段必须在实体类中存在，否则会抛出异常
	 */
	@Test
	public void findByProperties() {
		Map<String, Object> propertiesMap = new HashMap<>();
		propertiesMap.put("createBy", "ddf");
		// 带removed = 0
		userRepository.findByProperties(propertiesMap);

		// 不带removed = 0
		userRepository.findByProperties(propertiesMap, false);

	}


	/**
	 * 单表复杂查询条件返回结果集，value值必须与在实体里对应属性的类型相同,条件字段必须在实体类中存在，否则会抛出异常
	 */
	@Test
	public void testFindByQueryParams() {
		List<QueryParam> queryParams = new ArrayList<>();
		queryParams.add(new QueryParam<>("userName", "ddf"));
		queryParams.add(new QueryParam<>("id", QueryParam.Op.GT, 0L));
		queryParams.add(new QueryParam<>("createBy", QueryParam.Op.LIKE, "d"));
		queryParams.add(new QueryParam<>("createTime", QueryParam.Op.LT,  new Date(), QueryParam.Relative.OR, "createTime"));
		queryParams.add(new QueryParam<>("createTime", QueryParam.Op.LT,  new Date().getTime() + 3000000, QueryParam.Relative.OR, "createTime"));
		queryParams.add(new QueryParam<>("removed", QueryParam.Op.NE, 100));
		queryParams.add(new QueryParam<>("version", QueryParam.Op.NN, "", QueryParam.Relative.OR));
		queryParams.add(new QueryParam("userName", QueryParam.Op.EQ, "ddd", QueryParam.Relative.AND, "userName"));
		queryParams.add(new QueryParam("removed", QueryParam.Op.GE, 0, QueryParam.Relative.OR, "userName"));


		userRepository.findByQueryParams(queryParams);
		userRepository.findByQueryParams(queryParams, false);
	}

	/**
	 * 单表根据复杂条件更新部分字段值，version为可选项，在某些场景确定需要的情况下最好传入
	 */
	@Test
	public void testUpdateByMap() {
		List<QueryParam> queryParams = new ArrayList<>();
		queryParams.add(new QueryParam<>("userName", "ddf"));
		queryParams.add(new QueryParam<>("id", QueryParam.Op.GT, 0L));
		queryParams.add(new QueryParam<>("createBy", QueryParam.Op.LIKE, "d"));
		queryParams.add(new QueryParam<>("createTime", QueryParam.Op.LT, new Date()));
		queryParams.add(new QueryParam<>("removed", QueryParam.Op.NE, 100));
		queryParams.add(new QueryParam<>("version", QueryParam.Op.NN, 5, QueryParam.Relative.OR));

		Map<String, Object> fieldMap = new HashMap<>();
		fieldMap.put("removed", 1);
		userRepository.updateByMap(fieldMap, queryParams);
	}

	/**
	 * 单表保存或更新，当对象的id在数据库重存在时，则此时为更新，若id不存在，则为保存，如果为更新,则version必传，否则会报错
	 */
	@Test
	public void testSave() {
		User user = new User();
		user.setId(1L);
		user.setUserName("ddf^");
		user.setVersion(111);
		userRepository.save(user);
	}


	/**
	 * 单表根据条件进行分页和排序查询，支持简单和复杂条件
	 */
	@Test
	public void testPageByProperties() {
		Map<String, Object> propertiesMap = new HashMap<>();
		propertiesMap.put("createBy", "ddf");
		Pageable pageable = PageRequest.of(1, 2);
		Page<User> users = userRepository.pageByProperties(propertiesMap, pageable);
		System.out.println(users);


		List<QueryParam> queryParams = new ArrayList<>();
		queryParams.add(new QueryParam("version", QueryParam.Op.GE, 0));

		Sort sort = Sort.by(Sort.Order.asc("createBy"), Sort.Order.desc("createTime"));
		Pageable pageable1 = PageRequest.of(1, 2, sort);

		Page<User> users1 = userRepository.pageByQueryParams(queryParams, pageable1);
		System.out.println(users1);
	}

	/**
	 * 单表根据复杂条件查询匹配结果大小
	 */
	@Test
	public void testQuerySize() {
		List<QueryParam> queryParams = new ArrayList<>();
		queryParams.add(new QueryParam<>("userName", "ddf"));
		queryParams.add(new QueryParam<>("id", QueryParam.Op.GT, 0L));
		queryParams.add(new QueryParam<>("createBy", QueryParam.Op.LIKE, "d"));
		queryParams.add(new QueryParam<>("createTime", QueryParam.Op.LT, new Date()));
		queryParams.add(new QueryParam<>("removed", QueryParam.Op.NE, 100));
		Long count = userRepository.querySize(queryParams, null);
		System.out.println(count);


		Map<String, Object> propertiesMap = new HashMap<>();
		propertiesMap.put("version", 0);
		Long countId = userRepository.querySize(propertiesMap, "id");
		System.out.println(countId);

	}
}