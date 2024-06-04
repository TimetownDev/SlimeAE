package me.ddggdd135.slimeae.utils;

import java.util.Objects;

public class KeyValuePair<TKey, TValue> {
    private TKey key;
    private TValue value;

    public KeyValuePair(TKey key, TValue value) {
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
        KeyValuePair<?, ?> keyValuePair = (KeyValuePair<?, ?>) o;
        return Objects.equals(key, keyValuePair.key) && Objects.equals(value, keyValuePair.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }
}
