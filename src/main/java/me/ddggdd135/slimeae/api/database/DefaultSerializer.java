package me.ddggdd135.slimeae.api.database;

import me.ddggdd135.slimeae.utils.ReflectionUtils;
import me.ddggdd135.slimeae.utils.SerializeUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class DefaultSerializer<T> implements Serializer<T>{
    private final Class<T> clazz;
    public DefaultSerializer(Class<T> clazz) {
        this.clazz = clazz;
    }
    @Override
    public T deserialize(Map<String, String> data) {
        try {
            Map<String, Object> fields = new HashMap<>();

            for (Map.Entry<String, String> entry : data.entrySet()) {
                String base64 = entry.getValue();
                if (base64.isEmpty()) {
                    fields.put(entry.getKey(), null);
                }
                fields.put(entry.getKey(), SerializeUtils.string2Object(base64));
            }

            T object = clazz.newInstance();

            Field[] fields1 = ReflectionUtils.getAllFields(object);

            for (Field field : fields1) {
                String name = field.getName();
                if (!fields.containsKey(name)) continue;

                field.set(object, fields.get(name));
            }

            return object;
        }
        catch(Exception e) {
            return null;
        }
    }

    @Override
    public Map<String, String> serialize(T object) {
        Map<String, String> data = new HashMap<>();
        try {
            Field[] fields = ReflectionUtils.getAllFields(object);

            for (Field field : fields) {
                Object value = field.get(object);
                String base64 = "";

                if (value != null) {
                    base64 = SerializeUtils.object2String(value);
                }

                data.put(field.getName(), base64);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return data;
    }
}
