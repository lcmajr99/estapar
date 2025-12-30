package com.estapar.parking.service;

import com.estapar.parking.domain.ParkingSession;
import com.estapar.parking.domain.Sector;
import com.estapar.parking.dto.WebhookEventDTO;
import com.estapar.parking.repository.ParkingSessionLogRepository;
import com.estapar.parking.repository.ParkingSessionRepository;
import com.estapar.parking.repository.SectorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParkingServiceEntryTest {

    @Mock
    private ParkingSessionRepository sessionRepository;

    @Mock
    private SectorRepository sectorRepository;

    @Mock
    private ParkingSessionLogRepository logRepository;

    @InjectMocks
    private EntryEventHandler handler;

    @Test
    void shouldRejectDuplicateEntry() {
        when(sessionRepository
                .findByLicensePlateIgnoreCaseAndExitTimeIsNull(any()))
                .thenReturn(Optional.of(new ParkingSession()));

        WebhookEventDTO event = entryEvent();

        assertThrows(IllegalStateException.class,
                () -> handler.handle(event));
    }

    @Test
    void shouldRejectWhenFull() {
        Sector full = new Sector("A", 10, BigDecimal.TEN);
        full.setOccupiedCount(10);

        when(sectorRepository.findAll())
                .thenReturn(List.of(full));

        WebhookEventDTO event = entryEvent();

        assertThrows(IllegalStateException.class,
                () -> handler.handle(event));
    }

    @Test
    void shouldApplySurgePricing() {
        Sector sector = new Sector("A", 100, BigDecimal.TEN);
        sector.setOccupiedCount(80);

        when(sectorRepository.findAll())
                .thenReturn(List.of(sector));
        when(sessionRepository
                .findByLicensePlateIgnoreCaseAndExitTimeIsNull(any()))
                .thenReturn(Optional.empty());

        handler.handle(entryEvent());

        verify(sessionRepository).save(any(ParkingSession.class));
    }

    private WebhookEventDTO entryEvent() {
        WebhookEventDTO dto = new WebhookEventDTO();
        dto.setLicensePlate("ABC-1234");
        dto.setEventType("ENTRY");
        dto.setEntryTime(LocalDateTime.now());
        return dto;
    }
}