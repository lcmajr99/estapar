package com.estapar.parking.service;

import com.estapar.parking.domain.ParkingSession;
import com.estapar.parking.domain.Sector;
import com.estapar.parking.dto.WebhookEventDTO;
import com.estapar.parking.repository.ParkingSessionLogRepository;
import com.estapar.parking.repository.ParkingSessionRepository;
import com.estapar.parking.repository.ParkingSpotRepository;
import com.estapar.parking.repository.SectorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ParkingServiceExitTest {
    @Mock
    private ParkingSessionRepository sessionRepository;

    @Mock
    private ParkingSpotRepository spotRepository;

    @Mock
    private SectorRepository sectorRepository;

    @Mock
    private ParkingSessionLogRepository logRepository;

    @InjectMocks
    private ExitEventHandler handler;

    @Test
    void shouldFailOnTimeTravel() {
        ParkingSession session = new ParkingSession();
        session.setEntryTime(LocalDateTime.now());

        when(sessionRepository
                .findByLicensePlateIgnoreCaseAndExitTimeIsNull(any()))
                .thenReturn(Optional.of(session));

        WebhookEventDTO event = new WebhookEventDTO();
        event.setLicensePlate("CAR-1");
        event.setEventType("EXIT");
        event.setExitTime(LocalDateTime.now().minusMinutes(5));

        assertThrows(IllegalArgumentException.class,
                () -> handler.handle(event));
    }

    @Test
    void shouldApplyFreeTier() {
        ParkingSession session = new ParkingSession();
        session.setEntryTime(LocalDateTime.now().minusMinutes(20));
        session.setSector(new Sector("A", 100, BigDecimal.TEN));
        session.setAppliedPriceFactor(BigDecimal.ONE);

        when(sessionRepository
                .findByLicensePlateIgnoreCaseAndExitTimeIsNull(any()))
                .thenReturn(Optional.of(session));

        handler.handle(exitEvent());

        assertEquals(BigDecimal.ZERO, session.getTotalAmount());
    }

    private WebhookEventDTO exitEvent() {
        WebhookEventDTO dto = new WebhookEventDTO();
        dto.setLicensePlate("CAR-1");
        dto.setEventType("EXIT");
        dto.setExitTime(LocalDateTime.now());
        return dto;
    }
}