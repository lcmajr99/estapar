package com.estapar.parking.controller;

import com.estapar.parking.repository.ParkingSessionRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/revenue")
public class RevenueController {


    private final ParkingSessionRepository sessionRepository;

    public RevenueController(ParkingSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Operation(summary = "Retorna o faturamento total em um período")
    @GetMapping
    public ResponseEntity<RevenueDTO> getRevenue(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now();

        LocalDateTime start = targetDate.atStartOfDay();
        LocalDateTime end = targetDate.atTime(LocalTime.MAX);

        BigDecimal total = sessionRepository.sumTotalRevenue(start, end);

        return ResponseEntity.ok(new RevenueDTO(targetDate, total));
    }

    public record RevenueDTO(LocalDate date, BigDecimal totalAmount) {}
}