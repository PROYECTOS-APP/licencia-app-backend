// src/main/java/com/licencia/licenciabackendapp/controller/AuthController.java
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
@CrossOrigin(
        origins = {
                "http://localhost:8100",
                "http://localhost:4200",
                "http://localhost:8080"
        },
        allowCredentials = "true",
        allowedHeaders = "*"
)
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ============================================
    // LOGIN
    // ============================================
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        try {
            String email = request.get("email");
            String password = request.get("password");

            System.out.println("LOGIN: " + email);

            var usuarioOpt = usuarioService.findByEmail(email);

            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "mensaje", "Usuario no encontrado"
                ));
            }

            Usuario usuario = usuarioOpt.get();

            if (!passwordEncoder.matches(password, usuario.getPassword())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "mensaje", "Contraseña incorrecta"
                ));
            }

            HttpSession session = httpRequest.getSession(true);
            session.setAttribute("usuarioId", usuario.getId());
            session.setAttribute("usuarioEmail", usuario.getEmail());
            session.setMaxInactiveInterval(3600);

            String avatarUrl = usuario.getAvatar();
            if (avatarUrl == null || avatarUrl.isEmpty()) {
                avatarUrl = "assets/icon/avatar-default.png";
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("id", usuario.getId());
            response.put("nombre", usuario.getNombre());
            response.put("email", usuario.getEmail());
            response.put("avatar", avatarUrl);
            response.put("message", "Login exitoso");

            System.out.println("Login exitoso para: " + email);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error en login: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "mensaje", "Error interno del servidor"
            ));
        }
    }

    // ============================================
    // REGISTRO
    // ============================================
    @PostMapping("/registro")
    public ResponseEntity<?> registro(
            @RequestBody Map<String, String> request) {
        try {
            String nombre = request.get("nombre");
            String email = request.get("email");
            String password = request.get("password");

            System.out.println("REGISTRO: " + email);

            if (nombre == null || nombre.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "mensaje", "El nombre es obligatorio"
                ));
            }

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "mensaje", "El email es obligatorio"
                ));
            }

            if (password == null || password.length() < 6) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "mensaje", "La contraseña debe tener al menos 6 caracteres"
                ));
            }

            if (usuarioService.existsByEmail(email)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "mensaje", "El email ya está registrado"
                ));
            }

            Usuario usuario = new Usuario();
            usuario.setNombre(nombre);
            usuario.setEmail(email);
            usuario.setPassword(passwordEncoder.encode(password));
            usuario.setAvatar("assets/icon/avatar-default.png");

            Usuario saved = usuarioService.registrar(usuario);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("id", saved.getId());
            response.put("nombre", saved.getNombre());
            response.put("email", saved.getEmail());
            response.put("avatar", saved.getAvatar());
            response.put("message", "Registro exitoso");

            System.out.println("Registro exitoso para: " + email);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error en registro: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "mensaje", "Error al registrar usuario"
            ));
        }
    }

    // ============================================
    // FORGOT PASSWORD
    // ============================================
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");

            System.out.println("Solicitud recuperación: " + email);

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "mensaje", "El email es obligatorio"
                ));
            }

            var usuarioOpt = usuarioService.findByEmail(email);

            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "mensaje", "Email no registrado"
                ));
            }

            Usuario usuario = usuarioOpt.get();

            String token = UUID.randomUUID().toString();
            usuario.setResetToken(token);
            usuario.setResetTokenExpiry(LocalDateTime.now().plusHours(1));

            usuarioService.actualizar(usuario);

            System.out.println(" Token generado para " + email + ": " + token);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", token);
            response.put("message", "Se ha enviado un enlace para restablecer tu contraseña");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println(" Error en forgot-password: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "mensaje", "Error al procesar la solicitud"
            ));
        }
    }

    // ============================================
    // RESET PASSWORD
    // ============================================
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            String newPassword = request.get("newPassword");

            System.out.println("Restableciendo contraseña con token: " + token);

            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "mensaje", "Token inválido"
                ));
            }

            if (newPassword == null || newPassword.length() < 6) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "mensaje", "La contraseña debe tener al menos 6 caracteres"
                ));
            }

            var usuarioOpt = usuarioService.findByResetToken(token);

            if (usuarioOpt.isEmpty()) {
                System.out.println("Token no encontrado: " + token);
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "mensaje", "Token inválido. El enlace no es válido o ya fue usado."
                ));
            }

            Usuario usuario = usuarioOpt.get();

            if (usuario.getResetTokenExpiry() == null ||
                    usuario.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
                System.out.println("Token expirado: " + usuario.getResetTokenExpiry());
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "mensaje", "El enlace ha expirado. Solicita uno nuevo"
                ));
            }

            String encodedPassword = passwordEncoder.encode(newPassword);
            usuario.setPassword(encodedPassword);
            usuario.setPasswordUpdatedAt(LocalDateTime.now());
            usuario.setResetToken(null);
            usuario.setResetTokenExpiry(null);

            usuarioService.actualizar(usuario);

            System.out.println("Contraseña restablecida exitosamente para: " + usuario.getEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Contraseña actualizada correctamente");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error en reset-password: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "mensaje", "Error al restablecer la contraseña: " + e.getMessage()
            ));
        }
    }

    // ============================================
    // LOGOUT
    // ============================================
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest httpRequest) {
        try {
            HttpSession session = httpRequest.getSession(false);
            if (session != null) {
                session.invalidate();
                System.out.println(" Logout");
            }
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Sesión cerrada correctamente"
            ));
        } catch (Exception e) {
            System.err.println("Error en logout: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "mensaje", "Error al cerrar sesión"
            ));
        }
    }
}