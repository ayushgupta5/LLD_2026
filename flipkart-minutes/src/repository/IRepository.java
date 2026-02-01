package src.repository;

import java.util.List;
import java.util.Optional;


public interface IRepository<T> {
    T save(T entity);

    Optional<T> findById(String id);

    List<T> findAll();

    void delete(String id);

    boolean exists(String id);
}