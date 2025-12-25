package com.estapar.parking.repository;

import com.estapar.parking.domain.ParkingSpot;
import com.estapar.parking.domain.Sector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {

    List<ParkingSpot> findBySectorAndOccupiedFalse(Sector sector);

    boolean existsBySectorAndLatAndLng(Sector sector, Double lat, Double lng);
}
