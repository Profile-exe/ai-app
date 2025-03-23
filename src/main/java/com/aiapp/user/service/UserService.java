package com.aiapp.user.service;

import com.aiapp.user.entity.User;
import com.aiapp.user.exception.AlreadyExistEmailException;
import com.aiapp.user.exception.InvalidPasswordException;
import com.aiapp.user.exception.UserNotFoundException;
import com.aiapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(String email, String password, String name) {

        if (userRepository.existsByEmail(email)) {
            throw AlreadyExistEmailException.EXCEPTION;
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .name(name)
                .build();

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User findByEmailAndPassword(String email, String password) {
        User user = getUserByEmailOrThrow(email);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw InvalidPasswordException.EXCEPTION;
        }

        return user;
    }

    private User getUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> UserNotFoundException.EXCEPTION);
    }

    @Transactional(readOnly = true)
    public User getUserByIdOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.EXCEPTION);
    }
}
