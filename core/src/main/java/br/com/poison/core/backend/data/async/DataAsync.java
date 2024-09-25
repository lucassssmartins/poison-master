package br.com.poison.core.backend.data.async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface DataAsync {

    ExecutorService service = Executors.newCachedThreadPool();

    default void runAsync(Runnable runnable) {
        CompletableFuture.runAsync(runnable, service);
    }
}