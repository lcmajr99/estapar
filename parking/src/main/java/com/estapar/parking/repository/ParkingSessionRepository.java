package com.estapar.parking.repository;

import com.estapar.parking.domain.ParkingSession;
import com.estapar.parking.domain.Sector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface ParkingSessionRepository extends JpaRepository<ParkingSession, Long> {

    Optional<ParkingSession> findByLicensePlateAndExitTimeIsNull(String licensePlate);

    @Query("""
        select count(ps)
        from ParkingSession ps
        where ps.sector = :sector
          and ps.exitTime is null
    """)
    long countActiveBySector(@Param("sector") Sector sector);

    Optional<ParkingSession> findByLicensePlateIgnoreCaseAndExitTimeIsNull(String licencePlate);

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM ParkingSession s " +
            "WHERE s.status = 'FINISHED' AND s.exitTime BETWEEN :start AND :end")
    BigDecimal sumTotalRevenue(@Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end);

}
