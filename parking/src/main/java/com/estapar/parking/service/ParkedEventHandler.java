package com.estapar.parking.service;

import com.estapar.parking.domain.ParkingSession;
import com.estapar.parking.domain.ParkingSessionLog;
import com.estapar.parking.domain.ParkingSpot;
import com.estapar.parking.domain.Sector;
import com.estapar.parking.dto.WebhookEventDTO;
import com.estapar.parking.repository.ParkingSessionLogRepository;
import com.estapar.parking.repository.ParkingSessionRepository;
import com.estapar.parking.repository.ParkingSpotRepository;
import com.estapar.parking.repository.SectorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParkedEventHandler {

    private final ParkingSessionRepository sessionRepository;
    private final ParkingSpotRepository spotRepository;
    private final SectorRepository sectorRepository;
    private final ParkingSessionLogRepository logRepository;

    public ParkedEventHandler(
            ParkingSessionRepository sessionRepository,
            ParkingSpotRepository spotRepository,
            SectorRepository sectorRepository,
            ParkingSessionLogRepository logRepository
    ) {
        this.sessionRepository = sessionRepository;
        this.spotRepository = spotRepository;
        this.sectorRepository = sectorRepository;
        this.logRepository = logRepository;
    }

    @Transactional
    public void handle(WebhookEventDTO event) {

        ParkingSession session =
                sessionRepository
                        .findByLicensePlateIgnoreCaseAndExitTimeIsNull(
                                event.getLicensePlate()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Evento PARKED sem sessão ativa"
                                )
                        );

        ParkingSpot spot =
                spotRepository
                        .findByLatAndLng(
                                event.getLat(),
                                event.getLng()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Carro parou em coordenadas desconhecidas!"
                                )
                        );

        Sector predicted = session.getSector();
        Sector real = spot.getSector();

        if (!predicted.equals(real)) {
            predicted.decrementOccupancy();
            real.incrementOccupancy();

            sectorRepository.save(predicted);
            sectorRepository.save(real);

            session.setSector(real);
        }

        spot.setPhysicallyOccupied(true);
        spotRepository.save(spot);

        session.setParkingSpot(spot);
        session.setStatus(ParkingSession.SessionStatus.PARKED);

        sessionRepository.save(session);

        logRepository.save(
                new ParkingSessionLog(
                        session,
                        ParkingSessionLog.LogType.PARKED_CONFIRMED,
                        "Veículo estacionado com sucesso"
                )
        );
    }
}
