package org.example.sftp;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.pool2.KeyedObjectPool;
import org.example.KeyedObjectPoolFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Log4j2
public abstract class AbstractSftpPoolTests {

    private SftpConnectionInfo server1;
    private SftpConnectionInfo server2;
    private KeyedObjectPool<SftpConnectionInfo, ?> pool;

    @BeforeEach
    void setup() {
        server1 = new SftpConnectionInfo("localhost", 2222, "user1", "pass");
        server2 = new SftpConnectionInfo("localhost", 2223, "user2", "secret");
        var poolFactory = this.createPoolFactory();
        poolFactory.setMaxTotal(8);
        pool = poolFactory.createPool();
    }

    @AfterEach
    void teardown() {
        pool.close();
    }

    abstract protected KeyedObjectPoolFactory<SftpConnectionInfo, ?> createPoolFactory();

    @Test
    void testPooling() {
        Executor executor = Executors.newFixedThreadPool(4);
        var futures = IntStream.rangeClosed(1, 4)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    try (var closeable = new PooledObjectCloseable<>(pool, server1)) {
                        Object object = closeable.getValue();
                        assertThat(object).isNotNull();
                        Thread.sleep(100);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, executor))
                .toList();

        var future = CompletableFuture.allOf(futures.toArray(new CompletableFuture[4]));
        await().atMost(Duration.ofSeconds(30)).until(future::isDone);
        assertThat(future).isCompleted();
        assertThat(pool.getNumActive()).isEqualTo(0);
        assertThat(pool.getNumIdle()).isEqualTo(4);
    }

    @Test
    void testPoolingWithMultipleSftpConnections() {
        CompletableFuture<Void> start = new CompletableFuture<>();

        Executor executor1 = Executors.newFixedThreadPool(2);
        var futures1 = IntStream.rangeClosed(1, 4)
                .mapToObj(i -> start.thenRunAsync(() -> {
                    try (var closeable = new PooledObjectCloseable<>(pool, server1)) {
                        Object object = closeable.getValue();
                        assertThat(object).isNotNull();
                        Thread.sleep(100);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, executor1))
                .toList();
        Executor executor2 = Executors.newFixedThreadPool(2);
        var futures2 = IntStream.rangeClosed(1, 4)
                .mapToObj(i -> start.thenRunAsync(() -> {
                    try (var closeable = new PooledObjectCloseable<>(pool, server2)) {
                        Object object = closeable.getValue();
                        assertThat(object).isNotNull();
                        Thread.sleep(100);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, executor2))
                .toList();

        var futures = new ArrayList<>(futures1);
        futures.addAll(futures2);
        var future = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        start.complete(null);
        await().atMost(Duration.ofSeconds(30)).until(future::isDone);
        assertThat(future).isCompleted();
        assertThat(pool.getNumActive()).isEqualTo(0);
        assertThat(pool.getNumIdle(server1)).isEqualTo(2);
        assertThat(pool.getNumIdle(server2)).isEqualTo(2);
    }
}
