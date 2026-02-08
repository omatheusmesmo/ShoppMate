package com.omatheusmesmo.shoppmate.item.repository;

import com.omatheusmesmo.shoppmate.item.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

}
