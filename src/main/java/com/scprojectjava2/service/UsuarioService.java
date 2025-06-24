package com.scprojectjava2.service;

import com.scprojectjava2.model.Usuario;
import com.scprojectjava2.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public boolean validarUsuario(String nombreUsuario, String contrasena) {
        Optional<Usuario> usuario = usuarioRepository.findByNombreUsuarioAndActivo(nombreUsuario);
        if (usuario.isPresent()) {
            // Usar BCrypt para validar la contraseña
            return passwordEncoder.matches(contrasena, usuario.get().getContrasena());
        }
        return false;
    }
    
    public Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario) {
        return usuarioRepository.findByNombreUsuario(nombreUsuario);
    }
      public Usuario guardar(Usuario usuario) {
        // Encriptar la contraseña antes de guardar si no está ya encriptada
        if (usuario.getContrasena() != null && !usuario.getContrasena().startsWith("$2a$")) {
            usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        }
        return usuarioRepository.save(usuario);
    }
    
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }
    
    public boolean existeNombreUsuario(String nombreUsuario) {
        return usuarioRepository.existsByNombreUsuario(nombreUsuario);
    }
      public Optional<Usuario> buscarPorId(Integer id) {
        return usuarioRepository.findById(id);
    }
    
    public void eliminar(Integer id) {
        usuarioRepository.deleteById(id);
    }
    
    // Nuevos métodos para el controlador de usuarios
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }
      public Usuario guardarUsuario(Usuario usuario) {
        // Encriptar la contraseña antes de guardar si no está ya encriptada
        if (usuario.getContrasena() != null && !usuario.getContrasena().startsWith("$2a$")) {
            usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        }
        return usuarioRepository.save(usuario);
    }
    
    public Usuario obtenerUsuarioPorId(int id) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        return usuario.orElse(null);
    }
    
    public void eliminarUsuario(int id) {
        usuarioRepository.deleteById(id);
    }
      public List<Usuario> obtenerUsuariosActivos() {
        return usuarioRepository.findByActivo(1);
    }
    
    public Usuario actualizarUsuario(Usuario usuarioActualizado) {
        Optional<Usuario> usuarioExistente = usuarioRepository.findById(usuarioActualizado.getId());
        if (usuarioExistente.isPresent()) {
            Usuario usuario = usuarioExistente.get();
            usuario.setNombreUsuario(usuarioActualizado.getNombreUsuario());
            usuario.setNombre(usuarioActualizado.getNombre());
            usuario.setActivo(usuarioActualizado.getActivo());
            
            // Solo actualizar contraseña si se proporciona una nueva
            if (usuarioActualizado.getContrasena() != null && 
                !usuarioActualizado.getContrasena().trim().isEmpty()) {
                usuario.setContrasena(passwordEncoder.encode(usuarioActualizado.getContrasena()));
            }
            
            return usuarioRepository.save(usuario);
        }
        return null;
    }
}
