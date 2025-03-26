package com.fajar.website.fajar.integration;

import com.fajar.website.fajar.dto.AuthRequest;
import com.fajar.website.fajar.dto.AuthResponse;
import com.fajar.website.fajar.dto.BlogRequest;
import com.fajar.website.fajar.model.Admin;
import com.fajar.website.fajar.model.Blog;
import com.fajar.website.fajar.repository.AdminRepository;
import com.fajar.website.fajar.repository.BlogRepository;
import com.fajar.website.fajar.service.AuthService;
import com.fajar.website.fajar.service.BlogService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthBlogIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private BlogService blogService;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private BlogRepository blogRepository;

    private Admin testAdmin;
    private Admin anotherAdmin;

    @BeforeEach
    void setUp() {
        // Create test admins
        testAdmin = new Admin();
        testAdmin.setUsername("testadmin");
        testAdmin.setPassword("password123");
        testAdmin = authService.register(testAdmin);

        anotherAdmin = new Admin();
        anotherAdmin.setUsername("anotheradmin");
        anotherAdmin.setPassword("password456");
        anotherAdmin = authService.register(anotherAdmin);
    }

    @AfterEach
    void tearDown() {
        blogRepository.deleteAll();
        adminRepository.deleteAll();
    }

    @Test
    void completeUserFlow_AuthenticationAndBlogOperations() {
        // 1. Login as testAdmin
        AuthRequest loginRequest = new AuthRequest("testadmin", "password123");
        AuthResponse authResponse = authService.login(loginRequest);
        assertNotNull(authResponse);
        assertNotNull(authResponse.token());

        // 2. Create a blog post
        BlogRequest createBlogRequest = new BlogRequest("First Blog", "This is my first blog post");
        Blog createdBlog = blogService.createBlog(createBlogRequest, testAdmin);

        assertNotNull(createdBlog);
        assertEquals("First Blog", createdBlog.getTitle());
        assertEquals("This is my first blog post", createdBlog.getContent());
        assertEquals(testAdmin.getId(), createdBlog.getAuthor().getId());
        assertNotNull(createdBlog.getCreatedAt());

        // 3. Get all blogs
        List<Blog> allBlogs = blogService.getAllBlogs();
        assertEquals(1, allBlogs.size());
        assertEquals(createdBlog.getId(), allBlogs.get(0).getId());

        // 4. Get blog by ID
        Blog retrievedBlog = blogService.getBlogById(createdBlog.getId());
        assertEquals(createdBlog.getId(), retrievedBlog.getId());
        assertEquals(createdBlog.getTitle(), retrievedBlog.getTitle());

        // 5. Update the blog
        BlogRequest updateBlogRequest = new BlogRequest("Updated Blog", "This blog has been updated");
        Blog updatedBlog = blogService.updateBlog(createdBlog.getId(), updateBlogRequest, testAdmin);

        assertEquals(createdBlog.getId(), updatedBlog.getId());
        assertEquals("Updated Blog", updatedBlog.getTitle());
        assertEquals("This blog has been updated", updatedBlog.getContent());
        assertNotNull(updatedBlog.getUpdatedAt());

        // 6. Try to update blog with different user (should fail with UnauthorizedException)
        BlogRequest unauthorizedUpdateRequest = new BlogRequest("Unauthorized Update", "This should fail");
        assertThrows(Exception.class, () -> {
            blogService.updateBlog(createdBlog.getId(), unauthorizedUpdateRequest, anotherAdmin);
        });

        // 7. Create blog as another admin
        BlogRequest anotherBlogRequest = new BlogRequest("Another Admin Blog", "Posted by another admin");
        Blog anotherBlog = blogService.createBlog(anotherBlogRequest, anotherAdmin);

        // 8. Check all blogs again
        List<Blog> updatedAllBlogs = blogService.getAllBlogs();
        assertEquals(2, updatedAllBlogs.size());

        // 9. Delete blog
        blogService.deleteBlog(createdBlog.getId(), testAdmin);

        // 10. Verify blog is deleted
        List<Blog> blogsAfterDeletion = blogService.getAllBlogs();
        assertEquals(1, blogsAfterDeletion.size());
        assertEquals(anotherBlog.getId(), blogsAfterDeletion.get(0).getId());

        // 11. Try to get deleted blog (should fail with ResourceNotFoundException)
        assertThrows(Exception.class, () -> {
            blogService.getBlogById(createdBlog.getId());
        });
    }

    @Test
    void authenticationFlow_IncorrectPasswordAndDuplicateRegistration() {
        // 1. Try login with incorrect password
        AuthRequest incorrectLoginRequest = new AuthRequest("testadmin", "wrongpassword");
        assertThrows(RuntimeException.class, () -> {
            authService.login(incorrectLoginRequest);
        });

        // 2. Try login with non-existent user
        AuthRequest nonExistentLoginRequest = new AuthRequest("nonexistent", "password");
        assertThrows(RuntimeException.class, () -> {
            authService.login(nonExistentLoginRequest);
        });

        // 3. Try to register with existing username
        Admin duplicateAdmin = new Admin();
        duplicateAdmin.setUsername("testadmin");
        duplicateAdmin.setPassword("anotherpassword");

        assertThrows(RuntimeException.class, () -> {
            authService.register(duplicateAdmin);
        });
    }

    @Test
    void blogValidationTest() {
        // 1. Try to create blog with invalid data
        BlogRequest invalidBlogRequest = new BlogRequest("", "");
        assertThrows(Exception.class, () -> {
            blogService.createBlog(invalidBlogRequest, testAdmin);
        });

        // 2. Try to create blog with null content
        BlogRequest nullContentBlogRequest = new BlogRequest("Title Only", null);
        assertThrows(Exception.class, () -> {
            blogService.createBlog(nullContentBlogRequest, testAdmin);
        });

        // 3. Create a valid blog then try to update with invalid data
        BlogRequest validBlogRequest = new BlogRequest("Valid Blog", "This is valid content");
        Blog validBlog = blogService.createBlog(validBlogRequest, testAdmin);

        BlogRequest invalidUpdateRequest = new BlogRequest(null, "Only content");
        assertThrows(Exception.class, () -> {
            blogService.updateBlog(validBlog.getId(), invalidUpdateRequest, testAdmin);
        });
    }
}