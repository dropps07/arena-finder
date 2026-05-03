package com.arenafinder.auth.service;

import com.arenafinder.auth.dto.AuthDTOs.*;
import com.arenafinder.auth.exception.AuthException;
import com.arenafinder.auth.model.User;
import com.arenafinder.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 *
 * UNIT TEST PHILOSOPHY:
 * ─────────────────────
 * A unit test tests ONE class in isolation.
 * All dependencies (UserRepository, PasswordEncoder, JwtUtil) are MOCKED.
 * A mock is a fake object that returns whatever you tell it to.
 *
 * This means:
 * - No database needed
 * - No Spring context started (fast — runs in milliseconds)
 * - Tests are deterministic (no flakiness from network/DB state)
 *
 * @ExtendWith(MockitoExtension.class) — activates Mockito annotations
 * @Mock — creates a mock of the interface/class
 * @InjectMocks — creates the real AuthService and injects the mocks into it
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Arrange shared test data — built once, used in each test
        testUser = User.builder()
                .id(1L)
                .email("player@arena.com")
                .password("$2a$10$hashedpassword")
                .name("Test Player")
                .role(User.Role.PLAYER)
                .build();

        loginRequest = new LoginRequest();
        loginRequest.setEmail("player@arena.com");
        loginRequest.setPassword("password123");
    }

    // ── Login tests ───────────────────────────────────────────────

    @Test
    @DisplayName("Login succeeds with valid credentials")
    void login_validCredentials_returnsToken() {
        // ARRANGE — tell mocks what to return
        when(userRepository.findByEmail("player@arena.com"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword()))
                .thenReturn(true);
        when(jwtUtil.generateToken(testUser))
                .thenReturn("mock.jwt.token");
        when(jwtUtil.getExpirationMs())
                .thenReturn(86400000L);

        // ACT — call the method under test
        LoginResponse response = authService.login(loginRequest);

        // ASSERT — verify the result
        assertThat(response.getToken()).isEqualTo("mock.jwt.token");
        assertThat(response.getEmail()).isEqualTo("player@arena.com");
        assertThat(response.getRole()).isEqualTo("PLAYER");

        // Verify the repository was called exactly once with the right email
        verify(userRepository, times(1)).findByEmail("player@arena.com");
    }

    @Test
    @DisplayName("Login fails when email does not exist")
    void login_emailNotFound_throwsInvalidCredentials() {
        // ARRANGE — user not found
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        // ASSERT — exception is thrown (ACT happens inside assertThatThrownBy)
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(AuthException.InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        // Verify password check was NEVER called (short-circuit on missing user)
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    @DisplayName("Login fails when password is wrong")
    void login_wrongPassword_throwsInvalidCredentials() {
        // ARRANGE
        when(userRepository.findByEmail("player@arena.com"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(any(), any()))
                .thenReturn(false); // wrong password

        // ASSERT
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(AuthException.InvalidCredentialsException.class);

        // Token should never be generated on failed login
        verify(jwtUtil, never()).generateToken(any());
    }

    // ── Register tests ────────────────────────────────────────────

    @Test
    @DisplayName("Register fails when email already exists")
    void register_duplicateEmail_throwsEmailAlreadyExists() {
        // ARRANGE
        RegisterRequest request = new RegisterRequest();
        request.setEmail("player@arena.com");
        request.setPassword("password123");
        request.setName("Another User");

        when(userRepository.existsByEmail("player@arena.com"))
                .thenReturn(true); // email taken

        // ASSERT
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(AuthException.EmailAlreadyExistsException.class)
                .hasMessageContaining("player@arena.com");

        // User should never be saved
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Register succeeds with valid new user")
    void register_validRequest_returnsToken() {
        // ARRANGE
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@arena.com");
        request.setPassword("password123");
        request.setName("New User");

        User savedUser = User.builder()
                .id(2L)
                .email("newuser@arena.com")
                .password("$hashed")
                .name("New User")
                .role(User.Role.PLAYER)
                .build();

        when(userRepository.existsByEmail("newuser@arena.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$hashed");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken(savedUser)).thenReturn("new.jwt.token");
        when(jwtUtil.getExpirationMs()).thenReturn(86400000L);

        // ACT
        LoginResponse response = authService.register(request);

        // ASSERT
        assertThat(response.getToken()).isEqualTo("new.jwt.token");
        assertThat(response.getEmail()).isEqualTo("newuser@arena.com");

        // Verify password was hashed before saving
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }
}
