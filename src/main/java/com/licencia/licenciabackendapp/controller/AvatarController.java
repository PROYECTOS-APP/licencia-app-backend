package com.licencia.licenciabackendapp.controller;

import com.licencia.licenciabackendapp.model.Usuario;
import com.licencia.licenciabackendapp.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/avatar")
@CrossOrigin(origins = "http://localhost:8100", allowCredentials = "true")
public class AvatarController {

    @Autowired
    private UsuarioService usuarioService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.url}")
    private String uploadUrl;

    // ============================================
    // SUBIR AVATAR
    // ============================================
    @PostMapping("/upload")
    public ResponseEntity<?> uploadAvatar(
            @RequestParam("avatar") MultipartFile file,
            HttpServletRequest httpRequest) {

        try {
            // Obtener usuario de la sesión
            HttpSession session = httpRequest.getSession(false);
            if (session == null) {
                return ResponseEntity.status(401).body(
                        Map.of("success", false, "message", "No autenticado")
                );
            }

            Long usuarioId = (Long) session.getAttribute("usuarioId");
            if (usuarioId == null) {
                return ResponseEntity.status(401).body(
                        Map.of("success", false, "message", "No autenticado")
                );
            }

            Usuario usuario = usuarioService.findById(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Validar archivo
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Map.of("success", false, "message", "No se ha seleccionado ningún archivo")
                );
            }

            // Validar tipo de archivo
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(
                        Map.of("success", false, "message", "Solo se permiten imágenes")
                );
            }

            // Validar tamaño (5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(
                        Map.of("success", false, "message", "La imagen no puede superar los 5MB")
                );
            }

            // Crear directorio si no existe
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Eliminar avatar anterior si no es el default
            String oldAvatar = usuario.getAvatar();
            if (oldAvatar != null && !oldAvatar.equals("assets/icon/avatar-default.png")) {
                String oldFilename = oldAvatar.substring(oldAvatar.lastIndexOf("/") + 1);
                File oldFile = new File(uploadDir + File.separator + oldFilename);
                if (oldFile.exists()) {
                    oldFile.delete();
                    System.out.println("🗑️ Avatar anterior eliminado: " + oldFilename);
                }
            }

            // Generar nombre único
            String extension = getExtension(file.getOriginalFilename());
            String filename = "avatar-" + usuarioId + "-" + UUID.randomUUID().toString() + extension;

            // Guardar archivo
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Guardar URL en base de datos
            String avatarUrl = uploadUrl + filename;
            usuario.setAvatar(avatarUrl);
            usuarioService.actualizar(usuario);

            System.out.println("✅ Avatar actualizado para usuario: " + usuario.getEmail() + " - " + avatarUrl);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Avatar actualizado correctamente");
            response.put("avatar", avatarUrl);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            System.err.println("❌ Error al guardar avatar: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("success", false, "message", "Error al guardar el archivo: " + e.getMessage())
            );
        } catch (Exception e) {
            System.err.println("❌ Error en upload: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("success", false, "message", "Error al procesar la solicitud: " + e.getMessage())
            );
        }
    }

    // ============================================
    // ELIMINAR AVATAR
    // ============================================
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteAvatar(HttpServletRequest httpRequest) {
        try {
            HttpSession session = httpRequest.getSession(false);
            if (session == null) {
                return ResponseEntity.status(401).body(
                        Map.of("success", false, "message", "No autenticado")
                );
            }

            Long usuarioId = (Long) session.getAttribute("usuarioId");
            if (usuarioId == null) {
                return ResponseEntity.status(401).body(
                        Map.of("success", false, "message", "No autenticado")
                );
            }

            Usuario usuario = usuarioService.findById(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Eliminar archivo físico si no es el default
            String currentAvatar = usuario.getAvatar();
            if (currentAvatar != null && !currentAvatar.equals("assets/icon/avatar-default.png")) {
                String filename = currentAvatar.substring(currentAvatar.lastIndexOf("/") + 1);
                File file = new File(uploadDir + File.separator + filename);
                if (file.exists()) {
                    file.delete();
                    System.out.println("🗑️ Avatar eliminado: " + filename);
                }
            }

            // Resetear a avatar por defecto
            usuario.setAvatar("assets/icon/avatar-default.png");
            usuarioService.actualizar(usuario);

            System.out.println("✅ Avatar resetear a default para usuario: " + usuario.getEmail());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Avatar eliminado correctamente",
                    "avatar", "assets/icon/avatar-default.png"
            ));

        } catch (Exception e) {
            System.err.println("❌ Error al eliminar avatar: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("success", false, "message", "Error al eliminar avatar: " + e.getMessage())
            );
        }
    }

    // ============================================
    // OBTENER AVATAR POR USUARIO
    // ============================================
    @GetMapping("/{userId}")
    public ResponseEntity<?> getAvatar(@PathVariable Long userId) {
        try {
            Usuario usuario = usuarioService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "avatar", usuario.getAvatar() != null ? usuario.getAvatar() : "assets/icon/avatar-default.png"
            ));

        } catch (Exception e) {
            System.err.println("❌ Error al obtener avatar: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("success", false, "message", "Usuario no encontrado")
            );
        }
    }

    // ============================================
    // MÉTODOS AUXILIARES
    // ============================================

    private String getExtension(String filename) {
        if (filename == null) return ".jpg";
        int lastDot = filename.lastIndexOf(".");
        if (lastDot == -1) return ".jpg";
        return filename.substring(lastDot);
    }
}