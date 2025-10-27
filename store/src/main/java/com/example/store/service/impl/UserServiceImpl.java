package com.example.store.service.impl;

import com.example.store.entity.User;
import com.example.store.repository.UserRepository;
import com.example.store.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<User> findById(Integer userId) {
        return userRepository.findById(userId);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User registerUser(String username,
                             String rawPassword,
                             String email,
                             String firstName,
                             String lastName,
                             String bankAccountId) {
        userRepository.findByUsername(username).ifPresent(existing -> {
            throw new IllegalArgumentException("Username already exists: " + username);
        });

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setBankAccountId(bankAccountId);
        return userRepository.save(user);
    }
}
