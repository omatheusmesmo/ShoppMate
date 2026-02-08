package com.omatheusmesmo.shoppmate.list.controller;

import com.omatheusmesmo.shoppmate.list.dtos.ListItemRequestDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ListItemResponseDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ListItemSummaryDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ListItemUpdateRequestDTO;
import com.omatheusmesmo.shoppmate.list.entity.ListItem;
import com.omatheusmesmo.shoppmate.list.mapper.ListItemMapper;
import com.omatheusmesmo.shoppmate.list.service.ListItemService;
import com.omatheusmesmo.shoppmate.utils.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/lists/{listId}/items")
public class ListItemController {

    @Autowired
    private ListItemService service;

    @Autowired
    ListItemMapper listItemMapper;

    @Operation(summary = "Get a specific ListItem by its ID within a ShoppingList")
    @GetMapping("/{id}")
    public ResponseEntity<ListItemResponseDTO> getListItemById(
            @PathVariable Long id) {

        ListItem listItem = service.findListItemById(id);

        ListItemResponseDTO responseDTO = listItemMapper.toResponseDTO(listItem);
        return HttpResponseUtil.ok(responseDTO);
    }


    @Operation(description = "Return all ListItems for a specific ShoppingList")
    @GetMapping
    public ResponseEntity<List<ListItemSummaryDTO>> getAllListItemsByListId(@PathVariable Long listId) {
        List<ListItem> listItems = service.findAll(listId);

        List<ListItemSummaryDTO> responseDTOs = listItems.stream()
                .map(listItemMapper::toSummaryDTO)
                .toList();

        return HttpResponseUtil.ok(responseDTOs);
    }

    @Operation(summary = "Add a new ListItem")
    @PostMapping
    public ResponseEntity<ListItemResponseDTO> addListItem(
            @Valid @RequestBody ListItemRequestDTO requestDTO) {
        ListItem addedListItem = service.addShoppItemList(requestDTO);
        ListItemResponseDTO responseDTO = listItemMapper.toResponseDTO(addedListItem);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(addedListItem.getId())
                .toUri();

        return ResponseEntity.created(location).body(responseDTO);
    }

    @Operation(summary = "Delete a ListItem by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteListItem(
            @PathVariable Long id) {

        service.removeList(id);
        return HttpResponseUtil.noContent();
    }

    @Operation(summary = "Update a ListItem")
    @PutMapping("/{id}")
    public ResponseEntity<ListItemResponseDTO> updateListItem(
            @PathVariable Long id,
            @Valid @RequestBody ListItemUpdateRequestDTO requestDTO) {

        ListItem updatedListItem = service.editList(id, requestDTO);

        ListItemResponseDTO responseDTO = listItemMapper.toResponseDTO(updatedListItem);
        return HttpResponseUtil.ok(responseDTO);
    }
}