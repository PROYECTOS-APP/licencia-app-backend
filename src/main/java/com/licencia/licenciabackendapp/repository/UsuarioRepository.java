package com.licencia.licenciabackendapp.repository;

import com.licencia.licenciabackendapp.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByResetToken(String resetToken);
    boolean existsByEmail(String email);

    @Modifying
    @Transactional
    @Query("UPDATE Usuario u SET u.password = :password, u.resetToken = null, u.resetTokenExpiry = null, u.passwordUpdatedAt = CURRENT_TIMESTAMP WHERE u.resetToken = :token")
    int updatePasswordByResetToken(@Param("token") String token, @Param("password") String password);
}