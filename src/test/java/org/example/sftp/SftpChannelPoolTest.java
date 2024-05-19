package org.example.sftp;

import org.example.KeyedObjectPoolFactory;

class SftpChannelPoolTest extends AbstractSftpPoolTests {

    @Override
    protected KeyedObjectPoolFactory<SftpConnectionInfo, ?> createPoolFactory(int maxSize) {
        SftpChannelFactory sftpChannelFactory = new SftpChannelFactory();
        KeyedObjectPoolFactory<SftpConnectionInfo, ?> poolFactory = new KeyedObjectPoolFactory<>(sftpChannelFactory);
        poolFactory.setMaxTotal(maxSize);
        return poolFactory;
    }
}
