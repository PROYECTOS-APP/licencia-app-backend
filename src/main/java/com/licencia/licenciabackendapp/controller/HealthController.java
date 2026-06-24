// src/main/java/com/licencia/licenciabackendapp/controller/HealthController.java
package com.licencia.licenciabackendapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(
        origins = {
                "http://localhost:8100",
                "http://localhost:4200",
                "http://localhost:8080"
        },
        allowCredentials = "true",
        allowedHeaders = "*",
        methods = {org.springframework.web.bind.annotation.RequestMethod.GET,
                org.springframework.web.bind.annotation.RequestMethod.OPTIONS}
)
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("service", "LicenciaBackend");
        response.put("version", "1.0.0");
        response.put("database", "MySQL");
        response.put("cors", "Configurado correctamente");

        System.out.println(" check realizado: " + LocalDateTime.now());
        System.out.println("Respuesta enviada: " + response);

        return ResponseEntity.ok(response);
    }
}