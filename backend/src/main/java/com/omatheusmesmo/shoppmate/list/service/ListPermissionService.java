package com.omatheusmesmo.shoppmate.list.service;

import com.omatheusmesmo.shoppmate.list.dtos.listpermission.ListPermissionRequestDTO;
import com.omatheusmesmo.shoppmate.list.dtos.listpermission.ListPermissionUpdateRequestDTO;
import com.omatheusmesmo.shoppmate.list.entity.ListPermission;
import com.omatheusmesmo.shoppmate.list.entity.ShoppingList;
import com.omatheusmesmo.shoppmate.list.mapper.ListPermissionMapper;
import com.omatheusmesmo.shoppmate.list.repository.ListPermissionRepository;
import com.omatheusmesmo.shoppmate.shared.service.AuditService;
import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ListPermissionService {

    @Autowired
    private ListPermissionRepository listPermissionRepository;

    @Autowired
    private ShoppingListService shoppingListService;
    @Autowired
    private UserService userService;
    @Autowired
    private AuditService auditService;
    @Autowired
    private ListPermissionMapper listPermissionMapper;

    public ListPermission addListPermission(ListPermissionRequestDTO listPermissionRequestDTO) {
        ShoppingList shoppingList = shoppingListService.findListById(listPermissionRequestDTO.idList());
        User user = userService.findUserById(listPermissionRequestDTO.idUser());

        ListPermission listPermission = listPermissionMapper.toEntity(listPermissionRequestDTO, shoppingList, user);

        isListValid(listPermission);
        auditService.setAuditData(listPermission, true);
        listPermissionRepository.save(listPermission);
        return listPermission;
    }

    public void isListValid(ListPermission listPermission) {
        userService.isUserValid(listPermission.getUser());
        shoppingListService.isListValid(listPermission.getShoppingList());
    }
    
    public Optional<ListPermission> findListItem(ListPermission listPermission) {
        Optional<ListPermission> foundList = listPermissionRepository.findById(listPermission.getId());
        if (foundList.isPresent()) {
            return foundList;
        } else {
            throw new NoSuchElementException("ShoppingList not found");
        }
    }

    public ListPermission findListUserPermissionById(Long id) {
        return listPermissionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("ListPermission not found"));
    }

    public void removeList(Long id) {
        ListPermission listPermission = findListUserPermissionById(id);
        auditService.softDelete(listPermission);
        listPermissionRepository.save(listPermission);
    }

    public ListPermission editList(Long id, ListPermissionUpdateRequestDTO listPermissionUpdateRequestDTO) {
        ListPermission listPermission = findListUserPermissionById(id);
        listPermission.setPermission(listPermissionUpdateRequestDTO.permission());
        isListValid(listPermission);
        auditService.setAuditData(listPermission, false);
        listPermissionRepository.save(listPermission);
        return listPermission;
    }

    public List<ListPermission> findAllPermissionsByListId(Long id) {
        return listPermissionRepository.findByShoppingListIdAndDeletedFalse(id);
    }
}
