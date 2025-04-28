package org.example.menuback.servicio;

import org.example.menuback.dtos.UsuarioDTO;
import org.example.menuback.modelo.Usuario;

public interface IUsuarioService extends ICrudGenericoService<Usuario, Long> {
    public UsuarioDTO login(UsuarioDTO.CredencialesDto credentialsDto);
    public UsuarioDTO register(UsuarioDTO.UsuarioCrearDto userDto);
}