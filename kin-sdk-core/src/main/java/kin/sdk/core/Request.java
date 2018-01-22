package kin.sdk.core;


import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Represents {@link KinAccount} method invocation, each request will run sequentially on background thread,
 * and will notify {@link ResultCallback} witch success or error on main thread.
 *
 * @param <T> request result type
 */
public class Request<T> {

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler;
    private final Callable<T> callable;
    private boolean cancelled;
    private boolean executed;
    private Future<?> future;
    private ResultCallback<T> resultCallback;

    Request(Callable<T> callable) {
        checkNotNull(callable, "callable");
        this.callable = callable;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Run request asynchronously, notify {@code callback} with successful result or error
     */
    synchronized public void run(ResultCallback<T> callback) {
        checkBeforeRun(callback);
        executed = true;
        submitFuture(callable, callback);
    }

    private void checkBeforeRun(ResultCallback<T> callback) {
        checkNotNull(callback, "callback");
        if (executed) {
            throw new IllegalStateException("Request already running.");
        }
        if (cancelled) {
            throw new IllegalStateException("Request already cancelled.");
        }
    }

    private void checkNotNull(Object param, String name) {
        if (param == null) {
            throw new IllegalArgumentException(name + " cannot be null.");
        }
    }

    private void submitFuture(final Callable<T> callable, ResultCallback<T> callback) {
        this.resultCallback = callback;
        future = executorService.submit(() -> {
            try {
                final T result = callable.call();
                executeOnMainThreadIfNotCancelled(() -> resultCallback.onResult(result));
            } catch (final Exception e) {
                executeOnMainThreadIfNotCancelled(() -> resultCallback.onError(e));
            }
        });
    }

    private synchronized void executeOnMainThreadIfNotCancelled(Runnable runnable) {
        if (!cancelled) {
            mainHandler.post(runnable);
        }
    }

    /**
     * Cancel {@code Request} and detach its callback,
     * an attempt will be made to cancel ongoing request, if request has not run yet it will never run.
     *
     * @param mayInterruptIfRunning true if the request should be interrupted; otherwise,
     * in-progress requests are allowed to complete
     */
    synchronized public void cancel(boolean mayInterruptIfRunning) {
        if (!cancelled) {
            cancelled = true;
            if (future != null) {
                future.cancel(mayInterruptIfRunning);
            }
            future = null;
            mainHandler.removeCallbacksAndMessages(null);
            mainHandler.post(() -> resultCallback = null);
        }
    }

}
