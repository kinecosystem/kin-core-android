package kin.sdk.core;

public interface ResultCallback<T> {

    /**
     * Method will be called when operation has completed successfully
     *
     * @param result the result received
     */
    void onResult(T result);

    /**
     * Method will be called when operation has completed with error
     *
     * @param e the exception in case of error
     */
    void onError(Exception e);
}