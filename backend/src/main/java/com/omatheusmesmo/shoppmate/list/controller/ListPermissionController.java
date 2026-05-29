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

import com.omatheusmesmo.shoppmate.list.dtos.listpermission.ListPermissionRequestDTO;
import com.omatheusmesmo.shoppmate.list.dtos.listpermission.ListPermissionResponseDTO;
import com.omatheusmesmo.shoppmate.list.dtos.listpermission.ListPermissionUpdateRequestDTO;
import com.omatheusmesmo.shoppmate.list.dtos.listpermission.ListPermissionSummaryDTO;
import com.omatheusmesmo.shoppmate.list.entity.ListPermission;
import com.omatheusmesmo.shoppmate.list.mapper.ListPermissionMapper;
import com.omatheusmesmo.shoppmate.list.service.ListPermissionService;
import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.utils.HttpResponseUtil;

@RestController
@RequestMapping("/lists/{listId}/permissions")
public class ListPermissionController {

    private final ListPermissionService service;

    private final ListPermissionMapper listPermissionMapper;

    public ListPermissionController(ListPermissionService service, ListPermissionMapper listPermissionMapper) {
        this.service = service;
        this.listPermissionMapper = listPermissionMapper;
    }

    @Operation(description = "Return all ListPermissions for a list (only the list owner can view)")
    @GetMapping
    public ResponseEntity<List<ListPermissionSummaryDTO>> getAllListPermissions(@PathVariable Long listId,
            @AuthenticationPrincipal User user) {
        List<ListPermission> listPermissions = service.findAllPermissionsByListId(listId, user);
        List<ListPermissionSummaryDTO> responseDTOs = listPermissions.stream().map(listPermissionMapper::toSummaryDTO)
                .toList();
        return HttpResponseUtil.ok(responseDTOs);
    }

    @Operation(summary = "Add a new ListPermission")
    @PostMapping
    public ResponseEntity<ListPermissionResponseDTO> addListPermission(
            @Valid @RequestBody ListPermissionRequestDTO requestDTO, @AuthenticationPrincipal User requester) {
        ListPermission addedListPermission = service.addListPermission(requestDTO, requester);
        ListPermissionResponseDTO responseDTO = listPermissionMapper.toResponseDTO(addedListPermission);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(addedListPermission.getId()).toUri();

        return ResponseEntity.created(location).body(responseDTO);
    }

    @Operation(summary = "Delete a ListPermission by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteListPermission(@PathVariable Long id, @AuthenticationPrincipal User user) {
        service.removeList(id, user);
        return HttpResponseUtil.noContent();
    }

    @Operation(summary = "Update a ListPermission")
    @PutMapping("/{id}")
    public ResponseEntity<ListPermissionResponseDTO> updateListPermission(@PathVariable Long id,
            @Valid @RequestBody ListPermissionUpdateRequestDTO requestDTO, @AuthenticationPrincipal User user) {

        ListPermission updatedListPermission = service.editList(id, requestDTO, user);
        ListPermissionResponseDTO responseDTO = listPermissionMapper.toResponseDTO(updatedListPermission);

        return ResponseEntity.ok(responseDTO);
    }
}
