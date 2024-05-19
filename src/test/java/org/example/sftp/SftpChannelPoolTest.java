package org.example.sftp;

import org.example.KeyedObjectPoolFactory;

class SftpChannelPoolTest extends AbstractSftpPoolTests {

    @Override
    protected KeyedObjectPoolFactory<SftpConnectionInfo, ?> createPoolFactory() {
        SftpChannelFactory sftpChannelFactory = new SftpChannelFactory();
        return new KeyedObjectPoolFactory<>(sftpChannelFactory);
    }
}
