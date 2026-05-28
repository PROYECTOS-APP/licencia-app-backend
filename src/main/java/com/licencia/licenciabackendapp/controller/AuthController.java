package com.licencia.licenciabackendapp.controller;

import com.licencia.licenciabackendapp.model.Usuario;
import com.licencia.licenciabackendapp.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:8100", allowCredentials = "true")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request, HttpSession session) {
        String email = request.get("email");
        String password = request.get("password");

        System.out.println("=========================================");
        System.out.println("📥 INTENTO DE LOGIN");
        System.out.println("Email: " + email);
        System.out.println("Password enviada: " + password);
        System.out.println("=========================================");

        var usuarioOpt = usuarioService.findByEmail(email);

        if (usuarioOpt.isEmpty()) {
            System.out.println("❌ Usuario no encontrado: " + email);
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Usuario no encontrado"));
        }

        Usuario usuario = usuarioOpt.get();
        System.out.println("✅ Usuario encontrado en BD");
        System.out.println("   Password en BD: " + usuario.getPassword());
        System.out.println("   Comparación: " + (usuario.getPassword().equals(password) ? "IGUALES" : "DIFERENTES"));

        if (!usuario.getPassword().equals(password)) {
            System.out.println("❌ Contraseña incorrecta");
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Contraseña incorrecta"));
        }

        session.setAttribute("usuarioId", usuario.getId());
        session.setAttribute("usuarioEmail", usuario.getEmail());

        System.out.println("✅ LOGIN EXITOSO!");
        System.out.println("   Usuario ID: " + usuario.getId());
        System.out.println("   Session ID: " + session.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("nombre", usuario.getNombre());
        response.put("email", usuario.getEmail());
        response.put("avatar", usuario.getAvatar());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registro(@RequestBody Map<String, String> request) {
        String nombre = request.get("nombre");
        String email = request.get("email");
        String password = request.get("password");

        System.out.println("📝 REGISTRO - Email: " + email);

        if (usuarioService.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "El email ya está registrado"));
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setPassword(password);
        usuario.setAvatar("assets/icon/avatar-default.png");

        Usuario saved = usuarioService.registrar(usuario);

        System.out.println("✅ Usuario registrado: " + email);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("nombre", saved.getNombre());
        response.put("email", saved.getEmail());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        System.out.println("📤 CERRANDO SESIÓN - Session ID: " + session.getId());
        session.invalidate();
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");

        if (usuarioId == null) {
            return ResponseEntity.status(401).body(Map.of("mensaje", "No autenticado"));
        }

        var usuario = usuarioService.findByEmail((String) session.getAttribute("usuarioEmail"));
        if (usuario.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("mensaje", "Usuario no encontrado"));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("nombre", usuario.get().getNombre());
        response.put("email", usuario.get().getEmail());
        response.put("avatar", usuario.get().getAvatar());

        return ResponseEntity.ok(response);
    }
}