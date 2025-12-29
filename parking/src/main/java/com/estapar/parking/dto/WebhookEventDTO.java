package com.estapar.parking.dto;

import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
public class WebhookEventDTO {

    private String license_plate;
    private String event_type;

    private LocalDateTime entry_time;
    private LocalDateTime exit_time;

    private Double lat;
    private Double lng;

    public String getLicense_plate() {
        return license_plate;
    }

    public String getEvent_type() {
        return event_type;
    }

    public LocalDateTime getEntry_time() {
        return entry_time;
    }

    public LocalDateTime getExit_time() {
        return exit_time;
    }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }

    public void setLng(Double lng) { this.lng = lng; }

    public void setExit_time(LocalDateTime exit_time) {
        this.exit_time = exit_time;
    }

    public void setEntry_time(LocalDateTime entry_time) {
        this.entry_time = entry_time;
    }

    public void setEvent_type(String event_type) {
        this.event_type = event_type;
    }

    public void setLicense_plate(String license_plate) {
        this.license_plate = license_plate;
    }

    public WebhookEventDTO(String license_plate, LocalDateTime entry_time, LocalDateTime exit_time, String event_type, Double lat, Double lng) {
        this.lng = lng;
        this.lat = lat;
        this.exit_time = exit_time;
        this.entry_time = entry_time;
        this.event_type = event_type;
        this.license_plate = license_plate;
    }
}
