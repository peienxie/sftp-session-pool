package org.example;

import lombok.Setter;
import lombok.experimental.Delegate;
import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.PoolUtils;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;

@Setter
public class ObjectPoolFactory<K, V> {

    private final KeyedPooledObjectFactory<K, V> objectFactory;
    @Delegate
    private final GenericKeyedObjectPoolConfig<V> config;

    public ObjectPoolFactory(KeyedPooledObjectFactory<K, V> objectFactory) {
        this.objectFactory = objectFactory;
        this.config = new GenericKeyedObjectPoolConfig<>();
    }

    public ObjectPoolFactory(KeyedPooledObjectFactory<K, V> objectFactory,
                             GenericKeyedObjectPoolConfig<V> config) {
        this.objectFactory = objectFactory;
        this.config = config;
    }

    public KeyedObjectPool<K, V> createPool() {
        return PoolUtils.synchronizedPool(new GenericKeyedObjectPool<>(objectFactory, config));
    }
}

