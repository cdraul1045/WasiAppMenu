package org.example.menuback.repositorio;

import org.example.menuback.modelo.UsuarioRol;
import org.example.menuback.modelo.UsuarioRolPK;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IUsuarioRolRepository extends ICrudGenericoRepository<UsuarioRol, UsuarioRolPK>{
    @Query("SELECT ur FROM UsuarioRol ur WHERE ur.usuario.user = :user")//Consulta JPQL
    List<UsuarioRol> findOneByUsuarioUser(@Param("user") String user);
}