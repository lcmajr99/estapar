package com.estapar.parking.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParkingSpotDTO {

    private String sector;
    private Double lat;
    private Double lng;
    private boolean occupied;

}
