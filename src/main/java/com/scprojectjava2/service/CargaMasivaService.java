package com.scprojectjava2.service;

import com.scprojectjava2.model.*;
import com.scprojectjava2.repository.OperacionCargaMasivaRepository;
import com.scprojectjava2.repository.DetalleCargaMasivaRepository;
import com.scprojectjava2.repository.CategoriaCreadaCargaMasivaRepository;
import com.scprojectjava2.repository.UnidadCreadaCargaMasivaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class CargaMasivaService {

    @Autowired
    private OperacionCargaMasivaRepository operacionRepository;
    
    @Autowired
    private DetalleCargaMasivaRepository detalleRepository;
    
    @Autowired
    private CategoriaCreadaCargaMasivaRepository categoriaCreadaRepository;
    
    @Autowired
    private UnidadCreadaCargaMasivaRepository unidadCreadaRepository;
    
    @Autowired
    private ProductoService productoService;
    
    @Autowired
    private CategoriaService categoriaService;
    
    @Autowired
    private UnidadService unidadService;
    
    @Autowired
    private HistorialStockService historialStockService;

    // Crear una nueva operación de carga masiva
    @Transactional
    public OperacionCargaMasiva iniciarOperacion(String nombreArchivo, String usuario, Integer totalProductos) {
        OperacionCargaMasiva operacion = new OperacionCargaMasiva(nombreArchivo, usuario, totalProductos);
        return operacionRepository.save(operacion);
    }

    // Procesar un producto dentro de una operación
    @Transactional
    public DetalleCargaMasiva procesarProducto(OperacionCargaMasiva operacion, Integer lineaArchivo,
                                              String codigo, String nombre, String descripcion,
                                              Double precio, Integer stock, String categoriaNombre,
                                              String unidadNombre, Double iva, String fechaVencimiento) {
        
        DetalleCargaMasiva detalle = new DetalleCargaMasiva(operacion, lineaArchivo, codigo, nombre);
          try {
            // Validaciones básicas
            if (codigo == null || codigo.trim().isEmpty()) {
                throw new Exception("Código de producto vacío");
            }
            
            if (nombre == null || nombre.trim().isEmpty()) {
                throw new Exception("Nombre de producto vacío");
            }
            
            if (precio == null || precio <= 0) {
                throw new Exception("Precio inválido");
            }
            
            if (stock == null || stock < 0) {
                throw new Exception("Stock inválido");
            }

            // Validar que categoría y unidad estén especificadas en el CSV
            if (categoriaNombre == null || categoriaNombre.trim().isEmpty()) {
                throw new Exception("Categoría no especificada - requerida en el CSV");
            }
            
            if (unidadNombre == null || unidadNombre.trim().isEmpty()) {
                throw new Exception("Unidad no especificada - requerida en el CSV");
            }            // Buscar o crear categoría
            Categoria categoria = categoriaService.obtenerPorNombre(categoriaNombre);
            if (categoria == null) {
                categoria = new Categoria();
                categoria.setNombre(categoriaNombre);
                categoria.setDescripcion("Categoría creada automáticamente");
                categoriaService.agregar(categoria);
                
                // Registrar que esta categoría fue creada en esta operación
                CategoriaCreadaCargaMasiva categoriaCreada = new CategoriaCreadaCargaMasiva(operacion, categoria);
                categoriaCreadaRepository.save(categoriaCreada);
            }            // Buscar o crear unidad
            Unidad unidad = unidadService.obtenerPorNombre(unidadNombre);
            if (unidad == null) {
                unidad = new Unidad();
                unidad.setNombre(unidadNombre);
                // Generar abreviatura única automáticamente
                String abreviaturaUnica = unidadService.generarAbreviaturaUnica(unidadNombre);
                unidad.setAbreviatura(abreviaturaUnica);
                unidadService.agregar(unidad);
                
                // Registrar que esta unidad fue creada en esta operación
                UnidadCreadaCargaMasiva unidadCreada = new UnidadCreadaCargaMasiva(operacion, unidad);
                unidadCreadaRepository.save(unidadCreada);
            }// Verificar si el producto existe (activo o inactivo)
            Producto productoExistente = productoService.obtenerPorCodigo(codigo);
              if (productoExistente != null) {
                // Actualizar producto existente
                int stockAnterior = productoExistente.getStock();
                boolean estabaInactivo = productoExistente.getActivo() == 0;
                
                productoExistente.setNombre(nombre);
                productoExistente.setDescripcion(descripcion);
                productoExistente.setPrecio(precio);
                productoExistente.setStock(stock);
                productoExistente.setCategoria(categoria);
                productoExistente.setUnidad(unidad);
                
                // Reactivar producto si estaba inactivo
                if (estabaInactivo) {
                    productoExistente.setActivo(1);
                }
                
                if (iva != null && iva >= 0) {
                    productoExistente.setIva(iva);
                }
                
                if (fechaVencimiento != null && !fechaVencimiento.trim().isEmpty()) {
                    try {
                        productoExistente.setFechaVencimiento(java.time.LocalDate.parse(fechaVencimiento));
                    } catch (Exception e) {
                        // Si no se puede parsear la fecha, se ignora
                    }
                }                
                productoService.actualizar(productoExistente);
                  // Registrar movimiento de stock si cambió
                if (stockAnterior != stock) {
                    String motivo = "Actualización por carga masiva - Archivo: " + operacion.getNombreArchivo();
                    historialStockService.registrarMovimiento(
                        productoExistente.getId(), stockAnterior, stock,
                        stock > stockAnterior ? "ENTRADA" : "SALIDA", motivo, operacion.getUsuario());
                }
                
                // Registrar reactivación si fue necesaria
                if (estabaInactivo) {
                    String motivoReactivacion = "Producto reactivado por carga masiva - Archivo: " + operacion.getNombreArchivo();
                    historialStockService.registrarMovimiento(
                        productoExistente.getId(), stockAnterior, stock,
                        "REACTIVACION", motivoReactivacion, operacion.getUsuario());
                }
                
                detalle.marcarComoExitoso(estabaInactivo ? "REACTIVADO" : "ACTUALIZADO", productoExistente);
                operacion.incrementarExitosos();
                  } else {
                // Crear nuevo producto
                Producto nuevoProducto = new Producto();
                nuevoProducto.setCodigo(codigo);
                nuevoProducto.setNombre(nombre);
                nuevoProducto.setDescripcion(descripcion);
                nuevoProducto.setPrecio(precio);
                nuevoProducto.setStock(stock);
                nuevoProducto.setCategoria(categoria);
                nuevoProducto.setUnidad(unidad);
                nuevoProducto.setActivo(1); // Asegurar que el producto esté activo
                
                if (iva != null && iva >= 0) {
                    nuevoProducto.setIva(iva);
                } else {
                    nuevoProducto.setIva(0.0); // IVA por defecto
                }
                
                if (fechaVencimiento != null && !fechaVencimiento.trim().isEmpty()) {
                    try {
                        nuevoProducto.setFechaVencimiento(java.time.LocalDate.parse(fechaVencimiento));
                    } catch (Exception e) {
                        // Si no se puede parsear la fecha, se ignora
                    }
                }
                
                productoService.agregar(nuevoProducto);
                
                // El método agregar ya registra el historial de stock inicial
                detalle.marcarComoExitoso("CREADO", nuevoProducto);
                operacion.incrementarExitosos();
            }
            
        } catch (Exception e) {
            detalle.marcarComoFallido(e.getMessage());
            operacion.incrementarFallidos();
        }
        
        return detalleRepository.save(detalle);
    }

    // Finalizar una operación
    @Transactional
    public OperacionCargaMasiva finalizarOperacion(OperacionCargaMasiva operacion, String observaciones) {
        operacion.setObservaciones(observaciones);
        operacion.finalizarOperacion();
        return operacionRepository.save(operacion);
    }

    // Obtener todas las operaciones
    public List<OperacionCargaMasiva> obtenerTodasLasOperaciones() {
        return operacionRepository.findAllByOrderByFechaOperacionDesc();
    }

    // Obtener operaciones por usuario
    public List<OperacionCargaMasiva> obtenerOperacionesPorUsuario(String usuario) {
        return operacionRepository.findByUsuarioOrderByFechaOperacionDesc(usuario);
    }

    // Obtener operaciones entre fechas
    public List<OperacionCargaMasiva> obtenerOperacionesEntreFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicioDateTime = fechaInicio.atStartOfDay();
        LocalDateTime finDateTime = fechaFin.atTime(LocalTime.MAX);
        return operacionRepository.findByFechaOperacionBetweenOrderByFechaOperacionDesc(inicioDateTime, finDateTime);
    }

    // Obtener operación por ID
    public OperacionCargaMasiva obtenerOperacionPorId(Integer id) {
        return operacionRepository.findById(id).orElse(null);
    }

    // Obtener detalles de una operación
    public List<DetalleCargaMasiva> obtenerDetallesOperacion(Integer operacionId) {
        return detalleRepository.findByOperacionCargaIdOrderByLineaArchivo(operacionId);
    }

    // Obtener detalles fallidos de una operación
    public List<DetalleCargaMasiva> obtenerDetallesFallidos(Integer operacionId) {
        return detalleRepository.findByOperacionCargaIdAndEstadoOrderByLineaArchivo(operacionId, "FALLIDO");
    }

    // Obtener detalles exitosos de una operación
    public List<DetalleCargaMasiva> obtenerDetallesExitosos(Integer operacionId) {
        return detalleRepository.findByOperacionCargaIdAndEstadoOrderByLineaArchivo(operacionId, "EXITOSO");
    }    // Obtener estadísticas generales
    public EstadisticasCargaMasiva obtenerEstadisticas() {
        EstadisticasCargaMasiva stats = new EstadisticasCargaMasiva();
        
        stats.setTotalOperaciones(operacionRepository.count());
        stats.setOperacionesCompletadas(operacionRepository.countByEstado("COMPLETADO"));
        stats.setOperacionesParciales(operacionRepository.countByEstado("PARCIAL"));
        stats.setOperacionesFallidas(operacionRepository.countByEstado("FALLIDO"));
        stats.setOperacionesRevertidas((long) operacionRepository.findByRevertidaTrueOrderByFechaOperacionDesc().size());
        
        // Obtener operaciones con fallidos para mostrar alertas
        stats.setOperacionesConFallidos(operacionRepository.findOperacionesConFallidos());
        
        return stats;
    }    // Método para revertir una operación de carga masiva
    @Transactional
    public ResultadoReversion revertirOperacion(Integer operacionId, String usuarioReversion, String motivo) {
        OperacionCargaMasiva operacion = operacionRepository.findById(operacionId).orElse(null);
        if (operacion == null) {
            return new ResultadoReversion(false, "Operación no encontrada", 0, 0);
        }

        // Verificar que la operación sea elegible para reversión
        if (!"COMPLETADO".equals(operacion.getEstado()) && !"PARCIAL".equals(operacion.getEstado())) {
            return new ResultadoReversion(false, "Solo se pueden revertir operaciones completadas o parciales", 0, 0);
        }

        // Obtener solo los productos que fueron creados en esta operación
        List<DetalleCargaMasiva> productosCreados = detalleRepository.findProductosCreadosEnOperacion(operacionId);
        
        int productosRevertidos = 0;
        int erroresReversion = 0;
        StringBuilder errores = new StringBuilder();        for (DetalleCargaMasiva detalle : productosCreados) {
            try {
                Producto producto = detalle.getProducto();
                if (producto != null) {
                    // Marcar producto como inactivo en lugar de eliminarlo completamente
                    productoService.eliminar(producto.getId());
                    
                    // Registrar movimiento en el historial
                    if (producto.getStock() > 0) {
                        historialStockService.registrarMovimiento(
                            producto.getId(), producto.getStock(), 0,
                            "REVERSION", "Reversión de carga masiva - Operación ID: " + operacionId, 
                            usuarioReversion);
                    }
                    
                    // Marcar el detalle como revertido
                    detalle.marcarComoRevertido();
                    detalleRepository.save(detalle);
                    
                    productosRevertidos++;
                }
            } catch (Exception e) {
                erroresReversion++;
                errores.append("Error al revertir producto ").append(detalle.getCodigoProducto())
                       .append(": ").append(e.getMessage()).append("; ");
            }
        }

        // Para productos que fueron actualizados, intentamos restaurar el estado anterior
        List<DetalleCargaMasiva> productosActualizados = detalleRepository.findProductosActualizadosEnOperacion(operacionId);
          for (DetalleCargaMasiva detalle : productosActualizados) {
            try {
                Producto producto = detalle.getProducto();
                if (producto != null) {
                    // Obtener el historial para encontrar el estado anterior
                    List<HistorialStock> historial = historialStockService.obtenerHistorialPorProducto(producto.getId());
                    
                    // Buscar el movimiento de esta operación de carga masiva
                    HistorialStock movimientoCarga = historial.stream()
                        .filter(h -> h.getMotivo() != null && 
                                h.getMotivo().contains("carga masiva") && 
                                h.getMotivo().contains(operacion.getNombreArchivo()))
                        .findFirst()
                        .orElse(null);
                    
                    if (movimientoCarga != null) {
                        // Restaurar el stock anterior
                        Integer stockAnterior = movimientoCarga.getStockAnterior();
                        productoService.actualizarStock(producto.getId(), stockAnterior, 
                            "Reversión de carga masiva - Operación ID: " + operacionId, usuarioReversion);
                        
                        // Marcar el detalle como revertido
                        detalle.marcarComoRevertido();
                        detalleRepository.save(detalle);
                        
                        productosRevertidos++;
                    }
                }
            } catch (Exception e) {
                erroresReversion++;
                errores.append("Error al revertir actualización del producto ").append(detalle.getCodigoProducto())
                       .append(": ").append(e.getMessage()).append("; ");
            }        }

        // Eliminar categorías creadas durante esta operación de carga masiva
        List<CategoriaCreadaCargaMasiva> categoriasCreadas = categoriaCreadaRepository.findByOperacionCargaIdAndRevertidaFalse(operacionId);
        int categoriasEliminadas = 0;
        for (CategoriaCreadaCargaMasiva categoriaCreada : categoriasCreadas) {
            try {
                Categoria categoria = categoriaCreada.getCategoria();
                if (categoria != null) {
                    // Verificar si la categoría está siendo usada por otros productos
                    // que no sean de esta operación de carga masiva
                    boolean categoriaEnUso = productoService.existeProductoConCategoria(categoria.getId());
                    
                    if (!categoriaEnUso) {
                        // Eliminar la categoría ya que no está en uso
                        categoriaService.eliminar(categoria.getId());
                        categoriaCreada.marcarComoRevertida();
                        categoriaCreadaRepository.save(categoriaCreada);
                        categoriasEliminadas++;
                    } else {
                        // La categoría está en uso, solo marcar como revertida pero no eliminar
                        categoriaCreada.marcarComoRevertida();
                        categoriaCreadaRepository.save(categoriaCreada);
                        errores.append("Categoría '").append(categoria.getNombre())
                               .append("' no eliminada - en uso por otros productos; ");
                    }
                }
            } catch (Exception e) {
                erroresReversion++;
                errores.append("Error al eliminar categoría: ").append(e.getMessage()).append("; ");
            }
        }

        // Eliminar unidades creadas durante esta operación de carga masiva
        List<UnidadCreadaCargaMasiva> unidadesCreadas = unidadCreadaRepository.findByOperacionCargaIdAndRevertidaFalse(operacionId);
        int unidadesEliminadas = 0;
        for (UnidadCreadaCargaMasiva unidadCreada : unidadesCreadas) {
            try {
                Unidad unidad = unidadCreada.getUnidad();
                if (unidad != null) {
                    // Verificar si la unidad está siendo usada por otros productos
                    // que no sean de esta operación de carga masiva
                    boolean unidadEnUso = productoService.existeProductoConUnidad(unidad.getId());
                    
                    if (!unidadEnUso) {
                        // Eliminar la unidad ya que no está en uso
                        unidadService.eliminar(unidad.getId());
                        unidadCreada.marcarComoRevertida();
                        unidadCreadaRepository.save(unidadCreada);
                        unidadesEliminadas++;
                    } else {
                        // La unidad está en uso, solo marcar como revertida pero no eliminar
                        unidadCreada.marcarComoRevertida();
                        unidadCreadaRepository.save(unidadCreada);
                        errores.append("Unidad '").append(unidad.getNombre())
                               .append("' no eliminada - en uso por otros productos; ");
                    }
                }
            } catch (Exception e) {
                erroresReversion++;
                errores.append("Error al eliminar unidad: ").append(e.getMessage()).append("; ");
            }
        }

        // Marcar la operación como revertida
        operacion.marcarComoRevertida(motivo, usuarioReversion);
        operacionRepository.save(operacion);

        String mensaje = construirMensajeReversion(erroresReversion, productosRevertidos, 
                                                  categoriasEliminadas, unidadesEliminadas, errores);

        return new ResultadoReversion(true, mensaje, productosRevertidos, erroresReversion);
    }

    private String construirMensajeReversion(int erroresReversion, int productosRevertidos, 
                                           int categoriasEliminadas, int unidadesEliminadas, 
                                           StringBuilder errores) {
        if (erroresReversion > 0) {
            return String.format("Reversión parcial completada. Productos revertidos: %d, " +
                    "Categorías eliminadas: %d, Unidades eliminadas: %d. Errores: %s", 
                    productosRevertidos, categoriasEliminadas, unidadesEliminadas, errores.toString());
        } else {
            return String.format("Reversión completada exitosamente. Productos revertidos: %d, " +
                    "Categorías eliminadas: %d, Unidades eliminadas: %d", 
                    productosRevertidos, categoriasEliminadas, unidadesEliminadas);
        }
    }    // Verificar si una operación puede ser revertida
    public boolean puedeSerRevertida(Integer operacionId) {
        OperacionCargaMasiva operacion = operacionRepository.findById(operacionId).orElse(null);
        if (operacion == null) {
            return false;
        }
        
        // Solo se pueden revertir operaciones completadas o parciales que no hayan sido ya revertidas
        return ("COMPLETADO".equals(operacion.getEstado()) || "PARCIAL".equals(operacion.getEstado())) &&
               !operacion.getRevertida();
    }

    // Obtener operaciones revertidas
    public List<OperacionCargaMasiva> obtenerOperacionesRevertidas() {
        return operacionRepository.findByRevertidaTrueOrderByFechaOperacionDesc();
    }

    // Obtener operaciones no revertidas
    public List<OperacionCargaMasiva> obtenerOperacionesNoRevertidas() {
        return operacionRepository.findByRevertidaFalseOrderByFechaOperacionDesc();
    }

    // Clase auxiliar para estadísticas
    public static class EstadisticasCargaMasiva {
        private Long totalOperaciones;
        private Long operacionesCompletadas;
        private Long operacionesParciales;
        private Long operacionesFallidas;
        private Long operacionesRevertidas;
        private List<OperacionCargaMasiva> operacionesConFallidos;

        // Getters y setters
        public Long getTotalOperaciones() { return totalOperaciones; }
        public void setTotalOperaciones(Long totalOperaciones) { this.totalOperaciones = totalOperaciones; }
        
        public Long getOperacionesCompletadas() { return operacionesCompletadas; }
        public void setOperacionesCompletadas(Long operacionesCompletadas) { this.operacionesCompletadas = operacionesCompletadas; }
        
        public Long getOperacionesParciales() { return operacionesParciales; }
        public void setOperacionesParciales(Long operacionesParciales) { this.operacionesParciales = operacionesParciales; }
        
        public Long getOperacionesFallidas() { return operacionesFallidas; }
        public void setOperacionesFallidas(Long operacionesFallidas) { this.operacionesFallidas = operacionesFallidas; }
        
        public Long getOperacionesRevertidas() { return operacionesRevertidas; }
        public void setOperacionesRevertidas(Long operacionesRevertidas) { this.operacionesRevertidas = operacionesRevertidas; }
        
        public List<OperacionCargaMasiva> getOperacionesConFallidos() { return operacionesConFallidos; }
        public void setOperacionesConFallidos(List<OperacionCargaMasiva> operacionesConFallidos) { this.operacionesConFallidos = operacionesConFallidos; }
    }

    // Clase auxiliar para el resultado de la reversión
    public static class ResultadoReversion {
        private boolean exito;
        private String mensaje;
        private int productosRevertidos;
        private int errores;

        public ResultadoReversion(boolean exito, String mensaje, int productosRevertidos, int errores) {
            this.exito = exito;
            this.mensaje = mensaje;
            this.productosRevertidos = productosRevertidos;
            this.errores = errores;
        }

        // Getters
        public boolean isExito() { return exito; }
        public String getMensaje() { return mensaje; }
        public int getProductosRevertidos() { return productosRevertidos; }
        public int getErrores() { return errores; }
    }
}
