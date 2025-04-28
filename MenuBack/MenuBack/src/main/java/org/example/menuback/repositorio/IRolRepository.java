package org.example.menuback.repositorio;

import org.example.menuback.modelo.Rol;

import java.util.Optional;

public interface IRolRepository extends ICrudGenericoRepository<Rol, Long>{
    Optional<Rol> findByNombre(Rol.RolNombre rolNombre);

    Optional<Rol> findByDescripcion(String nombre);

}
