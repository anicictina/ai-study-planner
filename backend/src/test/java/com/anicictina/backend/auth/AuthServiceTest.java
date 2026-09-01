package com.anicictina.backend.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.anicictina.backend.auth.dto.LoginRequest;
import com.anicictina.backend.auth.dto.RegisterRequest;
import com.anicictina.backend.common.exception.EmailAlreadyExistsException;
import com.anicictina.backend.common.exception.InvalidCredentialsException;
import com.anicictina.backend.security.JwtService;
import com.anicictina.backend.user.Role;
import com.anicictina.backend.user.User;
import com.anicictina.backend.user.UserRepository;
import jakarta.validation.ValidationException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Tina");
        request.setLastName("Anicic");
        request.setEmail("Tina@Example.com");
        request.setPassword("Test1234!");
        return request;
    }

    @Test
    void registerThrowsWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("tina@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(registerRequest()));
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerNormalizesEmailAndCreatesUnverifiedUser() {
        when(userRepository.existsByEmail("tina@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Test1234!")).thenReturn("hashed-password");

        authService.register(registerRequest());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertEquals("tina@example.com", saved.getEmail());
        assertFalse(saved.isEmailVerified());
        assertEquals(Role.STUDENT, saved.getRole());
    }

    @Test
    void registerSendsVerificationEmailWithGeneratedToken() {
        when(userRepository.existsByEmail("tina@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-password");

        authService.register(registerRequest());

        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendVerificationEmail(org.mockito.ArgumentMatchers.eq("tina@example.com"), tokenCaptor.capture());
        assertFalse(tokenCaptor.getValue().isBlank());
    }

    @Test
    void loginThrowsWhenUserNotFound() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest();
        request.setEmail("nobody@example.com");
        request.setPassword("whatever");

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void loginThrowsWhenPasswordDoesNotMatch() {
        User user = User.builder().email("tina@example.com").passwordHash("hashed").emailVerified(true).build();
        when(userRepository.findByEmail("tina@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        LoginRequest request = new LoginRequest();
        request.setEmail("tina@example.com");
        request.setPassword("wrong");

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void loginThrowsWhenEmailNotVerified() {
        User user = User.builder().email("tina@example.com").passwordHash("hashed").emailVerified(false).build();
        when(userRepository.findByEmail("tina@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct", "hashed")).thenReturn(true);

        LoginRequest request = new LoginRequest();
        request.setEmail("tina@example.com");
        request.setPassword("correct");

        assertThrows(ValidationException.class, () -> authService.login(request));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void loginSucceedsAndReturnsTokenWhenVerifiedAndPasswordMatches() {
        User user = User.builder()
            .id(1L)
            .email("tina@example.com")
            .passwordHash("hashed")
            .role(Role.STUDENT)
            .emailVerified(true)
            .build();
        when(userRepository.findByEmail("tina@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct", "hashed")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("signed.jwt.token");

        LoginRequest request = new LoginRequest();
        request.setEmail("tina@example.com");
        request.setPassword("correct");

        var response = authService.login(request);

        assertEquals("signed.jwt.token", response.token());
    }

    @Test
    void verifyEmailThrowsWhenTokenNotFound() {
        when(userRepository.findByVerificationToken("bad-token")).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> authService.verifyEmail("bad-token"));
    }

    @Test
    void verifyEmailThrowsWhenTokenExpired() {
        User user = User.builder()
            .email("tina@example.com")
            .verificationToken("expired-token")
            .verificationTokenExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
            .emailVerified(false)
            .build();
        when(userRepository.findByVerificationToken("expired-token")).thenReturn(Optional.of(user));

        assertThrows(ValidationException.class, () -> authService.verifyEmail("expired-token"));
        assertFalse(user.isEmailVerified());
    }

    @Test
    void verifyEmailMarksUserVerifiedAndClearsTokenWhenValid() {
        User user = User.builder()
            .email("tina@example.com")
            .verificationToken("valid-token")
            .verificationTokenExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
            .emailVerified(false)
            .build();
        when(userRepository.findByVerificationToken("valid-token")).thenReturn(Optional.of(user));

        authService.verifyEmail("valid-token");

        assertTrue(user.isEmailVerified());
        assertNull(user.getVerificationToken());
    }
}
