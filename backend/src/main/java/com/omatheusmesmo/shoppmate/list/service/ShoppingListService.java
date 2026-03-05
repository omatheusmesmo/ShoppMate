package com.omatheusmesmo.shoppmate.list.service;

import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.list.entity.ShoppingList;

import com.omatheusmesmo.shoppmate.list.repository.ShoppingListRepository;
import com.omatheusmesmo.shoppmate.user.service.UserService;
import com.omatheusmesmo.shoppmate.shared.service.AuditService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ShoppingListService {

    private final ShoppingListRepository shoppingListRepository;

    private final AuditService auditService;

    private final UserService userService;

    public ShoppingListService(ShoppingListRepository shoppingListRepository, AuditService auditService,
            UserService userService) {
        this.shoppingListRepository = shoppingListRepository;
        this.auditService = auditService;
        this.userService = userService;
    }

    public ShoppingList saveList(ShoppingList ShoppingList) {
        isListValid(ShoppingList);
        auditService.setAuditData(ShoppingList, true);
        shoppingListRepository.save(ShoppingList);
        return ShoppingList;
    }

    public void isListValid(ShoppingList ShoppingList) {
        ShoppingList.checkName();
        getOwnerId(ShoppingList);
    }

    private User getOwnerId(ShoppingList ShoppingList) {
        return userService.findUser(ShoppingList.getOwner().getId());
    }

    public Optional<ShoppingList> findList(ShoppingList ShoppingList) {
        Optional<ShoppingList> foundList = shoppingListRepository.findById(ShoppingList.getId());
        if (foundList.isPresent()) {
            return foundList;
        } else {
            throw new NoSuchElementException("ShoppingList not found");
        }
    }

    public ShoppingList findListById(Long id) {
        return shoppingListRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("ShoppingList not found"));
    }

    // TODO: implement soft delete?
    public void removeList(Long id) {
        findListById(id);
        shoppingListRepository.deleteById(id);
    }

    public ShoppingList editList(ShoppingList ShoppingList) {
        findListById(ShoppingList.getId());
        isListValid(ShoppingList);
        auditService.setAuditData(ShoppingList, false);
        shoppingListRepository.save(ShoppingList);
        return ShoppingList;
    }

    public List<ShoppingList> findAll() {
        return shoppingListRepository.findAll();
    }
}
