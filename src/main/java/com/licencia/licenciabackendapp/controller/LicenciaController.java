package com.licencia.licenciabackendapp.controller;

import com.licencia.licenciabackendapp.model.Licencia;
import com.licencia.licenciabackendapp.model.Producto;
import com.licencia.licenciabackendapp.model.Usuario;
import com.licencia.licenciabackendapp.service.LicenciaService;
import com.licencia.licenciabackendapp.service.ProductoService;
import com.licencia.licenciabackendapp.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/licencias")
@CrossOrigin(origins = "http://localhost:8100", allowCredentials = "true")
public class LicenciaController {

    private static final double USD_TO_PEN = 3.80;

    @Autowired
    private LicenciaService licenciaService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ProductoService productoService;

    @PostMapping("/crear")
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> request, HttpSession session) {
        try {
            System.out.println("Datos recibidos: " + request);

            Long usuarioId = (Long) session.getAttribute("usuarioId");
            if (usuarioId == null) {
                return ResponseEntity.status(401).body(Map.of("mensaje", "No autenticado"));
            }

            Usuario usuario = usuarioService.findByEmail((String) session.getAttribute("usuarioEmail")).orElse(null);
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("mensaje", "Usuario no encontrado"));
            }

            // Obtener producto por ID
            Long productoId = ((Number) request.get("productoId")).longValue();
            Producto producto = productoService.getProductoById(productoId);
            if (producto == null) {
                return ResponseEntity.badRequest().body(Map.of("mensaje", "Producto no encontrado"));
            }

            // Obtener valores con validación
            String moneda = (String) request.get("moneda");
            if (moneda == null) moneda = "USD";

            Integer cantidadLicencias = 1;
            if (request.get("cantidadLicencias") != null) {
                cantidadLicencias = ((Number) request.get("cantidadLicencias")).intValue();
            }

            // Calcular precios
            Double precioTotalUSD = producto.getPrecioBaseUsd() * cantidadLicencias;

            // Aplicar descuento según tipo
            String tipo = (String) request.get("tipo");
            if (tipo != null) {
                switch (tipo) {
                    case "Trimestral": precioTotalUSD *= 0.95; break;
                    case "Semestral": precioTotalUSD *= 0.90; break;
                    case "Anual": precioTotalUSD *= 0.80; break;
                    case "Perpetua": precioTotalUSD *= 0.70; break;
                    default: break;
                }
            }

            Double precioTotalPEN = precioTotalUSD * USD_TO_PEN;

            // Crear licencia
            Licencia licencia = new Licencia();
            licencia.setProducto(producto.getNombre());
            licencia.setProductoRel(producto);
            licencia.setTipo(tipo != null ? tipo : "Anual");
            licencia.setFechaVencimiento(LocalDate.parse((String) request.get("fechaVencimiento")));
            licencia.setCantidadUsuarios(1);
            licencia.setCantidadLicencias(cantidadLicencias);
            licencia.setPrecioTotalUSD(precioTotalUSD);
            licencia.setPrecioTotalPEN(precioTotalPEN);
            licencia.setMoneda(moneda);
            licencia.setCliente((String) request.get("cliente"));
            licencia.setEmpresa((String) request.get("empresa"));
            licencia.setCorreo((String) request.get("correo"));
            licencia.setNotas((String) request.get("notas"));

            Licencia saved = licenciaService.crearLicencia(licencia, usuario);

            System.out.println(" Licencia guardada con ID: " + saved.getId());
            System.out.println("Código: " + saved.getCodigoLicencia());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("licencia", saved);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("mensaje", "Error: " + e.getMessage()));
        }
    }

    @GetMapping("/mis-licencias")
    public ResponseEntity<?> misLicencias(HttpSession session) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return ResponseEntity.status(401).body(Map.of("mensaje", "No autenticado"));
        }
        List<Licencia> licencias = licenciaService.getLicenciasByUsuario(usuarioId);
        return ResponseEntity.ok(licencias);
    }
}