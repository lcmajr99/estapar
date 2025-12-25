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

@Entity
@Table(name = "parking_spots")
@Getter
@NoArgsConstructor
public class ParkingSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sector_id")
    private Sector sector;

    @Column(nullable = false)
    @Setter(AccessLevel.NONE)
    private boolean occupied;

    private Double lat;
    private Double lng;

    public ParkingSpot(Sector sector, Double lat, Double lng) {
        this.sector = sector;
        this.lat = lat;
        this.lng = lng;
        this.occupied = false;
    }

    public void occupy() {
        if (this.occupied) {
            throw new IllegalStateException("Vaga já está ocupada");
        }
        this.occupied = true;
    }

    public void release() {
        this.occupied = false;
    }
}
