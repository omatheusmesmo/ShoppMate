package com.omatheusmesmo.shoppmate.item.repository;

import java.util.List;
import java.util.Optional;

import com.omatheusmesmo.shoppmate.item.entity.Item;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    @EntityGraph(attributePaths = { "category", "unit" })
    List<Item> findAll();

    @EntityGraph(attributePaths = { "category", "unit" })
    Optional<Item> findById(Long id);

    @EntityGraph(attributePaths = { "category", "unit" })
    Optional<Item> findByIdAndDeletedFalse(Long id);
}
