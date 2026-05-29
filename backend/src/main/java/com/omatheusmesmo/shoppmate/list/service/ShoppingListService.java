package com.omatheusmesmo.shoppmate.list.service;

import com.omatheusmesmo.shoppmate.list.dtos.ShoppingListUpdateRequestDTO;
import com.omatheusmesmo.shoppmate.list.entity.ShoppingList;
import com.omatheusmesmo.shoppmate.list.mapper.ListMapper;
import com.omatheusmesmo.shoppmate.list.repository.ShoppingListRepository;
import com.omatheusmesmo.shoppmate.shared.service.AuditService;
import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.user.service.UserService;
import com.omatheusmesmo.shoppmate.utils.exception.ResourceOwnershipException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ShoppingListService {

    private final ShoppingListRepository shoppingListRepository;

    private final AuditService auditService;

    private final UserService userService;

    private final ListMapper listMapper;

    public ShoppingListService(ShoppingListRepository shoppingListRepository, AuditService auditService,
            UserService userService, ListMapper listMapper) {
        this.shoppingListRepository = shoppingListRepository;
        this.auditService = auditService;
        this.userService = userService;
        this.listMapper = listMapper;
    }

    public ShoppingList saveList(ShoppingList ShoppingList) {
        isListValid(ShoppingList);
        auditService.setAuditData(ShoppingList, true);
        shoppingListRepository.save(ShoppingList);
        return ShoppingList;
    }

    public void isListValid(ShoppingList ShoppingList) {
        ShoppingList.checkName();
    }

    public Optional<ShoppingList> findList(ShoppingList ShoppingList) {
        Optional<ShoppingList> foundList = shoppingListRepository.findByIdAndDeletedFalse(ShoppingList.getId());
        if (foundList.isPresent()) {
            return foundList;
        } else {
            throw new NoSuchElementException("ShoppingList not found");
        }
    }

    public ShoppingList findListById(Long id) {
        return shoppingListRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NoSuchElementException("ShoppingList not found"));
    }

    // TODO: implement soft delete?
    public void removeList(Long id, User currentLoggedUser) {
        ShoppingList shoppingList = findAndVerifyAccess(id, currentLoggedUser);

        // Only the owner can delete the list
        if (!shoppingList.getOwner().getId().equals(currentLoggedUser.getId())) {
            throw new ResourceOwnershipException("You can only delete your own shopping lists!");
        }

        shoppingListRepository.deleteById(id);
    }

    public ShoppingList editList(ShoppingList ShoppingList, User currentLoggedUser) {
        findAndVerifyAccess(ShoppingList.getId(), currentLoggedUser);

        return editListWithoutVerification(ShoppingList);
    }

    public ShoppingList editList(Long id, ShoppingListUpdateRequestDTO dto, User currentLoggedUser) {
        ShoppingList existingList = findAndVerifyAccess(id, currentLoggedUser);
        listMapper.updateEntityFromDto(dto, existingList);
        return editListWithoutVerification(existingList);
    }

    private ShoppingList editListWithoutVerification(ShoppingList ShoppingList) {
        isListValid(ShoppingList);
        auditService.setAuditData(ShoppingList, false);
        shoppingListRepository.save(ShoppingList);
        return ShoppingList;
    }

    public List<ShoppingList> findAll() {
        return shoppingListRepository.findAll();
    }

    public List<ShoppingList> findAllByUser(User user) {
        return shoppingListRepository.findAllAccessibleByUserId(user.getId());
    }

    public ShoppingList findAndVerifyAccess(Long listId, User user) {
        ShoppingList shoppingList = findListById(listId);

        if (!shoppingList.getOwner().getId().equals(user.getId())) {
            throw new ResourceOwnershipException("Access Denied: You do not have permission to access this list.");
        }

        return shoppingList;
    }

    public void verifyOwnership(Long listId, User user) {
        ShoppingList shoppingList = findListById(listId);

        if (!shoppingList.getOwner().getId().equals(user.getId())) {
            throw new ResourceOwnershipException("You do not have permission to access this resource");
        }
    }
}
