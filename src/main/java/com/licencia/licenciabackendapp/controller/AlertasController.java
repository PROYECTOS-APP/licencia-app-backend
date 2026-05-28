package com.licencia.licenciabackendapp.controller;

import com.licencia.licenciabackendapp.model.Alerta;
import com.licencia.licenciabackendapp.model.Usuario;
import com.licencia.licenciabackendapp.service.AlertasService;
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
@RequestMapping("/api/alertas")
@CrossOrigin(origins = "http://localhost:8100", allowCredentials = "true")
public class AlertasController {

    @Autowired
    private AlertasService alertasService;

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/crear")
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> request, HttpSession session) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");

        if (usuarioId == null) {
            return ResponseEntity.status(401).body(Map.of("mensaje", "No autenticado"));
        }

        Usuario usuario = usuarioService.findByEmail((String) session.getAttribute("usuarioEmail")).orElse(null);
        if (usuario == null) {
            return ResponseEntity.status(401).body(Map.of("mensaje", "Usuario no encontrado"));
        }

        System.out.println("📝 Creando alerta para usuario: " + usuario.getEmail());

        Alerta alerta = new Alerta();
        alerta.setTitulo((String) request.get("titulo"));
        alerta.setMensaje((String) request.get("mensaje"));
        alerta.setTipo((String) request.get("tipo"));
        alerta.setPrioridad((String) request.get("prioridad"));

        String fechaExp = (String) request.get("fechaExpiracion");
        if (fechaExp != null && !fechaExp.isEmpty()) {
            alerta.setFechaExpiracion(LocalDate.parse(fechaExp.split("T")[0]));
        }

        alerta.setUsuario(usuario);

        Alerta saved = alertasService.crearAlerta(alerta);

        System.out.println("✅ Alerta guardada en BD con ID: " + saved.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("alerta", saved);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/mis-alertas")
    public ResponseEntity<?> misAlertas(HttpSession session) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");

        if (usuarioId == null) {
            return ResponseEntity.status(401).body(Map.of("mensaje", "No autenticado"));
        }

        List<Alerta> alertas = alertasService.getAlertasByUsuario(usuarioId);
        System.out.println("📋 Obteniendo " + alertas.size() + " alertas del usuario");
        return ResponseEntity.ok(alertas);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id, HttpSession session) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");

        if (usuarioId == null) {
            return ResponseEntity.status(401).body(Map.of("mensaje", "No autenticado"));
        }

        alertasService.eliminarAlerta(id, usuarioId);
        System.out.println("🗑️ Alerta eliminada ID: " + id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}