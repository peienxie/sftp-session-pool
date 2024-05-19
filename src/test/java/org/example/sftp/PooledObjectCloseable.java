package org.example.sftp;

import lombok.Getter;
import org.apache.commons.pool2.KeyedObjectPool;

class PooledObjectCloseable<K, V> implements AutoCloseable {

    private final KeyedObjectPool<K, V> pool;
    private final K key;
    @Getter
    private final V value;

    public PooledObjectCloseable(KeyedObjectPool<K, V> pool, K key) {
        this.pool = pool;
        this.key = key;
        this.value = borrowQuietly(pool, key);
    }

    @Override
    public void close() {
        returnQuietly(pool, key, value);
    }

    private V borrowQuietly(KeyedObjectPool<K, V> pool, K key) {
        try {
            return pool.borrowObject(key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void returnQuietly(KeyedObjectPool<K, V> pool, K key, V object) {
        try {
            pool.returnObject(key, object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
