package com.estapar.parking.dto.sim;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SimSectorDTO(
        @JsonAlias("sector") String code, // Mapeia "sector" do JSON para "code" do Java
        @JsonAlias("base_price") Double basePrice,
        @JsonAlias("max_capacity") Integer maxCapacity
) {}