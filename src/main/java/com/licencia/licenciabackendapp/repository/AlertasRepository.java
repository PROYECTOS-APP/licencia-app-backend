package com.licencia.licenciabackendapp.repository;

import com.licencia.licenciabackendapp.model.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface AlertasRepository extends JpaRepository<Alerta, Long> {

    List<Alerta> findByUsuarioId(Long usuarioId);

    Optional<Alerta> findByIdAndUsuarioId(Long id, Long usuarioId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Alerta a WHERE a.id = :id AND a.usuario.id = :usuarioId")
    void deleteByIdAndUsuarioId(@Param("id") Long id, @Param("usuarioId") Long usuarioId);
}