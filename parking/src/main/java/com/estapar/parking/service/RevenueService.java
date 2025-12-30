package com.estapar.parking.service;

import com.estapar.parking.dto.RevenueDTO;
import com.estapar.parking.repository.ParkingSessionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class RevenueService {

    private final ParkingSessionRepository sessionRepository;

    public RevenueService(ParkingSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public RevenueDTO calculateRevenueByDate(
            LocalDate date,
            String sector
    ) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        BigDecimal amount =
                (sector != null && !sector.isBlank())
                        ? sessionRepository
                        .sumRevenueBySectorAndPeriod(
                                sector,
                                start,
                                end
                        )
                        : sessionRepository
                        .sumTotalRevenue(start, end);

        return new RevenueDTO(
                amount != null ? amount : BigDecimal.ZERO,
                start
        );
    }
}
