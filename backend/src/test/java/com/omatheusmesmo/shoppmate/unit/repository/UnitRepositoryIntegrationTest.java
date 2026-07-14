package com.omatheusmesmo.shoppmate.unit.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.omatheusmesmo.shoppmate.shared.testcontainers.AbstractIntegrationTest;
import com.omatheusmesmo.shoppmate.unit.entity.Unit;
import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.user.repository.UserRepository;

class UnitRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private UserRepository userRepository;

    private User currentUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        unitRepository.deleteAll();
        userRepository.deleteAll();

        currentUser = createUser("current-unit-user@example.com");
        otherUser = createUser("other-unit-user@example.com");
    }

    @Test
    void findAccessibleByIdAndUserId_OwnUnit_ReturnsUnit() {
        Unit unit = createUnit("Own Unit", "own", currentUser, false, false);

        Optional<Unit> result = unitRepository.findAccessibleByIdAndUserId(unit.getId(), currentUser.getId());

        assertTrue(result.isPresent());
        assertEquals(unit.getId(), result.get().getId());
    }

    @Test
    void findAccessibleByIdAndUserId_SystemUnit_ReturnsUnit() {
        Unit unit = createUnit("System Unit", "sys", null, true, false);

        Optional<Unit> result = unitRepository.findAccessibleByIdAndUserId(unit.getId(), currentUser.getId());

        assertTrue(result.isPresent());
        assertEquals(unit.getId(), result.get().getId());
    }

    @Test
    void findAccessibleByIdAndUserId_OtherUsersUnit_ReturnsEmpty() {
        Unit unit = createUnit("Other User Unit", "other", otherUser, false, false);

        Optional<Unit> result = unitRepository.findAccessibleByIdAndUserId(unit.getId(), currentUser.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void findAccessibleByIdAndUserId_DeletedOwnedUnit_ReturnsEmpty() {
        Unit unit = createUnit("Deleted Unit", "deleted", currentUser, false, true);

        Optional<Unit> result = unitRepository.findAccessibleByIdAndUserId(unit.getId(), currentUser.getId());

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

    private Unit createUnit(String name, String symbol, User owner, boolean systemStandard, boolean deleted) {

        Unit unit = new Unit();
        unit.setName(name);
        unit.setSymbol(symbol);
        unit.setOwner(owner);
        unit.setSystemStandard(systemStandard);
        unit.setDeleted(deleted);

        return unitRepository.save(unit);
    }
}
