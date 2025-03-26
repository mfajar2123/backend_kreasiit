package com.fajar.website.fajar.dto;

import java.time.LocalDateTime;

public record BlogResponse(
        Long id,
        String title,
        String content,
        String authorUsername,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}