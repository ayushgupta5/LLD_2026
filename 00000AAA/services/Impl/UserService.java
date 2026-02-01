package services.Impl;

import exceptions.UserServiceException;
import models.User;
import repositories.IUserRepository;
import services.IUserService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserService implements IUserService {
    private Map<String, User> loggedInUsers;
    private final IUserRepository userRepository;
    UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
        loggedInUsers = new HashMap<>();
    }
    @Override
    public User signUp(String name, String email, String profession) {
        String userID = UUID.randomUUID().toString();
        User user = new User(userID, name, email, "111", "aaa", "Ayush", "Ayush", LocalDateTime.now(), LocalDateTime.now());
        return userRepository.save(user);
    }
    @Override
    public User login(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UserServiceException("UserEmail Not Found");
        }
        if (!user.getPasswordHash().equalsIgnoreCase(password)) {
            throw new UserServiceException("Password Not Matched");
        }
        loggedInUsers.put(user.getUserID(), user);
        return user;
    }
    @Override
    public void logOut() {
        loggedInUsers.clear();
        System.out.println("User Logged Out");
    }
    @Override
    public User getCurrentUser() {
        return (User) loggedInUsers.values().stream().toArray()[0];
    }
}

