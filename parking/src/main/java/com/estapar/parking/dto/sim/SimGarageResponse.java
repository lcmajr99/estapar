package com.estapar.parking.dto.sim;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SimGarageResponse(
        List<SimSectorDTO> garage,
        List<SimSpotDTO> spots
) {}