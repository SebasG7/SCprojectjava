package com.scprojectjava2.repository;

import com.scprojectjava2.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);
    
    @Query("SELECT u FROM Usuario u WHERE u.nombreUsuario = :nombreUsuario AND u.activo = 1")
    Optional<Usuario> findByNombreUsuarioAndActivo(@Param("nombreUsuario") String nombreUsuario);
    
    boolean existsByNombreUsuario(String nombreUsuario);
    
    List<Usuario> findByActivo(int activo);
}
