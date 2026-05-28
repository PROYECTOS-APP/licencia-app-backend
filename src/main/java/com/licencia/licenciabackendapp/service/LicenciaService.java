package com.licencia.licenciabackendapp.service;

import com.licencia.licenciabackendapp.model.Licencia;
import com.licencia.licenciabackendapp.model.Usuario;
import com.licencia.licenciabackendapp.repository.LicenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class LicenciaService {

    @Autowired
    private LicenciaRepository licenciaRepository;

    public Licencia crearLicencia(Licencia licencia, Usuario usuario) {
        licencia.setUsuario(usuario);
        return licenciaRepository.save(licencia);
    }

    public List<Licencia> getLicenciasByUsuario(Long usuarioId) {
        return licenciaRepository.findByUsuarioId(usuarioId);
    }

    public Optional<Licencia> getLicenciaById(Long id) {
        return licenciaRepository.findById(id);
    }

    public long countByUsuarioId(Long usuarioId) {
        return licenciaRepository.countByUsuarioId(usuarioId);
    }
}