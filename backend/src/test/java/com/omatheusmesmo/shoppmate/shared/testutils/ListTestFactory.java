package com.omatheusmesmo.shoppmate.shared.testutils;

import com.omatheusmesmo.shoppmate.list.dtos.ListItemRequestDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ListItemUpdateRequestDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ShoppingListRequestDTO;
import com.omatheusmesmo.shoppmate.list.dtos.listpermission.ListPermissionRequestDTO;
import com.omatheusmesmo.shoppmate.list.dtos.listpermission.ListPermissionUpdateRequestDTO;
import com.omatheusmesmo.shoppmate.list.entity.ListItem;
import com.omatheusmesmo.shoppmate.list.entity.ListPermission;
import com.omatheusmesmo.shoppmate.list.entity.Permission;
import com.omatheusmesmo.shoppmate.list.entity.ShoppingList;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

public class ListTestFactory {

    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);

    public static ShoppingList createValidShoppingList() {
        ShoppingList shoppingList = new ShoppingList();
        shoppingList.setId(ID_GENERATOR.getAndIncrement());
        shoppingList.setName(FakerUtil.getFaker().lorem().word());
        shoppingList.setOwner(UserTestFactory.createValidUser());
        shoppingList.setCreatedAt(LocalDateTime.now());
        shoppingList.setUpdatedAt(LocalDateTime.now());
        shoppingList.setDeleted(false);
        return shoppingList;
    }

    public static ListItem createValidListItem(ShoppingList list) {
        ListItem listItem = new ListItem();
        listItem.setId(ID_GENERATOR.getAndIncrement());
        listItem.setShoppList(list);
        listItem.setItem(ItemTestFactory.createValidItem());
        listItem.setQuantity(FakerUtil.getFaker().number().numberBetween(1, 10));
        listItem.setPurchased(false);
        listItem.setUnitPrice(BigDecimal.valueOf(FakerUtil.getFaker().number().randomDouble(2, 1, 100)));
        listItem.setCreatedAt(LocalDateTime.now());
        listItem.setUpdatedAt(LocalDateTime.now());
        listItem.setDeleted(false);
        return listItem;
    }

    public static ListPermission createValidListPermission(ShoppingList list) {
        ListPermission permission = new ListPermission();
        permission.setId(ID_GENERATOR.getAndIncrement());
        permission.setShoppingList(list);
        permission.setUser(UserTestFactory.createValidUser());
        permission.setPermission(FakerUtil.getFaker().options().option(Permission.class));
        permission.setCreatedAt(LocalDateTime.now());
        permission.setUpdatedAt(LocalDateTime.now());
        permission.setDeleted(false);
        return permission;
    }

    public static ShoppingListRequestDTO createValidShoppingListRequestDTO() {
        return new ShoppingListRequestDTO(FakerUtil.getFaker().commerce().department() + " List");
    }

    public static ListItemRequestDTO createValidListItemRequestDTO(Long listId, Long itemId) {
        return new ListItemRequestDTO(listId, itemId, FakerUtil.getFaker().number().numberBetween(1, 10),
                BigDecimal.valueOf(FakerUtil.getFaker().number().randomDouble(2, 1, 100)));
    }

    public static ListItemUpdateRequestDTO createValidListItemUpdateRequestDTO(Long listId, Long itemId) {
        return new ListItemUpdateRequestDTO(listId, itemId, FakerUtil.getFaker().number().numberBetween(1, 10),
                FakerUtil.getFaker().bool().bool(),
                BigDecimal.valueOf(FakerUtil.getFaker().number().randomDouble(2, 1, 100)));
    }

    public static ListPermissionRequestDTO createValidListPermissionRequestDTO(Long listId, Long userId) {
        return new ListPermissionRequestDTO(listId, userId, FakerUtil.getFaker().options().option(Permission.class));
    }

    public static ListPermissionUpdateRequestDTO createValidListPermissionUpdateRequestDTO() {
        return new ListPermissionUpdateRequestDTO(FakerUtil.getFaker().options().option(Permission.class));
    }
}
