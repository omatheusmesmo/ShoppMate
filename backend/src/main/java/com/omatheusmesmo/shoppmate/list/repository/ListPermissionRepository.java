package com.omatheusmesmo.shoppmate.list.repository;

import com.omatheusmesmo.shoppmate.list.entity.ListPermission;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ListPermissionRepository extends JpaRepository<ListPermission, Long> {
    @EntityGraph(attributePaths = { "user" })
    List<ListPermission> findByShoppingListIdAndDeletedFalse(Long id);

    @EntityGraph(attributePaths = { "user", "shoppingList", "shoppingList.owner" })
    Optional<ListPermission> findByIdAndDeletedFalse(Long id);
}
