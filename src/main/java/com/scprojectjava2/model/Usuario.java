package com.scprojectjava2.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
      @Column(name = "nombre_usuario", unique = true, nullable = false)
    private String nombreUsuario;
    
    @Column(name = "contrasena", nullable = false)
    private String contrasena;
    
    @Column(name = "nombre", nullable = false)
    private String nombre;
      @Column(name = "activo")
    private int activo = 1;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.CAJERO; // Por defecto es cajero
    
    // Constructores
    public Usuario() {}
    
    public Usuario(int id) {
        this.id = id;
    }    public Usuario(String nombreUsuario, String contrasena, String nombre) {
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.nombre = nombre;
        this.activo = 1;
        this.role = Role.CAJERO; // Por defecto es cajero
    }
    
    public Usuario(String nombreUsuario, String contrasena, String nombre, Role role) {
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.nombre = nombre;
        this.activo = 1;
        this.role = role;
    }
    
    // Getters y Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getNombreUsuario() {
        return nombreUsuario;
    }
    
    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }
      public String getContrasena() {
        return contrasena;
    }
    
    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public int getActivo() {
        return activo;
    }
      public void setActivo(int activo) {
        this.activo = activo;
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
      // Métodos de conveniencia para verificar roles
    public boolean isAdministrador() {
        return Role.ADMINISTRADOR.equals(this.role);
    }
    
    public boolean isCajero() {
        return Role.CAJERO.equals(this.role);
    }
    
    public boolean isCliente() {
        // En este sistema no hay rol cliente, siempre devuelve false
        return false;
    }@Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nombreUsuario='" + nombreUsuario + '\'' +
                ", nombre='" + nombre + '\'' +
                ", role=" + role +
                ", activo=" + activo +
                '}';
    }
}
