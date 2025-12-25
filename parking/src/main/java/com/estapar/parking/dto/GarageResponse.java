package com.estapar.parking.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GarageResponse {

    private List<SectorDTO> garage;
    private List<ParkingSpotDTO> spots;

}
