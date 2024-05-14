package org.example.sftp;

import com.jcraft.jsch.Session;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.PoolUtils;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;

import java.time.Duration;

@RequiredArgsConstructor
@Setter
public class SftpSessionPoolFactory {

    private final SftpSessionObjectFactory sessionFactory;
    private int maxTotal = 50;
    private int maxTotalPerHost = 10;
    private int minIdlePerHost = 3;
    private long maxWaitMillis = 10_000;
    private boolean testOnBorrow = true;
    private boolean testOnCreate = true;
    private boolean testOnReturn = true;
    private boolean testWhileIdle = true;


    public KeyedObjectPool<SftpConnectionInfo, Session> createPool() {
        GenericKeyedObjectPoolConfig<Session> config = new GenericKeyedObjectPoolConfig<>();
        config.setMaxTotal(maxTotal);
        config.setMaxTotalPerKey(maxTotalPerHost);
        config.setMinIdlePerKey(minIdlePerHost);
        config.setMaxWait(Duration.ofMillis(maxWaitMillis));
        config.setBlockWhenExhausted(true);
        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnCreate(testOnCreate);
        config.setTestOnReturn(testOnReturn);
        config.setTestWhileIdle(testWhileIdle);
        return PoolUtils.synchronizedPool(new GenericKeyedObjectPool<>(sessionFactory, config));
    }
}

