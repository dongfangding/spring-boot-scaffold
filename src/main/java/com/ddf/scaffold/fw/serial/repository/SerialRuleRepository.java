package com.ddf.scaffold.fw.serial.repository;

import com.ddf.scaffold.fw.entity.PSerialRule;
import com.ddf.scaffold.fw.jpa.JpaBaseDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author DDf on 2019/1/18
 */
@Repository
@Transactional(readOnly = true)
public interface SerialRuleRepository extends JpaBaseDao<PSerialRule, Integer> {
}
