package repositories;

import models.User;

import java.util.List;

public interface IUserRepository {
    User save(User user);
    User findById(String userID);
    List<User> findAllUser();
    User findByEmail(String userEmail);
    User updateUser(String userID, User user);
}
