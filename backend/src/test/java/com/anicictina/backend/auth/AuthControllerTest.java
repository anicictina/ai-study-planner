package com.anicictina.backend.auth;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.anicictina.backend.auth.dto.AuthResponse;
import com.anicictina.backend.auth.dto.LoginRequest;
import com.anicictina.backend.auth.dto.RegisterRequest;
import com.anicictina.backend.auth.dto.RegisterResponse;
import com.anicictina.backend.common.exception.InvalidCredentialsException;
import com.anicictina.backend.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    // JwtAuthenticationFilter is a @Component (implements Filter), so @WebMvcTest still
    // instantiates it even with addFilters=false - it needs a JwtService bean to satisfy DI.
    @MockitoBean
    private JwtService jwtService;

    private RegisterRequest validRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Tina");
        request.setLastName("Anicic");
        request.setEmail("tina@example.com");
        request.setPassword("Test1234!");
        return request;
    }

    @Test
    void registerReturns201WithRegisterResponseBody() throws Exception {
        when(authService.register(any())).thenReturn(
            new RegisterResponse("tina@example.com", "Poslali smo ti mejl za potvrdu naloga."));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email", is("tina@example.com")));
    }

    @Test
    void registerReturns400WhenPasswordTooShort() throws Exception {
        RegisterRequest request = validRegisterRequest();
        request.setPassword("short");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void registerReturns400WhenEmailIsMissing() throws Exception {
        RegisterRequest request = validRegisterRequest();
        request.setEmail("");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void loginReturns401WhenCredentialsAreInvalid() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("tina@example.com");
        request.setPassword("wrong");

        when(authService.login(any())).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message", is("Invalid email or password")));
    }

    @Test
    void loginReturns200WithTokenWhenCredentialsAreValid() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("tina@example.com");
        request.setPassword("correct");

        when(authService.login(any())).thenReturn(
            AuthResponse.builder()
                .token("signed.jwt.token")
                .id(1L)
                .firstName("Tina")
                .lastName("Anicic")
                .email("tina@example.com")
                .role("STUDENT")
                .build());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token", is("signed.jwt.token")));
    }

    @Test
    void verifyEmailReturns400WhenTokenIsInvalid() throws Exception {
        org.mockito.Mockito.doThrow(new jakarta.validation.ValidationException("Link za potvrdu nije validan."))
            .when(authService).verifyEmail("bad-token");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/api/auth/verify-email").param("token", "bad-token"))
            .andExpect(status().isBadRequest());
    }
}
