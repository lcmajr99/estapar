package com.estapar.parking.service;

import com.estapar.parking.dto.WebhookEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class ParkingService {

    private static final Logger logger =
            LoggerFactory.getLogger(ParkingService.class);

    private final EntryEventHandler entryHandler;
    private final ParkedEventHandler parkedHandler;
    private final ExitEventHandler exitHandler;

    public ParkingService(
            EntryEventHandler entryHandler,
            ParkedEventHandler parkedHandler,
            ExitEventHandler exitHandler
    ) {
        this.entryHandler = entryHandler;
        this.parkedHandler = parkedHandler;
        this.exitHandler = exitHandler;
    }

    public void processEvent(WebhookEventDTO event) {

        logger.info("Processando evento de webhook");

        switch (event.getEventType()) {
            case "ENTRY" -> entryHandler.handle(event);
            case "PARKED" -> parkedHandler.handle(event);
            case "EXIT" -> exitHandler.handle(event);
            default -> logger.warn("Evento de webhook desconhecido ignorado");
        }
    }
}

