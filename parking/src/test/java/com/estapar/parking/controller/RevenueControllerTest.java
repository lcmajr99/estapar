package com.estapar.parking.controller;

import com.estapar.parking.dto.RevenueDTO;
import com.estapar.parking.service.RevenueService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RevenueController.class)
class RevenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RevenueService revenueService;

    @Test
    @DisplayName("GET /revenue deve retornar receita por setor")
    void shouldReturnRevenueBySector() throws Exception {

        when(revenueService.calculateRevenueByDate(
                LocalDate.of(2025, 1, 1),
                "A"
        )).thenReturn(new RevenueDTO(
                new BigDecimal("0.00"),
                LocalDateTime.of(2025, 1, 1, 0, 0)
        ));

        mockMvc.perform(get("/revenue")
                        .param("date", "2025-01-01")
                        .param("sector", "A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(0.00))
                .andExpect(jsonPath("$.currency").value("BRL"));
    }
}
