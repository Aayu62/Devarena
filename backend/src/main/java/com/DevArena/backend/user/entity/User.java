package com.DevArena.backend.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;
    
    @Column(unique = true)
    private String email;

    private String password;

    private String college;

    private String github;

    private String linkedin;

    private Integer xp = 0;

    private String rank = "Bronze";

    @Column(nullable = false)
    private Integer rating = 1000;

    @Column(nullable = false)
    private String role = "USER";
}
