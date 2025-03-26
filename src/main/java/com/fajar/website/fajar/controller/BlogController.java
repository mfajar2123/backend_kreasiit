package com.fajar.website.fajar.controller;

import com.fajar.website.fajar.dto.BlogRequest;
import com.fajar.website.fajar.dto.BlogResponse;
import com.fajar.website.fajar.mapper.BlogMapper;
import com.fajar.website.fajar.model.Admin;
import com.fajar.website.fajar.model.Blog;
import com.fajar.website.fajar.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;
    private final BlogMapper blogMapper;

    @GetMapping
    public ResponseEntity<List<BlogResponse>> getAllBlogs() {
        List<BlogResponse> blogs = blogService.getAllBlogs().stream()
                .map(blogMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(blogs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlogResponse> getBlogById(@PathVariable Long id) {
        Blog blog = blogService.getBlogById(id);
        return ResponseEntity.ok(blogMapper.toResponse(blog));
    }

    @PostMapping
    public ResponseEntity<BlogResponse> createBlog(
            @RequestBody BlogRequest blogRequest,
            @AuthenticationPrincipal Admin admin) {
        Blog blog = blogService.createBlog(blogRequest, admin);
        return ResponseEntity.status(HttpStatus.CREATED).body(blogMapper.toResponse(blog));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BlogResponse> updateBlog(
            @PathVariable Long id,
            @RequestBody BlogRequest blogRequest,
            @AuthenticationPrincipal Admin admin) {
        Blog updatedBlog = blogService.updateBlog(id, blogRequest, admin);
        return ResponseEntity.ok(blogMapper.toResponse(updatedBlog));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBlog(
            @PathVariable Long id,
            @AuthenticationPrincipal Admin admin) {
        blogService.deleteBlog(id, admin);
        return ResponseEntity.noContent().build();
    }
}