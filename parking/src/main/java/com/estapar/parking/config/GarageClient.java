package com.estapar.parking.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.estapar.parking.dto.GarageResponse;

@Component
public class GarageClient {

    private final RestTemplate restTemplate;
    private final String garageUrl;

    public GarageClient(
            RestTemplate restTemplate,
            @Value("${garage.sim.url}") String garageUrl) {
        this.restTemplate = restTemplate;
        this.garageUrl = garageUrl;
    }

    public GarageResponse fetchGarage() {
        return restTemplate.getForObject(
                garageUrl + "/garage",
                GarageResponse.class);
    }
}
