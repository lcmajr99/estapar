package com.estapar.parking.service;

import com.estapar.parking.domain.ParkingSession;
import com.estapar.parking.domain.ParkingSpot;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ParkingServiceParkedTest {

    @Mock
    private ParkingSessionRepository sessionRepository;

    @Mock
    private ParkingSpotRepository spotRepository;

    @Mock
    private SectorRepository sectorRepository;

    @Mock
    private ParkingSessionLogRepository logRepository;

    @InjectMocks
    private ParkedEventHandler handler;

    @Test
    void shouldFailOnUnknownCoordinates() {
        when(sessionRepository
                .findByLicensePlateIgnoreCaseAndExitTimeIsNull(any()))
                .thenReturn(Optional.of(new ParkingSession()));

        when(spotRepository.findByLatAndLng(anyDouble(), anyDouble()))
                .thenReturn(Optional.empty());

        WebhookEventDTO event = parkedEvent();

        assertThrows(IllegalStateException.class,
                () -> handler.handle(event));
    }

    @Test
    void shouldChangeSector() {
        Sector a = new Sector("A", 100, BigDecimal.TEN);
        Sector b = new Sector("B", 100, BigDecimal.TEN);

        ParkingSession session = new ParkingSession();
        session.setSector(a);

        ParkingSpot spot = new ParkingSpot();
        spot.setSector(b);

        when(sessionRepository
                .findByLicensePlateIgnoreCaseAndExitTimeIsNull(any()))
                .thenReturn(Optional.of(session));
        when(spotRepository.findByLatAndLng(anyDouble(), anyDouble()))
                .thenReturn(Optional.of(spot));

        handler.handle(parkedEvent());

        assertEquals(b, session.getSector());
    }

    private WebhookEventDTO parkedEvent() {
        WebhookEventDTO dto = new WebhookEventDTO();
        dto.setLicensePlate("ABC-1234");
        dto.setEventType("PARKED");
        dto.setLat(-23.5);
        dto.setLng(-46.6);
        return dto;
    }
}
