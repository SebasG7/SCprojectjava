package com.scprojectjava2.service;

import com.scprojectjava2.model.Unidad;
import com.scprojectjava2.repository.UnidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UnidadService {

    @Autowired
    private UnidadRepository unidadRepository;

    public List<Unidad> listar() {
        return unidadRepository.findAllActivos();
    }
    
    public List<Unidad> listarActivas() {
        return unidadRepository.findAllActivos();
    }    public void agregar(Unidad unidad) {
        unidad.setActivo(1);
        unidadRepository.save(unidad);
    }

    public Unidad obtenerPorId(int id) {
        return unidadRepository.findById(id).orElse(null);
    }

    public void actualizar(Unidad unidad) {
        Unidad existente = unidadRepository.findById(unidad.getId()).orElse(null);
        if (existente != null) {
            existente.setNombre(unidad.getNombre());
            existente.setAbreviatura(unidad.getAbreviatura());
            unidadRepository.save(existente);
        }
    }    public void eliminar(int id) {
        Unidad unidad = unidadRepository.findById(id).orElse(null);
        if (unidad != null) {
            unidad.setActivo(0);
            unidadRepository.save(unidad);
        }
    }

    public List<Unidad> listarInactivos() {
        return unidadRepository.findAllInactivos();
    }    public void reactivar(int id) {
        Unidad unidad = unidadRepository.findById(id).orElse(null);
        if (unidad != null) {
            unidad.setActivo(1);
            unidadRepository.save(unidad);
        }
    }    // Método para buscar unidad por nombre
    public Unidad obtenerPorNombre(String nombre) {
        List<Unidad> unidades = listarActivas();
        return unidades.stream()
                .filter(u -> u.getNombre().equalsIgnoreCase(nombre))
                .findFirst()
                .orElse(null);
    }

    // Método para verificar si una abreviatura ya existe
    public boolean existeAbreviatura(String abreviatura) {
        List<Unidad> unidades = unidadRepository.findAll(); // Buscar en todas, activas e inactivas
        return unidades.stream()
                .anyMatch(u -> u.getAbreviatura().equalsIgnoreCase(abreviatura));
    }

    // Método para generar una abreviatura única
    public String generarAbreviaturaUnica(String nombreUnidad) {
        // Generar abreviatura base (primeras 3 letras en mayúsculas)
        String abreviaturaBase = nombreUnidad.substring(0, Math.min(3, nombreUnidad.length())).toUpperCase();
        
        // Si la abreviatura base no existe, usarla
        if (!existeAbreviatura(abreviaturaBase)) {
            return abreviaturaBase;
        }
        
        // Si existe, agregar números incrementales hasta encontrar una única
        int contador = 2;
        String abreviaturaUnica;
        do {
            abreviaturaUnica = abreviaturaBase + contador;
            contador++;
        } while (existeAbreviatura(abreviaturaUnica));
        
        return abreviaturaUnica;
    }
}
