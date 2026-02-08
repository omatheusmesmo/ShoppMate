package com.omatheusmesmo.shoppmate.item.controller;

import com.omatheusmesmo.shoppmate.item.dto.ItemRequestDTO;
import com.omatheusmesmo.shoppmate.item.dto.ItemResponseDTO;
import com.omatheusmesmo.shoppmate.item.entity.Item;
import com.omatheusmesmo.shoppmate.item.mapper.ItemMapper;
import com.omatheusmesmo.shoppmate.item.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/item")
public class ItemController {

    @Autowired
    private ItemService itemService;
    @Autowired
    private ItemMapper itemMapper;

    @Operation(summary = "Return all items")
    @GetMapping
    public ResponseEntity<List<ItemResponseDTO>> getAllItems() {
        List<Item> items = itemService.findAll();
        List<ItemResponseDTO> responseDTOs = items.stream()
                .map(itemMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(responseDTOs);
    }

    @Operation(summary = "Add a new item")
    @PostMapping
    public ResponseEntity<ItemResponseDTO> addItem(@Valid @RequestBody ItemRequestDTO itemDTO) {

        Item item = itemMapper.toEntity(itemDTO);
        Item savedItem = itemService.addItem(item);
        ItemResponseDTO responseDTO = itemMapper.toResponseDTO(savedItem);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedItem.getId())
                .toUri();

        return ResponseEntity.created(location).body(responseDTO);
    }

    @Operation(summary = "Delete a item by id")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable Long id) {
        itemService.removeItem(id);
    }

    @Operation(summary = "Update a item")
    @PutMapping("/{id}")
    public ResponseEntity<ItemResponseDTO> updateItem(@PathVariable Long id, @Valid @RequestBody ItemRequestDTO itemDTO) {

        Item itemToUpdate = itemMapper.toEntity(itemDTO);
        itemToUpdate.setId(id);

        Item updatedItem = itemService.editItem(itemToUpdate);
        ItemResponseDTO responseDTO = itemMapper.toResponseDTO(updatedItem);

        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "Get an item by id")
    @GetMapping("/{id}")
    public ResponseEntity<ItemResponseDTO> getItemById(@PathVariable Long id) {
        Item item = itemService.findById(id);
        ItemResponseDTO responseDTO = itemMapper.toResponseDTO(item);
        return ResponseEntity.ok(responseDTO);
    }
}