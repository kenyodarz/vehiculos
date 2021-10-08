package com.bykenyodarz.vehiculos.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class Vehiculo {
    private String tipo;
    private String marca;
    private String referencia;
    private Integer modelo;
    private Double precio;
    private String numPuertas;
    private String color;
    private String transmission;
    private String cc;
    private Double impuesto;
    private String estado;
}
