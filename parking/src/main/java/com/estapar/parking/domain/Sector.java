package com.estapar.parking.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Table(name = "sectors")
public class Sector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(nullable = false)
    private Integer maxCapacity;

    @Column(nullable = false)
    private Integer occupiedCount = 0;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    public Sector(String code, Integer maxCapacity, BigDecimal basePrice) {
        this.code = code;
        this.maxCapacity = maxCapacity;
        this.basePrice = basePrice;
    }

    public void incrementOccupancy() {
        this.occupiedCount++;
    }

    public void decrementOccupancy() {
        if (this.occupiedCount > 0) this.occupiedCount--;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sector other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}