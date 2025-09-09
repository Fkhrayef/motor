package com.fkhrayef.motor.DTOout;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CarDocumentInfoResponse {
    private String carId;
    private String carInfo;
    private String documentName;
    private String hasManual;
}
