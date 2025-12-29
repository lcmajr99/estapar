package com.estapar.parking.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Table(name = "parking_session_logs")
public class ParkingSessionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    @ToString.Exclude
    private ParkingSession session;

    @Column(nullable = false)
    private LocalDateTime eventTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LogType type;

    @Column(length = 500) // Texto livre para detalhes (ex: "Mudou do Setor A para B")
    private String description;

    public enum LogType {
        ENTRY_CREATED,
        PARKED_CONFIRMED,
        SECTOR_CHANGED, // O famoso Swap
        EXIT_COMPLETED
    }

    // Construtor utilitário
    public ParkingSessionLog(ParkingSession session, LogType type, String description) {
        this.session = session;
        this.eventTime = LocalDateTime.now();
        this.type = type;
        this.description = description;
    }
}