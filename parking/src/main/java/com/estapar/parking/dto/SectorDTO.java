package com.estapar.parking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SectorDTO {

    private String sector;

    @JsonProperty("base_price")
    private Double basePrice;

    @JsonProperty("max_capacity")
    private Integer maxCapacity;

    @JsonProperty("open_hour")
    private String openHour;

    @JsonProperty("close_hour")
    private String closeHour;

    @JsonProperty("duration_limit_minutes")
    private Integer durationLimitMinutes;
}
