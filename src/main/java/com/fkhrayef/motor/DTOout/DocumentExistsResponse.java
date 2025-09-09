package com.fkhrayef.motor.DTOout;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentExistsResponse {
    private String document_name;
    private boolean exists;
}
