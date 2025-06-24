package com.scprojectjava2.model;

import jakarta.persistence.*;

@Entity
@Table(name = "unidades")
public class Unidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String abreviatura;

    @Column(nullable = false)
    private Integer activo = 1;

    // Constructores
    public Unidad() {
    }
    
    public Unidad(Integer id) {
        this.id = id;
    }
    
    public Unidad(Integer id, String nombre, String abreviatura, Integer activo) {
        this.id = id;
        this.nombre = nombre;
        this.abreviatura = abreviatura;
        this.activo = activo;
    }
    
    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getAbreviatura() {
        return abreviatura;
    }

    public void setAbreviatura(String abreviatura) {
        this.abreviatura = abreviatura;
    }

    public Integer getActivo() {
        return activo;
    }

    public void setActivo(Integer activo) {
        this.activo = activo;
    }
}
