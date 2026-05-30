package com.licencia.licenciabackendapp.controller;

import com.licencia.licenciabackendapp.model.Usuario;
import com.licencia.licenciabackendapp.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:8100", allowCredentials = "true")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        String email = request.get("email");
        String password = request.get("password");

        System.out.println("📥 LOGIN: " + email);

        var usuarioOpt = usuarioService.findByEmail(email);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Usuario no encontrado"));
        }

        Usuario usuario = usuarioOpt.get();

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Contraseña incorrecta"));
        }

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute("usuarioId", usuario.getId());
        session.setAttribute("usuarioEmail", usuario.getEmail());

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

        if (usuarioService.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "El email ya está registrado"));
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setAvatar("assets/icon/avatar-default.png");

        Usuario saved = usuarioService.registrar(usuario);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("nombre", saved.getNombre());
        response.put("email", saved.getEmail());
        response.put("avatar", saved.getAvatar());

        return ResponseEntity.ok(response);
    }

    // ============ RECUPERAR CONTRASEÑA ============

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        System.out.println("📧 Solicitud recuperación: " + email);

        var usuarioOpt = usuarioService.findByEmail(email);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Email no registrado"));
        }

        Usuario usuario = usuarioOpt.get();

        // Generar token único
        String token = UUID.randomUUID().toString();
        usuario.setResetToken(token);
        usuario.setResetTokenExpiry(LocalDateTime.now().plusHours(1)); // Token válido por 1 hora

        usuarioService.actualizar(usuario);

        System.out.println("✅ Token generado: " + token);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("token", token);
        response.put("message", "Se ha enviado un enlace para restablecer tu contraseña");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        System.out.println("🔐 Restableciendo contraseña con token: " + token);

        var usuarioOpt = usuarioService.findByResetToken(token);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Token inválido"));
        }

        Usuario usuario = usuarioOpt.get();

        // Verificar si el token ha expirado
        if (usuario.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "El enlace ha expirado. Solicita uno nuevo"));
        }

        // Actualizar contraseña
        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuario.setPasswordUpdatedAt(LocalDateTime.now());
        usuario.setResetToken(null);
        usuario.setResetTokenExpiry(null);

        usuarioService.actualizar(usuario);

        System.out.println("✅ Contraseña restablecida exitosamente");

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Contraseña actualizada correctamente");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);

        if (session == null) {
            return ResponseEntity.status(401).body(Map.of("mensaje", "No autenticado"));
        }

        Long usuarioId = (Long) session.getAttribute("usuarioId");

        if (usuarioId == null) {
            return ResponseEntity.status(401).body(Map.of("mensaje", "No autenticado"));
        }

        var usuario = usuarioService.findById(usuarioId);
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