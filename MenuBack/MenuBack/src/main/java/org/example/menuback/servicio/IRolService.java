package org.example.menuback.servicio;

import org.example.menuback.modelo.Rol;

import java.util.Optional;

public interface IRolService extends ICrudGenericoService<Rol, Long> {
    public Optional<Rol> getByNombre(Rol.RolNombre rolNombre);
}
