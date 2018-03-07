package io.baschel.ulserver.util;

public class Ref<T> {
    private T v;

    public Ref(T val) {
        v = val;
    }

    public T get() {
        return v;
    }

    public void set(T val) {
        v = val;
    }
}
