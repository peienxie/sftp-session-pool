package org.example.sftp;

import org.example.KeyedObjectPoolFactory;

class SftpSessionPoolTest extends AbstractSftpPoolTests {

    @Override
    protected KeyedObjectPoolFactory<SftpConnectionInfo, ?> createPoolFactory() {
        SftpSessionFactory sessionFactory = new SftpSessionFactory();
        return new KeyedObjectPoolFactory<>(sessionFactory);
    }
}