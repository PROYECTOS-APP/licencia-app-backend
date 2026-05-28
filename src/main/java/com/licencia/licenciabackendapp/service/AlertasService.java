package com.licencia.licenciabackendapp.service;

import com.licencia.licenciabackendapp.model.Alerta;
import com.licencia.licenciabackendapp.repository.AlertasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AlertasService {

    @Autowired
    private AlertasRepository alertasRepository;

    public Alerta crearAlerta(Alerta alerta) {
        return alertasRepository.save(alerta);
    }

    public List<Alerta> getAlertasByUsuario(Long usuarioId) {
        return alertasRepository.findByUsuarioId(usuarioId);
    }

    public void eliminarAlerta(Long id, Long usuarioId) {
        alertasRepository.deleteByIdAndUsuarioId(id, usuarioId);
    }
}