package org.example.sftp;

import org.example.ObjectPoolFactory;

class SftpChannelPoolTest extends AbstractSftpPoolTests {

    @Override
    protected ObjectPoolFactory<SftpConnectionInfo, ?> createPoolFactory(int maxSize) {
        SftpChannelFactory sftpChannelFactory = new SftpChannelFactory();
        ObjectPoolFactory<SftpConnectionInfo, ?> poolFactory = new ObjectPoolFactory<>(sftpChannelFactory);
        poolFactory.setMaxTotal(maxSize);
        return poolFactory;
    }
}
