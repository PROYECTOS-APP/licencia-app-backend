package com.licencia.licenciabackendapp.service;

import com.licencia.licenciabackendapp.model.Producto;
import com.licencia.licenciabackendapp.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    public List<Producto> getProductosActivos() {
        return productoRepository.findByActivoTrue();
    }

    public Producto getProductoById(Long id) {
        return productoRepository.findById(id).orElse(null);
    }
}