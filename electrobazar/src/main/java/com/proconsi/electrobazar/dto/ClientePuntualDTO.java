package com.proconsi.electrobazar.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClientePuntualDTO {

    @NotBlank
    private String nombre;

    @NotBlank
    private String nif;

    @NotBlank
    private String direccion;

    @NotBlank
    private String codigoPostal;

    @NotBlank
    private String ciudad;
}
