package com.omatheusmesmo.shoppmate.list.controller;

import com.omatheusmesmo.shoppmate.list.dtos.ShoppingListRequestDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ShoppingListResponseDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ShoppingListUpdateRequestDTO;
import com.omatheusmesmo.shoppmate.list.entity.ShoppingList;
import com.omatheusmesmo.shoppmate.list.mapper.ListMapper;
import com.omatheusmesmo.shoppmate.list.service.ShoppingListService;
import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.utils.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/lists")
public class ShoppingListController {

    private final ShoppingListService service;

    private final ListMapper listMapper;

    public ShoppingListController(ShoppingListService service, ListMapper listMapper) {
        this.service = service;
        this.listMapper = listMapper;
    }

    @Operation(description = "Return all Shopping Lists owned by or shared with the authenticated user")
    @GetMapping
    public ResponseEntity<List<ShoppingListResponseDTO>> getAllShoppingLists(@AuthenticationPrincipal User user) {
        List<ShoppingList> shoppingLists = service.findAllByUser(user);

        List<ShoppingListResponseDTO> responseDTOs = shoppingLists.stream().map(listMapper::toResponseDTO).toList();
        return HttpResponseUtil.ok(responseDTOs);
    }

    @Operation(description = "Return a Shopping List by ID (only if owned by or shared with authenticated user)")
    @GetMapping("/{id}")
    public ResponseEntity<ShoppingListResponseDTO> getShoppingListById(@PathVariable Long id,
            @AuthenticationPrincipal User user) {
        ShoppingList shoppingList = service.findAndVerifyAccess(id, user);
        ShoppingListResponseDTO responseDTO = listMapper.toResponseDTO(shoppingList);
        return HttpResponseUtil.ok(responseDTO);
    }

    @Operation(summary = "Add a new Shopping List")
    @PostMapping
    public ResponseEntity<ShoppingListResponseDTO> addShoppingList(@Valid @RequestBody ShoppingListRequestDTO dto,
            @AuthenticationPrincipal User user) {
        ShoppingList shoppingList = listMapper.toEntity(dto, user);
        ShoppingList savedList = service.saveList(shoppingList);
        ShoppingListResponseDTO responseDTO = listMapper.toResponseDTO(savedList);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(savedList.getId())
                .toUri();

        return ResponseEntity.created(location).body(responseDTO);
    }

    @Operation(summary = "Delete a Shopping List by id")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteShoppingList(@PathVariable Long id, @AuthenticationPrincipal User user) {
        service.removeList(id, user);
    }

    @Operation(summary = "Update a Shopping List")
    @PutMapping("/{id}")
    public ResponseEntity<ShoppingListResponseDTO> updateShoppingList(@PathVariable Long id,
            @Valid @RequestBody ShoppingListUpdateRequestDTO requestDTO, @AuthenticationPrincipal User user) {

        ShoppingList updatedList = service.editList(id, requestDTO, user);

        ShoppingListResponseDTO responseDTO = listMapper.toResponseDTO(updatedList);

        return HttpResponseUtil.ok(responseDTO);
    }
}
