package org.example;

import lombok.Setter;
import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.PoolUtils;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;

import java.time.Duration;

@Setter
public class ObjectPoolFactory<K, V> {

    private final KeyedPooledObjectFactory<K, V> objectFactory;
    private int maxTotal = 50;
    private int maxTotalPerHost = 10;
    private int minIdlePerHost = 3;
    private long maxWaitMillis = 10_000;
    private boolean testOnBorrow = true;
    private boolean testOnCreate = true;
    private boolean testOnReturn = true;
    private boolean testWhileIdle = true;

    public ObjectPoolFactory(KeyedPooledObjectFactory<K, V> objectFactory) {
        this.objectFactory = objectFactory;
    }

    public KeyedObjectPool<K, V> createPool() {
        GenericKeyedObjectPoolConfig<V> config = new GenericKeyedObjectPoolConfig<>();
        config.setMaxTotal(maxTotal);
        config.setMaxTotalPerKey(maxTotalPerHost);
        config.setMinIdlePerKey(minIdlePerHost);
        config.setMaxWait(Duration.ofMillis(maxWaitMillis));
        config.setBlockWhenExhausted(true);
        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnCreate(testOnCreate);
        config.setTestOnReturn(testOnReturn);
        config.setTestWhileIdle(testWhileIdle);
        return this.createPool(config);
    }

    public KeyedObjectPool<K, V> createPool(GenericKeyedObjectPoolConfig<V> config) {
        return PoolUtils.synchronizedPool(new GenericKeyedObjectPool<>(objectFactory, config));
    }
}

