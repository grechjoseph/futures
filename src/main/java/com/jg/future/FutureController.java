package com.jg.future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@RestController
public class FutureController {

    public static final long MILLIS = 2000L;

    @GetMapping()
    public void supplyAsync() throws ExecutionException, InterruptedException {
        // Start CompletableFuture immediately (runAsync)
        final CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        System.out.println("1");
                        Thread.sleep(500);
                        System.out.println("2");
                        Thread.sleep(500);
                        System.out.println("3");
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });

        System.out.println("4");
        Thread.sleep(500);
        System.out.println("5");
        Thread.sleep(500);

        // Wait for CompletableFuture to complete.
        future.get();

        System.out.println("6");
        Thread.sleep(500);
    }

    /* SIMPLE FUTURE */

    @GetMapping("/simple")
    public String simpleFuture() throws ExecutionException, InterruptedException {
        return doSimpleFuture(MILLIS).get().toString();
    }

    /**
     * Initializes a CompletableFuture to set the value of the object to testKey=key and testValue=value after value of
     * 'millis'. Therefore, when called with .get(), the calling Thread is blocked until the CompletableFuture is
     * completed.
     * @param millis The milliseconds to wait before completing.
     * @return The completed future.
     */
    private Future<TestObject> doSimpleFuture(long millis) {
        final CompletableFuture<TestObject> completableFuture = new CompletableFuture<>();

        Executors.newCachedThreadPool().submit(() -> {
            Thread.sleep(millis);
            completableFuture.complete(new TestObject("key", "value"));
            return null;
        });

        return completableFuture;
    }

    /* FUTURE WITH CANCELLATION */

    @GetMapping("/cancellation")
    public String futureWithCancellation() throws InterruptedException, ExecutionException {
        return doFutureWithCancellation(MILLIS).get().toString();
    }

    /**
     * Should throw CancellationException since cancelled.
     * @param millis The milliseconds to wait before cancelling.
     * @return
     * @throws InterruptedException
     */
    private Future<TestObject> doFutureWithCancellation(long millis) throws InterruptedException {
        final CompletableFuture<TestObject> completableFuture = new CompletableFuture<>();

        Executors.newCachedThreadPool().submit(() -> {
            Thread.sleep(millis);
            completableFuture.cancel(false);
            return null;
        });

        return completableFuture;
    }

    /* COMPLETABLE FUTURE WITH ENCAPSULATED COMPUTATION LOGIC */

    @GetMapping("/encapsulated")
    public String futureWithEncapsulatedLogic() throws InterruptedException, ExecutionException {
        doFutureWithEncapsulatedLogicNoReturn(MILLIS);
        return doFutureWithEncapsulatedLogic(MILLIS).get().toString();
    }

    /**
     * Uses .supplyAsync instead of first initializaing then executing the Future.
     * @param millis
     * @return
     * @throws InterruptedException
     */
    private Future<TestObject> doFutureWithEncapsulatedLogic(long millis) throws InterruptedException {
        return CompletableFuture.supplyAsync( () -> {
            try {
                Thread.sleep(millis);
                return new TestObject("key", "value");
            } catch (InterruptedException e) {
                return null;
            }
        });
    }

    /**
     * Uses .supplyAsync instead of first initializaing then executing the Future but without returning an object.
     * @param millis
     * @return
     * @throws InterruptedException
     */
    private void doFutureWithEncapsulatedLogicNoReturn(long millis) throws InterruptedException {
        CompletableFuture<String> completableFuture
                = CompletableFuture.supplyAsync(() -> {
                    try {
                        Thread.sleep(millis);
                        return "Hello";
                    } catch (InterruptedException e) {
                        return null;
                    }
                });

        CompletableFuture<Void> future = completableFuture
                .thenAccept(s -> System.out.println("Computation returned: " + s));
    }

    @Data
    @ToString
    @AllArgsConstructor
    private class TestObject {
        private String testKey;
        private String testValue;
    }
}
