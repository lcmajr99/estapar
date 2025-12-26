package com.estapar.parking.controller;

import com.estapar.parking.dto.WebhookEventDTO;
import com.estapar.parking.service.ParkingService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Profile("db")
@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private final ParkingService parkingService;

    public WebhookController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    @Operation(summary = "Recebe eventos ENTRY / PARKED / EXIT do simulador")
    @PostMapping
    public ResponseEntity<Void> receive(@RequestBody WebhookEventDTO event) {
        parkingService.processEvent(event);
        return ResponseEntity.ok().build();
    }
}
