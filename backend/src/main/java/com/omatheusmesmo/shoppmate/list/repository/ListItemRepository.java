package com.omatheusmesmo.shoppmate.list.repository;

import com.omatheusmesmo.shoppmate.list.entity.ListItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ListItemRepository extends JpaRepository<ListItem, Long> {

    @EntityGraph(attributePaths = { "item" })
    List<ListItem> findByShoppListId(Long shoppListId);

    @EntityGraph(attributePaths = { "item" })
    List<ListItem> findByShoppListIdAndDeletedFalse(Long shoppListId);

    @EntityGraph(attributePaths = { "shoppList.owner", "item.category", "item.unit" })
    Optional<ListItem> findByIdAndDeletedFalse(Long id);

}
