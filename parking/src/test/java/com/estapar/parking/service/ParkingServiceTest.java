package com.estapar.parking.service;

import com.estapar.parking.dto.WebhookEventDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ParkingServiceTest {

    @Mock    private EntryEventHandler entryHandler;
    @Mock private ParkedEventHandler parkedHandler;
    @Mock private ExitEventHandler exitHandler;

    @InjectMocks
    private ParkingService service;

    @Test
    void shouldRouteEntryEvent() {
        WebhookEventDTO event = new WebhookEventDTO();
        event.setEventType("ENTRY");

        service.processEvent(event);

        verify(entryHandler).handle(event);
    }
}