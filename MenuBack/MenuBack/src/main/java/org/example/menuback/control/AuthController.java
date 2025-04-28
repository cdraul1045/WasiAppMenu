package org.example.menuback.control;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.menuback.dtos.UsuarioDTO;
import org.example.menuback.security.JwtTokenUtil;
import org.example.menuback.security.JwtUserDetailsService;
import org.example.menuback.servicio.IUsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class AuthController {
    private final IUsuarioService userService;
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtUserDetailsService jwtUserDetailsService;
    @PostMapping("/login")
    public ResponseEntity<UsuarioDTO> login(@RequestBody @Valid UsuarioDTO.CredencialesDto credentialsDto, HttpServletRequest request) {
        UsuarioDTO userDto = userService.login(credentialsDto);
        final UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(credentialsDto.user());
        userDto.setToken(jwtTokenUtil.generateToken(userDetails));
        request.getSession().setAttribute("USER_SESSION", userDto.getUser());
        return ResponseEntity.ok(userDto);
    }
    @PostMapping("/register")
    public ResponseEntity<UsuarioDTO> register(@RequestBody @Valid UsuarioDTO.UsuarioCrearDto user) {
        System.out.println("Passss...."+ user.rol());
        UsuarioDTO createdUser = userService.register(user);
        final UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(user.user());
        createdUser.setToken(jwtTokenUtil.generateToken(userDetails));
        //createdUser.setClave("");
        //createdUser.setToken(userAuthenticationProvider.createToken(createdUser));
        return ResponseEntity.created(URI.create("/users/" + createdUser.getUser())).body(createdUser);
    }
}

