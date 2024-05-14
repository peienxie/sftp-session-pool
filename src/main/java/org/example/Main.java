package org.example;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.example.sftp.SftpConnectionInfo;
import org.example.sftp.SftpSessionObjectFactory;
import org.example.sftp.SftpSessionPoolFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class Main {

    private static final Logger log = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        Configurator.setRootLevel(Level.DEBUG);

        SftpSessionObjectFactory sessionFactory = new SftpSessionObjectFactory();
        SftpSessionPoolFactory poolFactory = new SftpSessionPoolFactory(sessionFactory);
        KeyedObjectPool<SftpConnectionInfo, Session> pool = poolFactory.createPool();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        Set<Session> usedSessions = new HashSet<>();
        List<CompletableFuture<Boolean>> futures = IntStream.rangeClosed(1, 100)
                .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                    try {
                        SftpConnectionInfo connectionInfo = new SftpConnectionInfo("localhost", 2222, "user", "pass");
                        Session session = pool.borrowObject(connectionInfo);
                        try {
                            log.info("[{}] start connecting", i);
                            ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
                            sftp.disconnect();
                            log.info("[{}] closed channel", i);
                            usedSessions.add(session);
                        } finally {
                            pool.returnObject(connectionInfo, session);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return true;
                }, executor))
                .toList();
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
            log.info("All used sessions: {}, {}", usedSessions.size(), usedSessions);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        pool.close();
    }
}