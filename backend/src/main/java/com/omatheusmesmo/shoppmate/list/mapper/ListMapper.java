package com.omatheusmesmo.shoppmate.list.mapper;

import com.omatheusmesmo.shoppmate.list.dtos.ShoppingListRequestDTO;
import java.math.BigDecimal;
import com.omatheusmesmo.shoppmate.list.dtos.ShoppingListResponseDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ShoppingListUpdateRequestDTO;
import com.omatheusmesmo.shoppmate.list.entity.ListItem;
import com.omatheusmesmo.shoppmate.list.entity.ShoppingList;
import com.omatheusmesmo.shoppmate.user.dtos.UserResponseDTO;
import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.user.mapper.UserMapper;
import com.omatheusmesmo.shoppmate.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListMapper {

    private final UserService userService;
    private final UserMapper userMapper;

    @Autowired
    public ListMapper(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    public ShoppingList toEntity(ShoppingListRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        ShoppingList entity = new ShoppingList();
        entity.setName(dto.name());

        User owner = userService.findUser(dto.idUser());
        entity.setOwner(owner);

        return entity;
    }

    public ShoppingListResponseDTO toResponseDTO(ShoppingList entity) {
        if (entity == null) {
            return null;
        }

        UserResponseDTO ownerDTO = userMapper.toResponseDTO(entity.getOwner());
        BigDecimal totalValue = calculateTotalValue(entity);

        return new ShoppingListResponseDTO(entity.getId(), entity.getName(), ownerDTO, totalValue);
    }

    private BigDecimal calculateTotalValue(ShoppingList entity) {
        if (entity.getItems() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = BigDecimal.ZERO;
        for (ListItem item : entity.getItems()) {
            if (item.getDeleted() != null && item.getDeleted())
                continue;
            BigDecimal itemTotal = calculateItemTotal(item);
            if (itemTotal == null) {
                return null;
            }
            total = total.add(itemTotal);
        }
        return total;
    }

    private BigDecimal calculateItemTotal(ListItem item) {
        if (item.getUnitPrice() == null) {
            return null;
        }
        Integer quantity = item.getQuantity() != null ? item.getQuantity() : 0;
        return item.getUnitPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public void updateEntityFromDto(ShoppingListUpdateRequestDTO dto, ShoppingList entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setName(dto.name());
    }
}
