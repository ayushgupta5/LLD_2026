package com.User.User.Service;

import com.User.User.dto.UserDto;

import java.util.List;

public interface IUser {
    public List<UserDto> getAllUsers();
    public UserDto getUserById(int userId);
    public UserDto createUser(UserDto user);
    public UserDto updateUser(int userId, UserDto user);
    public void deleteUser(int userId);
}
