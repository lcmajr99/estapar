package com.estapar.parking.service;

import com.estapar.parking.domain.ParkingSession;
import com.estapar.parking.domain.ParkingSpot;
import com.estapar.parking.domain.Sector;
import com.estapar.parking.dto.WebhookEventDTO;
import com.estapar.parking.repository.ParkingSessionRepository;
import com.estapar.parking.repository.ParkingSpotRepository;
import com.estapar.parking.repository.SectorRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Profile("db")
@Service
public class ParkingService {

    private static final long FREE_MINUTES = 30;

    private final ParkingSessionRepository sessionRepository;
    private final ParkingSpotRepository parkingSpotRepository;
    private final SectorRepository sectorRepository;

    public ParkingService(
            ParkingSessionRepository sessionRepository,
            ParkingSpotRepository parkingSpotRepository,
            SectorRepository sectorRepository
    ) {
        this.sessionRepository = sessionRepository;
        this.parkingSpotRepository = parkingSpotRepository;
        this.sectorRepository = sectorRepository;
    }


    @Transactional
    public void processEvent(WebhookEventDTO event) {

        if ("ENTRY".equals(event.getEvent_type())) {
            handleEntry(event);
            return;
        }if ("EXIT".equals(event.getEvent_type())) {
            handleExit(event);
            return;
        }
    }

    private void handleEntry(WebhookEventDTO event) {
        sessionRepository.findByLicensePlateAndExitTimeIsNull(event.getLicense_plate())
                .ifPresent(s -> {
                    throw new IllegalStateException("Veículo já possui sessão ativa");
                });

        List<Sector> sectors = sectorRepository.findAll();
        Sector selectedSector = null;
        ParkingSpot selectedSpot = null;

        double pricePerHour = 0.0;

        for (Sector sector : sectors) {

            List<ParkingSpot> freeSpots =
                    parkingSpotRepository.findBySectorAndOccupiedFalse(sector);

            if (!freeSpots.isEmpty()) {
                selectedSector = sector;
                selectedSpot = freeSpots.get(0);

                long active = sector.getMaxCapacity() - freeSpots.size();
                double ratio = (double) active / sector.getMaxCapacity();
                double base = sector.getBasePrice();

                if (ratio < 0.25) pricePerHour = base * 0.90;
                else if (ratio < 0.50) pricePerHour = base;
                else if (ratio < 0.75) pricePerHour = base * 1.10;
                else pricePerHour = base * 1.25;

                break;
            }
        }

        if (selectedSector == null ) {
            throw new IllegalStateException("Entrada bloqueada.");
        }

        selectedSpot.occupy();

        ParkingSession session = new ParkingSession(
                event.getLicense_plate(),
                selectedSector,
                selectedSpot,
                event.getEntry_time(),
                pricePerHour
        );
        parkingSpotRepository.save(selectedSpot);
        sessionRepository.save(session);
    }

    private void handleExit(WebhookEventDTO event) {

        ParkingSession session = sessionRepository
                .findByLicensePlateAndExitTimeIsNull(event.getLicense_plate())
                .orElseThrow(() ->
                        new IllegalStateException("Sessão ativa não encontrada")
                );

        Duration duration = Duration.between(
                session.getEntryTime(),
                event.getExit_time()
        );

        long totalMinutes = duration.toMinutes();
        double totalAmount;

        if (totalMinutes <= FREE_MINUTES) {
            totalAmount = 0.0;
        } else {
            long billableHours = (long) Math.ceil(totalMinutes / 60.0);
            totalAmount = billableHours * session.getPricePerHour();
        }

        session.finish(event.getExit_time(), totalAmount);

        ParkingSpot spot = session.getParkingSpot();
        spot.release();

        parkingSpotRepository.save(spot);
        sessionRepository.save(session);
    }
}
