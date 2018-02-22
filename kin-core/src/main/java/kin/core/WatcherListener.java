package kin.core;


public interface WatcherListener<T> {

    void onEvent(T data);
}
