package com.fau.biblioteca_api.controller;

import com.fau.biblioteca_api.dto.LoginRequest;
import com.fau.biblioteca_api.dto.RecuperarRequest;
import com.fau.biblioteca_api.dto.SesionResponse;
import com.fau.biblioteca_api.entity.AccionAuditoria;
import com.fau.biblioteca_api.entity.UsuarioSistema;
import com.fau.biblioteca_api.service.AuditoriaService;
import com.fau.biblioteca_api.service.UsuarioActualService;
import com.fau.biblioteca_api.service.UsuarioSistemaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final UsuarioActualService usuarioActualService;
    private final AuditoriaService auditoriaService;
    private final UsuarioSistemaService usuarioSistemaService;

    public AuthController(
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            UsuarioActualService usuarioActualService,
            AuditoriaService auditoriaService,
            UsuarioSistemaService usuarioSistemaService) {

        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.usuarioActualService = usuarioActualService;
        this.auditoriaService = auditoriaService;
        this.usuarioSistemaService = usuarioSistemaService;
    }

    @PostMapping("/login")
    public SesionResponse login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        if (request.getUsuario() == null || request.getPassword() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Usuario y contraseña son obligatorios");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsuario(),
                            request.getPassword()));

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, httpRequest, httpResponse);
        } catch (BadCredentialsException | DisabledException ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Credenciales inválidas o usuario desactivado");
        }

        UsuarioSistema usuario = usuarioActualService.obtener();

        auditoriaService.registrar(
                usuario,
                "USUARIOS_SISTEMA",
                usuario.getIdUsuario(),
                AccionAuditoria.LOGIN,
                null,
                usuario.getUsuario(),
                "Inicio de sesión"
        );

        return SesionResponse.desde(usuario);
    }

    @PostMapping("/recuperar/preguntas")
    public Map<String, List<String>> preguntas(@RequestBody RecuperarRequest request) {
        return Map.of("preguntas", usuarioSistemaService.preguntasDe(request.getUsuario()));
    }

    @PostMapping("/recuperar")
    public void recuperar(@RequestBody RecuperarRequest request) {
        usuarioSistemaService.restablecer(request.getUsuario(), request.getRespuestas(), request.getPassword());
    }

    @GetMapping("/me")
    public SesionResponse me() {
        return SesionResponse.desde(usuarioActualService.obtener());
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
