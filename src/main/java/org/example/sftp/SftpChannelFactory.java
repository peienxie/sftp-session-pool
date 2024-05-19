package org.example.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.DestroyMode;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

@Log4j2
@Setter
public class SftpChannelFactory extends BaseKeyedPooledObjectFactory<SftpConnectionInfo, ChannelSftp> {

    private int connectTimeout = 3_000;
    private int socketTimeout = 3_000;

    @Override
    public ChannelSftp create(SftpConnectionInfo info) throws Exception {
        JSch jSch = new JSch();
        Session session = jSch.getSession(info.username(), info.host(), info.port());
        session.setPassword(info.password());
        session.setConfig("StrictHostKeyChecking", "no");
        session.setTimeout(socketTimeout);
        log.debug("creating session to {}:{}", info.host(), info.port());
        session.connect(connectTimeout);

        ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
        log.debug("creating sftp channel");
        sftp.connect();
        return sftp;
    }

    @Override
    public boolean validateObject(SftpConnectionInfo info, PooledObject<ChannelSftp> p) {
        return p.getObject().isConnected();
    }

    @Override
    public PooledObject<ChannelSftp> wrap(ChannelSftp obj) {
        return new DefaultPooledObject<>(obj);
    }

    @Override
    public void destroyObject(SftpConnectionInfo key, PooledObject<ChannelSftp> p, DestroyMode destroyMode) {
        p.getObject().disconnect();
    }

}
