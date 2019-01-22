package com.ddf.scaffold.fw.repository;

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

/**
 * @author DDf on 2019/1/18
 */
@Repository
@Transactional(readOnly = true)
public interface SerialNoRepository extends JpaBaseDao<PSerialNo, Integer> {

    /**
     * 获得序列号,手动控制事务，确保这里的序列号一算出来就把事务提交，防止因序列号已经生成因后续操作导致的延迟事务
     * 提交而导致变数，多个请求会获取不到之前未提交的事务里的序列号，最终导致序列号重复；
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
                final String queryString = " insert into P_SERIAL_NO (seru_id, seru_code, comp_code," +
                        " seno_suffix,seno_current_no, seno_expire) " +
                        " values(:seruId, :seruCode, :compCode," +
                        " :senoSuffix, :senoCurrentNo, :senoExpire)" +
                        " on duplicate key update" +
                        " seno_current_no = last_insert_id(seno_current_no + 1) ";
                Query nativeQuery = getEntityManager().createNativeQuery(queryString);

                /**
                 * 临时测试使用
                 */
                if (propertyMap.get("compCode") == null) {
                    propertyMap.put("compCode", "HT");
                }

                propertyMap.forEach(nativeQuery::setParameter);
                int affectRows = nativeQuery.executeUpdate();
                Long id = null;
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
