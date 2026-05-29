package com.omatheusmesmo.shoppmate.list.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.v3.oas.annotations.Operation;

import com.omatheusmesmo.shoppmate.list.dtos.ListItemRequestDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ListItemResponseDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ListItemSummaryDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ListItemUpdateRequestDTO;
import com.omatheusmesmo.shoppmate.list.entity.ListItem;
import com.omatheusmesmo.shoppmate.list.mapper.ListItemMapper;
import com.omatheusmesmo.shoppmate.list.service.ListItemService;
import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.utils.HttpResponseUtil;

@RestController
@RequestMapping("/lists/{listId}/items")
public class ListItemController {

    private final ListItemService service;

    private final ListItemMapper listItemMapper;

    public ListItemController(ListItemService service, ListItemMapper listItemMapper) {
        this.service = service;
        this.listItemMapper = listItemMapper;
    }

    @Operation(summary = "Get a specific ListItem by its ID within a ShoppingList")
    @GetMapping("/{id}")
    public ResponseEntity<ListItemResponseDTO> getListItemById(@PathVariable Long listId, @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        ListItem listItem = service.findListItemById(listId, id, user);

        ListItemResponseDTO responseDTO = listItemMapper.toResponseDTO(listItem);
        return HttpResponseUtil.ok(responseDTO);
    }

    @Operation(description = "Return all ListItems for a specific ShoppingList")
    @GetMapping
    public ResponseEntity<List<ListItemSummaryDTO>> getAllListItemsByListId(@PathVariable Long listId,
            @AuthenticationPrincipal User user) {
        List<ListItem> listItems = service.findAll(listId, user);

        List<ListItemSummaryDTO> responseDTOs = listItems.stream().map(listItemMapper::toSummaryDTO).toList();

        return HttpResponseUtil.ok(responseDTOs);
    }

    @Operation(summary = "Add a new ListItem")
    @PostMapping
    public ResponseEntity<ListItemResponseDTO> addListItem(@PathVariable Long listId,
            @Valid @RequestBody ListItemRequestDTO requestDTO, @AuthenticationPrincipal User user) {

        if (!listId.equals(requestDTO.listId())) {
            throw new IllegalArgumentException(
                    "Path listId (" + listId + ") does not match body listId (" + requestDTO.listId() + ")");
        }

        ListItem addedListItem = service.addShoppItemList(requestDTO, user);
        ListItemResponseDTO responseDTO = listItemMapper.toResponseDTO(addedListItem);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(addedListItem.getId()).toUri();

        return ResponseEntity.created(location).body(responseDTO);
    }

    @Operation(summary = "Delete a ListItem by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteListItem(@PathVariable Long listId, @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        service.removeList(listId, id, user);
        return HttpResponseUtil.noContent();
    }

    @Operation(summary = "Update a ListItem")
    @PutMapping("/{id}")
    public ResponseEntity<ListItemResponseDTO> updateListItem(@PathVariable Long listId, @PathVariable Long id,
            @Valid @RequestBody ListItemUpdateRequestDTO requestDTO, @AuthenticationPrincipal User user) {

        if (!listId.equals(requestDTO.listId())) {
            throw new IllegalArgumentException(
                    "Path listId (" + listId + ") does not match body listId (" + requestDTO.listId() + ")");
        }

        ListItem updatedListItem = service.editList(listId, id, requestDTO, user);

        ListItemResponseDTO responseDTO = listItemMapper.toResponseDTO(updatedListItem);
        return HttpResponseUtil.ok(responseDTO);
    }
}
