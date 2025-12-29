package com.estapar.parking.service;

import com.estapar.parking.domain.ParkingSession;
import com.estapar.parking.domain.ParkingSpot;
import com.estapar.parking.domain.Sector;
import com.estapar.parking.dto.WebhookEventDTO;
import com.estapar.parking.repository.ParkingSessionLogRepository;
import com.estapar.parking.repository.ParkingSessionRepository;
import com.estapar.parking.repository.ParkingSpotRepository;
import com.estapar.parking.repository.SectorRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ParkingServiceParkedTest {

    @Mock private ParkingSessionRepository sessionRepository;
    @Mock private SectorRepository sectorRepository;
    @Mock private ParkingSpotRepository parkingSpotRepository;
    @Mock private ParkingSessionLogRepository logRepository;

    @InjectMocks
    private ParkingService parkingService;

    @Test
    @DisplayName("PARKED: confirma vaga correta sem recalcular preço")
    void shouldConfirmParking() {
        Sector sector = sector("A", "10.00");

        ParkingSession session = session(sector, BigDecimal.ONE);
        ParkingSpot spot = spot(sector, false);

        when(sessionRepository.findByLicensePlateIgnoreCaseAndExitTimeIsNull("CAR-1"))
                .thenReturn(Optional.of(session));
        when(parkingSpotRepository.findByLatAndLng(anyDouble(), anyDouble()))
                .thenReturn(Optional.of(spot));

        parkingService.processEvent(parked("CAR-1"));

        assertEquals(sector, session.getSector());
        assertTrue(spot.isPhysicallyOccupied());
    }

    @Test
    @DisplayName("PARKED: deve trocar setor e recalcular fator")
    void shouldChangeSector() {
        Sector A = sector("A", "10.00");
        Sector B = sector("B", "15.00");

        ParkingSession session = session(A, BigDecimal.ONE);
        ParkingSpot spot = spot(B, false);

        when(sessionRepository.findByLicensePlateIgnoreCaseAndExitTimeIsNull("SHIFT"))
                .thenReturn(Optional.of(session));
        when(parkingSpotRepository.findByLatAndLng(anyDouble(), anyDouble()))
                .thenReturn(Optional.of(spot));

        parkingService.processEvent(parked("SHIFT"));

        assertEquals(B, session.getSector());
        assertTrue(session.getAppliedPriceFactor().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("PARKED: deve falhar se coordenada não existir")
    void shouldFailOnUnknownCoordinates() {
        when(sessionRepository.findByLicensePlateIgnoreCaseAndExitTimeIsNull(any()))
                .thenReturn(Optional.of(new ParkingSession()));
        when(parkingSpotRepository.findByLatAndLng(anyDouble(), anyDouble()))
                .thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> parkingService.processEvent(parked("GPS-ERROR"))
        );

        assertEquals("Carro parou em coordenadas desconhecidas!", ex.getMessage());
    }

    /* ---------- helpers ---------- */

    private WebhookEventDTO parked(String plate) {
        WebhookEventDTO dto = new WebhookEventDTO();
        dto.setLicense_plate(plate);
        dto.setEvent_type("PARKED");
        dto.setLat(-23.5);
        dto.setLng(-46.6);
        return dto;
    }

    private ParkingSession session(Sector sector, BigDecimal factor) {
        ParkingSession s = new ParkingSession();
        s.setSector(sector);
        s.setAppliedPriceFactor(factor);
        return s;
    }

    private ParkingSpot spot(Sector sector, boolean occupied) {
        ParkingSpot s = new ParkingSpot();
        s.setSector(sector);
        s.setPhysicallyOccupied(occupied);
        return s;
    }

    private Sector sector(String code, String price) {
        return new Sector(code, 100, new BigDecimal(price));
    }
}

