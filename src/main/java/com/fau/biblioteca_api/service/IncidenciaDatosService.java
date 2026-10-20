package com.fau.biblioteca_api.service;

import com.fau.biblioteca_api.dto.IncidenciaRequest;
import com.fau.biblioteca_api.entity.AccionAuditoria;
import com.fau.biblioteca_api.entity.Autor;
import com.fau.biblioteca_api.entity.Editorial;
import com.fau.biblioteca_api.entity.Ejemplar;
import com.fau.biblioteca_api.entity.EstadoPrestamo;
import com.fau.biblioteca_api.entity.IncidenciaDatos;
import com.fau.biblioteca_api.entity.Libro;
import com.fau.biblioteca_api.entity.LibroAutor;
import com.fau.biblioteca_api.entity.LibroAutorId;
import com.fau.biblioteca_api.entity.Ubicacion;
import com.fau.biblioteca_api.entity.UsuarioSistema;
import com.fau.biblioteca_api.repository.AutorRepository;
import com.fau.biblioteca_api.repository.EditorialRepository;
import com.fau.biblioteca_api.repository.EjemplarRepository;
import com.fau.biblioteca_api.repository.IncidenciaDatosRepository;
import com.fau.biblioteca_api.repository.LibroAutorRepository;
import com.fau.biblioteca_api.repository.LibroRepository;
import com.fau.biblioteca_api.repository.PrestamoRepository;
import com.fau.biblioteca_api.repository.UbicacionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class IncidenciaDatosService {

    private final IncidenciaDatosRepository incidenciaRepository;
    private final LibroRepository libroRepository;
    private final EditorialRepository editorialRepository;
    private final EjemplarRepository ejemplarRepository;
    private final UbicacionRepository ubicacionRepository;
    private final AutorRepository autorRepository;
    private final LibroAutorRepository libroAutorRepository;
    private final PrestamoRepository prestamoRepository;
    private final AutorService autorService;
    private final AuditoriaService auditoriaService;
    private final UsuarioActualService usuarioActualService;

    public IncidenciaDatosService(
            IncidenciaDatosRepository incidenciaRepository,
            LibroRepository libroRepository,
            EditorialRepository editorialRepository,
            EjemplarRepository ejemplarRepository,
            UbicacionRepository ubicacionRepository,
            AutorRepository autorRepository,
            LibroAutorRepository libroAutorRepository,
            PrestamoRepository prestamoRepository,
            AutorService autorService,
            AuditoriaService auditoriaService,
            UsuarioActualService usuarioActualService) {

        this.incidenciaRepository = incidenciaRepository;
        this.libroRepository = libroRepository;
        this.editorialRepository = editorialRepository;
        this.ejemplarRepository = ejemplarRepository;
        this.ubicacionRepository = ubicacionRepository;
        this.autorRepository = autorRepository;
        this.libroAutorRepository = libroAutorRepository;
        this.prestamoRepository = prestamoRepository;
        this.autorService = autorService;
        this.auditoriaService = auditoriaService;
        this.usuarioActualService = usuarioActualService;
    }

    public List<IncidenciaDatos> listar(Boolean resuelto, Integer idLibro) {
        return incidenciaRepository.buscar(resuelto, idLibro);
    }

    public IncidenciaDatos obtener(Integer id) {
        return incidenciaRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Incidencia no encontrada"));
    }

    @Transactional
    public IncidenciaDatos crear(IncidenciaRequest request) {
        if (request.getIdLibro() == null) {
            throw new RuntimeException("El libro es obligatorio");
        }
        if (esBlanco(request.getCampo())) {
            throw new RuntimeException("El campo afectado es obligatorio");
        }
        if (esBlanco(request.getDescripcion())) {
            throw new RuntimeException("La descripción es obligatoria");
        }

        Libro libro = libroRepository.findById(request.getIdLibro())
                .orElseThrow(() ->
                        new RuntimeException("Libro no encontrado"));

        IncidenciaDatos incidencia = new IncidenciaDatos();
        incidencia.setLibro(libro);
        incidencia.setCampo(request.getCampo().trim());
        incidencia.setValorOriginal(request.getValorOriginal());
        incidencia.setDescripcion(request.getDescripcion().trim());
        incidencia.setResuelto(false);

        IncidenciaDatos guardada = incidenciaRepository.save(incidencia);
        UsuarioSistema usuario = usuarioActualService.obtener();

        auditoriaService.registrar(
                usuario,
                "INCIDENCIAS_DATOS",
                guardada.getIdIncidencia(),
                AccionAuditoria.INSERT,
                null,
                guardada.getCampo(),
                "Incidencia registrada"
        );

        return guardada;
    }

    @Transactional
    public IncidenciaDatos resolver(Integer id, String valorRecibido) {
        IncidenciaDatos incidencia = obtener(id);

        if (Boolean.TRUE.equals(incidencia.getResuelto())) {
            throw mal("La incidencia ya está resuelta");
        }

        String valor = valorRecibido == null ? "" : valorRecibido.trim().replaceAll("\\s+", " ");
        if (valor.isEmpty()) {
            throw mal("Ingresa el valor corregido");
        }
        if (igual(valor, incidencia.getValorOriginal())) {
            throw mal("El valor sigue igual que el dato con error");
        }

        Libro libro = incidencia.getLibro();
        String clave = clave(incidencia.getCampo());
        String antes = aplicar(libro, clave, valor);
        String campo = etiqueta(clave, incidencia.getCampo());

        incidencia.setResuelto(true);
        IncidenciaDatos guardada = incidenciaRepository.save(incidencia);
        UsuarioSistema usuario = usuarioActualService.obtener();

        auditoriaService.registrar(
                usuario,
                "LIBROS",
                libro.getIdLibro(),
                AccionAuditoria.UPDATE,
                antes,
                valor,
                campo
        );
        auditoriaService.registrar(
                usuario,
                "INCIDENCIAS_DATOS",
                guardada.getIdIncidencia(),
                AccionAuditoria.RESOLVER_INCIDENCIA,
                antes,
                valor,
                campo
        );

        return guardada;
    }

    private String aplicar(Libro libro, String clave, String valor) {
        if (clave.contains("FECHA") || clave.equals("ANO") || clave.equals("ANIO")) {
            return corregirFecha(libro, valor);
        }
        if (clave.contains("EJEMPLAR")) {
            return corregirEjemplares(libro, valor);
        }
        if (clave.contains("EDITORIAL")) {
            return corregirEditorial(libro, valor);
        }
        if (clave.contains("COTA")) {
            return corregirCota(libro, valor);
        }
        if (clave.contains("TIPO")) {
            return corregirTexto(libro.getTipo(), valor, 100, "El tipo", (nuevo) -> libro.setTipo(nuevo));
        }
        if (clave.contains("TITUL")) {
            return corregirTexto(libro.getTitulo(), valor, 500, "El título", (nuevo) -> libro.setTitulo(nuevo));
        }
        if (clave.contains("AUTOR")) {
            return corregirAutor(libro, valor);
        }
        if (clave.contains("FILA") || clave.contains("UBICACION")) {
            return corregirUbicacion(libro, valor);
        }
        if (valor.length() > 255) {
            throw mal("El valor es demasiado largo");
        }
        String antes = libro.getObservaciones();
        String nota = incidenciaNota(incidenciaCampoVisible(clave), valor);
        libro.setObservaciones(antes == null || antes.isBlank() ? nota : antes + "\n" + nota);
        libroRepository.save(libro);
        return antes;
    }

    private String incidenciaCampoVisible(String clave) {
        return clave.isBlank() ? "Campo" : clave;
    }

    private String incidenciaNota(String campo, String valor) {
        return "Corrección de " + campo + ": " + valor;
    }

    private String corregirFecha(Libro libro, String valor) {
        if (!valor.matches("1[5-9]\\d{2}|20\\d{2}")) {
            throw mal("El año debe tener 4 dígitos, entre 1500 y 2099");
        }
        int anio = Integer.parseInt(valor);
        int tope = Year.now().getValue();
        if (anio > tope) {
            throw mal("El año no puede ser posterior a " + tope);
        }
        if (libro.getFechaPublicacion() != null && libro.getFechaPublicacion() == anio) {
            throw mal("Ese año ya está registrado. Ingresa el año corregido");
        }
        String antes = libro.getFechaPublicacion() != null
                ? String.valueOf(libro.getFechaPublicacion())
                : libro.getFechaOriginal();
        libro.setFechaPublicacion((short) anio);
        libro.setRequiereRevisionFecha(false);
        libroRepository.save(libro);
        return antes;
    }

    private String corregirEjemplares(Libro libro, String valor) {
        if (!valor.matches("\\d+")) {
            throw mal("La cantidad de ejemplares debe ser un número entero");
        }
        int objetivo = Integer.parseInt(valor);
        if (objetivo < 1 || objetivo > 20) {
            throw mal("La cantidad de ejemplares debe estar entre 1 y 20");
        }

        List<Ejemplar> copias = new ArrayList<>(ejemplarRepository.findByLibro_IdLibro(libro.getIdLibro()));
        copias.sort(Comparator.comparing(copia ->
                copia.getNumeroEjemplar() == null ? Integer.MAX_VALUE : copia.getNumeroEjemplar()));
        boolean pendiente = copias.stream().anyMatch(copia ->
                Boolean.TRUE.equals(copia.getRequiereRevision()) || copia.getNumeroEjemplar() == null);
        if (copias.size() == objetivo && !pendiente) {
            throw mal("Esa cantidad ya está registrada y no corrige la incidencia");
        }

        String antes = String.valueOf(copias.size());
        while (copias.size() > objetivo) {
            Ejemplar sobra = null;
            for (int indice = copias.size() - 1; indice >= 0; indice--) {
                Ejemplar candidata = copias.get(indice);
                boolean prestada = prestamoRepository.existsByEjemplar_IdEjemplarAndEstado(
                        candidata.getIdEjemplar(), EstadoPrestamo.prestado);
                if (!prestada) {
                    sobra = candidata;
                    break;
                }
            }
            if (sobra == null) {
                throw mal("No se puede dejar esa cantidad porque hay ejemplares prestados");
            }
            ejemplarRepository.delete(sobra);
            copias.remove(sobra);
        }

        Ubicacion ubicacion = copias.isEmpty() ? null : copias.get(0).getUbicacion();
        while (copias.size() < objetivo) {
            Ejemplar nuevo = new Ejemplar();
            nuevo.setLibro(libro);
            nuevo.setUbicacion(ubicacion);
            nuevo.setDisponible(true);
            nuevo.setRequiereRevision(false);
            nuevo.setFechaCreacion(LocalDateTime.now());
            nuevo.setNumeroEjemplar(copias.size() + 1);
            copias.add(ejemplarRepository.save(nuevo));
        }

        int numero = 1;
        for (Ejemplar copia : copias) {
            copia.setNumeroEjemplar(numero++);
            copia.setRequiereRevision(false);
            ejemplarRepository.save(copia);
        }
        return antes;
    }

    private String corregirEditorial(Libro libro, String valor) {
        if (valor.length() > 255) {
            throw mal("El nombre de la editorial es demasiado largo");
        }
        String actual = libro.getEditorial() == null ? null : libro.getEditorial().getNombre();
        if (igual(actual, valor)) {
            throw mal("Esa editorial ya está registrada en la obra");
        }
        Editorial editorial = editorialRepository.findFirstByNombreIgnoreCase(valor)
                .orElseGet(() -> {
                    Editorial nueva = new Editorial();
                    nueva.setNombre(valor);
                    return editorialRepository.save(nueva);
                });
        libro.setEditorial(editorial);
        libroRepository.save(libro);
        return actual;
    }

    private String corregirCota(Libro libro, String valor) {
        if (valor.length() > 100) {
            throw mal("La cota es demasiado larga");
        }
        if (valor.equals("?") || valor.equals("-") || valor.equals("??")) {
            throw mal("La cota no puede ser un signo vacío");
        }
        if (igual(libro.getCota(), valor)) {
            throw mal("Esa cota ya está registrada en la obra");
        }
        String antes = libro.getCota();
        libro.setCota(valor);
        libroRepository.save(libro);
        return antes;
    }

    private String corregirTexto(String actual, String valor, int maximo, String nombre, java.util.function.Consumer<String> asignar) {
        if (valor.length() > maximo) {
            throw mal(nombre + " supera los " + maximo + " caracteres");
        }
        if (igual(actual, valor)) {
            throw mal(nombre + " no cambia el dato actual de la obra");
        }
        asignar.accept(valor);
        return actual;
    }

    private String corregirAutor(Libro libro, String valor) {
        if (valor.length() < 3 || !valor.matches(".*\\p{L}.*")) {
            throw mal("Escribe el nombre del autor");
        }
        if (valor.length() > 300) {
            throw mal("El nombre del autor es demasiado largo");
        }
        String[] partes = valor.split(" ");
        String apellido = partes.length > 1 ? partes[partes.length - 1] : "";
        String nombre = partes.length > 1
                ? String.join(" ", java.util.Arrays.copyOf(partes, partes.length - 1))
                : valor;
        Autor autor = autorRepository.findFirstByNombreCompletoIgnoreCase(valor).orElseGet(() -> {
            Autor nuevo = new Autor();
            nuevo.setNombre(nombre);
            nuevo.setApellido(apellido.isEmpty() ? null : apellido);
            nuevo.setNombreCompleto(valor);
            return autorService.guardar(nuevo);
        });
        LibroAutorId id = new LibroAutorId(libro.getIdLibro(), autor.getIdAutor());
        if (libroAutorRepository.existsById(id)) {
            throw mal("Ese autor ya está asociado a la obra");
        }
        LibroAutor vinculo = new LibroAutor();
        vinculo.setLibro(libro);
        vinculo.setAutor(autor);
        libroAutorRepository.save(vinculo);
        return null;
    }

    private String corregirUbicacion(Libro libro, String valor) {
        if (valor.length() > 100) {
            throw mal("La ubicación es demasiado larga");
        }
        if (!valor.contains("/")) {
            throw mal("Indica estantería y posición, por ejemplo Est.3/12");
        }
        String[] partes = valor.split("/", 2);
        String estanteria = partes[0].trim();
        String posicion = partes[1].trim();
        if (estanteria.isEmpty() || posicion.isEmpty()) {
            throw mal("La estantería y la posición son obligatorias");
        }
        if (estanteria.length() > 20 || posicion.length() > 20) {
            throw mal("La estantería y la posición admiten hasta 20 caracteres");
        }

        Ubicacion ubicacion = ubicacionRepository.findFirstByUbicacionOriginalIgnoreCase(valor)
                .orElseGet(() -> {
                    Ubicacion nueva = new Ubicacion();
                    nueva.setUbicacionOriginal(valor);
                    nueva.setEstanteria(estanteria);
                    nueva.setPosicion(posicion);
                    nueva.setRequiereRevision(false);
                    return ubicacionRepository.save(nueva);
                });

        List<Ejemplar> copias = ejemplarRepository.findByLibro_IdLibro(libro.getIdLibro());
        if (copias.isEmpty()) {
            throw mal("La obra no tiene ejemplares a los cuales asignar la ubicación");
        }
        boolean yaEstaba = copias.stream().allMatch(copia ->
                copia.getUbicacion() != null
                        && igual(copia.getUbicacion().getUbicacionOriginal(), valor));
        if (yaEstaba) {
            throw mal("Esa ubicación ya está asignada a los ejemplares");
        }
        String antes = copias.get(0).getUbicacion() == null
                ? null
                : copias.get(0).getUbicacion().getUbicacionOriginal();
        for (Ejemplar copia : copias) {
            copia.setUbicacion(ubicacion);
            ejemplarRepository.save(copia);
        }
        return antes;
    }

    private String etiqueta(String clave, String original) {
        if (clave.contains("FECHA") || clave.equals("ANO") || clave.equals("ANIO")) return "Año";
        if (clave.contains("EJEMPLAR")) return "Ejemplares";
        if (clave.contains("EDITORIAL")) return "Editorial";
        if (clave.contains("COTA")) return "Cota";
        if (clave.contains("TIPO")) return "Tipo";
        if (clave.contains("TITUL")) return "Título";
        if (clave.contains("AUTOR")) return "Autor";
        if (clave.contains("FILA") || clave.contains("UBICACION")) return "Ubicación";
        return original;
    }

    private String clave(String campo) {
        String limpio = Normalizer.normalize(campo == null ? "" : campo, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replace("Nº", "N")
                .replace("N°", "N");
        return limpio.replaceAll("[.\\s]", "");
    }

    private boolean igual(String izquierda, String derecha) {
        String a = izquierda == null ? "" : izquierda.trim();
        String b = derecha == null ? "" : derecha.trim();
        return a.equalsIgnoreCase(b);
    }

    private boolean esBlanco(String valor) {
        return valor == null || valor.isBlank();
    }

    private ResponseStatusException mal(String mensaje) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
    }
}
