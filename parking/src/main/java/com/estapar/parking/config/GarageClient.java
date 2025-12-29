package com.estapar.parking.config;

import com.estapar.parking.dto.sim.SimGarageResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GarageClient {

    private final RestTemplate restTemplate;
    private final String garageUrl;

    public GarageClient(RestTemplate restTemplate,
                        @Value("${garage.sim.url}") String garageUrl) {
        this.restTemplate = restTemplate;
        this.garageUrl = garageUrl;
    }

    public SimGarageResponse fetchGarage() {
        return restTemplate.getForObject(
                garageUrl + "/garage",
                SimGarageResponse.class
        );
    }
}