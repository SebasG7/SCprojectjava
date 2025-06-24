package com.scprojectjava2.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "detalle_carga_masiva")
public class DetalleCargaMasiva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_operacion_carga", nullable = false)
    private OperacionCargaMasiva operacionCarga;

    @Column(name = "linea_archivo", nullable = false)
    private Integer lineaArchivo;

    @Column(name = "codigo_producto", length = 50)
    private String codigoProducto;

    @Column(name = "nombre_producto", length = 255)
    private String nombreProducto;

    @ManyToOne
    @JoinColumn(name = "id_producto")
    private Producto producto; // Solo se llena si el producto se creó/actualizó exitosamente

    @Column(name = "estado", nullable = false, length = 50)
    private String estado; // EXITOSO, FALLIDO

    @Column(name = "mensaje_error", columnDefinition = "TEXT")
    private String mensajeError;

    @Column(name = "accion_realizada", length = 50)
    private String accionRealizada; // CREADO, ACTUALIZADO

    @Column(name = "revertido", nullable = false)
    private Boolean revertido = false;

    @Column(name = "fecha_reversion")
    private LocalDateTime fechaReversion;

    // Constructores
    public DetalleCargaMasiva() {}

    public DetalleCargaMasiva(OperacionCargaMasiva operacionCarga, Integer lineaArchivo, 
                             String codigoProducto, String nombreProducto) {
        this.operacionCarga = operacionCarga;
        this.lineaArchivo = lineaArchivo;
        this.codigoProducto = codigoProducto;
        this.nombreProducto = nombreProducto;
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

    public Integer getLineaArchivo() {
        return lineaArchivo;
    }

    public void setLineaArchivo(Integer lineaArchivo) {
        this.lineaArchivo = lineaArchivo;
    }

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public void setCodigoProducto(String codigoProducto) {
        this.codigoProducto = codigoProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMensajeError() {
        return mensajeError;
    }

    public void setMensajeError(String mensajeError) {
        this.mensajeError = mensajeError;
    }

    public String getAccionRealizada() {
        return accionRealizada;
    }

    public void setAccionRealizada(String accionRealizada) {
        this.accionRealizada = accionRealizada;
    }

    public Boolean getRevertido() {
        return revertido;
    }

    public void setRevertido(Boolean revertido) {
        this.revertido = revertido;
    }

    public LocalDateTime getFechaReversion() {
        return fechaReversion;
    }

    public void setFechaReversion(LocalDateTime fechaReversion) {
        this.fechaReversion = fechaReversion;
    }

    // Métodos auxiliares
    public void marcarComoExitoso(String accion, Producto producto) {
        this.estado = "EXITOSO";
        this.accionRealizada = accion;
        this.producto = producto;
        this.mensajeError = null;
    }

    public void marcarComoFallido(String error) {
        this.estado = "FALLIDO";
        this.mensajeError = error;
        this.accionRealizada = null;
        this.producto = null;
    }

    public void marcarComoRevertido() {
        this.revertido = true;
        this.fechaReversion = LocalDateTime.now();
        if ("CREADO".equals(this.accionRealizada)) {
            this.accionRealizada = "REVERTIDO (ELIMINADO)";
        } else if ("ACTUALIZADO".equals(this.accionRealizada)) {
            this.accionRealizada = "REVERTIDO (RESTAURADO)";
        }
    }
}
