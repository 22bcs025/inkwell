package com.inkwell.service;

import com.inkwell.dto.UpdateUserRequest;
import com.inkwell.dto.UserResponse;
import com.inkwell.entity.User;
import com.inkwell.exception.DuplicateResourceException;
import com.inkwell.exception.ForbiddenOperationException;
import com.inkwell.exception.ResourceNotFoundException;
import com.inkwell.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse getById(Long id) {
        User user = findUserOrThrow(id);
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request, User currentUser) {
        User user = findUserOrThrow(id);

        if (!user.getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("You can only update your own account");
        }

        if (request.username() != null && !request.username().isBlank()
                && !request.username().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.username())) {
                throw new DuplicateResourceException("Username already taken: " + request.username());
            }
            user.setUsername(request.username());
        }

        if (request.email() != null && !request.email().isBlank()
                && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new DuplicateResourceException("Email already registered: " + request.email());
            }
            user.setEmail(request.email());
        }

        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    @Transactional
    public void delete(Long id, User currentUser) {
        User user = findUserOrThrow(id);

        if (!user.getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("You can only delete your own account");
        }

        userRepository.delete(user);
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}
