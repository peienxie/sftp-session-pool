package org.example.sftp;

import lombok.extern.log4j.Log4j2;
import org.example.KeyedObjectPoolFactory;
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
public abstract class AbstractSftpPoolTests {

    private SftpConnectionInfo connectionInfo;
    private ExecutorService executor;
    private KeyedObjectPoolFactory<SftpConnectionInfo, ?> poolFactory;
    private int runCounts;

    @BeforeEach
    void setup() {
        connectionInfo = new SftpConnectionInfo("localhost", 2222, "user", "pass");
        executor = Executors.newFixedThreadPool(8);
        poolFactory = this.createPoolFactory(8);
        runCounts = 1000;
    }

    abstract protected KeyedObjectPoolFactory<SftpConnectionInfo, ?> createPoolFactory(int maxSize);

    @Test
    void testPooling() {
        Set<Object> usedObjects = new HashSet<>();

        try (var pool = poolFactory.createPool()) {
            List<CompletableFuture<Void>> futures = IntStream.rangeClosed(1, runCounts)
                    .mapToObj(i -> CompletableFuture.runAsync(() -> {
                        try (var closeable = new PooledObjectCloseable<>(pool, connectionInfo)) {
                            Object object = closeable.getValue();
                            usedObjects.add(object);
                            log.debug("i={}, sizeOfUsedObjects={}", i, usedObjects.size());
                            log.trace("i={}, usedObjects={}", i, usedObjects);
                        }
                    }, executor))
                    .toList();

            var future = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            await().atMost(Duration.ofSeconds(20)).until(future::isDone);
            assertThat(usedObjects).size().isEqualTo(8);
        }
    }

}
