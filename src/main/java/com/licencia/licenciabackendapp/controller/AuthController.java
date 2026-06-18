package com.licencia.licenciabackendapp.controller;

import com.licencia.licenciabackendapp.model.Usuario;
import com.licencia.licenciabackendapp.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(name = "Autenticación", description = "Endpoints para autenticación y gestión de usuarios")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ============================================
    // LOGIN
    // ============================================
    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica a un usuario con email y contraseña. Devuelve los datos del usuario y crea una sesión."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login exitoso",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"success\": true, \"id\": 1, \"nombre\": \"Juan Pérez\", \"email\": \"juan@email.com\", \"avatar\": \"assets/icon/avatar-default.png\", \"message\": \"Login exitoso\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Credenciales incorrectas",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = "{\"success\": false, \"mensaje\": \"Usuario no encontrado\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    public ResponseEntity<?> login(
            @Parameter(description = "Credenciales de acceso", required = true)
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        try {
            String email = request.get("email");
            String password = request.get("password");

            System.out.println("📥 LOGIN: " + email);

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
    @Operation(
            summary = "Registrar nuevo usuario",
            description = "Crea una nueva cuenta de usuario con nombre, email y contraseña."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Registro exitoso",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = "{\"success\": true, \"id\": 2, \"nombre\": \"Nuevo Usuario\", \"email\": \"nuevo@email.com\", \"avatar\": \"assets/icon/avatar-default.png\", \"message\": \"Registro exitoso\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o email ya registrado"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    public ResponseEntity<?> registro(
            @Parameter(description = "Datos del nuevo usuario", required = true)
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
            System.err.println(" Error en registro: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "mensaje", "Error al registrar usuario"
            ));
        }
    }

    // ============================================
    // RECUPERAR CONTRASEÑA - FORGOT PASSWORD
    // ============================================
    @PostMapping("/forgot-password")
    @Operation(
            summary = "Recuperar contraseña",
            description = "Envía un enlace de recuperación al email del usuario. Genera un token válido por 1 hora."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Enlace enviado exitosamente",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = "{\"success\": true, \"token\": \"uuid-token\", \"message\": \"Se ha enviado un enlace para restablecer tu contraseña\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Email no registrado"
            )
    })
    public ResponseEntity<?> forgotPassword(
            @Parameter(description = "Email del usuario", required = true)
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

            // Generar token único
            String token = UUID.randomUUID().toString();
            usuario.setResetToken(token);
            usuario.setResetTokenExpiry(LocalDateTime.now().plusHours(1));

            usuarioService.actualizar(usuario);

            System.out.println("Token generado para " + email + ": " + token);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", token);
            response.put("message", "Se ha enviado un enlace para restablecer tu contraseña");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error en forgot-password: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "mensaje", "Error al procesar la solicitud"
            ));
        }
    }

    // ============================================
    // RESTABLECER CONTRASEÑA - RESET PASSWORD
    // ============================================
    @PostMapping("/reset-password")
    @Operation(
            summary = "Restablecer contraseña",
            description = "Restablece la contraseña usando el token enviado por email."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Contraseña actualizada exitosamente",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = "{\"success\": true, \"message\": \"Contraseña actualizada correctamente\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Token inválido o expirado"
            )
    })
    public ResponseEntity<?> resetPassword(
            @Parameter(description = "Token de recuperación y nueva contraseña", required = true)
            @RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            String newPassword = request.get("newPassword");

            System.out.println("Restableciendo contraseña con token: " + token);

            // Validar token
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "mensaje", "Token inválido"
                ));
            }

            // Validar contraseña
            if (newPassword == null || newPassword.length() < 6) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "mensaje", "La contraseña debe tener al menos 6 caracteres"
                ));
            }

            // Buscar usuario por token
            var usuarioOpt = usuarioService.findByResetToken(token);

            if (usuarioOpt.isEmpty()) {
                System.out.println("Token no encontrado: " + token);
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "mensaje", "Token inválido. El enlace no es válido o ya fue usado."
                ));
            }

            Usuario usuario = usuarioOpt.get();
            System.out.println("📧 Usuario encontrado: " + usuario.getEmail());

            // Verificar si el token ha expirado
            if (usuario.getResetTokenExpiry() == null) {
                System.out.println("Token sin fecha de expiración");
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "mensaje", "Token inválido. El enlace no es válido."
                ));
            }

            if (usuario.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
                System.out.println("Token expirado: " + usuario.getResetTokenExpiry());
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "mensaje", "El enlace ha expirado. Solicita uno nuevo"
                ));
            }

            // Actualizar contraseña
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
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "mensaje", "Error al restablecer la contraseña: " + e.getMessage()
            ));
        }
    }

    // ============================================
    // VALIDAR TOKEN
    // ============================================
    @PostMapping("/validate-token")
    @Operation(
            summary = "Validar token de recuperación",
            description = "Verifica si un token de recuperación es válido y no ha expirado."
    )
    public ResponseEntity<?> validateToken(
            @Parameter(description = "Token a validar", required = true)
            @RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");

            System.out.println("🔍 Validando token: " + token);

            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "mensaje", "Token inválido"
                ));
            }

            var usuarioOpt = usuarioService.findByResetToken(token);

            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "mensaje", "Token inválido"
                ));
            }

            Usuario usuario = usuarioOpt.get();

            if (usuario.getResetTokenExpiry() == null ||
                    usuario.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "mensaje", "Token expirado"
                ));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("email", usuario.getEmail());
            response.put("message", "Token válido");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error en validate-token: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "mensaje", "Error al validar token"
            ));
        }
    }

    // ============================================
    // LOGOUT
    // ============================================
    @PostMapping("/logout")
    @Operation(
            summary = "Cerrar sesión",
            description = "Invalida la sesión actual del usuario."
    )
    public ResponseEntity<?> logout(HttpServletRequest httpRequest) {
        try {
            HttpSession session = httpRequest.getSession(false);
            if (session != null) {
                session.invalidate();
                System.out.println("Logout exitoso");
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

    // ============================================
    // OBTENER USUARIO ACTUAL
    // ============================================
    @GetMapping("/me")
    @Operation(
            summary = "Obtener usuario actual",
            description = "Obtiene los datos del usuario autenticado en la sesión actual."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario encontrado",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = "{\"success\": true, \"id\": 1, \"nombre\": \"Juan Pérez\", \"email\": \"juan@email.com\", \"avatar\": \"assets/icon/avatar-default.png\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado"
            )
    })
    public ResponseEntity<?> getCurrentUser(HttpServletRequest httpRequest) {
        try {
            HttpSession session = httpRequest.getSession(false);

            if (session == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "mensaje", "No autenticado"
                ));
            }

            Long usuarioId = (Long) session.getAttribute("usuarioId");

            if (usuarioId == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "mensaje", "No autenticado"
                ));
            }

            var usuarioOpt = usuarioService.findById(usuarioId);

            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "mensaje", "Usuario no encontrado"
                ));
            }

            Usuario usuario = usuarioOpt.get();

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

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error en getCurrentUser: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "mensaje", "Error al obtener usuario"
            ));
        }
    }
}