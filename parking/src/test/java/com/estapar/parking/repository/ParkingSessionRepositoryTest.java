package com.estapar.parking.repository;

import com.estapar.parking.domain.ParkingSession;
import com.estapar.parking.domain.Sector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ParkingSessionRepositoryTest {

    @Autowired private ParkingSessionRepository repository;
    @Autowired private TestEntityManager entityManager; // Helper para persistir dados reais no H2

    @Test
    @DisplayName("Deve somar APENAS sessões FINISHED e ignorar as ativas")
    void shouldSumOnlyFinishedSessions() {
        Sector sector = new Sector("A", 100, BigDecimal.TEN);
        entityManager.persist(sector);

        createSession(sector, "ABC-100", ParkingSession.SessionStatus.FINISHED, new BigDecimal("50.00"), LocalDateTime.now().minusDays(1));

        createSession(sector, "ABC-200", ParkingSession.SessionStatus.FINISHED, new BigDecimal("30.00"), LocalDateTime.now());

        createSession(sector, "ABC-300", ParkingSession.SessionStatus.PARKED, null, LocalDateTime.now());

        entityManager.flush();

        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(1);

        BigDecimal total = repository.sumTotalRevenue(start, end);

        assertThat(total).isEqualByComparingTo(new BigDecimal("30.00"));
    }

    @Test
    @DisplayName("Deve retornar Zero se não houver nenhuma sessão no período")
    void shouldReturnZeroIfNoData() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now();

        BigDecimal total = repository.sumTotalRevenue(start, end);

        assertThat(total).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private void createSession(Sector s, String plate, ParkingSession.SessionStatus status, BigDecimal amount, LocalDateTime exitTime) {
        ParkingSession ps = new ParkingSession();
        ps.setLicensePlate(plate);
        ps.setSector(s);
        ps.setEntryTime(LocalDateTime.now().minusHours(2));
        ps.setStatus(status);
        ps.setTotalAmount(amount);
        ps.setAppliedPriceFactor(BigDecimal.ONE);
        if (exitTime != null) ps.setExitTime(exitTime);
        entityManager.persist(ps);
    }
}