package com.example.store.service.impl;

import com.example.store.entity.User;
import com.example.store.repository.UserRepository;
import com.example.store.service.UserService;

import java.util.Optional;

public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> findById(Integer userId) {
        return userRepository.findById(userId);
    }
}
