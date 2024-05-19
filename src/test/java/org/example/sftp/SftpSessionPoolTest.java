package org.example.sftp;

import org.example.ObjectPoolFactory;

class SftpSessionPoolTest extends AbstractSftpPoolTests {

    @Override
    protected ObjectPoolFactory<SftpConnectionInfo, ?> createPoolFactory(int maxSize) {
        SftpSessionFactory sessionFactory = new SftpSessionFactory();
        ObjectPoolFactory<SftpConnectionInfo, ?> poolFactory = new ObjectPoolFactory<>(sessionFactory);
        poolFactory.setMaxTotal(maxSize);
        return poolFactory;
    }
}