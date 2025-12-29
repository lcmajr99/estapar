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

    @Mock private ParkingSessionRepository sessionRepository;
    @Mock private SectorRepository sectorRepository;
    @Mock private ParkingSpotRepository parkingSpotRepository;
    @Mock private ParkingSessionLogRepository logRepository;

    @InjectMocks
    private ParkingService parkingService;

    @Test
    @DisplayName("EXIT: 29 minutos deve cobrar zero")
    void shouldApplyFreeTier() {
        ParkingSession session = new ParkingSession();
        session.setEntryTime(LocalDateTime.now().minusMinutes(29));
        session.setSector(new Sector("A", 100, BigDecimal.TEN));
        session.setAppliedPriceFactor(BigDecimal.ONE);

        when(sessionRepository.findByLicensePlateIgnoreCaseAndExitTimeIsNull(any()))
                .thenReturn(Optional.of(session));

        parkingService.processEvent(exit("FREE"));

        assertEquals(BigDecimal.ZERO, session.getTotalAmount());
    }

    @Test
    @DisplayName("EXIT: deve falhar se saída for antes da entrada")
    void shouldFailOnTimeTravel() {
        ParkingSession session = new ParkingSession();
        session.setEntryTime(LocalDateTime.now());

        when(sessionRepository.findByLicensePlateIgnoreCaseAndExitTimeIsNull(any()))
                .thenReturn(Optional.of(session));

        WebhookEventDTO exit = exit("TIME");
        exit.setExit_time(LocalDateTime.now().minusMinutes(5));

        assertThrows(IllegalArgumentException.class,
                () -> parkingService.processEvent(exit));
    }

    private WebhookEventDTO exit(String plate) {
        WebhookEventDTO dto = new WebhookEventDTO();
        dto.setLicense_plate(plate);
        dto.setEvent_type("EXIT");
        dto.setExit_time(LocalDateTime.now());
        return dto;
    }
}
