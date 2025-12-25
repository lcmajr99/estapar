package com.estapar.parking.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SectorDTO {

    private String sector;
    private Double base_price;
    private Integer max_capacity;
    private String open_hour;
    private String close_hour;
    private Integer duration_limit_minutes;

}
