package com.estapar.parking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
public class WebhookEventDTO {

    @JsonProperty("license_plate")
    private String licensePlate;

    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("entry_time")
    private LocalDateTime entryTime;

    @JsonProperty("exit_time")
    private LocalDateTime exitTime;

    private Double lat;
    private Double lng;

    /* ---------- getters ---------- */

    public String getLicensePlate() {
        return licensePlate;
    }

    public String getEventType() {
        return eventType;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    /* ---------- setters ---------- */

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }

    public void setExitTime(LocalDateTime exitTime) {
        this.exitTime = exitTime;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }


    public WebhookEventDTO(
            String licensePlate,
            LocalDateTime entryTime,
            LocalDateTime exitTime,
            String eventType,
            Double lat,
            Double lng
    ) {
        this.licensePlate = licensePlate;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.eventType = eventType;
        this.lat = lat;
        this.lng = lng;
    }
}
