package com.licencia.licenciabackendapp.repository;

import com.licencia.licenciabackendapp.model.Licencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface LicenciaRepository extends JpaRepository<Licencia, Long> {

    List<Licencia> findByUsuarioId(Long usuarioId);

    long countByUsuarioId(Long usuarioId);

    // Método para buscar por ID y usuario ID
    Optional<Licencia> findByIdAndUsuarioId(Long id, Long usuarioId);

    // O usando @Query
    @Query("SELECT l FROM Licencia l WHERE l.id = :id AND l.usuario.id = :usuarioId")
    Optional<Licencia> buscarPorIdYUsuario(@Param("id") Long id, @Param("usuarioId") Long usuarioId);
}