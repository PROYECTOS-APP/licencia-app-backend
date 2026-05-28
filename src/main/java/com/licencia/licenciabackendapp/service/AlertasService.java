package com.licencia.licenciabackendapp.service;

import com.licencia.licenciabackendapp.model.Alerta;
import com.licencia.licenciabackendapp.repository.AlertasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class AlertasService {

    @Autowired
    private AlertasRepository alertasRepository;

    @Transactional
    public Alerta crearAlerta(Alerta alerta) {
        return alertasRepository.save(alerta);
    }

    public List<Alerta> getAlertasByUsuario(Long usuarioId) {
        return alertasRepository.findByUsuarioId(usuarioId);
    }

    public Optional<Alerta> getAlertaById(Long id) {
        return alertasRepository.findById(id);
    }

    @Transactional
    public void eliminarAlerta(Long id) {
        alertasRepository.deleteById(id);
    }

    @Transactional
    public void eliminarAlertaPorIdYUsuario(Long id, Long usuarioId) {
        alertasRepository.deleteByIdAndUsuarioId(id, usuarioId);
    }
}