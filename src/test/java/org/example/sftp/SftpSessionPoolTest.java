package org.example.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.example.ObjectPoolFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class SftpSessionPoolTest {

    private SftpConnectionInfo connectionInfo;
    private ObjectPoolFactory<SftpConnectionInfo, Session> poolFactory;
    private ExecutorService executor;

    @BeforeEach
    void setup() {
        connectionInfo = new SftpConnectionInfo("localhost", 2222, "user", "pass");
        poolFactory = new ObjectPoolFactory<>(new SftpSessionFactory());
        poolFactory.setMaxTotal(8);
        executor = Executors.newFixedThreadPool(8);
    }

    @Test
    void testPooledSftpSession() {
        Set<Session> usedSessions = new HashSet<>();

        try (var pool = poolFactory.createPool()) {
            List<CompletableFuture<Void>> futures = IntStream.rangeClosed(1, 100)
                    .parallel()
                    .mapToObj(i -> CompletableFuture.runAsync(() -> {
                        try (var closeable = new PooledObjectCloseable<>(pool, connectionInfo)) {
                            Session session = closeable.getValue();
                            ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
                            sftp.disconnect();
                            usedSessions.add(session);
                        } catch (JSchException e) {
                            throw new RuntimeException(e);
                        }
                    }, executor))
                    .toList();

            var future = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            await().atMost(Duration.ofSeconds(10)).until(future::isDone);
            assertThat(usedSessions).size().isEqualTo(8);
        }
    }
}