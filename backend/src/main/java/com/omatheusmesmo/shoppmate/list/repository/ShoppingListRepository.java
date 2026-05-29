package com.omatheusmesmo.shoppmate.list.repository;

import java.util.List;
import java.util.Optional;

import com.omatheusmesmo.shoppmate.list.entity.ShoppingList;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {
    @EntityGraph(attributePaths = { "owner" })
    List<ShoppingList> findAll();

    @EntityGraph(attributePaths = { "owner" })
    Optional<ShoppingList> findByIdAndDeletedFalse(Long id);

    List<ShoppingList> findByOwnerIdAndDeletedFalse(Long ownerId);

    @Query("""
            SELECT DISTINCT l FROM ShoppingList l
            LEFT JOIN ListPermission lp ON l.id = lp.shoppingList.id AND lp.deleted = false
            WHERE l.deleted = false AND (l.owner.id = :userId OR lp.user.id = :userId)
            """)
    List<ShoppingList> findAllAccessibleByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT l FROM ShoppingList l
            LEFT JOIN ListPermission lp ON l.id = lp.shoppingList.id AND lp.deleted = false AND lp.user.id = :userId
            WHERE l.id = :listId AND l.deleted = false AND (l.owner.id = :userId OR lp.user.id = :userId)
            """)
    Optional<ShoppingList> findByIdAndUserId(@Param("listId") Long listId, @Param("userId") Long userId);
}
