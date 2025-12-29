package com.estapar.parking.controller;

import com.estapar.parking.dto.RevenueDTO;
import com.estapar.parking.service.ParkingService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class RevenueController {

    private final ParkingService parkingService;

    public RevenueController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    @GetMapping("/revenue")
    public RevenueDTO getRevenue(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam(required = false)
            String sector
    ) {
        return parkingService.calculateRevenueByDate(date, sector);
    }
}
