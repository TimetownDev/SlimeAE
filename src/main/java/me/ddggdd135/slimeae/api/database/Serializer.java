package me.ddggdd135.slimeae.api.database;

import java.util.Map;

public interface Serializer<T> {
    T deserialize(Map<String, String> data);

    Map<String, String> serialize(T object);
}
