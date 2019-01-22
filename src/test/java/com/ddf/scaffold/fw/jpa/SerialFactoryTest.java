package com.ddf.scaffold.fw.jpa;

import com.ddf.scaffold.ApplicationTest;
import com.ddf.scaffold.fw.session.RequestContext;
import com.ddf.scaffold.fw.session.SessionContext;
import com.ddf.scaffold.fw.util.SerialFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

/**
 * @author DDf on 2019/1/18
 */
@Transactional
public class SerialFactoryTest extends ApplicationTest {
    @Autowired
    private SerialFactory serialFactory;
    @Autowired
    private SessionContext sessionContext;
    @Autowired
    private RequestContext requestContext;
    @Autowired
    private JpaBaseDaoAspect jpaBaseDaoAspect;


    @Test
    public void test() {
        String code = "CUSTOMER_NO";
        String serial = serialFactory.getSerial(code, null, null);
        System.out.println("serial = " + serial);
    }

    @Test
    public void testThread() throws ExecutionException, InterruptedException {
        String code = "CUSTOMER_NO";
        CountDownLatch countDownLatch = new CountDownLatch(50);
        for (int i = 0; i < 50; i++) {
            Thread thread = new Thread(() -> {
                try {
                    serialFactory.getSerial(code, null, null);
                } finally {
                    countDownLatch.countDown();
                }
            });
            thread.start();
        }
        countDownLatch.await();
    }
}
