package com.estapar.parking.config;

import com.estapar.parking.domain.ParkingSpot;
import com.estapar.parking.domain.Sector;
import com.estapar.parking.dto.sim.SimGarageResponse;
import com.estapar.parking.dto.sim.SimSectorDTO;
import com.estapar.parking.dto.sim.SimSpotDTO;
import com.estapar.parking.repository.ParkingSpotRepository;
import com.estapar.parking.repository.SectorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Profile("!test")
@Component
public class StartupConfig {

    private static final Logger logger = LoggerFactory.getLogger(StartupConfig.class);

    private final GarageClient garageClient;
    private final SectorRepository sectorRepository;
    private final ParkingSpotRepository parkingSpotRepository;

    public StartupConfig(GarageClient garageClient,
                         SectorRepository sectorRepository,
                         ParkingSpotRepository parkingSpotRepository) {
        this.garageClient = garageClient;
        this.sectorRepository = sectorRepository;
        this.parkingSpotRepository = parkingSpotRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void bootstrap() {
        logger.info("Iniciando sincronização com o Simulador...");

        try {
            SimGarageResponse response = garageClient.fetchGarage();

            if (response == null || response.garage() == null) {
                logger.warn("Simulador retornou dados vazios.");
                return;
            }

            Map<String, Sector> sectorCache = new HashMap<>();

            for (SimSectorDTO dto : response.garage()) {

                Sector sector = sectorRepository.findAll().stream()
                        .filter(s -> s.getCode().equals(dto.code()))
                        .findFirst()
                        .orElseGet(() -> {
                            Sector newSector = new Sector();
                            newSector.setCode(dto.code());
                            return newSector;
                        });

                sector.setMaxCapacity(dto.maxCapacity());
                sector.setBasePrice(BigDecimal.valueOf(dto.basePrice()));

                sector = sectorRepository.save(sector);
                sectorCache.put(sector.getCode(), sector);
            }

            int spotsCreated = 0;
            for (SimSpotDTO dto : response.spots()) {
                Sector sector = sectorCache.get(dto.sectorCode());

                if (sector == null) {
                    logger.error("Vaga {} aponta para setor inexistente: {}", dto.id(), dto.sectorCode());
                    continue;
                }

                boolean exists = parkingSpotRepository.existsBySectorAndLatAndLng(
                        sector, dto.lat(), dto.lng()
                );

                if (!exists) {
                    ParkingSpot spot = new ParkingSpot();
                    spot.setSector(sector);
                    spot.setLat(dto.lat());
                    spot.setLng(dto.lng());
                    spot.setPhysicallyOccupied(false);

                    parkingSpotRepository.save(spot);
                    spotsCreated++;
                }
            }

            logger.info("✅ Startup finalizado! Setores: {}, Vagas Novas: {}", sectorCache.size(), spotsCreated);

        } catch (Exception ex) {
            logger.error("❌ FALHA CRÍTICA na inicialização da garagem", ex);
        }
    }
}