package kin.sdk;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLongArray;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class RequestTest {

    private static final int TASK_DURATION_MILLIS = 100;
    private static final int TIMEOUT_DURATION_MILLIS = 500;

    public interface Consumer<T> {

        void accept(T t);
    }

    private <T> void runRequest(final Callable<T> task, final Consumer<T> onResultCallback,
        final Consumer<Exception> onErrorCallback)
        throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        Request<T> mockRequest = new Request<>(new Callable<T>() {
            @Override
            public T call() throws Exception {
                T result = task.call();
                Thread.sleep(TASK_DURATION_MILLIS);
                return result;
            }
        });
        mockRequest.run(new ResultCallback<T>() {
            @Override
            public void onResult(T result) {
                if (onResultCallback != null) {
                    onResultCallback.accept(result);
                }
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                if (onErrorCallback != null) {
                    onErrorCallback.accept(e);
                }
                latch.countDown();
            }
        });
        assertTrue(latch.await(TIMEOUT_DURATION_MILLIS, TimeUnit.MILLISECONDS));
    }

    @Test
    public void run_verifyExpectedResult() throws InterruptedException {
        final String expectedResult = "ExpectedResult";
        runRequest(
            new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    return expectedResult;
                }
            },
            new Consumer<Object>() {
                @Override
                public void accept(Object result) {
                    assertEquals(expectedResult, result);
                }
            },
            null
        );
    }

    @Test
    public void run_verifyCorrectThreads() throws InterruptedException {
        final AtomicLongArray threadIds = new AtomicLongArray(2);
        runRequest(new Callable<Object>() {
                       @Override
                       public Object call() throws Exception {
                           Log.d("debug", "Result Thread = " + Thread.currentThread());
                           threadIds.set(0, Thread.currentThread().getId());
                           return null;
                       }
                   },
            new Consumer<Object>() {
                @Override
                public void accept(Object result) {
                    Log.d("debug", "Error Thread = " + Thread.currentThread());
                    threadIds.set(1, Thread.currentThread().getId());
                }
            },
            null
        );

        long mainThreadId = Looper.getMainLooper().getThread().getId();
        assertNotEquals(mainThreadId, threadIds.get(0));
        assertEquals(mainThreadId, threadIds.get(1));
    }

    @Test
    public void run_verifyExceptionPropagation() throws InterruptedException {
        final Exception expectedException = new Exception("some exception");
        runRequest(new Callable<Object>() {
                       @Override
                       public Object call() throws Exception {
                           throw expectedException;
                       }
                   },
            null
            , new Consumer<Exception>() {
                @Override
                public void accept(Exception e) {
                    assertEquals(expectedException, e);
                }
            });
    }

    @Test
    public void run_cancelInterrupt() throws InterruptedException {
        threadInterruptTest(true);
    }

    @Test
    public void run_cancelDoNotInterrupt() throws InterruptedException {
        threadInterruptTest(false);
    }

    private void threadInterruptTest(boolean expectThreadInterruptted) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final CountDownLatch runLatch = new CountDownLatch(1);
        final AtomicBoolean threadInterrupted = new AtomicBoolean(false);
        final AtomicBoolean callbackExecuted = new AtomicBoolean(false);
        Request<Object> mockRequest = new Request<>(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                try {
                    runLatch.countDown();
                    Thread.sleep(TASK_DURATION_MILLIS);
                } catch (InterruptedException ie) {
                    threadInterrupted.set(true);
                }
                latch.countDown();
                return new Object();
            }
        });
        mockRequest.run(new ResultCallback<Object>() {
            @Override
            public void onResult(Object result) {
                callbackExecuted.set(true);
            }

            @Override
            public void onError(Exception e) {
                callbackExecuted.set(true);
            }
        });
        assertTrue(runLatch.await(TIMEOUT_DURATION_MILLIS, TimeUnit.MILLISECONDS));
        mockRequest.cancel(expectThreadInterruptted);
        assertTrue(latch.await(TIMEOUT_DURATION_MILLIS, TimeUnit.MILLISECONDS));
        assertEquals(expectThreadInterruptted, threadInterrupted.get());
        assertFalse(callbackExecuted.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void request_nullCallable() {
        new Request<>(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void run_nullCallback() {
        Request<String> request = new Request<>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "";
            }
        });
        request.run(null);
    }

    @Test(expected = IllegalStateException.class)
    public void run_twice() {
        Request<String> request = new Request<>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "";
            }
        });
        request.run(getEmptyResultCallback());
        request.run(getEmptyResultCallback());
    }

    @Test(expected = IllegalStateException.class)
    public void runAfterCancel() {
        Request<String> request = new Request<>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "";
            }
        });
        request.cancel(true);
        request.run(getEmptyResultCallback());
    }

    @NonNull
    private ResultCallback<String> getEmptyResultCallback() {
        return new ResultCallback<String>() {
            @Override
            public void onResult(String result) {
            }

            @Override
            public void onError(Exception e) {
            }
        };
    }
}