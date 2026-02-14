package com.omatheusmesmo.shoppmate.list.controller;

import com.omatheusmesmo.shoppmate.list.dtos.ShoppingListRequestDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ShoppingListResponseDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ShoppingListUpdateRequestDTO;
import com.omatheusmesmo.shoppmate.list.entity.ShoppingList;
import com.omatheusmesmo.shoppmate.list.mapper.ListMapper;
import com.omatheusmesmo.shoppmate.list.service.ShoppingListService;
import com.omatheusmesmo.shoppmate.utils.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

// TODO: return only lists owned by the user or that are shared with him
@RestController
@RequestMapping("/lists")
public class ShoppingListController {

    @Autowired
    private ShoppingListService service;

    @Autowired
    private ListMapper listMapper;

    @Operation(description = "Return all Shopping Lists")
    @GetMapping
    public ResponseEntity<List<ShoppingListResponseDTO>> getAllShoppingLists() {
        List<ShoppingList> shoppingLists = service.findAll();

        List<ShoppingListResponseDTO> responseDTOs = shoppingLists.stream().map(listMapper::toResponseDTO).toList();
        return HttpResponseUtil.ok(responseDTOs);
    }

    @Operation(description = "Return a Shopping List by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ShoppingListResponseDTO> getShoppingListById(@PathVariable Long id) {
        ShoppingList shoppingList = service.findListById(id);
        ShoppingListResponseDTO responseDTO = listMapper.toResponseDTO(shoppingList);
        return HttpResponseUtil.ok(responseDTO);
    }

    @Operation(summary = "Add a new Shopping List")
    @PostMapping
    public ResponseEntity<ShoppingListResponseDTO> addShoppingList(@Valid @RequestBody ShoppingListRequestDTO dto) {
        ShoppingList shoppingList = listMapper.toEntity(dto);
        ShoppingList savedList = service.saveList(shoppingList);
        ShoppingListResponseDTO responseDTO = listMapper.toResponseDTO(savedList);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(savedList.getId())
                .toUri();

        return ResponseEntity.created(location).body(responseDTO);
    }

    @Operation(summary = "Delete a Shopping List by id")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteShoppingList(@PathVariable Long id) {
        service.removeList(id);
    }

    @Operation(summary = "Update a Shopping List")
    @PutMapping("/{id}")
    public ResponseEntity<ShoppingListResponseDTO> updateShoppingList(@PathVariable Long id,
            @Valid @RequestBody ShoppingListUpdateRequestDTO requestDTO) {

        ShoppingList existingList = service.findListById(id);

        listMapper.updateEntityFromDto(requestDTO, existingList);

        ShoppingList updatedList = service.editList(existingList);

        ShoppingListResponseDTO responseDTO = listMapper.toResponseDTO(updatedList);

        return HttpResponseUtil.ok(responseDTO);
    }
}
