package com.scprojectjava2.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    private String nombre;

    private String descripcion;

    private double precio;

    private int stock;

    @ManyToOne
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "id_unidad", nullable = false)
    private Unidad unidad;

    private int activo = 1;

    // Nuevos campos
    @Column(name = "iva", nullable = true)
    private Double iva;

    @Column(name = "fecha_vencimiento", nullable = true)
    private LocalDate fechaVencimiento;

    // Constructores
    public Producto() {}

    // Getters y Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Unidad getUnidad() {
        return unidad;
    }

    public void setUnidad(Unidad unidad) {
        this.unidad = unidad;
    }

    public int getActivo() {
        return activo;
    }

    public void setActivo(int activo) {
        this.activo = activo;
    }

    public Double getIva() {
        return iva;
    }

    public void setIva(Double iva) {
        this.iva = iva;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    // Método auxiliar para obtener el precio con IVA
    public Double getPrecioConIva() {
        if (iva != null && iva > 0) {
            return precio + (precio * iva / 100);
        }
        return precio;
    }

    // Método auxiliar para verificar si el producto está vencido (incluye el día actual)
    public boolean isVencido() {
        return fechaVencimiento != null && !fechaVencimiento.isAfter(LocalDate.now());
    }

    // Método auxiliar para verificar si el producto está próximo a vencer (7 días, excluyendo hoy)
    public boolean isProximoAVencer() {
        return fechaVencimiento != null && 
               fechaVencimiento.isAfter(LocalDate.now()) && 
               fechaVencimiento.isBefore(LocalDate.now().plusDays(7));
    }

    // Método auxiliar para verificar si el producto está agotado
    public boolean isAgotado() {
        return stock <= 0;
    }

    // Método auxiliar para verificar si el producto no se puede vender (vencido o agotado)
    public boolean isNoVendible() {
        return isVencido() || isAgotado();
    }
}
