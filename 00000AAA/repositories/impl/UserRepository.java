package repositories.impl;

import exceptions.UserRepositoryException;
import models.User;
import repositories.IUserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRepository implements IUserRepository {
    public Map<String, User> userMap;
    public UserRepository() {
        this.userMap = new HashMap<>();
    }
    @Override
    public User save(User user) {
        return userMap.put(user.getUserID(), user);
    }
    @Override
    public User findById(String userID) {
        if(userMap.containsKey(userID)) {
            return userMap.get(userID);
        }
        throw new UserRepositoryException("UserID Not Found");
    }
    @Override
    public List<User> findAllUser() {
        return userMap.values().stream().toList();
    }
    @Override
    public User findByEmail(String userEmail) {
        for(User user: userMap.values()) {
            if(user.getUserEmail().equalsIgnoreCase(userEmail)) {
                return user;
            }
        }
        return null;
    }
    @Override
    public User updateUser(String userID, User user) {
        User currentUser = this.findById(userID);
        if(currentUser != null) {
            userMap.put(userID, user);
            return user;
        }
        return null;
    }
}
