package com.scprojectjava2.model;

import jakarta.persistence.*;

@Entity
@Table(name = "categoria_creada_carga_masiva")
public class CategoriaCreadaCargaMasiva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operacion_carga_id", nullable = false)
    private OperacionCargaMasiva operacionCarga;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(name = "revertida", nullable = false)
    private Boolean revertida = false;

    // Constructores
    public CategoriaCreadaCargaMasiva() {}

    public CategoriaCreadaCargaMasiva(OperacionCargaMasiva operacionCarga, Categoria categoria) {
        this.operacionCarga = operacionCarga;
        this.categoria = categoria;
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

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
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
