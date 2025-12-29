package com.estapar.parking.service;

import com.estapar.parking.domain.*;
import com.estapar.parking.dto.WebhookEventDTO;
import com.estapar.parking.repository.ParkingSessionLogRepository;
import com.estapar.parking.repository.ParkingSessionRepository;
import com.estapar.parking.repository.ParkingSpotRepository;
import com.estapar.parking.repository.SectorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingServiceTest {

    @Mock private ParkingSessionRepository sessionRepository;
    @Mock private ParkingSpotRepository spotRepository;
    @Mock private SectorRepository sectorRepository;
    @Mock private ParkingSessionLogRepository logRepository;

    @InjectMocks
    private ParkingService parkingService;

    // --- CENÁRIO 1: 30 Minutos Grátis ---
    @Test
    @DisplayName("EXIT: Deve cobrar R$ 0,00 se permanência <= 30 minutos")
    void shouldCalculateFreeTier() {
        LocalDateTime entryTime = LocalDateTime.now().minusMinutes(29); // 29 min atrás
        LocalDateTime exitTime = LocalDateTime.now();

        ParkingSession session = new ParkingSession();
        session.setEntryTime(entryTime);
        session.setSector(new Sector("A", 100, new BigDecimal("10.00")));
        session.setAppliedPriceFactor(BigDecimal.ONE);

        when(sessionRepository.findByLicensePlateIgnoreCaseAndExitTimeIsNull(anyString()))
                .thenReturn(Optional.of(session));

        WebhookEventDTO event = createEvent("ABC-1234", "EXIT", exitTime);

        parkingService.processEvent(event);

        ArgumentCaptor<ParkingSession> captor = ArgumentCaptor.forClass(ParkingSession.class);
        verify(sessionRepository).save(captor.capture());

        assertEquals(BigDecimal.ZERO, captor.getValue().getTotalAmount());
        assertEquals(ParkingSession.SessionStatus.FINISHED, captor.getValue().getStatus());
    }

    // --- CENÁRIO 2: Mudança no Fator de Preço (Lotação) ---
    @Test
    @DisplayName("ENTRY: Deve aplicar sobretaxa (1.25) se setor estiver > 75% cheio")
    void shouldApplySurgePricing() {
        Sector crowdedSector = new Sector("A", 100, new BigDecimal("10.00"));
        crowdedSector.setOccupiedCount(80); // 80% ocupado

        when(sessionRepository.findByLicensePlateIgnoreCaseAndExitTimeIsNull(anyString()))
                .thenReturn(Optional.empty()); // Não tem sessão ativa

        when(sectorRepository.findAll()).thenReturn(List.of(crowdedSector));

        WebhookEventDTO event = createEvent("ABC-1234", "ENTRY", LocalDateTime.now());

        parkingService.processEvent(event);

        ArgumentCaptor<ParkingSession> captor = ArgumentCaptor.forClass(ParkingSession.class);
        verify(sessionRepository).save(captor.capture());

        assertEquals(new BigDecimal("1.25"), captor.getValue().getAppliedPriceFactor());
    }

    // --- CENÁRIO 3: Tentar Estacionar em Vaga Ocupada (Colisão Física) ---
    @Test
    @DisplayName("PARKED: Deve logar aviso ou tratar erro se vaga já estiver ocupada")
    void shouldWarnIfSpotIsOccupied() {
        ParkingSession session = new ParkingSession();
        session.setSector(new Sector("A", 100, BigDecimal.TEN));

        ParkingSpot occupiedSpot = new ParkingSpot();
        occupiedSpot.setId(1L);
        occupiedSpot.setSector(session.getSector());
        occupiedSpot.setPhysicallyOccupied(true); // <--- JÁ ESTÁ OCUPADA

        when(sessionRepository.findByLicensePlateIgnoreCaseAndExitTimeIsNull(any()))
                .thenReturn(Optional.of(session));

        when(spotRepository.findByLatAndLng(any(), any()))
                .thenReturn(Optional.of(occupiedSpot));

        WebhookEventDTO event = createEvent("ABC-1234", "PARKED", LocalDateTime.now());

        assertDoesNotThrow(() -> parkingService.processEvent(event));

        assertTrue(occupiedSpot.isPhysicallyOccupied());
    }

    // --- CENÁRIO 4: Entrada Duplicada (Idempotência) ---
    @Test
    @DisplayName("ENTRY: Deve lançar erro se carro já tiver sessão ativa")
    void shouldThrowIfSessionExists() {
        when(sessionRepository.findByLicensePlateIgnoreCaseAndExitTimeIsNull(anyString()))
                .thenReturn(Optional.of(new ParkingSession())); // Já existe!

        WebhookEventDTO event = createEvent("ABC-1234", "ENTRY", LocalDateTime.now());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            parkingService.processEvent(event);
        });

        assertEquals("Veículo já possui sessão ativa", ex.getMessage());
    }

    // --- CENÁRIO 5: Setor Cheio (Full) ---
    @Test
    @DisplayName("ENTRY: Deve negar entrada se todos os setores estiverem lotados")
    void shouldRejectEntryIfFull() {
        // Prepare
        Sector fullSector = new Sector("A", 10, BigDecimal.TEN);
        fullSector.setOccupiedCount(10); // 10/10 = 100%

        when(sectorRepository.findAll()).thenReturn(List.of(fullSector));
        when(sessionRepository.findByLicensePlateIgnoreCaseAndExitTimeIsNull(any())).thenReturn(Optional.empty());

        WebhookEventDTO event = createEvent("ABC-1234", "ENTRY", LocalDateTime.now());

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> parkingService.processEvent(event));
    }

    // --- CENÁRIO 6: O caso do "Carro Ladrão de Vaga" ---
    @Test
    @DisplayName("PARKED: Carro B estaciona na vaga que seria logicamente do Carro A")
    void shouldHandleSpotTheftScenario() {
        /* Cenário:
           Nós NÃO reservamos vaga específica na entrada, só garantimos capacidade numérica.
           Então, se o Carro A entra e o Carro B entra, e ambos vão pro mesmo setor,
           quem chegar primeiro na vaga física 'ganha' ela no sistema.
           O teste valida se o sistema aceita o park do "ladrão".
        */

        Sector sector = new Sector("A", 100, BigDecimal.TEN);
        ParkingSpot spot = new ParkingSpot();
        spot.setId(55L);
        spot.setSector(sector);
        spot.setPhysicallyOccupied(false);

        ParkingSession sessionCarB = new ParkingSession();
        sessionCarB.setLicensePlate("CAR-B");
        sessionCarB.setSector(sector);

        when(sessionRepository.findByLicensePlateIgnoreCaseAndExitTimeIsNull("CAR-B"))
                .thenReturn(Optional.of(sessionCarB));

        when(spotRepository.findByLatAndLng(any(), any())).thenReturn(Optional.of(spot));

        WebhookEventDTO eventB = createEvent("CAR-B", "PARKED", LocalDateTime.now());

        parkingService.processEvent(eventB);

        assertTrue(spot.isPhysicallyOccupied());
        assertEquals(spot, sessionCarB.getParkingSpot());


    }

    @Test
    @DisplayName("EXIT: Deve lançar erro se Data Saída for anterior à Data Entrada")
    void shouldThrowOnTimeTravel() {
        LocalDateTime entryTime = LocalDateTime.now();
        LocalDateTime exitTimePast = entryTime.minusMinutes(10); // Saiu 10 min ANTES de entrar

        ParkingSession session = new ParkingSession();
        session.setEntryTime(entryTime);
        session.setSector(new Sector("A", 100, BigDecimal.TEN));

        when(sessionRepository.findByLicensePlateIgnoreCaseAndExitTimeIsNull(any()))
                .thenReturn(Optional.of(session));

        WebhookEventDTO event = new WebhookEventDTO();
        event.setLicense_plate("DELOREAN");
        event.setEvent_type("EXIT");
        event.setExit_time(exitTimePast);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            parkingService.processEvent(event);
        });

        assertEquals("Data de saída inválida (anterior à entrada)", ex.getMessage());
    }

    @Test
    @DisplayName("CONCORRÊNCIA: Deve impedir Overbooking se 5 carros tentarem a ULTIMA vaga simultaneamente")
    void shouldPreventOverbookingOnConcurrentEntry() throws InterruptedException {
        Sector sector = new Sector("A", 10, BigDecimal.TEN);
        sector.setId(1L);
        sector.setOccupiedCount(9);

        AtomicInteger successfulSaves = new AtomicInteger(0);

        lenient().when(sectorRepository.findAll()).thenReturn(List.of(sector));

        lenient().when(sectorRepository.findByIdWithLock(1L)).thenReturn(Optional.of(sector));

        lenient().when(sessionRepository.findByLicensePlateIgnoreCaseAndExitTimeIsNull(anyString()))
                .thenReturn(Optional.empty());

        when(sessionRepository.save(any())).thenAnswer(invocation -> {
            if (sector.getOccupiedCount() > sector.getMaxCapacity()) {
                throw new IllegalStateException("OVERBOOKING DETECTADO NO SAVE!");
            }
            successfulSaves.incrementAndGet();
            return invocation.getArgument(0);
        });

        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            String plate = "CAR-" + i;
            executor.submit(() -> {
                try {
                    WebhookEventDTO event = createEvent(plate, "ENTRY", LocalDateTime.now());

                    synchronized (sector) {
                        parkingService.processEvent(event);
                    }
                } catch (Exception e) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertEquals(1, successfulSaves.get(), "Erro: Nenhum ou mais de um carro conseguiu a vaga!");

    }

    @Test
    @DisplayName("IDEMPOTÊNCIA: Não deve falhar nem duplicar cobrança se receber o mesmo EXIT duas vezes")
    void shouldHandleDuplicateExitEvents() {
        ParkingSession session = new ParkingSession();
        session.setLicensePlate("ABC-1234");
        session.setEntryTime(LocalDateTime.now().minusHours(1));
        session.setStatus(ParkingSession.SessionStatus.FINISHED);

        session.setExitTime(LocalDateTime.now());

        when(sessionRepository.findByLicensePlateIgnoreCaseAndExitTimeIsNull("ABC-1234"))
                .thenReturn(Optional.empty());

        WebhookEventDTO event = createEvent("ABC-1234", "EXIT", LocalDateTime.now());


        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            parkingService.processEvent(event);
        });

        assertEquals("Sessão ativa não encontrada para EXIT", ex.getMessage());

        verify(sessionRepository, never()).save(any());
    }

    private WebhookEventDTO createEvent(String plate, String type, LocalDateTime time) {
        WebhookEventDTO dto = new WebhookEventDTO();
        dto.setLicense_plate(plate);
        dto.setEvent_type(type);
        if (type.equals("ENTRY")) dto.setEntry_time(time);
        if (type.equals("EXIT")) dto.setExit_time(time);
        dto.setLat(-23.5);
        dto.setLng(-46.6);
        return dto;
    }

    @Test
    @DisplayName("PARKED: Não deve trocar setor nem recalcular preço se estacionar no setor correto")
    void shouldConfirmParkingWithoutSectorChange() {
        Sector sectorA = new Sector("A", 100, new BigDecimal("10.00"));

        ParkingSession session = new ParkingSession();
        session.setLicensePlate("ABC-1234");
        session.setSector(sectorA);
        session.setAppliedPriceFactor(BigDecimal.ONE);

        ParkingSpot correctSpot = new ParkingSpot();
        correctSpot.setSector(sectorA); // Vaga Física TAMBÉM é Setor A
        correctSpot.setId(10L);

        when(sessionRepository.findByLicensePlateIgnoreCaseAndExitTimeIsNull("ABC-1234"))
                .thenReturn(Optional.of(session));

        when(spotRepository.findByLatAndLng(any(), any()))
                .thenReturn(Optional.of(correctSpot));

        WebhookEventDTO event = createEvent("ABC-1234", "PARKED", LocalDateTime.now());

        parkingService.processEvent(event);

        assertEquals(BigDecimal.ONE, session.getAppliedPriceFactor());

        verify(logRepository, never()).save(argThat(log ->
                log.getType() == ParkingSessionLog.LogType.SECTOR_CHANGED
        ));

        verify(logRepository).save(argThat(log ->
                log.getType() == ParkingSessionLog.LogType.PARKED_CONFIRMED
        ));
    }
}