package com.fkhrayef.motor.DTOin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

// TODO: Make this RegisterDTO and make new UserDTO with more attributes!

@Getter
@Setter
@AllArgsConstructor
public class UserDTO {
    @NotEmpty(message = "Phone cannot be null")
    @Pattern(regexp = "^(\\+9665\\d{8})$")
    private String phone;

    @NotEmpty(message = "Name cannot be null")
    private String name;

    @NotEmpty(message = "Email cannot be null")
    @Email(message = "Email is not valid")
    private String email;

    @NotEmpty(message = "Password cannot be null")
    private String password;

    @NotEmpty(message = "City cannot be null")
    private String city;
}
