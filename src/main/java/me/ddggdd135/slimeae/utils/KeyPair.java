package me.ddggdd135.slimeae.utils;

import java.util.Objects;

public class KeyPair<TKey, TValue> {
    private TKey key;
    private TValue value;

    public KeyPair(TKey key, TValue value) {
        this.key = key;
        this.value = value;
    }

    public TKey getKey() {
        return key;
    }

    public void setKey(TKey key) {
        this.key = key;
    }

    public TValue getValue() {
        return value;
    }

    public void setValue(TValue value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeyPair<?, ?> keyPair = (KeyPair<?, ?>) o;
        return Objects.equals(key, keyPair.key) && Objects.equals(value, keyPair.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }
}
