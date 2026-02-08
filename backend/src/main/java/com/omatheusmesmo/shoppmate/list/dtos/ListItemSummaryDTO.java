package com.omatheusmesmo.shoppmate.list.dtos;

public record ListItemSummaryDTO(
    Long idListItem,
    Long itemId,
    String itemName,
    Integer quantity,
    Boolean purchased
) {}