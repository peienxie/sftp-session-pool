package org.example.sftp;

import org.example.KeyedObjectPoolFactory;

class SftpSessionPoolTest extends AbstractSftpPoolTests {

    @Override
    protected KeyedObjectPoolFactory<SftpConnectionInfo, ?> createPoolFactory(int maxSize) {
        SftpSessionFactory sessionFactory = new SftpSessionFactory();
        KeyedObjectPoolFactory<SftpConnectionInfo, ?> poolFactory = new KeyedObjectPoolFactory<>(sessionFactory);
        poolFactory.setMaxTotal(maxSize);
        return poolFactory;
    }
}