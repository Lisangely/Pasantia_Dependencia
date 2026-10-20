from pathlib import Path

from docx import Document
from docx.shared import Pt, Cm
from docx.enum.text import WD_ALIGN_PARAGRAPH

SALIDA = Path(__file__).resolve().parents[1] / "docs"


def base(titulo, subtitulo):
    doc = Document()
    estilo = doc.styles["Normal"]
    estilo.font.name = "Calibri"
    estilo.font.size = Pt(11)
    for seccion in doc.sections:
        seccion.top_margin = Cm(2)
        seccion.bottom_margin = Cm(2)
        seccion.left_margin = Cm(2.2)
        seccion.right_margin = Cm(2.2)
    encabezado = doc.add_heading(titulo, 0)
    encabezado.alignment = WD_ALIGN_PARAGRAPH.LEFT
    nota = doc.add_paragraph(subtitulo)
    nota.runs[0].italic = True
    return doc


def titulo(doc, texto):
    doc.add_heading(texto, level=1)


def sub(doc, texto):
    doc.add_heading(texto, level=2)


def p(doc, texto):
    doc.add_paragraph(texto)


def bullets(doc, items):
    for item in items:
        doc.add_paragraph(item, style="List Bullet")


def sistema():
    doc = base(
        "Biblioteca FAU — Descripción del sistema",
        "Documento actualizado según el sistema en funcionamiento. Facultad de Arquitectura y Urbanismo, UCV.",
    )
    titulo(doc, "1. Qué es")
    p(doc, "Es la aplicación de escritorio de la biblioteca de la FAU. Permite consultar el catálogo cargado desde el inventario, registrar obras, autores, editoriales, ubicaciones y ejemplares, prestar y devolver, resolver incidencias de datos y revisar la auditoría. El personal entra con usuario y contraseña. Los lectores no entran al sistema: se registran solo para los préstamos.")

    titulo(doc, "2. Arquitectura")
    p(doc, "El acceso de un clic es Biblioteca FAU.exe. Si falta Java, Node.js, el servicio de PostgreSQL o la base, el lanzador lo prepara y muestra un aviso. Después abre una ventana de Electron con la interfaz React. Esa interfaz habla con la API Spring Boot en el puerto 8081. La API guarda todo en PostgreSQL, base biblioteca_fau. La sesión es por cookie. No se usa JWT.")
    bullets(doc, [
        "Escritorio: Biblioteca FAU.exe, Electron y React en 127.0.0.1:5173.",
        "API: Spring Boot 4, Java 17, puerto 8081.",
        "Datos: PostgreSQL. Hibernate no modifica el esquema por su cuenta.",
        "Seguridad: BCrypt para contraseñas y respuestas. El administrador puede reestablecer la suya con tres preguntas. La contraseña de un ayudante solo la cambia el administrador.",
    ])

    titulo(doc, "3. Perfiles")
    p(doc, "Al crear una cuenta hay que elegir un solo perfil: ayudante o administrador. Si es administrador, se abre una ventana para las tres preguntas y las tres respuestas. Solo un administrador puede crear usuarios.")
    sub(doc, "Administrador")
    p(doc, "Ve todos los módulos, crea usuarios, asigna permisos y cambia contraseñas, nombres de usuario y datos de las demás cuentas. Su propia contraseña, si la olvida, se reestablece desde el ingreso con las preguntas que él registró.")
    sub(doc, "Ayudante")
    p(doc, "El administrador marca si puede eliminar, actualizar y crear. Dentro de crear se elige por separado libros, autores, editoriales, ubicaciones, ejemplares, lectores, préstamos e incidencias. Lo que no esté marcado no aparece en el menú ni en los botones. Auditoría queda visible para quien haya entrado.")

    titulo(doc, "4. Módulos")
    modulos = [
        ("Catálogo", "Lista las obras y filtra por el inicio del título, autor, cota o tipo. Nueva obra abre la ficha. Cargar Excel con datos incorpora filas de la plantilla del inventario. Si la fila completa ya está cargada, se ignora. El icono de historial muestra los cambios de esa obra."),
        ("Ficha de la obra", "Guarda título y, si se informan, tipo, año, fecha original, cota, editorial y observaciones. Si un dato de la obra queda vacío, el sistema pregunta si se guarda sin esa información y nombra el campo. Desde la ficha se asocian autores, ejemplares e incidencias."),
        ("Autores", "Nombre obligatorio. Apellido y nombre completo pueden quedar vacíos, con la misma confirmación."),
        ("Editoriales", "Nombre de la casa editora. Es el único dato y es obligatorio."),
        ("Ubicaciones", "Se conserva el texto original de la fila del inventario. Estantería y posición pueden quedar vacías, con confirmación."),
        ("Ejemplares", "Cada copia pertenece a una obra. La obra es obligatoria. Número y ubicación pueden quedar vacíos, con confirmación."),
        ("Lectores", "Nombre, apellido, cédula, correo y teléfono son obligatorios. Semestre y carrera son opcionales. No se guarda un lector incompleto."),
        ("Préstamos", "Se elige un ejemplar disponible, un lector y la fecha estimada de devolución. Registrar lector nuevo abre una ventana y deja ese lector elegido. Devolver cierra el préstamo. Un ejemplar no puede tener dos préstamos activos."),
        ("Incidencias", "Registra un dato del inventario que hay que revisar. Resolver pide el valor corregido y lo valida antes de marcar la incidencia como resuelta."),
        ("Auditoría", "Muestra inicios de sesión, altas, cambios, bajas, préstamos, devoluciones y resoluciones de incidencias."),
        ("Usuarios", "Solo lo ve el administrador. Crea cuentas, edita permisos, usuario, correo y contraseña. El correo es obligatorio."),
    ]
    for nombre, texto in modulos:
        sub(doc, nombre)
        p(doc, texto)

    titulo(doc, "5. Cambios respecto al mapa original")
    p(doc, "El mapa original se conserva: Editorial publica Libro; Libro y Autor se unen en Libro_Autor; Ubicacion almacena Ejemplar; Libro posee Ejemplar; Lector solicita Prestamo; Ejemplar corresponde a Prestamo; Usuario registra Prestamo y realiza Auditoria; Libro genera Incidencia_data.")
    bullets(doc, [
        "Usuario: el rol ya no es bibliotecaria. Ahora es administrador o ayudante. Se agregaron los permisos de eliminar, actualizar y crear, y el detalle de qué puede crear. Se agregaron tres preguntas y tres respuestas cifradas.",
        "Lector: cédula, correo y teléfono pasaron a ser obligatorios. Se agregaron semestre y carrera, ambos opcionales.",
        "Ejemplar: se conserva fecha_creacion, además de disponible y requiere_revision.",
        "Auditoria: las acciones incluyen INSERT, UPDATE, DELETE, PRESTAMO, DEVOLUCION, RESOLVER_INCIDENCIA y LOGIN.",
    ])
    p(doc, "El diagrama PlantUML actualizado está en docs/entidad-relacion.puml. La arquitectura está en docs/arquitectura.puml.")

    titulo(doc, "6. Carga del inventario")
    p(doc, "La plantilla es la del archivo FAU-CIDWO DATA GENERAL. La primera fila debe traer las columnas TITULO, NOMBRE AUTOR, APELLIDO AUTOR, FECHA, TIPO, FILA, EDITORIAL, COTA y Nº.EJEMPLARES. Puede existir una primera columna vacía, como en el archivo original. Una fila se considera ya cargada cuando coinciden título, autores, fecha, tipo, ubicación, editorial, cota y números de ejemplar. Un punto final de más o de menos en el título no cuenta como otra obra si el resto coincide. Esa fila no se vuelve a insertar y el catálogo que ya estaba no se borra. Las filas nuevas sí se agregan, junto con sus autores, editorial, ubicación, copias e incidencias de datos incompletos.")

    titulo(doc, "7. Ingreso")
    p(doc, "El usuario inicial es admin. La contraseña inicial es 123. Conviene cambiarla después de registrar las tres preguntas de seguridad, editando el propio usuario.")
    ruta = SALIDA / "Sistema_Biblioteca_FAU.docx"
    doc.save(ruta)
    return ruta


def instalacion():
    doc = base(
        "Biblioteca FAU — Instalación y módulos",
        "Paso a paso para dejar la aplicación lista y qué hace cada módulo.",
    )
    titulo(doc, "1. Qué se necesita")
    bullets(doc, [
        "Windows 10 o 11.",
        "Java JDK 17 o superior. En este equipo se usa el JDK 22 de Program Files\\Java.",
        "Node.js con npm. En este equipo está en C:\\nvm4w\\nodejs.",
        "PostgreSQL 16, en el puerto 5432, con la base biblioteca_fau.",
        "El proyecto en su carpeta y el acceso Biblioteca FAU.exe en el escritorio.",
        "Para la carga inicial o las cargas posteriores, un Excel .xlsx con la plantilla del inventario.",
    ])
    p(doc, "La contraseña de PostgreSQL no se repite en este documento. Es la que está en src/main/resources/application.properties, en spring.datasource.password. La contraseña de ingreso de la aplicación, la primera vez, es 123, con el usuario admin.")

    titulo(doc, "2. Instalación con un clic")
    bullets(doc, [
        "Copia o deja el ejecutable en el escritorio: Biblioteca FAU.exe.",
        "Haz doble clic.",
        "Si Java, Node, PostgreSQL o la base no están listos, el lanzador intenta prepararlos y muestra qué configuró.",
        "Si PostgreSQL no está instalado, Java no está instalado o Node no está instalado, el lanzador no puede instalar esos programas. Avisa cuál falta y se detiene.",
        "Cuando todo está listo, abre la ventana Biblioteca FAU.",
        "Entra con admin y 123.",
    ])

    titulo(doc, "3. Instalación manual")
    sub(doc, "Base de datos")
    bullets(doc, [
        "Instala PostgreSQL 16 y deja el servicio en marcha.",
        "Crea la base biblioteca_fau si no existe.",
        "Ejecuta src/main/resources/db/schema.sql contra esa base. El script crea las tablas y el usuario administrador si todavía no está.",
        "Confirma que application.properties apunta a localhost:5432, base biblioteca_fau, usuario postgres y la contraseña local.",
    ])
    sub(doc, "API")
    bullets(doc, [
        "Abre una consola en la carpeta del proyecto Java, la que contiene mvnw.cmd.",
        "Define JAVA_HOME con la carpeta del JDK.",
        "Ejecuta .\\mvnw.cmd spring-boot:run.",
        "Espera a que diga que Tomcat inició en el puerto 8081. No inicies otro servidor si ese puerto ya está ocupado.",
    ])
    sub(doc, "Ventana")
    bullets(doc, [
        "En la carpeta frontend ejecuta npm install --legacy-peer-deps la primera vez.",
        "Después ejecuta npm run desktop.",
        "La ventana carga http://127.0.0.1:5173 y, si hace falta, levanta Vite.",
    ])

    titulo(doc, "4. Primer uso")
    bullets(doc, [
        "Entra como admin.",
        "Abre Usuarios y edita tu propia cuenta para registrar las tres preguntas y respuestas. Sin eso no podrás reestablecer tu contraseña desde el ingreso.",
        "Crea los ayudantes que hagan falta y marca solo lo que cada uno puede hacer.",
        "En Catálogo usa Cargar Excel con datos si vas a incorporar obras nuevas con la plantilla. Las filas que ya estén completas en el sistema se ignoran.",
    ])

    titulo(doc, "5. Qué hace cada módulo")
    explicaciones = [
        ("Catálogo", "Es la lista de obras. El filtro muestra lo que empieza por el texto escrito. Nueva obra abre una ficha vacía. Cargar Excel con datos lee un .xlsx con las mismas columnas del inventario original. Abrir una fila entra a su ficha. El reloj muestra el historial de cambios de esa obra."),
        ("Ficha de la obra", "Concentra los datos bibliográficos, los autores asociados, las copias y las incidencias de esa obra. El título es obligatorio. El resto puede guardarse vacío, pero antes aparece la pregunta de confirmación con el nombre del campo vacío."),
        ("Autores", "Mantiene las personas responsables de las obras. El nombre es obligatorio. Si faltan apellido o nombre completo, hay que confirmar el guardado."),
        ("Editoriales", "Catálogo de casas editoras. Solo tiene el nombre, y ese nombre es obligatorio."),
        ("Ubicaciones", "Guarda el texto de la columna FILA del inventario y, cuando se puede interpretar, la estantería y la posición."),
        ("Ejemplares", "Cada copia física de una obra. Se presta por separado. Hay que indicar la obra. Número y ubicación pueden faltar, con confirmación."),
        ("Lectores", "Personas que piden material. No tienen acceso al programa. Hay que completar nombre, apellido, cédula, correo y teléfono. Semestre y carrera se pueden dejar en blanco."),
        ("Préstamos", "Registra la salida de un ejemplar disponible a un lector, con fecha estimada de devolución. Si el lector no existe, Registrar lector nuevo lo crea en una ventana y lo deja seleccionado. Devolver marca la devolución y libera el ejemplar."),
        ("Incidencias", "Anota datos del inventario que quedaron incompletos o incorrectos. Resolver abre una ventana, pide el valor corregido y solo entonces cambia el estado a resuelta."),
        ("Auditoría", "Consulta quién hizo cada acción y sobre qué registro. Sirve para seguir préstamos, devoluciones, correcciones e ingresos."),
        ("Usuarios", "Administración de cuentas. Solo el administrador entra aquí. Al crear, elige ayudante o administrador. El administrador nuevo debe dejar sus tres preguntas en la ventana que aparece. El ayudante recibe las casillas de permisos."),
    ]
    for nombre, texto in explicaciones:
        sub(doc, nombre)
        p(doc, texto)

    titulo(doc, "6. Plantilla del Excel")
    p(doc, "La primera fila del archivo debe llamarse, en este orden lógico: TITULO, NOMBRE AUTOR, APELLIDO AUTOR, FECHA, TIPO, FILA, EDITORIAL, COTA, Nº.EJEMPLARES. Es la misma plantilla de FAU-CIDWO DATA GENERAL. Si falta una columna, la carga se rechaza y no se modifica el catálogo. Si una fila coincide completa con una obra ya guardada, se cuenta como ignorada y no se duplica.")

    titulo(doc, "7. Si algo no abre")
    bullets(doc, [
        "Si el puerto 8081 ya tiene la API, no inicies otra. El ejecutable reutiliza la que está en marcha.",
        "Si la ventana dice que no conecta, comprueba que PostgreSQL esté iniciado y que la API haya arrancado.",
        "Si el ingreso del usuario anterior falla, usa admin. El perfil bibliotecaria ya no existe.",
        "Si Cargar Excel avisa que la plantilla no coincide, revisa los nombres de la primera fila. No cambies los títulos de las columnas.",
    ])
    ruta = SALIDA / "Instalacion_Biblioteca_FAU.docx"
    doc.save(ruta)
    return ruta


if __name__ == "__main__":
    SALIDA.mkdir(parents=True, exist_ok=True)
    print(sistema())
    print(instalacion())
