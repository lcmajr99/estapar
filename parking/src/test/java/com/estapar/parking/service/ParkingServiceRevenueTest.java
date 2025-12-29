package com.estapar.parking.service;

import com.estapar.parking.dto.RevenueDTO;
import com.estapar.parking.repository.ParkingSessionRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParkingServiceRevenueTest {

    @Mock
    private ParkingSessionRepository sessionRepository;

    @InjectMocks
    private ParkingService parkingService;

    @Test
    @DisplayName("REVENUE: retorna receita total do dia")
    void shouldReturnTotalRevenueForDay() {
        when(sessionRepository.sumTotalRevenue(any(), any()))
                .thenReturn(new BigDecimal("500.00"));

        RevenueDTO response =
                parkingService.calculateRevenueByDate(LocalDate.of(2025, 1, 1), null);

        assertEquals(new BigDecimal("500.00"), response.getAmount());
        assertEquals("BRL", response.getCurrency());
    }

    @Test
    @DisplayName("REVENUE: retorna receita do setor")
    void shouldReturnRevenueBySector() {
        when(sessionRepository.sumRevenueBySectorAndPeriod(
                eq("A"),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(new BigDecimal("200.00"));

        RevenueDTO response =
                parkingService.calculateRevenueByDate(LocalDate.of(2025, 1, 1), "A");

        assertEquals(new BigDecimal("200.00"), response.getAmount());
    }
}
