package com.davidcubesvk.yamlUpdater.core.utils;

public class Primitive<T> {

    private T value;

    public Primitive(T value) {
        this.value = value;
    }

    public void set(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

}