package com.fajar.website.fajar.service;

import com.fajar.website.fajar.dto.AuthRequest;
import com.fajar.website.fajar.dto.AuthResponse;
import com.fajar.website.fajar.model.Admin;
import com.fajar.website.fajar.repository.AdminRepository;
import com.fajar.website.fajar.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminRepository adminRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;  // Changed from BCryptPasswordEncoder to PasswordEncoder

    public AuthResponse login(AuthRequest authRequest) {
        Admin admin = adminRepository.findByUsername(authRequest.username())
                .orElseThrow(() -> new RuntimeException("Username tidak ditemukan"));

        if (!passwordEncoder.matches(authRequest.password(), admin.getPassword())) {
            throw new RuntimeException("Password salah");
        }

        String token = jwtTokenProvider.generateToken(admin.getUsername());
        return new AuthResponse(token);
    }

    public Admin register(Admin admin) {
        if (adminRepository.findByUsername(admin.getUsername()).isPresent()) {
            throw new RuntimeException("Username sudah digunakan");
        }
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        return adminRepository.save(admin);
    }
}