package com.omatheusmesmo.shoppmate.unit.repository;

import com.omatheusmesmo.shoppmate.unit.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Long> {

    Optional<Unit> findBySymbol(String symbol);

    Optional<Unit> findByName(String name);

    Optional<Unit> findByIdAndDeletedFalse(Long id);

    @Query("SELECT u FROM Unit u LEFT JOIN u.owner o WHERE u.deleted = false AND (u.isSystemStandard = true OR o.id = :userId)")
    List<Unit> findAllAccessibleByUserId(@Param("userId") Long userId);
}
