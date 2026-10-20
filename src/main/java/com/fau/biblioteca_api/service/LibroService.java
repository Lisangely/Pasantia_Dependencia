package com.fau.biblioteca_api.service;

import com.fau.biblioteca_api.dto.LibroRequest;
import com.fau.biblioteca_api.entity.*;
import com.fau.biblioteca_api.repository.AuditoriaRepository;
import com.fau.biblioteca_api.repository.AutorRepository;
import com.fau.biblioteca_api.repository.EditorialRepository;
import com.fau.biblioteca_api.repository.LibroAutorRepository;
import com.fau.biblioteca_api.repository.LibroRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class LibroService {

    private final LibroRepository libroRepository;
    private final EditorialRepository editorialRepository;
    private final AuditoriaService auditoriaService;
    private final AuditoriaRepository auditoriaRepository;
    private final LibroAutorRepository libroAutorRepository;
    private final AutorRepository autorRepository;
    private final UsuarioActualService usuarioActualService;
    private final PermisoService permisoService;

    public LibroService(
            LibroRepository libroRepository,
            EditorialRepository editorialRepository,
            AuditoriaService auditoriaService,
            AuditoriaRepository auditoriaRepository,
            LibroAutorRepository libroAutorRepository,
            AutorRepository autorRepository,
            UsuarioActualService usuarioActualService,
            PermisoService permisoService) {

        this.libroRepository = libroRepository;
        this.editorialRepository = editorialRepository;
        this.auditoriaService = auditoriaService;
        this.auditoriaRepository = auditoriaRepository;
        this.libroAutorRepository = libroAutorRepository;
        this.autorRepository = autorRepository;
        this.usuarioActualService = usuarioActualService;
        this.permisoService = permisoService;
    }

    public List<Libro> listar() {
        return libroRepository.findAll();
    }

    public List<Libro> buscar(
            String titulo,
            String autor,
            String cota,
            String tipo) {

        return libroRepository.buscar(
                enBlanco(titulo),
                enBlanco(autor),
                enBlanco(cota),
                enBlanco(tipo));
    }

    public Libro obtener(Integer id) {

        return libroRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Libro no encontrado"));
    }

    @Transactional
    public Libro crear(LibroRequest request) {

        Libro libro = new Libro();

        libro.setTitulo(request.getTitulo());
        libro.setTipo(request.getTipo());
        libro.setFechaPublicacion(
                request.getFechaPublicacion());

        libro.setFechaOriginal(
                request.getFechaOriginal());

        libro.setRequiereRevisionFecha(
                request.getRequiereRevisionFecha());

        libro.setCota(
                request.getCota());

        libro.setObservaciones(
                request.getObservaciones());

        if (request.getIdEditorial() != null) {

            Editorial editorial =
                    editorialRepository.findById(
                            request.getIdEditorial())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Editorial no encontrada"));

            libro.setEditorial(editorial);
        }

        Libro libroGuardado =
                libroRepository.save(libro);

        auditoriaService.registrar(
                obtenerUsuarioAuditoria(),
                "LIBROS",
                libroGuardado.getIdLibro(),
                AccionAuditoria.INSERT,
                null,
                libroGuardado.getTitulo(),
                "Libro creado"
        );

        return libroGuardado;
    }

    @Transactional
    public Libro actualizar(
            Integer id,
            LibroRequest request) {

        Libro libro =
                libroRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Libro no encontrado"));

        String tituloAntes = libro.getTitulo();
        String tipoAntes = libro.getTipo();
        String anioAntes = libro.getFechaPublicacion() == null ? null : String.valueOf(libro.getFechaPublicacion());
        String fechaAntes = libro.getFechaOriginal();
        String revisionAntes = Boolean.TRUE.equals(libro.getRequiereRevisionFecha()) ? "Sí" : "No";
        String cotaAntes = libro.getCota();
        String observacionesAntes = libro.getObservaciones();
        String editorialAntes = libro.getEditorial() == null ? null : libro.getEditorial().getNombre();

        libro.setTitulo(request.getTitulo());
        libro.setTipo(request.getTipo());

        libro.setFechaPublicacion(
                request.getFechaPublicacion());

        libro.setFechaOriginal(
                request.getFechaOriginal());

        libro.setRequiereRevisionFecha(
                request.getRequiereRevisionFecha());

        libro.setCota(
                request.getCota());

        libro.setObservaciones(
                request.getObservaciones());

        if (request.getIdEditorial() != null) {

            Editorial editorial =
                    editorialRepository.findById(
                            request.getIdEditorial())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Editorial no encontrada"));

            libro.setEditorial(editorial);
        } else {

            libro.setEditorial(null);
        }

        Libro libroActualizado =
                libroRepository.save(libro);

        UsuarioSistema usuario = obtenerUsuarioAuditoria();
        Integer idLibro = libroActualizado.getIdLibro();
        registrarCambio(usuario, idLibro, "Título", tituloAntes, libroActualizado.getTitulo());
        registrarCambio(usuario, idLibro, "Tipo", tipoAntes, libroActualizado.getTipo());
        registrarCambio(usuario, idLibro, "Año", anioAntes,
                libroActualizado.getFechaPublicacion() == null ? null : String.valueOf(libroActualizado.getFechaPublicacion()));
        registrarCambio(usuario, idLibro, "Fecha original", fechaAntes, libroActualizado.getFechaOriginal());
        registrarCambio(usuario, idLibro, "Revisión de fecha", revisionAntes,
                Boolean.TRUE.equals(libroActualizado.getRequiereRevisionFecha()) ? "Sí" : "No");
        registrarCambio(usuario, idLibro, "Cota", cotaAntes, libroActualizado.getCota());
        registrarCambio(usuario, idLibro, "Observaciones", observacionesAntes, libroActualizado.getObservaciones());
        registrarCambio(usuario, idLibro, "Editorial", editorialAntes,
                libroActualizado.getEditorial() == null ? null : libroActualizado.getEditorial().getNombre());

        return libroActualizado;
    }

    @Transactional
    public void eliminar(Integer id) {

        Libro libro =
                libroRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Libro no encontrado"));

        auditoriaService.registrar(
                obtenerUsuarioAuditoria(),
                "LIBROS",
                libro.getIdLibro(),
                AccionAuditoria.DELETE,
                libro.getTitulo(),
                null,
                "Libro eliminado"
        );

        libroRepository.delete(libro);
    }

    private void registrarCambio(
            UsuarioSistema usuario,
            Integer idLibro,
            String campo,
            String antes,
            String despues) {

        String anterior = antes == null ? "" : antes;
        String nuevo = despues == null ? "" : despues;
        if (anterior.equals(nuevo)) {
            return;
        }
        auditoriaService.registrar(
                usuario,
                "LIBROS",
                idLibro,
                AccionAuditoria.UPDATE,
                anterior.isEmpty() ? null : anterior,
                nuevo.isEmpty() ? null : nuevo,
                campo
        );
    }

    @Transactional
    public Libro restaurar(Integer id, Integer idAuditoria) {
        permisoService.exigirAdmin("Solo el administrador puede devolver una obra a una versión anterior");
        Libro libro = obtener(id);
        List<Auditoria> eventos = new ArrayList<>(
                auditoriaRepository.findByEntidadAndIdEntidadOrderByFechaEventoDesc("LIBROS", id));
        eventos.sort(Comparator
                .comparing(Auditoria::getFechaEvento)
                .thenComparing(Auditoria::getIdAuditoria));
        Auditoria marca = eventos.stream()
                .filter(evento -> evento.getIdAuditoria().equals(idAuditoria))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Esa versión no pertenece a esta obra"));

        List<Auditoria> posteriores = eventos.stream().filter(evento -> esPosterior(evento, marca)).toList();
        LibroRequest request = desde(libro);
        for (int i = posteriores.size() - 1; i >= 0; i--) {
            Auditoria evento = posteriores.get(i);
            if (evento.getAccion() != AccionAuditoria.UPDATE || evento.getObservaciones() == null) {
                continue;
            }
            aplicar(request, evento.getObservaciones(), evento.getValorAnterior());
        }
        if (request.getTitulo() == null || request.getTitulo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Esa versión no tiene título y no se puede restaurar");
        }
        Libro restaurado = actualizar(id, request);
        restaurarAutores(id, posteriores);
        auditoriaService.registrar(
                obtenerUsuarioAuditoria(),
                "LIBROS",
                id,
                AccionAuditoria.UPDATE,
                null,
                marca.getFechaEvento() == null ? null : marca.getFechaEvento().toString(),
                "Versión restaurada");
        return restaurado;
    }

    private void restaurarAutores(Integer idLibro, List<Auditoria> posteriores) {
        List<String> deseados = new ArrayList<>();
        for (LibroAutor vinculo : libroAutorRepository.findByLibro_IdLibro(idLibro)) {
            deseados.add(nombreAutor(vinculo.getAutor()));
        }
        for (int i = posteriores.size() - 1; i >= 0; i--) {
            Auditoria evento = posteriores.get(i);
            if (!"Autor".equals(evento.getObservaciones())) {
                continue;
            }
            if (evento.getAccion() == AccionAuditoria.INSERT) {
                quitar(deseados, evento.getValorNuevo());
            } else if (evento.getAccion() == AccionAuditoria.DELETE && evento.getValorAnterior() != null) {
                deseados.add(evento.getValorAnterior());
            }
        }

        Libro libro = obtener(idLibro);
        UsuarioSistema usuario = obtenerUsuarioAuditoria();
        List<LibroAutor> actuales = new ArrayList<>(libroAutorRepository.findByLibro_IdLibro(idLibro));
        for (LibroAutor vinculo : actuales) {
            String visible = nombreAutor(vinculo.getAutor());
            if (quitar(deseados, visible)) {
                continue;
            }
            auditoriaService.registrar(
                    usuario, "LIBROS", idLibro, AccionAuditoria.DELETE, visible, null, "Autor");
            libroAutorRepository.delete(vinculo);
        }
        for (String nombre : deseados) {
            Autor autor = autorRepository.buscarPorTexto(nombre).stream().findFirst()
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "No se encontró el autor de esa versión: " + nombre));
            LibroAutorId clave = new LibroAutorId(idLibro, autor.getIdAutor());
            if (libroAutorRepository.existsById(clave)) {
                continue;
            }
            LibroAutor vinculo = new LibroAutor();
            vinculo.setLibro(libro);
            vinculo.setAutor(autor);
            libroAutorRepository.save(vinculo);
            auditoriaService.registrar(
                    usuario, "LIBROS", idLibro, AccionAuditoria.INSERT, null, nombreAutor(autor), "Autor");
        }
    }

    private LibroRequest desde(Libro libro) {
        LibroRequest request = new LibroRequest();
        request.setTitulo(libro.getTitulo());
        request.setTipo(libro.getTipo());
        request.setFechaPublicacion(libro.getFechaPublicacion());
        request.setFechaOriginal(libro.getFechaOriginal());
        request.setRequiereRevisionFecha(libro.getRequiereRevisionFecha());
        request.setCota(libro.getCota());
        request.setObservaciones(libro.getObservaciones());
        request.setIdEditorial(libro.getEditorial() == null ? null : libro.getEditorial().getIdEditorial());
        return request;
    }

    private void aplicar(LibroRequest request, String campo, String valor) {
        String texto = valor == null ? "" : valor.trim();
        switch (campo) {
            case "Título" -> request.setTitulo(texto);
            case "Tipo" -> request.setTipo(vacio(texto));
            case "Año" -> request.setFechaPublicacion(anio(texto));
            case "Fecha original" -> request.setFechaOriginal(vacio(texto));
            case "Revisión de fecha" -> request.setRequiereRevisionFecha("Sí".equalsIgnoreCase(texto));
            case "Cota" -> request.setCota(vacio(texto));
            case "Observaciones" -> request.setObservaciones(vacio(texto));
            case "Editorial" -> request.setIdEditorial(editorial(texto));
            default -> {
            }
        }
    }

    private Integer editorial(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return null;
        }
        return editorialRepository.findFirstByNombreIgnoreCase(nombre)
                .map(Editorial::getIdEditorial)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No se encontró la editorial de esa versión: " + nombre));
    }

    private Short anio(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return Short.valueOf(texto);
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El año de esa versión no es válido");
        }
    }

    private boolean esPosterior(Auditoria evento, Auditoria marca) {
        int fecha = evento.getFechaEvento().compareTo(marca.getFechaEvento());
        if (fecha != 0) {
            return fecha > 0;
        }
        return evento.getIdAuditoria() > marca.getIdAuditoria();
    }

    private boolean quitar(List<String> nombres, String buscado) {
        if (buscado == null) {
            return false;
        }
        String clave = buscado.trim().toLowerCase(Locale.ROOT);
        for (int i = 0; i < nombres.size(); i++) {
            if (nombres.get(i) != null && nombres.get(i).trim().toLowerCase(Locale.ROOT).equals(clave)) {
                nombres.remove(i);
                return true;
            }
        }
        return false;
    }

    private String nombreAutor(Autor autor) {
        if (autor.getNombreCompleto() != null && !autor.getNombreCompleto().isBlank()) {
            return autor.getNombreCompleto();
        }
        String apellido = autor.getApellido() == null ? "" : " " + autor.getApellido();
        return (autor.getNombre() + apellido).trim();
    }

    private String vacio(String valor) {
        return valor == null || valor.isBlank() ? null : valor;
    }

    private UsuarioSistema obtenerUsuarioAuditoria() {
        return usuarioActualService.obtener();
    }

    private String enBlanco(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}