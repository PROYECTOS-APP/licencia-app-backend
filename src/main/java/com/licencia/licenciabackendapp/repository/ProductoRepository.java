package com.licencia.licenciabackendapp.repository;

import com.licencia.licenciabackendapp.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface  ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByActivoTrue();
}