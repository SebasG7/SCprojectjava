package com.scprojectjava2.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "operacion_carga_masiva")
public class OperacionCargaMasiva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "fecha_operacion", nullable = false)
    private LocalDateTime fechaOperacion;

    @Column(name = "usuario", nullable = false, length = 100)
    private String usuario;

    @Column(name = "total_productos", nullable = false)
    private Integer totalProductos;

    @Column(name = "productos_exitosos", nullable = false)
    private Integer productosExitosos;

    @Column(name = "productos_fallidos", nullable = false)
    private Integer productosFallidos;

    @Column(name = "estado", nullable = false, length = 50)
    private String estado; // COMPLETADO, PARCIAL, FALLIDO

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "revertida", nullable = false)
    private Boolean revertida = false;

    @Column(name = "fecha_reversion")
    private LocalDateTime fechaReversion;

    @Column(name = "motivo_reversion", columnDefinition = "TEXT")
    private String motivoReversion;

    @Column(name = "usuario_reversion", length = 100)
    private String usuarioReversion;

    @OneToMany(mappedBy = "operacionCarga", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetalleCargaMasiva> detalles;

    // Constructores
    public OperacionCargaMasiva() {
        this.fechaOperacion = LocalDateTime.now();
    }

    public OperacionCargaMasiva(String nombreArchivo, String usuario, Integer totalProductos) {
        this();
        this.nombreArchivo = nombreArchivo;
        this.usuario = usuario;
        this.totalProductos = totalProductos;
        this.productosExitosos = 0;
        this.productosFallidos = 0;
        this.estado = "EN_PROCESO";
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public LocalDateTime getFechaOperacion() {
        return fechaOperacion;
    }

    public void setFechaOperacion(LocalDateTime fechaOperacion) {
        this.fechaOperacion = fechaOperacion;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public Integer getTotalProductos() {
        return totalProductos;
    }

    public void setTotalProductos(Integer totalProductos) {
        this.totalProductos = totalProductos;
    }

    public Integer getProductosExitosos() {
        return productosExitosos;
    }

    public void setProductosExitosos(Integer productosExitosos) {
        this.productosExitosos = productosExitosos;
    }

    public Integer getProductosFallidos() {
        return productosFallidos;
    }

    public void setProductosFallidos(Integer productosFallidos) {
        this.productosFallidos = productosFallidos;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Boolean getRevertida() {
        return revertida;
    }

    public void setRevertida(Boolean revertida) {
        this.revertida = revertida;
    }

    public LocalDateTime getFechaReversion() {
        return fechaReversion;
    }

    public void setFechaReversion(LocalDateTime fechaReversion) {
        this.fechaReversion = fechaReversion;
    }

    public String getMotivoReversion() {
        return motivoReversion;
    }

    public void setMotivoReversion(String motivoReversion) {
        this.motivoReversion = motivoReversion;
    }

    public String getUsuarioReversion() {
        return usuarioReversion;
    }

    public void setUsuarioReversion(String usuarioReversion) {
        this.usuarioReversion = usuarioReversion;
    }

    public List<DetalleCargaMasiva> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleCargaMasiva> detalles) {
        this.detalles = detalles;
    }

    // Métodos auxiliares
    public void incrementarExitosos() {
        this.productosExitosos++;
    }

    public void incrementarFallidos() {
        this.productosFallidos++;
    }

    public void finalizarOperacion() {
        if (this.productosFallidos == 0) {
            this.estado = "COMPLETADO";
        } else if (this.productosExitosos > 0) {
            this.estado = "PARCIAL";
        } else {
            this.estado = "FALLIDO";
        }
    }

    public Double getPorcentajeExito() {
        if (totalProductos == 0) return 0.0;
        return (productosExitosos.doubleValue() / totalProductos.doubleValue()) * 100;
    }

    // Método para marcar como revertida
    public void marcarComoRevertida(String motivo, String usuario) {
        this.revertida = true;
        this.fechaReversion = LocalDateTime.now();
        this.motivoReversion = motivo;
        this.usuarioReversion = usuario;
        this.estado = "REVERTIDA";
    }
}
