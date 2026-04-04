package com.omatheusmesmo.shoppmate.list.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omatheusmesmo.shoppmate.auth.service.JwtService;
import com.omatheusmesmo.shoppmate.list.dtos.listpermission.ListPermissionRequestDTO;
import com.omatheusmesmo.shoppmate.list.dtos.listpermission.ListPermissionResponseDTO;
import com.omatheusmesmo.shoppmate.list.dtos.listpermission.ListPermissionSummaryDTO;
import com.omatheusmesmo.shoppmate.list.entity.ListPermission;
import com.omatheusmesmo.shoppmate.list.entity.Permission;
import com.omatheusmesmo.shoppmate.list.mapper.ListPermissionMapper;
import com.omatheusmesmo.shoppmate.list.service.ListPermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ListPermissionController.class)
@AutoConfigureMockMvc(addFilters = false)
class ListPermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListPermissionService listPermissionService;

    @MockBean
    private ListPermissionMapper listPermissionMapper;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private ListPermission listPermission;
    private ListPermissionSummaryDTO summaryDTO;
    private ListPermissionResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        listPermission = new ListPermission();
        listPermission.setId(1L);
        listPermission.setPermission(Permission.READ);

        summaryDTO = new ListPermissionSummaryDTO(1L, "John Doe", "john@example.com", Permission.READ);
        responseDTO = new ListPermissionResponseDTO(1L, null, null, Permission.READ);
    }

    @Test
    @WithMockUser
    void getAllListPermissions() throws Exception {
        when(listPermissionService.findAllPermissionsByListId(1L)).thenReturn(List.of(listPermission));
        when(listPermissionMapper.toSummaryDTO(any(ListPermission.class))).thenReturn(summaryDTO);

        mockMvc.perform(get("/lists/1/permissions")).andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(summaryDTO))));
    }

    @Test
    @WithMockUser
    void addListPermission() throws Exception {
        ListPermissionRequestDTO requestDTO = new ListPermissionRequestDTO(1L, 1L, Permission.READ);
        when(listPermissionService.addListPermission(any(ListPermissionRequestDTO.class))).thenReturn(listPermission);
        when(listPermissionMapper.toResponseDTO(any(ListPermission.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/lists/1/permissions").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO))).andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDTO)));
    }
}
