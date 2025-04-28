package org.example.menuback.repositorio;

import org.example.menuback.modelo.Usuario;

import java.util.Optional;

public interface IUsuarioRepository extends ICrudGenericoRepository<Usuario, Long>{

    Optional<Usuario> findOneByUser(String user);
}
