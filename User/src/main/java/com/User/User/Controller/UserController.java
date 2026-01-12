package com.User.User.Controller;

import com.User.User.Service.IUser;
import com.User.User.domain.User;
import com.User.User.dto.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
/getAllUsers GET
/getUserById/{userId} GET
/createUser POST
/updateUser/{userId} PUT
/deleteUser/{userId} DELETE
*/
@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private IUser iUserService;
    @GetMapping(path = "/getAllUsers")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = iUserService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping(path = "/getUserById/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable int userId) {
        UserDto user = iUserService.getUserById(userId);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping(path = "/createUser")
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto user) {
        UserDto createdUser = iUserService.createUser(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PutMapping(path = "/updateUser/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable int userId, @RequestBody UserDto user) {
        UserDto updatedUser = iUserService.updateUser(userId, user);
        if (updatedUser == null) {
            return new ResponseEntity<>("User Not Found with UserID: " + userId , HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @DeleteMapping(path = "/deleteUser/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable int userId) {
        iUserService.deleteUser(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
