package org.mika1212.common.json;

public interface JsonSerializer {
    String toJson(Object object);

    <T> T toObject(String json, Class<T> clazz);
}
