package com.fajar.website.fajar.mapper;

import com.fajar.website.fajar.dto.BlogResponse;
import com.fajar.website.fajar.model.Blog;
import org.springframework.stereotype.Component;

@Component
public class BlogMapper {

    public BlogResponse toResponse(Blog blog) {
        return new BlogResponse(
                blog.getId(),
                blog.getTitle(),
                blog.getContent(),
                blog.getAuthor().getUsername(),
                blog.getCreatedAt(),
                blog.getUpdatedAt()
        );
    }
}