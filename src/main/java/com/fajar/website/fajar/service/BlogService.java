package com.fajar.website.fajar.service;

import com.fajar.website.fajar.dto.BlogRequest;
import com.fajar.website.fajar.exception.ResourceNotFoundException;
import com.fajar.website.fajar.exception.UnauthorizedException;
import com.fajar.website.fajar.exception.ValidationException;
import com.fajar.website.fajar.model.Admin;
import com.fajar.website.fajar.model.Blog;
import com.fajar.website.fajar.repository.BlogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BlogService {

    private final BlogRepository blogRepository;

    public List<Blog> getAllBlogs() {
        return blogRepository.findAllByOrderByCreatedAtDesc();
    }

    public Blog getBlogById(Long id) {
        return blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog dengan ID " + id + " tidak ditemukan"));
    }

    public Blog createBlog(BlogRequest blogRequest, Admin author) {
        validateBlogRequest(blogRequest);

        Blog blog = new Blog();
        blog.setTitle(blogRequest.title());
        blog.setContent(blogRequest.content());
        blog.setAuthor(author);
        blog.setCreatedAt(LocalDateTime.now());
        return blogRepository.save(blog);
    }

    public Blog updateBlog(Long id, BlogRequest blogRequest, Admin author) {
        validateBlogRequest(blogRequest);

        Blog blog = getBlogById(id);
        validateBlogAuthor(blog, author);

        blog.setTitle(blogRequest.title());
        blog.setContent(blogRequest.content());
        blog.setUpdatedAt(LocalDateTime.now());
        return blogRepository.save(blog);
    }

    public void deleteBlog(Long id, Admin author) {
        Blog blog = getBlogById(id);
        validateBlogAuthor(blog, author);
        blogRepository.delete(blog);
    }

    private void validateBlogRequest(BlogRequest blogRequest) {
        if (blogRequest.title() == null || blogRequest.title().isBlank() ||
                blogRequest.content() == null || blogRequest.content().isBlank()) {
            throw new ValidationException("Judul dan konten harus diisi!");
        }
    }

    private void validateBlogAuthor(Blog blog, Admin author) {
        if (!blog.getAuthor().getId().equals(author.getId())) {
            throw new UnauthorizedException("Anda tidak memiliki izin untuk memodifikasi blog ini");
        }
    }
}