package com.anicictina.backend.auth;

import com.anicictina.backend.auth.dto.AuthResponse;
import com.anicictina.backend.auth.dto.LoginRequest;
import com.anicictina.backend.auth.dto.RegisterRequest;
import com.anicictina.backend.common.exception.EmailAlreadyExistsException;
import com.anicictina.backend.common.exception.InvalidCredentialsException;
import com.anicictina.backend.security.JwtService;
import com.anicictina.backend.user.Role;
import com.anicictina.backend.user.User;
import com.anicictina.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        User user = User.builder()
            .firstName(request.getFirstName().trim())
            .lastName(request.getLastName().trim())
            .email(normalizedEmail)
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .role(Role.STUDENT)
            .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return AuthResponse.from(user, token);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user);
        return AuthResponse.from(user, token);
    }
}
