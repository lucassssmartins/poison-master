package br.com.poison.core.manager.base;

import lombok.Getter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Getter
public abstract class Manager<K, V> {

    private final Map<K, V> cacheMap;

    public Manager() {
        this.cacheMap = new HashMap<>();
    }

    public abstract void save(V value);

    public V read(K key) {
        return cacheMap.get(key);
    }

    public void remove(K key) {
        cacheMap.remove(key);
    }

    public void clearAll() {
        cacheMap.clear();
    }

    public boolean exists(K key) {
        return cacheMap.containsKey(key);
    }

    public int size() {
        return cacheMap.size();
    }

    public Collection<V> documents() {
        return cacheMap.values();
    }

    public Stream<V> documents(Predicate<V> filter) {
        return documents().stream().filter(filter);
    }
}
