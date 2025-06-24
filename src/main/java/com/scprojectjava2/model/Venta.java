package com.scprojectjava2.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "venta")
public class Venta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime fecha;
    
    private double total;
    
    @Column(name = "correo_cliente")
    private String correoCliente;
    
    @Column(name = "nombre_cliente")
    private String nombreCliente;
    
    @Column(name = "usuario_cajero")
    private String usuarioCajero;
    
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<DetalleVenta> detalles;

    public Venta() {
        this.detalles = new java.util.ArrayList<>();
    }

    public Venta(LocalDateTime fecha, double total, String correoCliente) {
        this.fecha = fecha;
        this.total = total;
        this.correoCliente = correoCliente;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getCorreoCliente() {
        return correoCliente;
    }

    public void setCorreoCliente(String correoCliente) {
        this.correoCliente = correoCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getUsuarioCajero() {
        return usuarioCajero;
    }

    public void setUsuarioCajero(String usuarioCajero) {
        this.usuarioCajero = usuarioCajero;
    }

    public List<DetalleVenta> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVenta> detalles) {
        this.detalles = detalles;
    }
}