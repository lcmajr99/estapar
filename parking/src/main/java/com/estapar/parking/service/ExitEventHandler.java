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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

@Service
public class ExitEventHandler {

    private static final long FREE_MINUTES = 30;

    private final ParkingSessionRepository sessionRepository;
    private final ParkingSpotRepository spotRepository;
    private final SectorRepository sectorRepository;
    private final ParkingSessionLogRepository logRepository;

    public ExitEventHandler(
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
                                        "Sessão ativa não encontrada para EXIT"
                                )
                        );

        if (event.getExitTime().isBefore(session.getEntryTime())) {
            throw new IllegalArgumentException(
                    "Data de saída inválida (anterior à entrada)"
            );
        }

        long minutes =
                Duration.between(
                        session.getEntryTime(),
                        event.getExitTime()
                ).toMinutes();

        BigDecimal amount = calculateFinalAmount(session, minutes);

        if (session.getParkingSpot() != null) {
            ParkingSpot spot = session.getParkingSpot();
            spot.setPhysicallyOccupied(false);
            spotRepository.save(spot);
        }

        Sector sector = session.getSector();
        sector.decrementOccupancy();
        sectorRepository.save(sector);

        session.finish(event.getExitTime(), amount);
        sessionRepository.save(session);

        logRepository.save(
                new ParkingSessionLog(
                        session,
                        ParkingSessionLog.LogType.EXIT_COMPLETED,
                        "Sessão encerrada"
                )
        );
    }

    private BigDecimal calculateFinalAmount(
            ParkingSession session,
            long totalMinutes
    ) {
        if (totalMinutes <= FREE_MINUTES) {
            return BigDecimal.ZERO;
        }

        long hours = (long) Math.ceil(totalMinutes / 60.0);

        return session.getSector()
                .getBasePrice()
                .multiply(session.getAppliedPriceFactor())
                .multiply(BigDecimal.valueOf(hours))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
