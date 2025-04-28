package org.example.menuback.servicio.impl;

import lombok.RequiredArgsConstructor;
import org.example.menuback.modelo.UsuarioRol;
import org.example.menuback.repositorio.IUsuarioRolRepository;
import org.example.menuback.servicio.IUsuarioRolService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioRolServiceImp implements IUsuarioRolService {
    private final IUsuarioRolRepository repo;
    @Override
    public List<UsuarioRol> findOneByUsuarioUser(String user) {
        return repo.findOneByUsuarioUser(user);
    }
    @Override
    public UsuarioRol save(UsuarioRol ur) {
        return repo.save(ur);
    }
}
