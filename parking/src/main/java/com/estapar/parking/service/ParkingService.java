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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class ParkingService {

    private static final Logger logger = LoggerFactory.getLogger(ParkingService.class);
    private static final long FREE_MINUTES = 30;

    private final ParkingSessionLogRepository logRepository; // <--- NOVO
    private final ParkingSessionRepository sessionRepository;
    private final ParkingSpotRepository parkingSpotRepository;
    private final SectorRepository sectorRepository;

    public ParkingService(
            ParkingSessionRepository sessionRepository,
            ParkingSpotRepository parkingSpotRepository,
            SectorRepository sectorRepository,
            ParkingSessionLogRepository logRepository
    ) {
        this.sessionRepository = sessionRepository;
        this.parkingSpotRepository = parkingSpotRepository;
        this.sectorRepository = sectorRepository;
        this.logRepository = logRepository;

    }

    /**
     * Roteador de Eventos
     */
    @Transactional
    public void processEvent(WebhookEventDTO event) {
        logger.info("Processando evento: {} - Placa: {}", event.getEvent_type(), event.getLicense_plate());

        switch (event.getEvent_type()) {
            case "ENTRY" -> handleEntry(event);
            case "PARKED" -> handleParked(event);
            case "EXIT" -> handleExit(event);
            default -> logger.warn("Evento desconhecido ignorado: {}", event.getEvent_type());
        }
    }

    /**
     * Lógica de ENTRADA (Mundo Lógico)
     * - Garante que há capacidade no setor.
     * - Define o preço dinâmico.
     * - Cria a sessão SEM vaga física definida.
     */
    private void handleEntry(WebhookEventDTO event) {
        if (sessionRepository.findByLicensePlateIgnoreCaseAndExitTimeIsNull(event.getLicense_plate()).isPresent()) {
            throw new IllegalStateException("Veículo já possui sessão ativa");
        }
        List<Sector> sectors = sectorRepository.findAll();
        Sector selectedSector = null;

        for (Sector sector : sectors) {
            if (sector.getOccupiedCount() < sector.getMaxCapacity()) {
                selectedSector = sector;
                break; // Achou um setor com vaga
            }
        }

        if (selectedSector == null) {
            throw new IllegalStateException("Estacionamento Lotado (Full)");
        }

        BigDecimal priceFactor = calculateDynamicPriceFactor(selectedSector);

        selectedSector.incrementOccupancy();
        sectorRepository.save(selectedSector);

        ParkingSession session = new ParkingSession();
        session.setLicensePlate(event.getLicense_plate());
        session.setSector(selectedSector);
        session.setEntryTime(event.getEntry_time());
        session.setAppliedPriceFactor(priceFactor);
        session.setStatus(ParkingSession.SessionStatus.ENTERING);

        sessionRepository.save(session);
        saveLog(session, ParkingSessionLog.LogType.ENTRY_CREATED,
                "Entrada no Setor " + selectedSector.getCode() + ". Fator Preço: " + priceFactor);
        logger.info("Entrada registrada. Setor: {} | Fator Preço: {}", selectedSector.getCode(), priceFactor);
    }

    /**
     * Lógica de ESTACIONAMENTO (Mundo Físico)
     * - Descobre onde o carro parou via GPS (Lat/Lng).
     * - Vincula a vaga física à sessão.
     */
    @Transactional
    private void handleParked(WebhookEventDTO event) {
        ParkingSession session = sessionRepository
                .findByLicensePlateIgnoreCaseAndExitTimeIsNull(event.getLicense_plate().trim())
                .orElseThrow(() -> new IllegalStateException("Evento PARKED sem sessão de ENTRY ativa"));

        ParkingSpot realSpot = parkingSpotRepository
                .findByLatAndLng(event.getLat(), event.getLng())
                .orElseThrow(() -> new IllegalStateException("Carro parou em coordenadas desconhecidas!"));

        Sector predictedSector = session.getSector();
        Sector realSector = realSpot.getSector();

        if (!realSector.equals(predictedSector)) {
            logger.info("RECONCILIAÇÃO: Veículo trocou do Setor {} para o Setor {}",
                    predictedSector.getCode(), realSector.getCode());

            predictedSector.decrementOccupancy();
            realSector.incrementOccupancy();

            sectorRepository.save(predictedSector);
            sectorRepository.save(realSector);

            session.setSector(realSector);

            BigDecimal newFactor = calculateDynamicPriceFactor(realSector);
            session.setAppliedPriceFactor(newFactor);
            saveLog(session, ParkingSessionLog.LogType.SECTOR_CHANGED,
                    String.format("Cliente trocou do Setor %s para %s. Fator recalculado: %s",
                            predictedSector.getCode(), realSector.getCode(), newFactor));
            logger.info("Preço Recalculado. Novo Fator: {}", newFactor);
        }else{
            ParkingSessionLog log = new ParkingSessionLog();
            log.setSession(session);
            log.setEventTime(LocalDateTime.now());
            log.setType(ParkingSessionLog.LogType.PARKED_CONFIRMED);
            log.setDescription("Estacionamento confirmado no setor correto: " + realSector.getCode());

            logRepository.save(log);
        }

        if (realSpot.isPhysicallyOccupied()) {
            logger.warn("Conflito Físico: Vaga {} já estava marcada como ocupada.", realSpot.getId());
        }
        realSpot.setPhysicallyOccupied(true);
        parkingSpotRepository.save(realSpot);

        session.setParkingSpot(realSpot);
        session.setStatus(ParkingSession.SessionStatus.PARKED);
        sessionRepository.save(session);

    }

    /**
     * Lógica de SAÍDA (Financeiro e Limpeza)
     * - Calcula valor final.
     * - Libera vaga física e ocupação lógica.
     */
    private void handleExit(WebhookEventDTO event) {
        ParkingSession session = sessionRepository
                .findByLicensePlateIgnoreCaseAndExitTimeIsNull(event.getLicense_plate())
                .orElseThrow(() -> new IllegalStateException("Sessão ativa não encontrada para EXIT"));

        if (event.getExit_time().isBefore(session.getEntryTime())) {
            throw new IllegalArgumentException("Data de saída inválida (anterior à entrada)");
        }

        Duration duration = Duration.between(session.getEntryTime(), event.getExit_time());
        long totalMinutes = duration.toMinutes();
        BigDecimal finalAmount = calculateFinalAmount(session, totalMinutes);

        if (session.getParkingSpot() != null) {
            ParkingSpot spot = session.getParkingSpot();
            spot.setPhysicallyOccupied(false);
            parkingSpotRepository.save(spot);
        } else {
            logger.warn("Veículo saiu sem registrar evento PARKED (Uber/Drop-off).");
        }

        Sector sector = session.getSector();
        sector.decrementOccupancy();
        sectorRepository.save(sector);

        session.finish(event.getExit_time(), finalAmount);
        sessionRepository.save(session);
        saveLog(session, ParkingSessionLog.LogType.EXIT_COMPLETED,
                "Sessão encerrada. Valor Final: R$ " + finalAmount + ". Tempo: " + totalMinutes + " min.");
        logger.info("Saída processada. Valor: R$ {}", finalAmount);
    }

    // --- Métodos Auxiliares ---

    private BigDecimal calculateDynamicPriceFactor(Sector sector) {
        if (sector.getMaxCapacity() == 0) return BigDecimal.ONE;

        double ratio = (double) sector.getOccupiedCount() / sector.getMaxCapacity();

        if (ratio < 0.25) return new BigDecimal("0.90"); // -10%
        if (ratio < 0.50) return new BigDecimal("1.00"); // 0%
        if (ratio < 0.75) return new BigDecimal("1.10"); // +10%
        return new BigDecimal("1.25");                   // +25%
    }

    private BigDecimal calculateFinalAmount(ParkingSession session, long totalMinutes) {
        if (totalMinutes <= FREE_MINUTES) {
            return BigDecimal.ZERO;
        }


        long billableHours = (long) Math.ceil(totalMinutes / 60.0);

        BigDecimal basePrice = session.getSector().getBasePrice();
        BigDecimal factor = session.getAppliedPriceFactor();

        BigDecimal pricePerHour = basePrice.multiply(factor);
        BigDecimal total = pricePerHour.multiply(BigDecimal.valueOf(billableHours));

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private void saveLog(ParkingSession session, ParkingSessionLog.LogType type, String description) {
        ParkingSessionLog log = new ParkingSessionLog(session, type, description);
        logRepository.save(log);
    }
}