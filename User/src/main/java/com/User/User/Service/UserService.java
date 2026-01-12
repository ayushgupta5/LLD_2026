package com.User.User.Service;

import com.User.User.Repository.UserRepository;
import com.User.User.domain.User;
import com.User.User.dto.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService implements IUser {
    @Autowired
    private UserRepository userRepository;
    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserDto> userDtos = new ArrayList<>();
        for(User user : users){
            UserDto userDto = new UserDto();
            userDto.setUserName(user.getUserName());
            userDto.setEmail(user.getEmail());
            userDto.setPassword(user.getPassword());
            userDto.setPhoneNumber(user.getPhoneNumber());
            userDtos.add(userDto);
        }
        return userDtos;
    }

    @Override
    public UserDto getUserById(int userId) {
        User user = userRepository.findById(userId).orElse(null);
        if(user == null){
            return null;
        }
        UserDto userDto = new UserDto();
        userDto.setUserName(user.getUserName());
        userDto.setEmail(user.getEmail());
        userDto.setPassword(user.getPassword());
        userDto.setPhoneNumber(user.getPhoneNumber());
        return userDto;
    }

    @Override
    public UserDto createUser(UserDto user) {
        User newUser = new User();
        newUser.setUserName(user.getUserName());
        newUser.setEmail(user.getEmail());
        newUser.setPassword(user.getPassword());
        newUser.setPhoneNumber(user.getPhoneNumber());
        userRepository.save(newUser);
        return user;
    }

    @Override
    public UserDto updateUser(int userId, UserDto user) {
        User existingUser = userRepository.findById(userId).orElse(null);
        if(existingUser == null){
            return null;
        }
        existingUser.setUserName(user.getUserName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPassword(user.getPassword());
        existingUser.setPhoneNumber(user.getPhoneNumber());
        userRepository.save(existingUser);
        return user;
    }

    @Override
    public void deleteUser(int userId) {
        userRepository.deleteById(userId);
    }
}
