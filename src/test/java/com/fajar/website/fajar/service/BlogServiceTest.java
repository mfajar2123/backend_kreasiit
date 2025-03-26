package com.fajar.website.fajar.service;

import com.fajar.website.fajar.dto.BlogRequest;
import com.fajar.website.fajar.exception.ResourceNotFoundException;
import com.fajar.website.fajar.exception.UnauthorizedException;
import com.fajar.website.fajar.exception.ValidationException;
import com.fajar.website.fajar.model.Admin;
import com.fajar.website.fajar.model.Blog;
import com.fajar.website.fajar.repository.BlogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BlogServiceTest {

    @Mock
    private BlogRepository blogRepository;

    @InjectMocks
    private BlogService blogService;

    @Captor
    private ArgumentCaptor<Blog> blogCaptor;

    private Admin author;
    private Admin anotherAuthor;
    private Blog blog;
    private BlogRequest validBlogRequest;

    @BeforeEach
    void setUp() {
        author = new Admin();
        author.setId(1L);
        author.setUsername("author");

        anotherAuthor = new Admin();
        anotherAuthor.setId(2L);
        anotherAuthor.setUsername("anotherAuthor");

        blog = new Blog();
        blog.setId(1L);
        blog.setTitle("Test Blog");
        blog.setContent("Test Content");
        blog.setAuthor(author);
        blog.setCreatedAt(LocalDateTime.now());

        validBlogRequest = new BlogRequest("Test Title", "Test Content");
    }

    @Test
    void getAllBlogsSuccess() {
        // Arrange
        Blog blog2 = new Blog();
        blog2.setId(2L);
        blog2.setTitle("Second Blog");
        List<Blog> blogs = Arrays.asList(blog, blog2);

        when(blogRepository.findAllByOrderByCreatedAtDesc()).thenReturn(blogs);

        // Act
        List<Blog> result = blogService.getAllBlogs();

        // Assert
        assertEquals(2, result.size());
        verify(blogRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void getBlogByIdSuccess() {
        // Arrange
        when(blogRepository.findById(1L)).thenReturn(Optional.of(blog));

        // Act
        Blog result = blogService.getBlogById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Blog", result.getTitle());
        verify(blogRepository).findById(1L);
    }

    @Test
    void getBlogByIdNotFound() {
        // Arrange
        when(blogRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            blogService.getBlogById(99L);
        });
        assertEquals("Blog dengan ID 99 tidak ditemukan", exception.getMessage());
        verify(blogRepository).findById(99L);
    }

    @Test
    void createBlogSuccess() {
        // Arrange
        Blog savedBlog = new Blog();
        savedBlog.setId(3L);
        savedBlog.setTitle("Test Title");
        savedBlog.setContent("Test Content");
        savedBlog.setAuthor(author);

        when(blogRepository.save(any(Blog.class))).thenReturn(savedBlog);

        // Act
        Blog result = blogService.createBlog(validBlogRequest, author);

        // Assert
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("Test Title", result.getTitle());
        assertEquals("Test Content", result.getContent());
        assertEquals(author, result.getAuthor());

        verify(blogRepository).save(blogCaptor.capture());
        Blog capturedBlog = blogCaptor.getValue();
        assertEquals("Test Title", capturedBlog.getTitle());
        assertEquals("Test Content", capturedBlog.getContent());
        assertEquals(author, capturedBlog.getAuthor());
        assertNotNull(capturedBlog.getCreatedAt());
    }

    @Test
    void createBlogInvalidRequest() {
        // Arrange
        BlogRequest invalidRequest = new BlogRequest("", "");

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            blogService.createBlog(invalidRequest, author);
        });
        assertEquals("Judul dan konten harus diisi!", exception.getMessage());
        verify(blogRepository, never()).save(any(Blog.class));
    }

    @Test
    void updateBlogSuccess() {
        // Arrange
        when(blogRepository.findById(1L)).thenReturn(Optional.of(blog));
        BlogRequest updateRequest = new BlogRequest("Updated Title", "Updated Content");

        Blog updatedBlog = new Blog();
        updatedBlog.setId(1L);
        updatedBlog.setTitle("Updated Title");
        updatedBlog.setContent("Updated Content");
        updatedBlog.setAuthor(author);

        when(blogRepository.save(any(Blog.class))).thenReturn(updatedBlog);

        // Act
        Blog result = blogService.updateBlog(1L, updateRequest, author);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Content", result.getContent());

        verify(blogRepository).findById(1L);
        verify(blogRepository).save(blogCaptor.capture());
        Blog capturedBlog = blogCaptor.getValue();
        assertEquals("Updated Title", capturedBlog.getTitle());
        assertEquals("Updated Content", capturedBlog.getContent());
        assertNotNull(capturedBlog.getUpdatedAt());
    }

    @Test
    void updateBlogUnauthorized() {
        // Arrange
        when(blogRepository.findById(1L)).thenReturn(Optional.of(blog));

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            blogService.updateBlog(1L, validBlogRequest, anotherAuthor);
        });
        assertEquals("Anda tidak memiliki izin untuk memodifikasi blog ini", exception.getMessage());
        verify(blogRepository).findById(1L);
        verify(blogRepository, never()).save(any(Blog.class));
    }

    @Test
    void deleteBlogSuccess() {
        // Arrange
        when(blogRepository.findById(1L)).thenReturn(Optional.of(blog));

        // Act
        blogService.deleteBlog(1L, author);

        // Assert
        verify(blogRepository).findById(1L);
        verify(blogRepository).delete(blog);
    }

    @Test
    void deleteBlogUnauthorized() {
        // Arrange
        when(blogRepository.findById(1L)).thenReturn(Optional.of(blog));

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            blogService.deleteBlog(1L, anotherAuthor);
        });
        assertEquals("Anda tidak memiliki izin untuk memodifikasi blog ini", exception.getMessage());
        verify(blogRepository).findById(1L);
        verify(blogRepository, never()).delete(any(Blog.class));
    }
}