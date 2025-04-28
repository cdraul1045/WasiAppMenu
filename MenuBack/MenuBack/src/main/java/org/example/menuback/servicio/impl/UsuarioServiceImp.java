package org.example.menuback.servicio.impl;

import lombok.RequiredArgsConstructor;
import org.example.menuback.dtos.UsuarioDTO;
import org.example.menuback.excepciones.ModelNotFoundException;
import org.example.menuback.mappers.UsuarioMapper;
import org.example.menuback.modelo.Rol;
import org.example.menuback.modelo.Usuario;
import org.example.menuback.modelo.UsuarioRol;
import org.example.menuback.repositorio.ICrudGenericoRepository;
import org.example.menuback.repositorio.IUsuarioRepository;
import org.example.menuback.servicio.IRolService;
import org.example.menuback.servicio.IUsuarioRolService;
import org.example.menuback.servicio.IUsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.CharBuffer;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UsuarioServiceImp extends CrudGenericoServiceImp<Usuario, Long> implements IUsuarioService {
    private final IUsuarioRepository repo;

    private final IRolService rolService;
    private final IUsuarioRolService iurService;

    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper userMapper;

    @Override
    protected ICrudGenericoRepository<Usuario, Long> getRepo() {
        return repo;
    }



    @Override
    public UsuarioDTO login(UsuarioDTO.CredencialesDto credentialsDto) {
        Usuario user = repo.findOneByUser(credentialsDto.user())
                .orElseThrow(() -> new ModelNotFoundException("Unknown user", HttpStatus.NOT_FOUND));

        if (passwordEncoder.matches(CharBuffer.wrap(credentialsDto.clave()), user.getClave())) {
            return userMapper.toDTO(user);
        }

        throw new ModelNotFoundException("Invalid password", HttpStatus.BAD_REQUEST);
    }



    @Override
    public UsuarioDTO register(UsuarioDTO.UsuarioCrearDto userDto) {
        Optional<Usuario> optionalUser = repo.findOneByUser(userDto.user());
        if (optionalUser.isPresent()) {
            throw new ModelNotFoundException("Login already exists", HttpStatus.BAD_REQUEST);
        }
        Usuario user = userMapper.toEntityFromCADTO(userDto);
        user.setClave(passwordEncoder.encode(CharBuffer.wrap(userDto.clave())));
        Usuario savedUser = repo.save(user);
        Rol r;
        switch (userDto.rol()){
            case "ADMIN":{
                r=rolService.getByNombre(Rol.RolNombre.ADMIN).orElse(null);
            } break;
            default:{
                r=rolService.getByNombre(Rol.RolNombre.USER).orElse(null);
            } break;
        }

        /*UsuarioRol u=new UsuarioRol();
        u.setRol(r);
        u.setUsuario(savedUser);
        iurService.save(u);
        */

        iurService.save(UsuarioRol.builder()
                .usuario(savedUser)
                .rol(r)
                .build());
        return userMapper.toDTO(savedUser);
    }
}
