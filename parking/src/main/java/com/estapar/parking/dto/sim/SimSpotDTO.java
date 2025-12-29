package com.estapar.parking.dto.sim;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SimSpotDTO(
        Long id,
        @JsonAlias("sector") String sectorCode,
        Double lat,
        @JsonAlias("lng") Double lng
) {}