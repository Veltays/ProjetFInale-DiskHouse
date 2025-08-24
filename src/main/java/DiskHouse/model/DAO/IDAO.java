package DiskHouse.model.DAO;

import java.util.List;

public interface IDAO<T> {

    // Create
    void add(T element);

    // Read
    T getById(String id);
    T getByName(String name);
    List<T> getAll();

    // Update
    void update(T element);

    // Delete
    void delete(String id);
}
