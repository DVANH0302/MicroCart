package com.example.store.service;


import com.example.store.entity.User;

import java.util.Optional;

public interface UserService {
    Optional<User> findById(Integer id);
}
