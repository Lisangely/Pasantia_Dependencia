package com.fau.biblioteca_api.service;

import com.fau.biblioteca_api.dto.ImportacionResultado;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

@Service
public class ImportacionExcelService {

    private static final Pattern ANIO = Pattern.compile("^(1[5-9]\\d{2}|20\\d{2})$");
    private static final Pattern UNICO = Pattern.compile("(?i)u[nñ]ico");

    private final JdbcTemplate jdbc;
    private final DataFormatter formato = new DataFormatter(Locale.forLanguageTag("es"));

    public ImportacionExcelService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public ImportacionResultado importar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona un archivo de Excel");
        }
        String nombre = archivo.getOriginalFilename() == null ? "" : archivo.getOriginalFilename().toLowerCase(Locale.ROOT);
        if (!nombre.endsWith(".xlsx")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo debe ser un Excel .xlsx con la plantilla del inventario");
        }

        Set<String> yaCargadas = firmasExistentes();
        ImportacionResultado resultado = new ImportacionResultado();
        Map<String, Integer> editoriales = new HashMap<>();
        Map<String, Integer> autores = new HashMap<>();
        Map<String, Integer> ubicaciones = new HashMap<>();

        try (InputStream entrada = archivo.getInputStream(); Workbook libro = WorkbookFactory.create(entrada)) {
            Sheet hoja = libro.getNumberOfSheets() == 0 ? null : libro.getSheetAt(0);
            if (hoja == null || hoja.getPhysicalNumberOfRows() == 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El Excel no tiene filas");
            }
            Map<String, Integer> columnas = columnas(hoja.getRow(hoja.getFirstRowNum()));
            for (int i = hoja.getFirstRowNum() + 1; i <= hoja.getLastRowNum(); i++) {
                Row fila = hoja.getRow(i);
                if (fila == null) {
                    continue;
                }
                String titulo = texto(celda(fila, columnas.get("TITULO")));
                if (titulo.isBlank()) {
                    continue;
                }
                String firma = firmaDe(fila, columnas);
                if (yaCargadas.contains(firma)) {
                    resultado.sumarIgnorada();
                    continue;
                }
                insertar(fila, columnas, editoriales, autores, ubicaciones);
                yaCargadas.add(firma);
                resultado.sumarCargada();
            }
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer el Excel. Usa la misma plantilla del inventario.");
        }
        return resultado;
    }

    private void insertar(
            Row fila,
            Map<String, Integer> columnas,
            Map<String, Integer> editoriales,
            Map<String, Integer> autores,
            Map<String, Integer> ubicaciones) {

        String tituloOriginal = texto(celda(fila, columnas.get("TITULO")));
        String titulo = recortar(tituloOriginal, 500);
        Fecha fecha = fechaDe(celda(fila, columnas.get("FECHA")));
        String fechaOriginal = recortar(fecha.original(), 50);
        String tipo = recortar(texto(celda(fila, columnas.get("TIPO"))), 100);
        String cotaOriginal = texto(celda(fila, columnas.get("COTA")));
        String cota = recortar(cotaOriginal, 100);
        String editorial = texto(celda(fila, columnas.get("EDITORIAL")));
        String nombreAutor = texto(celda(fila, columnas.get("NOMBRE AUTOR")));
        String apellidoAutor = texto(celda(fila, columnas.get("APELLIDO AUTOR")));
        String filaUbicacion = texto(celda(fila, columnas.get("FILA")));
        String ejemplaresTexto = texto(celda(fila, columnas.get("EJEMPLARES")));

        Integer idEditorial = editorialId(editorial, editoriales);
        Integer idLibro = jdbc.queryForObject(
                """
                INSERT INTO libros (
                    titulo, id_editorial, tipo, fecha_publicacion, fecha_original,
                    requiere_revision_fecha, cota
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                RETURNING id_libro
                """,
                Integer.class,
                titulo,
                idEditorial,
                vacio(tipo),
                fecha.anio(),
                vacio(fechaOriginal),
                fecha.revisar() || fechaOriginal.length() < fecha.original().length(),
                vacio(cota));

        if (tituloOriginal.length() > 500) {
            incidencia(idLibro, "TITULO", tituloOriginal, "El título superaba el largo permitido y se conservó recortado.");
        }
        if (fecha.revisar() || fechaOriginal.isBlank()) {
            incidencia(idLibro, "FECHA", fechaOriginal, "La fecha está vacía o no es un año válido. Se conservó el valor original.");
        }
        if (editorial.isBlank()) {
            incidencia(idLibro, "EDITORIAL", "", "La editorial está vacía.");
        }
        if (cota.isBlank() || cota.equals("?") || cota.equals("-") || cota.equals("??") || cotaOriginal.length() > 100) {
            incidencia(idLibro, "COTA", cota, "La cota está vacía, es ambigua o fue recortada.");
        }
        if (tipo.isBlank()) {
            incidencia(idLibro, "TIPO", "", "El tipo de material está vacío.");
        }

        if (nombreAutor.isBlank() && apellidoAutor.isBlank()) {
            incidencia(idLibro, "AUTOR", "", "El autor está vacío.");
        }
        Set<Integer> vistos = new HashSet<>();
        for (String[] persona : partirPersonas(nombreAutor, apellidoAutor)) {
            Integer idAutor = autorId(persona[0], persona[1], autores);
            if (!vistos.add(idAutor)) {
                continue;
            }
            jdbc.update(
                    "INSERT INTO libro_autor (id_libro, id_autor) VALUES (?, ?) ON CONFLICT DO NOTHING",
                    idLibro,
                    idAutor);
        }

        Ubicacion ubicacion = ubicacionDe(filaUbicacion);
        Integer idUbicacion = ubicacionId(ubicacion, ubicaciones);
        if (idUbicacion == null || ubicacion.revisa()) {
            incidencia(idLibro, "FILA", filaUbicacion, "La ubicación está vacía o no tiene el formato de estantería esperado.");
        }

        List<Integer> numeros = copiasDe(ejemplaresTexto);
        boolean revisionCopia = numeros == null;
        if (revisionCopia) {
            incidencia(idLibro, "Nº.EJEMPLARES", ejemplaresTexto, "La cantidad de ejemplares está vacía o no se pudo interpretar. Se registró una copia para revisión.");
            numeros = new ArrayList<>();
            numeros.add(null);
        }
        for (Integer numero : numeros) {
            jdbc.update(
                    """
                    INSERT INTO ejemplares
                        (numero_ejemplar, disponible, requiere_revision, id_libro, id_ubicacion)
                    VALUES (?, TRUE, ?, ?, ?)
                    """,
                    numero,
                    revisionCopia || ubicacion.revisa(),
                    idLibro,
                    idUbicacion);
        }
    }

    private Set<String> firmasExistentes() {
        List<Map<String, Object>> filas = jdbc.queryForList(
                """
                SELECT l.id_libro AS id,
                       l.titulo AS titulo,
                       COALESCE(l.tipo, '') AS tipo,
                       l.fecha_publicacion AS anio,
                       COALESCE(l.fecha_original, '') AS fecha,
                       COALESCE(l.cota, '') AS cota,
                       COALESCE(ed.nombre, '') AS editorial,
                       COALESCE(a.nombre, '') AS nombre,
                       COALESCE(a.apellido, '') AS apellido,
                       e.numero_ejemplar AS numero,
                       COALESCE(u.ubicacion_original, '') AS ubicacion
                FROM libros l
                LEFT JOIN editoriales ed ON ed.id_editorial = l.id_editorial
                LEFT JOIN libro_autor la ON la.id_libro = l.id_libro
                LEFT JOIN autores a ON a.id_autor = la.id_autor
                LEFT JOIN ejemplares e ON e.id_libro = l.id_libro
                LEFT JOIN ubicaciones u ON u.id_ubicacion = e.id_ubicacion
                """);
        Map<Integer, Acumulado> grupos = new HashMap<>();
        for (Map<String, Object> fila : filas) {
            Integer id = ((Number) fila.get("id")).intValue();
            Acumulado grupo = grupos.computeIfAbsent(id, clave -> new Acumulado(fila));
            grupo.autores.add(claveAutor(texto(fila.get("nombre")), texto(fila.get("apellido"))));
            Object numero = fila.get("numero");
            grupo.numeros.add(numero == null ? "" : String.valueOf(((Number) numero).intValue()));
            String ubicacion = norm(texto(fila.get("ubicacion")));
            if (!ubicacion.isBlank()) {
                grupo.ubicaciones.add(ubicacion);
            }
        }
        Set<String> firmas = new HashSet<>();
        for (Acumulado grupo : grupos.values()) {
            firmas.add(grupo.firma());
        }
        return firmas;
    }

    private String firmaDe(Row fila, Map<String, Integer> columnas) {
        String titulo = recortar(texto(celda(fila, columnas.get("TITULO"))), 500);
        Fecha fecha = fechaDe(celda(fila, columnas.get("FECHA")));
        String tipo = recortar(texto(celda(fila, columnas.get("TIPO"))), 100);
        String cota = recortar(texto(celda(fila, columnas.get("COTA"))), 100);
        String editorial = recortar(texto(celda(fila, columnas.get("EDITORIAL"))), 255);
        Ubicacion ubicacion = ubicacionDe(texto(celda(fila, columnas.get("FILA"))));
        TreeSet<String> autores = new TreeSet<>();
        for (String[] persona : partirPersonas(
                texto(celda(fila, columnas.get("NOMBRE AUTOR"))),
                texto(celda(fila, columnas.get("APELLIDO AUTOR"))))) {
            String nombre = recortar(persona[0], 150);
            String apellido = recortar(persona[1], 150);
            if (nombre.isBlank() && !apellido.isBlank()) {
                nombre = apellido;
                apellido = "";
            }
            if (nombre.isBlank()) {
                nombre = "Sin autor";
            }
            autores.add(claveAutor(nombre, apellido));
        }
        TreeSet<String> numeros = new TreeSet<>();
        List<Integer> copias = copiasDe(texto(celda(fila, columnas.get("EJEMPLARES"))));
        if (copias == null) {
            numeros.add("");
        } else {
            for (Integer numero : copias) {
                numeros.add(numero == null ? "" : String.valueOf(numero));
            }
        }
        TreeSet<String> lugares = new TreeSet<>();
        if (ubicacion.original() != null && !ubicacion.original().isBlank()) {
            lugares.add(norm(recortar(ubicacion.original(), 100)));
        }
        return firma(titulo, tipo, fecha.anio(), recortar(fecha.original(), 50), cota, editorial, lugares, autores, numeros);
    }

    private String firma(
            String titulo,
            String tipo,
            Integer anio,
            String fecha,
            String cota,
            String editorial,
            Set<String> ubicaciones,
            Set<String> autores,
            Set<String> numeros) {
        return String.join("¦",
                norm(titulo).replaceAll("\\.+$", ""),
                norm(tipo),
                anio == null ? "" : String.valueOf(anio),
                norm(fecha),
                norm(cota),
                norm(editorial),
                String.join(";", ubicaciones),
                String.join(";", autores),
                String.join(",", numeros));
    }

    private Map<String, Integer> columnas(Row encabezado) {
        Map<String, Integer> columnas = new HashMap<>();
        if (encabezado != null) {
            for (Cell celda : encabezado) {
                String nombre = normalizarEncabezado(formato.formatCellValue(celda));
                if (nombre.contains("EJEMPLAR")) {
                    columnas.put("EJEMPLARES", celda.getColumnIndex());
                } else if (!nombre.isBlank()) {
                    columnas.put(nombre, celda.getColumnIndex());
                }
            }
        }
        String[] esperadas = {"TITULO", "NOMBRE AUTOR", "APELLIDO AUTOR", "FECHA", "TIPO", "FILA", "EDITORIAL", "COTA", "EJEMPLARES"};
        List<String> faltan = new ArrayList<>();
        for (String esperada : esperadas) {
            if (!columnas.containsKey(esperada)) {
                faltan.add(esperada.equals("EJEMPLARES") ? "Nº.EJEMPLARES" : esperada);
            }
        }
        if (!faltan.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La plantilla no coincide. Faltan las columnas: " + String.join(", ", faltan));
        }
        return columnas;
    }

    private Integer editorialId(String nombre, Map<String, Integer> cache) {
        String limpio = recortar(nombre, 255);
        if (limpio.isBlank()) {
            return null;
        }
        String clave = limpio.toLowerCase(Locale.ROOT);
        if (cache.containsKey(clave)) {
            return cache.get(clave);
        }
        List<Integer> existentes = jdbc.query(
                "SELECT id_editorial FROM editoriales WHERE lower(nombre) = ? LIMIT 1",
                (rs, row) -> rs.getInt(1),
                clave);
        Integer id = existentes.isEmpty()
                ? jdbc.queryForObject("INSERT INTO editoriales (nombre) VALUES (?) RETURNING id_editorial", Integer.class, limpio)
                : existentes.get(0);
        cache.put(clave, id);
        return id;
    }

    private Integer autorId(String nombre, String apellido, Map<String, Integer> cache) {
        nombre = recortar(nombre, 150);
        apellido = recortar(apellido, 150);
        if (nombre.isBlank() && !apellido.isBlank()) {
            nombre = apellido;
            apellido = "";
        }
        if (nombre.isBlank()) {
            nombre = "Sin autor";
        }
        String completo = recortar(nombreCompleto(nombre, apellido), 300);
        String clave = claveAutor(nombre, apellido);
        if (cache.containsKey(clave)) {
            return cache.get(clave);
        }
        List<Integer> existentes = jdbc.query(
                """
                SELECT id_autor FROM autores
                WHERE lower(nombre) = ? AND lower(coalesce(apellido, '')) = ?
                LIMIT 1
                """,
                (rs, row) -> rs.getInt(1),
                nombre.toLowerCase(Locale.ROOT),
                apellido.toLowerCase(Locale.ROOT));
        Integer id = existentes.isEmpty()
                ? jdbc.queryForObject(
                        "INSERT INTO autores (nombre, apellido, nombre_completo) VALUES (?, ?, ?) RETURNING id_autor",
                        Integer.class,
                        nombre,
                        vacio(apellido),
                        vacio(completo))
                : existentes.get(0);
        cache.put(clave, id);
        return id;
    }

    private Integer ubicacionId(Ubicacion ubicacion, Map<String, Integer> cache) {
        String original = recortar(ubicacion.original(), 100);
        if (original.isBlank()) {
            return null;
        }
        String clave = original.toLowerCase(Locale.ROOT);
        if (cache.containsKey(clave)) {
            return cache.get(clave);
        }
        List<Integer> existentes = jdbc.query(
                "SELECT id_ubicacion FROM ubicaciones WHERE lower(ubicacion_original) = ? LIMIT 1",
                (rs, row) -> rs.getInt(1),
                clave);
        Integer id = existentes.isEmpty()
                ? jdbc.queryForObject(
                        """
                        INSERT INTO ubicaciones (ubicacion_original, estanteria, posicion, requiere_revision)
                        VALUES (?, ?, ?, ?) RETURNING id_ubicacion
                        """,
                        Integer.class,
                        original,
                        vacio(ubicacion.estanteria()),
                        vacio(ubicacion.posicion()),
                        ubicacion.revisa() || original.length() < ubicacion.original().length())
                : existentes.get(0);
        cache.put(clave, id);
        return id;
    }

    private void incidencia(Integer idLibro, String campo, String valor, String descripcion) {
        String guardado = valor == null ? "" : valor;
        jdbc.update(
                """
                INSERT INTO incidencias_datos (id_libro, campo, valor_original, descripcion, resuelto)
                VALUES (?, ?, ?, ?, FALSE)
                """,
                idLibro,
                recortar(campo, 100),
                vacio(recortar(guardado, 255)),
                descripcion);
    }

    private List<String[]> partirPersonas(String nombre, String apellido) {
        String[] nombres = partir(nombre);
        String[] apellidos = partir(apellido);
        List<String[]> personas = new ArrayList<>();
        if (nombres.length > 1 && apellidos.length == nombres.length) {
            for (int i = 0; i < nombres.length; i++) {
                personas.add(new String[]{nombres[i], apellidos[i]});
            }
            return personas;
        }
        if (nombres.length > 1) {
            String ape = apellidos.length == 1 ? apellidos[0] : "";
            for (String parte : nombres) {
                personas.add(new String[]{parte, ape});
            }
            return personas;
        }
        if (apellidos.length > 1) {
            String nom = nombres.length == 1 ? nombres[0] : "";
            for (String parte : apellidos) {
                personas.add(new String[]{nom, parte});
            }
            return personas;
        }
        personas.add(new String[]{nombre, apellido});
        return personas;
    }

    private String[] partir(String valor) {
        if (valor == null || valor.isBlank()) {
            return new String[0];
        }
        return valor.split("\\s*/\\s*|\\s*;\\s*");
    }

    private List<Integer> copiasDe(String valor) {
        String crudo = valor == null ? "" : valor.trim();
        if (crudo.isBlank()) {
            return null;
        }
        if (UNICO.matcher(crudo).matches()) {
            return List.of(1);
        }
        if (crudo.matches("\\d+")) {
            int cantidad = Integer.parseInt(crudo);
            if (cantidad < 1 || cantidad > 20) {
                return null;
            }
            List<Integer> numeros = new ArrayList<>();
            for (int i = 1; i <= cantidad; i++) {
                numeros.add(i);
            }
            return numeros;
        }
        if (crudo.matches("\\d+(?:\\.\\d+)+")) {
            String[] partes = crudo.split("\\.");
            List<Integer> numeros = new ArrayList<>();
            for (String parte : partes) {
                int numero = Integer.parseInt(parte);
                if (numero < 1 || numero > 30) {
                    return null;
                }
                numeros.add(numero);
            }
            return numeros;
        }
        String[] partes = crudo.split("\\s*[,;/]\\s*|\\s+y\\s+");
        List<Integer> numeros = new ArrayList<>();
        for (String parte : partes) {
            if (!parte.matches("\\d+")) {
                return null;
            }
            numeros.add(Integer.parseInt(parte));
        }
        return numeros.isEmpty() ? null : numeros;
    }

    private Ubicacion ubicacionDe(String valor) {
        String original = valor == null ? "" : valor.replaceAll("\\s+", " ").trim();
        if (original.isBlank() || original.equals("?") || original.equals("-") || original.equals("??")) {
            return new Ubicacion(original, null, null, true);
        }
        if (original.contains("/")) {
            String[] partes = original.split("/", 2);
            return new Ubicacion(original, recortar(partes[0], 20), recortar(partes[1], 20),
                    partes[0].length() > 20 || partes[1].length() > 20);
        }
        boolean revisa = !original.toLowerCase(Locale.ROOT).contains("est") && !original.matches(".*\\d.*");
        return new Ubicacion(original, recortar(original, 20), null, revisa || original.length() > 20);
    }

    private Fecha fechaDe(Cell celda) {
        String original = texto(celda);
        if (ANIO.matcher(original).matches()) {
            return new Fecha(Integer.valueOf(original), original, false);
        }
        return new Fecha(null, original, true);
    }

    private String texto(Cell celda) {
        if (celda == null) {
            return "";
        }
        if (celda.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC && DateUtil.isCellDateFormatted(celda)) {
            LocalDateTime fecha = celda.getLocalDateTimeCellValue();
            if (fecha.getMonthValue() == 1 && fecha.getDayOfMonth() == 1 && fecha.getYear() >= 1500 && fecha.getYear() <= 2030) {
                return String.valueOf(fecha.getYear());
            }
            return fecha.toLocalDate().toString();
        }
        if (celda.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
            double numero = celda.getNumericCellValue();
            if (numero == Math.rint(numero)) {
                return String.valueOf((long) numero);
            }
        }
        return formato.formatCellValue(celda).replace('\u00a0', ' ').trim();
    }

    private String texto(Object valor) {
        return valor == null ? "" : String.valueOf(valor).trim();
    }

    private Cell celda(Row fila, Integer indice) {
        return indice == null ? null : fila.getCell(indice);
    }

    private String recortar(String valor, int limite) {
        if (valor == null) {
            return "";
        }
        String limpio = valor.replaceAll("\\s+", " ").trim();
        return limpio.length() <= limite ? limpio : limpio.substring(0, limite);
    }

    private String nombreCompleto(String nombre, String apellido) {
        if (!nombre.isBlank() && nombre.equalsIgnoreCase(apellido)) {
            return nombre;
        }
        return (nombre + " " + apellido).trim();
    }

    private String claveAutor(String nombre, String apellido) {
        return norm(nombre) + "/" + norm(apellido);
    }

    private String normalizarEncabezado(String valor) {
        return valor == null ? "" : valor
                .replace('\u00a0', ' ')
                .replace("º", "")
                .replace("°", "")
                .replace(".", "")
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private String norm(String valor) {
        return valor == null ? "" : valor.replaceAll("\\s+", " ").trim().toLowerCase(Locale.ROOT);
    }

    private String vacio(String valor) {
        return valor == null || valor.isBlank() ? null : valor;
    }

    private record Fecha(Integer anio, String original, boolean revisar) {
    }

    private record Ubicacion(String original, String estanteria, String posicion, boolean revisa) {
    }

    private final class Acumulado {
        private final String titulo;
        private final String tipo;
        private final Integer anio;
        private final String fecha;
        private final String cota;
        private final String editorial;
        private final Set<String> autores = new TreeSet<>();
        private final Set<String> numeros = new TreeSet<>();
        private final Set<String> ubicaciones = new TreeSet<>();

        private Acumulado(Map<String, Object> fila) {
            this.titulo = texto(fila.get("titulo"));
            this.tipo = texto(fila.get("tipo"));
            Object anioValor = fila.get("anio");
            this.anio = anioValor == null ? null : ((Number) anioValor).intValue();
            this.fecha = texto(fila.get("fecha"));
            this.cota = texto(fila.get("cota"));
            this.editorial = texto(fila.get("editorial"));
        }

        private String firma() {
            return ImportacionExcelService.this.firma(titulo, tipo, anio, fecha, cota, editorial, ubicaciones, autores, numeros);
        }
    }
}
