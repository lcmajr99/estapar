package com.estapar.parking.repository;

import com.estapar.parking.domain.ParkingSpot;
import com.estapar.parking.domain.Sector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {


    boolean existsBySectorAndLatAndLng(Sector sector, Double lat, Double lng);

    Optional<ParkingSpot> findByLatAndLng(Double lat, Double lng);


}
