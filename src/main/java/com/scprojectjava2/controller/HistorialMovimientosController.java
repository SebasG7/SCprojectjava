package com.scprojectjava2.controller;

import com.scprojectjava2.model.*;
import com.scprojectjava2.service.CargaMasivaService;
import com.scprojectjava2.service.HistorialStockService;
import com.scprojectjava2.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/historial")
public class HistorialMovimientosController {

    @Autowired
    private CargaMasivaService cargaMasivaService;
    
    @Autowired
    private HistorialStockService historialStockService;

    @Autowired
    private UsuarioService usuarioService;    @GetMapping("/movimientos")
    public String mostrarHistorialMovimientos(@RequestParam(required = false, defaultValue = "operaciones") String activeTab,
                                            Model model, HttpSession session) {
        // Verificar sesión
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("usuario", usuario);

        // Obtener todas las operaciones de carga masiva
        List<OperacionCargaMasiva> operaciones = cargaMasivaService.obtenerTodasLasOperaciones();
        model.addAttribute("operaciones", operaciones);

        // Obtener estadísticas
        CargaMasivaService.EstadisticasCargaMasiva estadisticas = cargaMasivaService.obtenerEstadisticas();
        model.addAttribute("estadisticas", estadisticas);        // Obtener historial de stock reciente (últimos 50 movimientos)
        List<HistorialStock> historialReciente = historialStockService.obtenerHistorialEntreFechas(
            LocalDate.now().minusDays(30), LocalDate.now());        model.addAttribute("historialReciente", historialReciente.stream().limit(50).toList());

        // Obtener usuarios activos para el filtro
        List<Usuario> usuariosActivos = usuarioService.obtenerUsuariosActivos();
        model.addAttribute("usuariosActivos", usuariosActivos);
        model.addAttribute("activeTab", activeTab);

        return "historial-movimientos";
    }

    @GetMapping("/operacion/{id}")
    public String verDetalleOperacion(@PathVariable Integer id, Model model, HttpSession session) {
        // Verificar sesión
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("usuario", usuario);

        // Obtener operación
        OperacionCargaMasiva operacion = cargaMasivaService.obtenerOperacionPorId(id);
        if (operacion == null) {
            model.addAttribute("error", "Operación no encontrada");
            return "redirect:/historial/movimientos";
        }

        // Obtener detalles de la operación
        List<DetalleCargaMasiva> detalles = cargaMasivaService.obtenerDetallesOperacion(id);
        List<DetalleCargaMasiva> detallesExitosos = cargaMasivaService.obtenerDetallesExitosos(id);
        List<DetalleCargaMasiva> detallesFallidos = cargaMasivaService.obtenerDetallesFallidos(id);

        model.addAttribute("operacion", operacion);
        model.addAttribute("detalles", detalles);
        model.addAttribute("detallesExitosos", detallesExitosos);
        model.addAttribute("detallesFallidos", detallesFallidos);

        return "detalle-operacion-carga";
    }    @GetMapping("/buscar")
    public String buscarMovimientos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String tipoMovimiento,
            @RequestParam(required = false, defaultValue = "operaciones") String activeTab,
            Model model, HttpSession session) {
        
        // Verificar sesión
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
        if (usuarioSesion == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("usuario", usuarioSesion);

        // Si no se especifican fechas, usar último mes
        if (fechaInicio == null) {
            fechaInicio = LocalDate.now().minusDays(30);
        }
        if (fechaFin == null) {
            fechaFin = LocalDate.now();
        }

        // Buscar operaciones de carga masiva
        List<OperacionCargaMasiva> operaciones;
        if (usuario != null && !usuario.trim().isEmpty()) {
            operaciones = cargaMasivaService.obtenerOperacionesPorUsuario(usuario);
        } else {
            operaciones = cargaMasivaService.obtenerOperacionesEntreFechas(fechaInicio, fechaFin);
        }        // Buscar movimientos de stock
        List<HistorialStock> movimientos = historialStockService.obtenerHistorialEntreFechas(fechaInicio, fechaFin);

        // Filtrar por usuario si se especifica
        if (usuario != null && !usuario.trim().isEmpty()) {
            movimientos = movimientos.stream()
                .filter(m -> m.getUsuario().equalsIgnoreCase(usuario))
                .toList();
        }

        // Filtrar por tipo de movimiento si se especifica
        if (tipoMovimiento != null && !tipoMovimiento.trim().isEmpty()) {
            movimientos = movimientos.stream()
                .filter(m -> m.getTipoMovimiento().equalsIgnoreCase(tipoMovimiento))
                .toList();
        }        model.addAttribute("operaciones", operaciones);
        model.addAttribute("movimientos", movimientos);        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        model.addAttribute("usuarioBusqueda", usuario);
        model.addAttribute("tipoMovimiento", tipoMovimiento);
        model.addAttribute("activeTab", activeTab);

        // Obtener estadísticas (mantener las cartas superiores)
        CargaMasivaService.EstadisticasCargaMasiva estadisticas = cargaMasivaService.obtenerEstadisticas();
        model.addAttribute("estadisticas", estadisticas);

        // Obtener usuarios activos para el filtro
        List<Usuario> usuariosActivos = usuarioService.obtenerUsuariosActivos();
        model.addAttribute("usuariosActivos", usuariosActivos);

        return "historial-movimientos";
    }

    @GetMapping("/estadisticas")
    @ResponseBody
    public CargaMasivaService.EstadisticasCargaMasiva obtenerEstadisticas() {
        return cargaMasivaService.obtenerEstadisticas();
    }    @PostMapping("/revertir/{id}")
    public String revertirOperacion(@PathVariable Integer id, 
                                   @RequestParam String motivo,
                                   HttpSession session,
                                   Model model) {
        // Verificar sesión
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }

        // Solo administradores pueden revertir operaciones
        if (!usuario.isAdministrador()) {
            model.addAttribute("error", "No tienes permisos para revertir operaciones de carga masiva");
            return "redirect:/historial/movimientos";
        }

        try {
            // Verificar si la operación puede ser revertida
            if (!cargaMasivaService.puedeSerRevertida(id)) {
                model.addAttribute("error", "Esta operación no puede ser revertida");
                return "redirect:/historial/operacion/" + id;
            }

            // Revertir la operación con el motivo
            CargaMasivaService.ResultadoReversion resultado = cargaMasivaService.revertirOperacion(id, usuario.getNombreUsuario(), motivo);

            if (resultado.isExito()) {
                model.addAttribute("mensaje", resultado.getMensaje() + 
                    " - Productos revertidos: " + resultado.getProductosRevertidos());
            } else {
                model.addAttribute("error", resultado.getMensaje());
            }

        } catch (Exception e) {
            model.addAttribute("error", "Error al revertir la operación: " + e.getMessage());
        }

        return "redirect:/historial/operacion/" + id;
    }

    @GetMapping("/confirmar-reversion/{id}")
    public String confirmarReversion(@PathVariable Integer id, Model model, HttpSession session) {
        // Verificar sesión
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }

        if (!usuario.isAdministrador()) {
            model.addAttribute("error", "No tienes permisos para revertir operaciones");
            return "redirect:/historial/movimientos";
        }

        // Obtener la operación
        OperacionCargaMasiva operacion = cargaMasivaService.obtenerOperacionPorId(id);
        if (operacion == null) {
            model.addAttribute("error", "Operación no encontrada");
            return "redirect:/historial/movimientos";
        }

        // Verificar si puede ser revertida
        if (!cargaMasivaService.puedeSerRevertida(id)) {
            model.addAttribute("error", "Esta operación no puede ser revertida");
            return "redirect:/historial/operacion/" + id;
        }

        // Obtener información sobre qué se va a revertir
        List<DetalleCargaMasiva> productosCreados = cargaMasivaService.obtenerDetallesExitosos(id)
            .stream()
            .filter(d -> "CREADO".equals(d.getAccionRealizada()))
            .toList();
        
        List<DetalleCargaMasiva> productosActualizados = cargaMasivaService.obtenerDetallesExitosos(id)
            .stream()
            .filter(d -> "ACTUALIZADO".equals(d.getAccionRealizada()))
            .toList();

        model.addAttribute("usuario", usuario);
        model.addAttribute("operacion", operacion);
        model.addAttribute("productosCreados", productosCreados);
        model.addAttribute("productosActualizados", productosActualizados);

        return "confirmar-reversion";
    }
}
