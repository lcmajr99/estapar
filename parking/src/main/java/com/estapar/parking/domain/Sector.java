package com.estapar.parking.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.util.Objects;

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
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Sector sector = (Sector) o;
        return getCode() != null && Objects.equals(getCode(), sector.getCode());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(code);
    }
}