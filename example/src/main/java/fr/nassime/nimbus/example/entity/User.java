package fr.nassime.nimbus.example.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    // A simple User entity with fields for id, username, password, and email.

    private String id;
    private String username;
    private String password;
    private String email;

}
