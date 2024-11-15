package com.iamkaf.bonded.util;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class LiteralMapHolder<K, V> {
    public final Map<K, V> map = new HashMap<>();

    public @Nullable V get(K key) {
        return map.get(key);
    }

    public void add(K id, V typeLeveler) {
        map.put(id, typeLeveler);
    }

    public void remove(K id) {
        map.remove(id);
    }

    public void clear() {
        map.clear();
    }

    public Map<K, V> getMap() {
        return map;
    }
}