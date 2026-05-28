package com.licencia.licenciabackendapp.controller;

import com.licencia.licenciabackendapp.model.Producto;
import com.licencia.licenciabackendapp.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "http://localhost:8100", allowCredentials = "true")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping("/activos")
    public ResponseEntity<List<Producto>> getProductosActivos() {
        return ResponseEntity.ok(productoService.getProductosActivos());
    }
}