package com.licencia.licenciabackendapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")  // Usa perfil de test
class LicenciaBackendAppApplicationTests {

    @Test
    void contextLoads() {
        // Verifica que el contexto de Spring cargue correctamente
        System.out.println("✅ Test de contexto ejecutado correctamente");
    }
}