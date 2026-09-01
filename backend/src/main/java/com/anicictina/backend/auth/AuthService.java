package com.anicictina.backend.auth;

import com.anicictina.backend.auth.dto.AuthResponse;
import com.anicictina.backend.auth.dto.LoginRequest;
import com.anicictina.backend.auth.dto.RegisterRequest;
import com.anicictina.backend.auth.dto.RegisterResponse;
import com.anicictina.backend.common.exception.EmailAlreadyExistsException;
import com.anicictina.backend.common.exception.InvalidCredentialsException;
import com.anicictina.backend.security.JwtService;
import com.anicictina.backend.user.Role;
import com.anicictina.backend.user.User;
import com.anicictina.backend.user.UserRepository;
import jakarta.validation.ValidationException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
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
    private final EmailService emailService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        String token = UUID.randomUUID().toString();

        User user = User.builder()
            .firstName(request.getFirstName().trim())
            .lastName(request.getLastName().trim())
            .email(normalizedEmail)
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .role(Role.STUDENT)
            .emailVerified(false)
            .verificationToken(token)
            .verificationTokenExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
            .build();

        userRepository.save(user);
        emailService.sendVerificationEmail(user.getEmail(), token);

        return new RegisterResponse(user.getEmail(), "Poslali smo ti mejl za potvrdu naloga.");
    }

    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
            .orElseThrow(() -> new ValidationException("Link za potvrdu nije validan."));

        if (user.getVerificationTokenExpiresAt() == null || user.getVerificationTokenExpiresAt().isBefore(Instant.now())) {
            throw new ValidationException("Link za potvrdu je istekao. Registruj se ponovo.");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiresAt(null);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        if (!user.isEmailVerified()) {
            throw new ValidationException("Molimo potvrdi svoj email pre prijave. Proveri inbox.");
        }

        String token = jwtService.generateToken(user);
        return AuthResponse.from(user, token);
    }
}
