package com.fajar.website.fajar.service;

import com.fajar.website.fajar.dto.AuthRequest;
import com.fajar.website.fajar.dto.AuthResponse;
import com.fajar.website.fajar.model.Admin;
import com.fajar.website.fajar.repository.AdminRepository;
import com.fajar.website.fajar.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private Admin admin;
    private AuthRequest authRequest;

    @BeforeEach
    void setUp() {
        admin = new Admin();
        admin.setId(1L);
        admin.setUsername("testuser");
        admin.setPassword("encodedPassword");

        authRequest = new AuthRequest("testuser", "password123");
    }

    @Test
    void loginSuccess() {
        // Arrange
        when(adminRepository.findByUsername("testuser")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken("testuser")).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.login(authRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.token());
        verify(adminRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtTokenProvider).generateToken("testuser");
    }

    @Test
    void loginFailUsernameNotFound() {
        // Arrange
        when(adminRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(authRequest);
        });
        assertEquals("Username tidak ditemukan", exception.getMessage());
        verify(adminRepository).findByUsername("testuser");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtTokenProvider, never()).generateToken(anyString());
    }

    @Test
    void loginFailWrongPassword() {
        // Arrange
        when(adminRepository.findByUsername("testuser")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(authRequest);
        });
        assertEquals("Password salah", exception.getMessage());
        verify(adminRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtTokenProvider, never()).generateToken(anyString());
    }

    @Test
    void registerSuccess() {
        // Arrange
        Admin newAdmin = new Admin();
        newAdmin.setUsername("newuser");
        newAdmin.setPassword("password123");

        Admin savedAdmin = new Admin();
        savedAdmin.setId(2L);
        savedAdmin.setUsername("newuser");
        savedAdmin.setPassword("encodedPassword");

        when(adminRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(adminRepository.save(any(Admin.class))).thenReturn(savedAdmin);

        // Act
        Admin result = authService.register(newAdmin);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("newuser", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        verify(adminRepository).findByUsername("newuser");
        verify(passwordEncoder).encode("password123");
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    void registerFailUsernameExists() {
        // Arrange
        Admin newAdmin = new Admin();
        newAdmin.setUsername("testuser");
        newAdmin.setPassword("password123");

        when(adminRepository.findByUsername("testuser")).thenReturn(Optional.of(admin));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(newAdmin);
        });
        assertEquals("Username sudah digunakan", exception.getMessage());
        verify(adminRepository).findByUsername("testuser");
        verify(passwordEncoder, never()).encode(anyString());
        verify(adminRepository, never()).save(any(Admin.class));
    }
}