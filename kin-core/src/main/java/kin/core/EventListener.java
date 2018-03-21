package kin.core;


public interface EventListener<T> {

    void onEvent(T data);
}
