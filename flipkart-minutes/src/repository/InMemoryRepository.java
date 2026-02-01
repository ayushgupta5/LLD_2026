package src.repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class InMemoryRepository<T> implements IRepository<T> {

    protected final Map<String, T> storage = new ConcurrentHashMap<>();

    @Override
    public T save(T entity) {
        String id = getEntityId(entity);
        storage.put(id, entity);
        return entity;
    }

    @Override
    public Optional<T> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void delete(String id) {
        storage.remove(id);
    }

    @Override
    public boolean exists(String id) {
        return storage.containsKey(id);
    }

    protected abstract String getEntityId(T entity);
}