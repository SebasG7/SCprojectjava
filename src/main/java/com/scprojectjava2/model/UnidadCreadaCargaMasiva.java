package com.scprojectjava2.model;

import jakarta.persistence.*;

@Entity
@Table(name = "unidad_creada_carga_masiva")
public class UnidadCreadaCargaMasiva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operacion_carga_id", nullable = false)
    private OperacionCargaMasiva operacionCarga;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id", nullable = false)
    private Unidad unidad;

    @Column(name = "revertida", nullable = false)
    private Boolean revertida = false;

    // Constructores
    public UnidadCreadaCargaMasiva() {}

    public UnidadCreadaCargaMasiva(OperacionCargaMasiva operacionCarga, Unidad unidad) {
        this.operacionCarga = operacionCarga;
        this.unidad = unidad;
        this.revertida = false;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public OperacionCargaMasiva getOperacionCarga() {
        return operacionCarga;
    }

    public void setOperacionCarga(OperacionCargaMasiva operacionCarga) {
        this.operacionCarga = operacionCarga;
    }

    public Unidad getUnidad() {
        return unidad;
    }

    public void setUnidad(Unidad unidad) {
        this.unidad = unidad;
    }

    public Boolean getRevertida() {
        return revertida;
    }

    public void setRevertida(Boolean revertida) {
        this.revertida = revertida;
    }

    public void marcarComoRevertida() {
        this.revertida = true;
    }
}
