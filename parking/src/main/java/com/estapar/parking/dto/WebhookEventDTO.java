package com.estapar.parking.dto;

import java.time.OffsetDateTime;

public class WebhookEventDTO {

    private String license_plate;
    private String event_type;

    private OffsetDateTime entry_time;
    private OffsetDateTime exit_time;

    public String getLicense_plate() {
        return license_plate;
    }

    public String getEvent_type() {
        return event_type;
    }

    public OffsetDateTime getEntry_time() {
        return entry_time;
    }

    public OffsetDateTime getExit_time() {
        return exit_time;
    }
}
