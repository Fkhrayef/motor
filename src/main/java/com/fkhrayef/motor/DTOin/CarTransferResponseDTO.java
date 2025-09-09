package com.fkhrayef.motor.DTOin;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CarTransferResponseDTO {

    private Integer id;
    private String status;
    private Integer carId;
    private Integer fromUserId;
    private Integer toUserId;

}
