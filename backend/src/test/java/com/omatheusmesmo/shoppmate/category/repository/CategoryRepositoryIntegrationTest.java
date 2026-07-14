package com.omatheusmesmo.shoppmate.category.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.omatheusmesmo.shoppmate.category.entity.Category;
import com.omatheusmesmo.shoppmate.shared.testcontainers.AbstractIntegrationTest;
import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.user.repository.UserRepository;

class CategoryRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private User currentUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        currentUser = createUser("current-user@example.com");
        otherUser = createUser("other-user@example.com");
    }

    @Test
    void findAccessibleByIdAndUserId_OwnCategory_ReturnsCategory() {
        Category category = createCategory("Own Category", currentUser, false, false);

        Optional<Category> result = categoryRepository.findAccessibleByIdAndUserId(category.getId(),
                currentUser.getId());

        assertTrue(result.isPresent());
        assertEquals(category.getId(), result.get().getId());
    }

    @Test
    void findAccessibleByIdAndUserId_SystemCategory_ReturnsCategory() {
        Category category = createCategory("System Category", null, true, false);

        Optional<Category> result = categoryRepository.findAccessibleByIdAndUserId(category.getId(),
                currentUser.getId());

        assertTrue(result.isPresent());
        assertEquals(category.getId(), result.get().getId());
    }

    @Test
    void findAccessibleByIdAndUserId_OtherUsersCategory_ReturnsEmpty() {
        Category category = createCategory("Other User Category", otherUser, false, false);

        Optional<Category> result = categoryRepository.findAccessibleByIdAndUserId(category.getId(),
                currentUser.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void findAccessibleByIdAndUserId_DeletedOwnedCategory_ReturnsEmpty() {
        Category category = createCategory("Deleted Category", currentUser, false, true);

        Optional<Category> result = categoryRepository.findAccessibleByIdAndUserId(category.getId(),
                currentUser.getId());

        assertTrue(result.isEmpty());
    }

    private User createUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setFullName("Repository Test User");
        user.setPassword("password");
        user.setRole("USER");
        user.setDeleted(false);

        return userRepository.save(user);
    }

    private Category createCategory(String name, User owner, boolean systemStandard, boolean deleted) {

        Category category = new Category();
        category.setName(name);
        category.setOwner(owner);
        category.setSystemStandard(systemStandard);
        category.setDeleted(deleted);

        return categoryRepository.save(category);
    }
}
