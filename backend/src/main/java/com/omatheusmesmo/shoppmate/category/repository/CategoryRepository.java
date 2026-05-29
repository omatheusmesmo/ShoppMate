package com.omatheusmesmo.shoppmate.category.repository;

import com.omatheusmesmo.shoppmate.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    Optional<Category> findByIdAndDeletedFalse(Long id);

    @Query("SELECT c FROM Category c LEFT JOIN c.owner o WHERE c.deleted = false AND (c.isSystemStandard = true OR o.id = :userId)")
    List<Category> findAllAccessibleByUserId(@Param("userId") Long userId);
}
