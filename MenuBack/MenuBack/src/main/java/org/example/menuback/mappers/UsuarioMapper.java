package org.example.menuback.mappers;

import org.example.menuback.dtos.UsuarioDTO;
import org.example.menuback.modelo.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsuarioMapper extends GenericMapper<UsuarioDTO, Usuario> {

    @Mapping(target = "clave", ignore = true)
    Usuario toEntityFromCADTO(UsuarioDTO.UsuarioCrearDto usuarioCrearDto);
}
