package com.fau.biblioteca_api.service;

import com.fau.biblioteca_api.entity.RolUsuario;
import com.fau.biblioteca_api.entity.UsuarioSistema;
import com.fau.biblioteca_api.repository.UsuarioSistemaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UsuarioSistemaService {

    private final UsuarioSistemaRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioSistemaService(
            UsuarioSistemaRepository repository,
            PasswordEncoder passwordEncoder) {

        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UsuarioSistema> listar() {
        return repository.findAll();
    }

    public UsuarioSistema obtener(Integer id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));
    }

    @Transactional
    public UsuarioSistema crear(UsuarioSistema usuario) {
        if (esBlanco(usuario.getNombre()) || esBlanco(usuario.getApellido())) {
            throw new RuntimeException("Nombre y apellido son obligatorios");
        }
        if (esBlanco(usuario.getUsuario())) {
            throw new RuntimeException("El nombre de usuario es obligatorio");
        }
        if (esBlanco(usuario.getPasswordHash())) {
            throw new RuntimeException("La contraseña es obligatoria");
        }
        if (esBlanco(usuario.getCorreo())) {
            throw new RuntimeException("El correo es obligatorio");
        }
        if (repository.findByUsuario(usuario.getUsuario()).isPresent()) {
            throw new RuntimeException("El nombre de usuario ya existe");
        }
        if (usuario.getRol() != RolUsuario.ayudante && usuario.getRol() != RolUsuario.administrador) {
            throw new RuntimeException("Elige si la cuenta es de ayudante o de administrador");
        }

        usuario.setPasswordHash(passwordEncoder.encode(usuario.getPasswordHash()));
        if (usuario.getActivo() == null) {
            usuario.setActivo(true);
        }
        if (usuario.getRol() == RolUsuario.administrador) {
            exigirPreguntas(usuario);
            usuario.setPregunta1(usuario.getPregunta1().trim());
            usuario.setPregunta2(usuario.getPregunta2().trim());
            usuario.setPregunta3(usuario.getPregunta3().trim());
            usuario.setRespuesta1(passwordEncoder.encode(usuario.getRespuesta1().trim().toLowerCase()));
            usuario.setRespuesta2(passwordEncoder.encode(usuario.getRespuesta2().trim().toLowerCase()));
            usuario.setRespuesta3(passwordEncoder.encode(usuario.getRespuesta3().trim().toLowerCase()));
            concederTodo(usuario);
        } else {
            usuario.setPregunta1(null);
            usuario.setPregunta2(null);
            usuario.setPregunta3(null);
            usuario.setRespuesta1(null);
            usuario.setRespuesta2(null);
            usuario.setRespuesta3(null);
            normalizarPermisos(usuario);
        }
        return repository.save(usuario);
    }

    @Transactional
    public UsuarioSistema actualizar(Integer id, UsuarioSistema datos) {
        UsuarioSistema usuario = obtener(id);

        if (!esBlanco(datos.getNombre())) {
            usuario.setNombre(datos.getNombre());
        }
        if (!esBlanco(datos.getApellido())) {
            usuario.setApellido(datos.getApellido());
        }
        if (!esBlanco(datos.getUsuario())
                && !datos.getUsuario().equals(usuario.getUsuario())) {
            if (repository.findByUsuario(datos.getUsuario()).isPresent()) {
                throw new RuntimeException("El nombre de usuario ya existe");
            }
            usuario.setUsuario(datos.getUsuario());
        }
        if (datos.getCorreo() != null) {
            if (esBlanco(datos.getCorreo())) {
                throw new RuntimeException("El correo es obligatorio");
            }
            usuario.setCorreo(datos.getCorreo().trim());
        }
        if (datos.getActivo() != null) {
            if (usuario.getRol() == RolUsuario.administrador
                    && Boolean.FALSE.equals(datos.getActivo())
                    && !quedaOtroAdminActivo(usuario.getIdUsuario())) {
                throw new RuntimeException("Debe quedar al menos un administrador activo");
            }
            usuario.setActivo(datos.getActivo());
        }
        if (!esBlanco(datos.getPasswordHash())) {
            usuario.setPasswordHash(passwordEncoder.encode(datos.getPasswordHash()));
        }
        if (usuario.getRol() == RolUsuario.administrador) {
            copiarPregunta(datos.getPregunta1(), usuario::setPregunta1);
            copiarPregunta(datos.getPregunta2(), usuario::setPregunta2);
            copiarPregunta(datos.getPregunta3(), usuario::setPregunta3);
            copiarRespuesta(datos.getRespuesta1(), usuario::setRespuesta1);
            copiarRespuesta(datos.getRespuesta2(), usuario::setRespuesta2);
            copiarRespuesta(datos.getRespuesta3(), usuario::setRespuesta3);
        } else {
            usuario.setPuedeEliminar(datos.getPuedeEliminar());
            usuario.setPuedeActualizar(datos.getPuedeActualizar());
            usuario.setPuedeCrear(datos.getPuedeCrear());
            usuario.setPuedeCrearLibros(datos.getPuedeCrearLibros());
            usuario.setPuedeCrearAutores(datos.getPuedeCrearAutores());
            usuario.setPuedeCrearEditoriales(datos.getPuedeCrearEditoriales());
            usuario.setPuedeCrearUbicaciones(datos.getPuedeCrearUbicaciones());
            usuario.setPuedeCrearEjemplares(datos.getPuedeCrearEjemplares());
            usuario.setPuedeCrearLectores(datos.getPuedeCrearLectores());
            usuario.setPuedeCrearPrestamos(datos.getPuedeCrearPrestamos());
            usuario.setPuedeCrearIncidencias(datos.getPuedeCrearIncidencias());
            normalizarPermisos(usuario);
        }

        return repository.save(usuario);
    }

    public List<String> preguntasDe(String nombreUsuario) {
        UsuarioSistema usuario = buscarActivo(nombreUsuario);
        if (usuario.getRol() != RolUsuario.administrador) {
            throw mal("Solo el administrador puede reestablecer su contraseña. Los demás perfiles los cambia el administrador.");
        }
        if (esBlanco(usuario.getPregunta1()) || esBlanco(usuario.getPregunta2()) || esBlanco(usuario.getPregunta3())
                || esBlanco(usuario.getRespuesta1()) || esBlanco(usuario.getRespuesta2()) || esBlanco(usuario.getRespuesta3())) {
            throw mal("El administrador todavía no ha registrado las tres preguntas de seguridad");
        }
        return List.of(usuario.getPregunta1(), usuario.getPregunta2(), usuario.getPregunta3());
    }

    @Transactional
    public void restablecer(String nombreUsuario, List<String> respuestas, String password) {
        UsuarioSistema usuario = buscarActivo(nombreUsuario);
        if (usuario.getRol() != RolUsuario.administrador) {
            throw mal("Solo el administrador puede reestablecer su contraseña. Los demás perfiles los cambia el administrador.");
        }
        if (respuestas == null || respuestas.size() != 3 || esBlanco(password)) {
            throw mal("Debes responder las tres preguntas y escribir una contraseña nueva");
        }
        if (!coincide(respuestas.get(0), usuario.getRespuesta1())
                || !coincide(respuestas.get(1), usuario.getRespuesta2())
                || !coincide(respuestas.get(2), usuario.getRespuesta3())) {
            throw mal("Alguna respuesta no coincide");
        }
        usuario.setPasswordHash(passwordEncoder.encode(password));
        repository.save(usuario);
    }

    private UsuarioSistema buscarActivo(String nombreUsuario) {
        if (esBlanco(nombreUsuario)) {
            throw mal("El usuario es obligatorio");
        }
        UsuarioSistema usuario = repository.findByUsuario(nombreUsuario.trim())
                .orElseThrow(() -> mal("Usuario no encontrado"));
        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw mal("El usuario está desactivado");
        }
        return usuario;
    }

    private boolean coincide(String respuesta, String hash) {
        if (esBlanco(respuesta) || esBlanco(hash)) return false;
        return passwordEncoder.matches(respuesta.trim().toLowerCase(), hash);
    }

    private void copiarPregunta(String valor, java.util.function.Consumer<String> asignar) {
        if (!esBlanco(valor)) asignar.accept(valor.trim());
    }

    private void copiarRespuesta(String valor, java.util.function.Consumer<String> asignar) {
        if (!esBlanco(valor)) asignar.accept(passwordEncoder.encode(valor.trim().toLowerCase()));
    }

    private void exigirPreguntas(UsuarioSistema usuario) {
        if (esBlanco(usuario.getPregunta1()) || esBlanco(usuario.getPregunta2()) || esBlanco(usuario.getPregunta3())
                || esBlanco(usuario.getRespuesta1()) || esBlanco(usuario.getRespuesta2()) || esBlanco(usuario.getRespuesta3())) {
            throw new RuntimeException("El administrador debe registrar las tres preguntas y sus respuestas");
        }
    }

    private void concederTodo(UsuarioSistema usuario) {
        usuario.setPuedeEliminar(true);
        usuario.setPuedeActualizar(true);
        usuario.setPuedeCrear(true);
        usuario.setPuedeCrearLibros(true);
        usuario.setPuedeCrearAutores(true);
        usuario.setPuedeCrearEditoriales(true);
        usuario.setPuedeCrearUbicaciones(true);
        usuario.setPuedeCrearEjemplares(true);
        usuario.setPuedeCrearLectores(true);
        usuario.setPuedeCrearPrestamos(true);
        usuario.setPuedeCrearIncidencias(true);
    }

    private boolean quedaOtroAdminActivo(Integer exceptoId) {
        return repository.findAll().stream().anyMatch(otro ->
                otro.getRol() == RolUsuario.administrador
                        && !Boolean.FALSE.equals(otro.getActivo())
                        && !otro.getIdUsuario().equals(exceptoId));
    }

    private void normalizarPermisos(UsuarioSistema usuario) {
        boolean crear = Boolean.TRUE.equals(usuario.getPuedeCrear());
        usuario.setPuedeEliminar(Boolean.TRUE.equals(usuario.getPuedeEliminar()));
        usuario.setPuedeActualizar(Boolean.TRUE.equals(usuario.getPuedeActualizar()));
        usuario.setPuedeCrear(crear);
        usuario.setPuedeCrearLibros(crear && Boolean.TRUE.equals(usuario.getPuedeCrearLibros()));
        usuario.setPuedeCrearAutores(crear && Boolean.TRUE.equals(usuario.getPuedeCrearAutores()));
        usuario.setPuedeCrearEditoriales(crear && Boolean.TRUE.equals(usuario.getPuedeCrearEditoriales()));
        usuario.setPuedeCrearUbicaciones(crear && Boolean.TRUE.equals(usuario.getPuedeCrearUbicaciones()));
        usuario.setPuedeCrearEjemplares(crear && Boolean.TRUE.equals(usuario.getPuedeCrearEjemplares()));
        usuario.setPuedeCrearLectores(crear && Boolean.TRUE.equals(usuario.getPuedeCrearLectores()));
        usuario.setPuedeCrearPrestamos(crear && Boolean.TRUE.equals(usuario.getPuedeCrearPrestamos()));
        usuario.setPuedeCrearIncidencias(crear && Boolean.TRUE.equals(usuario.getPuedeCrearIncidencias()));
    }

    private ResponseStatusException mal(String mensaje) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
    }

    private boolean esBlanco(String valor) {
        return valor == null || valor.isBlank();
    }
}
