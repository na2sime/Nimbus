package fr.nassime.nimbus.example.controller;

import fr.nassime.nimbus.annotations.*;
import fr.nassime.nimbus.annotations.request.PathVariable;
import fr.nassime.nimbus.annotations.request.RequestBody;
import fr.nassime.nimbus.annotations.type.Delete;
import fr.nassime.nimbus.annotations.type.Get;
import fr.nassime.nimbus.annotations.type.Post;
import fr.nassime.nimbus.annotations.type.Put;
import fr.nassime.nimbus.http.ResponseEntity;
import fr.nassime.nimbus.example.entity.User;

@Controller(path = "/api/users")
public class UserController {

    @Get(path = "/{id}")
    public ResponseEntity<User> getUser(@PathVariable("id") String id) {
        // Create a method to find a user by ID
        User user = findUserById(id);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound();
        }
    }

    @Post
    public ResponseEntity<User> createUser(@RequestBody User user) {
        // Create a method to save a new user
        User createdUser = saveUser(user);
        return ResponseEntity.created(createdUser);
    }

    @Put(path = "/{id}")
    public ResponseEntity<User> updateUser(@PathVariable("id") String id, @RequestBody User user) {
        // Create a method to update an existing user
        user.setId(id);
        User updatedUser = updateUser(user);
        return ResponseEntity.ok(updatedUser);
    }

    @Delete(path = "/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") String id) {
        // Create a method to delete a user by ID
        deleteUserById(id);
        return new ResponseEntity<>(null, 204);
    }

    // Fake methods to simulate database operations
    private User findUserById(String id) {
        return null;
    }

    private User saveUser(User user) {
        return user;
    }

    private User updateUser(User user) {
        return user;
    }

    private void deleteUserById(String id) {
    }

}
