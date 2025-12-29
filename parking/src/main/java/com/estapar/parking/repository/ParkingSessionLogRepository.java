package com.estapar.parking.repository;

import com.estapar.parking.domain.ParkingSessionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParkingSessionLogRepository extends JpaRepository<ParkingSessionLog, Long> {
}