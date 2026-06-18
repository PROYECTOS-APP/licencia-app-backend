package com.licencia.licenciabackendapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.licencia.licenciabackendapp"})
@EntityScan(basePackages = {"com.licencia.licenciabackendapp.model"})
@EnableJpaRepositories(basePackages = {"com.licencia.licenciabackendapp.repository"})
public class LicenciaBackendAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(LicenciaBackendAppApplication.class, args);
        System.out.println(" Backend iniciado ");
    }
}