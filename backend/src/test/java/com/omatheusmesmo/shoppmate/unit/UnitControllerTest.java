package com.omatheusmesmo.shoppmate.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omatheusmesmo.shoppmate.auth.service.JwtService;
import com.omatheusmesmo.shoppmate.unit.controller.UnitController;
import com.omatheusmesmo.shoppmate.unit.entity.Unit;
import com.omatheusmesmo.shoppmate.unit.service.UnitService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UnitController.class)
@AutoConfigureMockMvc(addFilters = false)
class UnitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UnitService unitService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private Unit unit;

    @BeforeEach
    void setUp() {
        unit = new Unit();
        unit.setId(1L);
        unit.setName("Kilogram");
        unit.setSymbol("kg");
    }

    @Test
    @WithMockUser
    void getAllUnits() throws Exception {
        when(unitService.findAll()).thenReturn(List.of(unit));

        mockMvc.perform(get("/unit")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void addUnit() throws Exception {
        when(unitService.saveUnit(any(Unit.class))).thenReturn(unit);

        mockMvc.perform(
                post("/unit").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(unit)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser
    void deleteUnit() throws Exception {
        doNothing().when(unitService).removeUnitById(1L);

        mockMvc.perform(delete("/unit/1")).andExpect(status().isNoContent());
    }
}
