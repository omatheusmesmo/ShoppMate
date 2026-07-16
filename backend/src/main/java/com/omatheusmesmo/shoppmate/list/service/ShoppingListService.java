package com.omatheusmesmo.shoppmate.list.service;

import com.omatheusmesmo.shoppmate.list.dtos.ShoppingListUpdateRequestDTO;
import com.omatheusmesmo.shoppmate.list.entity.ShoppingList;
import com.omatheusmesmo.shoppmate.list.mapper.ListMapper;
import com.omatheusmesmo.shoppmate.list.repository.ShoppingListRepository;
import com.omatheusmesmo.shoppmate.list.entity.ListPermission;
import com.omatheusmesmo.shoppmate.list.entity.Permission;
import com.omatheusmesmo.shoppmate.list.repository.ListPermissionRepository;
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

    private final ListPermissionRepository listPermissionRepository;

    private final AuditService auditService;

    private final UserService userService;

    private final ListMapper listMapper;

    private boolean isOwner(ShoppingList shoppingList, User user) {
        return shoppingList.getOwner().getId().equals(user.getId());
    }

    private Optional<ListPermission> findUserPermission(Long listId, User user) {
        return listPermissionRepository.findByShoppingListIdAndUserIdAndDeletedFalse(listId, user.getId());
    }

    public ShoppingListService(ShoppingListRepository shoppingListRepository,
            ListPermissionRepository listPermissionRepository, AuditService auditService, UserService userService,
            ListMapper listMapper) {
        this.shoppingListRepository = shoppingListRepository;
        this.listPermissionRepository = listPermissionRepository;
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
        findAndVerifyOwnership(id, currentLoggedUser);
        shoppingListRepository.deleteById(id);
    }

    public ShoppingList editList(ShoppingList shoppingList, User currentLoggedUser) {
        findAndVerifyOwnership(shoppingList.getId(), currentLoggedUser);

        return editListWithoutVerification(shoppingList);
    }

    public ShoppingList editList(Long id, ShoppingListUpdateRequestDTO dto, User currentLoggedUser) {

        ShoppingList existingList = findAndVerifyOwnership(id, currentLoggedUser);

        listMapper.updateEntityFromDto(dto, existingList);

        return editListWithoutVerification(existingList);
    }

    private ShoppingList editListWithoutVerification(ShoppingList shoppingList) {
        isListValid(shoppingList);
        auditService.setAuditData(shoppingList, false);
        shoppingListRepository.save(shoppingList);
        return shoppingList;
    }

    public List<ShoppingList> findAll() {
        return shoppingListRepository.findAll();
    }

    public List<ShoppingList> findAllByUser(User user) {
        return shoppingListRepository.findAllAccessibleByUserId(user.getId());
    }

    public ShoppingList findAndVerifyReadAccess(Long listId, User user) {
        ShoppingList shoppingList = findListById(listId);

        if (isOwner(shoppingList, user)) {
            return shoppingList;
        }

        ListPermission permission = findUserPermission(listId, user)
                .orElseThrow(() -> new ResourceOwnershipException("You do not have permission to access this list."));

        if (permission.getPermission() != Permission.READ && permission.getPermission() != Permission.WRITE) {
            throw new ResourceOwnershipException("You do not have permission to access this list.");
        }

        return shoppingList;
    }

    public ShoppingList findAndVerifyWriteAccess(Long listId, User user) {
        ShoppingList shoppingList = findListById(listId);

        if (isOwner(shoppingList, user)) {
            return shoppingList;
        }

        ListPermission permission = findUserPermission(listId, user)
                .orElseThrow(() -> new ResourceOwnershipException("You do not have write permission for this list."));

        if (permission.getPermission() != Permission.WRITE) {
            throw new ResourceOwnershipException("You do not have write permission for this list.");
        }

        return shoppingList;
    }

    public ShoppingList findAndVerifyOwnership(Long listId, User user) {
        ShoppingList shoppingList = findListById(listId);

        if (!isOwner(shoppingList, user)) {
            throw new ResourceOwnershipException("Only the list owner may perform this operation.");
        }

        return shoppingList;
    }

}
