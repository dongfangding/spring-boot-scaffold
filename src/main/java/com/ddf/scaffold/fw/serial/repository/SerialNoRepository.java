package com.ddf.scaffold.fw.serial.repository;

import com.ddf.scaffold.fw.entity.PSerialNo;
import com.ddf.scaffold.fw.exception.GlobalCustomizeException;
import com.ddf.scaffold.fw.exception.GlobalExceptionEnum;
import com.ddf.scaffold.fw.jpa.JpaBaseDao;
import com.ddf.scaffold.fw.util.SpringContextHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.persistence.Query;
import java.math.BigInteger;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author dongfang.ding on 2019/1/18
 */
@Repository
@Transactional(readOnly = true)
public interface SerialNoRepository extends JpaBaseDao<PSerialNo, Integer> {

    Set<String> seruCodeSet = new CopyOnWriteArraySet<>();

    /**
     * 获得序列号,手动控制事务，确保这里的序列号一算出来就把事务提交，防止因序列号已经生成因后续操作导致的延迟事务
     * 提交而导致变数，多个请求会获取不到之前未提交的事务里的序列号，最终导致序列号重复；
     * // 序列号重复的原因不在于锁的问题，即使是独占锁，但拿到后此时事务未提交对其他事务不可见，其它事务的序列号依然是在
     * 旧的数据上进行自增；
     * @param propertyMap
     * @return
     */
    default Long getNextSerialNo(final Map<String, Object> propertyMap) {
        PlatformTransactionManager transactionManager = SpringContextHolder.getBean(PlatformTransactionManager.class);
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            if (propertyMap != null && !propertyMap.isEmpty()) {
                Object seruCode = propertyMap.get("seruCode");
                seruCodeSet.add(seruCode.toString());
                // 只锁住同一种类型的序列号
                if (seruCodeSet.contains(seruCode.toString())) {
                    // 自旋
                    getNextSerialNo(propertyMap);
                }
                final String queryString = " insert into P_SERIAL_NO (seru_id, seru_code, comp_code," +
                        " seno_suffix,seno_current_no, seno_expire) " +
                        " values(:seruId, :seruCode, :compCode," +
                        " :senoSuffix, :senoCurrentNo, :senoExpire)" +
                        " on duplicate key update" +
                        " seno_current_no = last_insert_id(seno_current_no + 1) ";
                Query nativeQuery = getEntityManager().createNativeQuery(queryString);
                propertyMap.forEach(nativeQuery::setParameter);
                int affectRows = nativeQuery.executeUpdate();
                Long id;
                // add new record(not update), affectRows = 1; update the
                // record, affectRows=3
                if (affectRows == 1) {
                    id = Long.parseLong(propertyMap.get("senoCurrentNo").toString());
                } else {
                    nativeQuery = getEntityManager().createNativeQuery("select last_insert_id()");
                    BigInteger bigId = (BigInteger) nativeQuery.getSingleResult();
                    id = bigId.longValue();
                }
                transactionManager.commit(status);
                seruCodeSet.remove(seruCode.toString());
                return id;
            }
            throw new GlobalCustomizeException(GlobalExceptionEnum.SERIAL_NO_GENERATE_FAILURE);
        } catch (Exception e) {
            transactionManager.rollback(status);
        }
        return null;
    }

    /**
     * 初始化, 凌晨执行, 删除过期的号 delete the expired record
     */
    @Transactional
    default void init() {
        StringBuffer sb = new StringBuffer();
        sb.append("delete from P_SERIAL_NO p");
        sb.append(" where p.seno_expire <= now()");
        final String queryString = sb.toString();
        getEntityManager().createNativeQuery(queryString).executeUpdate();
    }
}
