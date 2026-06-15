package com.inkwell.service;

import com.inkwell.dto.AuthResponse;
import com.inkwell.dto.LoginRequest;
import com.inkwell.dto.RegisterRequest;
import com.inkwell.entity.User;
import com.inkwell.exception.DuplicateResourceException;
import com.inkwell.repository.UserRepository;
import com.inkwell.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock AuthenticationManager authenticationManager;
    @InjectMocks AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("alice", "alice@example.com", "hashed");
        user.setId(1L);
    }

    @Test
    void register_success() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password1")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(1L, "alice")).thenReturn("tok");

        AuthResponse resp = authService.register(new RegisterRequest("alice", "alice@example.com", "password1"));

        assertThat(resp.token()).isEqualTo("tok");
        assertThat(resp.username()).isEqualTo("alice");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateUsername_throws() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);
        assertThatThrownBy(() -> authService.register(new RegisterRequest("alice", "alice@example.com", "password1")))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void register_duplicateEmail_throws() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);
        assertThatThrownBy(() -> authService.register(new RegisterRequest("alice", "alice@example.com", "password1")))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void login_success() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(1L, "alice")).thenReturn("tok");

        AuthResponse resp = authService.login(new LoginRequest("alice", "password1"));

        assertThat(resp.token()).isEqualTo("tok");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_badCredentials_throws() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad"));
        assertThatThrownBy(() -> authService.login(new LoginRequest("alice", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }
}
