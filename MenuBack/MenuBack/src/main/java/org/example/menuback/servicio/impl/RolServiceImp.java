package org.example.menuback.servicio.impl;

import lombok.RequiredArgsConstructor;
import org.example.menuback.modelo.Rol;
import org.example.menuback.repositorio.ICrudGenericoRepository;
import org.example.menuback.repositorio.IRolRepository;
import org.example.menuback.servicio.IRolService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class RolServiceImp extends CrudGenericoServiceImp<Rol, Long> implements IRolService {
    private final IRolRepository repo;
    @Override
    protected ICrudGenericoRepository<Rol, Long> getRepo() {
        return repo;
    }
    @Override
    public Optional<Rol> getByNombre(Rol.RolNombre rolNombre) {
        return repo.findByNombre(rolNombre);
    }

}