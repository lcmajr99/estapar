package com.estapar.parking.controller;

import com.estapar.parking.repository.ParkingSessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RevenueController.class)
class RevenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ParkingSessionRepository sessionRepository;

    @Test
    @DisplayName("GET /revenue: Deve retornar 200 OK e o total faturado")
    void shouldReturnTotalRevenue() throws Exception {
        BigDecimal fakeTotal = new BigDecimal("150.00");

        when(sessionRepository.sumTotalRevenue(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(fakeTotal);

        // Ação e Validação
        mockMvc.perform(get("/revenue")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(150.00));
    }
}