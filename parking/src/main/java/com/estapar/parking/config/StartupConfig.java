package com.estapar.parking.config;

import com.estapar.parking.domain.ParkingSpot;
import com.estapar.parking.domain.Sector;
import com.estapar.parking.dto.GarageResponse;
import com.estapar.parking.dto.ParkingSpotDTO;
import com.estapar.parking.dto.SectorDTO;
import com.estapar.parking.repository.ParkingSpotRepository;
import com.estapar.parking.repository.SectorRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Profile("bootstrap")
@Component
public class StartupConfig {

    private static final Logger logger =
            LoggerFactory.getLogger(StartupConfig.class);

    private final GarageClient garageClient;
    private final SectorRepository sectorRepository;
    private final ParkingSpotRepository parkingSpotRepository;

    public StartupConfig(
            GarageClient garageClient,
            SectorRepository sectorRepository,
            ParkingSpotRepository parkingSpotRepository
    ) {
        this.garageClient = garageClient;
        this.sectorRepository = sectorRepository;
        this.parkingSpotRepository = parkingSpotRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void bootstrap() {

        logger.info("Startup iniciado (ApplicationReadyEvent)");

        try {
            GarageResponse response = garageClient.fetchGarage();
            Map<String, Sector> sectorMap = new HashMap<>();

            for (SectorDTO dto : response.getGarage()) {

                Sector sector = sectorRepository
                        .findByName(dto.getSector())
                        .orElseGet(() -> {
                            Sector s = new Sector();
                            s.setName(dto.getSector());
                            s.setBasePrice(dto.getBase_price());
                            s.setMaxCapacity(dto.getMax_capacity());
                            s.setOpenHour(LocalTime.parse(dto.getOpen_hour()));
                            s.setCloseHour(LocalTime.parse(dto.getClose_hour()));
                            s.setDurationLimitMinutes(dto.getDuration_limit_minutes());
                            return sectorRepository.save(s);
                        });

                sectorMap.put(sector.getName(), sector);
            }

            for (ParkingSpotDTO dto : response.getSpots()) {

                Sector sector = sectorMap.get(dto.getSector());

                boolean exists = parkingSpotRepository
                        .existsBySectorAndLatAndLng(sector, dto.getLat(), dto.getLng());

                if (!exists) {
                    ParkingSpot spot = new ParkingSpot(
                            sector,
                            dto.getLat(),
                            dto.getLng()
                    );
                    parkingSpotRepository.save(spot);
                }
            }

            logger.info(
                    "Startup finalizado: {} setores e {} vagas garantidas no sistema",
                    sectorMap.size(),
                    response.getSpots().size()
            );
        } catch (Exception ex) {
            logger.error("Falha ao carregar configuração da garagem", ex);
        }


    }
}
