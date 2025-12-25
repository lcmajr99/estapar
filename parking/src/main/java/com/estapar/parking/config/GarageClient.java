package com.estapar.parking.config;

import com.estapar.parking.dto.GarageResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GarageClient {

    private static final String GARAGE_URL = "http://localhost:3000/garage";

    private final RestTemplate restTemplate;

    public GarageClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public GarageResponse fetchGarage() {
        return restTemplate.getForObject(GARAGE_URL, GarageResponse.class);
    }
}
