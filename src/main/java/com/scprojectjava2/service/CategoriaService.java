package com.scprojectjava2.service;

import com.scprojectjava2.model.Categoria;
import com.scprojectjava2.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;    public List<Categoria> listarActivas() {
        return categoriaRepository.findByActivoTrue();
    }

    public List<Categoria> listarInactivas() {
        return categoriaRepository.findByActivoFalse();
    }    public Categoria agregar(Categoria categoria) {
        categoria.setActivo(1); // Asegura que siempre se guarde como activo
        return categoriaRepository.save(categoria);
    }

    public Categoria actualizar(Categoria categoria) {
        // Asegurarse de que el estado activo se mantenga
        Optional<Categoria> categoriaExistente = categoriaRepository.findById(categoria.getId());
        if (categoriaExistente.isPresent()) {
            categoria.setActivo(categoriaExistente.get().getActivo());
        }
        return categoriaRepository.save(categoria);
    }    public void eliminar(Integer id) {
        Optional<Categoria> categoriaOpt = categoriaRepository.findById(id);
        categoriaOpt.ifPresent(categoria -> {
            categoria.setActivo(0);
            categoriaRepository.save(categoria);
        });
    }

    public void reactivar(Integer id) {
        Optional<Categoria> categoriaOpt = categoriaRepository.findById(id);
        categoriaOpt.ifPresent(categoria -> {
            categoria.setActivo(1);
            categoriaRepository.save(categoria);
        });
    }

    public Categoria obtenerPorId(Integer id) {
        return categoriaRepository.findById(id).orElse(null);
    }

    // Método para buscar categoría por nombre
    public Categoria obtenerPorNombre(String nombre) {
        List<Categoria> categorias = categoriaRepository.findByNombreContainingIgnoreCaseAndActivoTrueOrderByNombre(nombre);
        return categorias.stream()
                .filter(c -> c.getNombre().equalsIgnoreCase(nombre))
                .findFirst()
                .orElse(null);
    }
}
