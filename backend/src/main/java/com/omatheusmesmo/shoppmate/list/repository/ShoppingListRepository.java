package com.omatheusmesmo.shoppmate.list.repository;

import java.util.List;
import java.util.Optional;

import com.omatheusmesmo.shoppmate.list.entity.ShoppingList;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {
    @EntityGraph(attributePaths = { "owner" })
    List<ShoppingList> findAll();

    @EntityGraph(attributePaths = { "owner" })
    Optional<ShoppingList> findByIdAndDeletedFalse(Long id);

}
