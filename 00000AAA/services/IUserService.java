package services;

import models.User;

public interface IUserService {
    User signUp(String name, String email, String profession);
    User login(String email, String password);
    void logOut();
    User getCurrentUser();
}
