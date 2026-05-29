package com.omatheusmesmo.shoppmate.item.repository;

import java.util.List;
import java.util.Optional;

import com.omatheusmesmo.shoppmate.item.entity.Item;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("SELECT i FROM Item i LEFT JOIN FETCH i.category LEFT JOIN FETCH i.unit WHERE i.id = :id AND i.deleted = false")
    Optional<Item> findByIdWithRelations(@Param("id") Long id);

    @EntityGraph(attributePaths = { "category", "unit" })
    List<Item> findAll();

    @EntityGraph(attributePaths = { "category", "unit" })
    List<Item> findAllByDeletedFalse();

    @EntityGraph(attributePaths = { "category", "unit" })
    Optional<Item> findById(Long id);

    @EntityGraph(attributePaths = { "category", "unit" })
    Optional<Item> findByIdAndDeletedFalse(Long id);
}
