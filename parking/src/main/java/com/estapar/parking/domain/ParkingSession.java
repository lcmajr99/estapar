package com.estapar.parking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "parking_sessions")
@Getter
@NoArgsConstructor
public class ParkingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false)
    private String licensePlate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sector_id")
    private Sector sector;

    @ManyToOne(optional = false)
    @JoinColumn(name = "parking_spot_id")
    private ParkingSpot parkingSpot;

    @Column(nullable = false)
    private OffsetDateTime entryTime;

    private OffsetDateTime exitTime;

    private Double pricePerHour;

    private Double totalAmount;

    public ParkingSession(
            String licensePlate,
            Sector sector,
            ParkingSpot parkingSpot,
            OffsetDateTime entryTime,
            Double pricePerHour
    ) {
        this.licensePlate = licensePlate;
        this.sector = sector;
        this.parkingSpot = parkingSpot;
        this.entryTime = entryTime;
        this.pricePerHour = pricePerHour;
    }

    public boolean isActive() {
        return exitTime == null;
    }

    public void finish(OffsetDateTime exitTime, Double totalAmount) {
        this.exitTime = exitTime;
        this.totalAmount = totalAmount;
    }
}
