package org.example.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.extern.log4j.Log4j2;
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

@Log4j2
public class SftpChannelPoolTest {

    private SftpConnectionInfo connectionInfo;
    private ObjectPoolFactory<SftpConnectionInfo, ChannelSftp> poolFactory;
    private ExecutorService executor;

    @BeforeEach
    void setup() {
        connectionInfo = new SftpConnectionInfo("localhost", 2222, "user", "pass");
        poolFactory = new ObjectPoolFactory<>(new SftpChannelFactory());
        poolFactory.setMaxTotal(8);
        executor = Executors.newFixedThreadPool(8);
    }

    @Test
    void testPooledSftpChannel() {
        Set<Session> usedSessions = new HashSet<>();
        Set<ChannelSftp> usedChannels = new HashSet<>();

        try (var pool = poolFactory.createPool()) {
            List<CompletableFuture<Void>> futures = IntStream.rangeClosed(1, 100)
                    .mapToObj(i -> CompletableFuture.runAsync(() -> {
                        try (var closeable = new PooledObjectCloseable<>(pool, connectionInfo)) {
                            ChannelSftp sftp = closeable.getValue();
                            usedSessions.add(sftp.getSession());
                            usedChannels.add(sftp);
                            log.debug("i={}, sizeOfSessions={}, sizeOfChannels={}", i, usedSessions.size(), usedChannels.size());
                        } catch (JSchException e) {
                            throw new RuntimeException(e);
                        }
                    }, executor))
                    .toList();

            var future = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            await().atMost(Duration.ofSeconds(20)).until(future::isDone);
            assertThat(usedSessions).size().isEqualTo(8);
            assertThat(usedChannels).size().isEqualTo(8);
        }
    }
}
