package com.fkhrayef.motor.DTOout;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentSearchResponse {
    private String query;
    private List<String> documents;
    private int count;
}
