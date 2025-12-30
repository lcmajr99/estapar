package com.estapar.parking.service;

import com.estapar.parking.domain.ParkingSession;
import com.estapar.parking.domain.ParkingSessionLog;
import com.estapar.parking.domain.Sector;
import com.estapar.parking.dto.WebhookEventDTO;
import com.estapar.parking.repository.ParkingSessionLogRepository;
import com.estapar.parking.repository.ParkingSessionRepository;
import com.estapar.parking.repository.SectorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class EntryEventHandler {

    private final ParkingSessionRepository sessionRepository;
    private final SectorRepository sectorRepository;
    private final ParkingSessionLogRepository logRepository;

    public EntryEventHandler(
            ParkingSessionRepository sessionRepository,
            SectorRepository sectorRepository,
            ParkingSessionLogRepository logRepository
    ) {
        this.sessionRepository = sessionRepository;
        this.sectorRepository = sectorRepository;
        this.logRepository = logRepository;
    }

    @Transactional
    public void handle(WebhookEventDTO event) {

        if (sessionRepository
                .findByLicensePlateIgnoreCaseAndExitTimeIsNull(
                        event.getLicensePlate()
                )
                .isPresent()) {
            throw new IllegalStateException(
                    "Veículo já possui sessão ativa"
            );
        }

        List<Sector> sectors = sectorRepository.findAll();

        Sector sector = sectors.stream()
                .filter(s ->
                        s.getOccupiedCount() < s.getMaxCapacity()
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Estacionamento Lotado (Full)"
                        )
                );

        BigDecimal factor = calculateDynamicPriceFactor(sector);

        sector.incrementOccupancy();
        sectorRepository.save(sector);

        ParkingSession session = new ParkingSession();
        session.setLicensePlate(event.getLicensePlate());
        session.setSector(sector);
        session.setEntryTime(event.getEntryTime());
        session.setAppliedPriceFactor(factor);
        session.setStatus(ParkingSession.SessionStatus.ENTERING);

        sessionRepository.save(session);

        logRepository.save(
                new ParkingSessionLog(
                        session,
                        ParkingSessionLog.LogType.ENTRY_CREATED,
                        "Entrada registrada no setor " + sector.getCode()
                )
        );
    }

    private BigDecimal calculateDynamicPriceFactor(Sector sector) {

        double ratio =
                (double) sector.getOccupiedCount()
                        / sector.getMaxCapacity();

        if (ratio < 0.25) return new BigDecimal("0.90");
        if (ratio < 0.50) return new BigDecimal("1.00");
        if (ratio < 0.75) return new BigDecimal("1.10");
        return new BigDecimal("1.25");
    }
}
