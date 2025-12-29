package com.estapar.parking.service;

import com.estapar.parking.domain.ParkingSession;
import com.estapar.parking.domain.Sector;
import com.estapar.parking.dto.WebhookEventDTO;
import com.estapar.parking.repository.ParkingSessionLogRepository;
import com.estapar.parking.repository.ParkingSessionRepository;
import com.estapar.parking.repository.ParkingSpotRepository;
import com.estapar.parking.repository.SectorRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParkingServiceEntryTest {

    @Mock
    private ParkingSessionRepository sessionRepository;
    @Mock private SectorRepository sectorRepository;
    @Mock private ParkingSpotRepository parkingSpotRepository;
    @Mock private ParkingSessionLogRepository logRepository;

    @InjectMocks
    private ParkingService parkingService;

    @Test
    @DisplayName("ENTRY: cria sessão com fator 1.25 se setor > 75% ocupado")
    void shouldApplySurgePricing() {
        Sector sector = sector("A", 100, 80, "10.00");

        when(sessionRepository.findByLicensePlateIgnoreCaseAndExitTimeIsNull(any()))
                .thenReturn(Optional.empty());
        when(sectorRepository.findAll()).thenReturn(List.of(sector));

        parkingService.processEvent(entry("ABC-1234"));

        ArgumentCaptor<ParkingSession> captor = ArgumentCaptor.forClass(ParkingSession.class);
        verify(sessionRepository).save(captor.capture());

        assertEquals(new BigDecimal("1.25"), captor.getValue().getAppliedPriceFactor());
    }

    @Test
    @DisplayName("ENTRY: deve falhar se já existir sessão ativa")
    void shouldRejectDuplicateEntry() {
        when(sessionRepository.findByLicensePlateIgnoreCaseAndExitTimeIsNull(any()))
                .thenReturn(Optional.of(new ParkingSession()));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> parkingService.processEvent(entry("ABC-1234"))
        );

        assertEquals("Veículo já possui sessão ativa", ex.getMessage());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("ENTRY: deve falhar se estacionamento estiver cheio")
    void shouldRejectWhenFull() {
        Sector full = sector("A", 10, 10, "10.00");

        when(sessionRepository.findByLicensePlateIgnoreCaseAndExitTimeIsNull(any()))
                .thenReturn(Optional.empty());
        when(sectorRepository.findAll()).thenReturn(List.of(full));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> parkingService.processEvent(entry("LATE-1"))
        );

        assertEquals("Estacionamento Lotado (Full)", ex.getMessage());
    }

    /* ---------- helpers ---------- */

    private WebhookEventDTO entry(String plate) {
        WebhookEventDTO dto = new WebhookEventDTO();
        dto.setLicense_plate(plate);
        dto.setEvent_type("ENTRY");
        dto.setEntry_time(LocalDateTime.now());
        return dto;
    }

    private Sector sector(String code, int max, int occupied, String price) {
        Sector s = new Sector(code, max, new BigDecimal(price));
        s.setOccupiedCount(occupied);
        return s;
    }
}
