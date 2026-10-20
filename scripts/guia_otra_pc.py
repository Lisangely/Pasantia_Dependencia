from pathlib import Path

from docx import Document
from docx.shared import Pt, Cm, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH

SALIDA = Path(__file__).resolve().parents[1] / "docs" / "Correr_en_otra_computadora.docx"


def base():
    doc = Document()
    estilo = doc.styles["Normal"]
    estilo.font.name = "Calibri"
    estilo.font.size = Pt(11)
    for seccion in doc.sections:
        seccion.top_margin = Cm(2)
        seccion.bottom_margin = Cm(2)
        seccion.left_margin = Cm(2.2)
        seccion.right_margin = Cm(2.2)
    titulo = doc.add_heading("Biblioteca FAU — Correr el sistema en otra computadora", 0)
    titulo.alignment = WD_ALIGN_PARAGRAPH.LEFT
    nota = doc.add_paragraph(
        "Guía desde cero para Windows 10 u 11. La computadora nueva no tiene Java, Node.js ni PostgreSQL. "
        "Todos los comandos se escriben en PowerShell."
    )
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


def comando(doc, texto):
    parrafo = doc.add_paragraph()
    run = parrafo.add_run(texto)
    run.font.name = "Consolas"
    run.font.size = Pt(10)
    run.font.color.rgb = RGBColor(0x1C, 0x24, 0x20)
    parrafo.paragraph_format.left_indent = Cm(0.4)
    parrafo.paragraph_format.space_before = Pt(2)
    parrafo.paragraph_format.space_after = Pt(2)


def main():
    doc = base()

    titulo(doc, "1. Qué queda instalado")
    p(doc, "Al terminar vas a tener cuatro piezas. Las tres primeras se instalan una sola vez. La cuarta es el proyecto.")
    bullets(doc, [
        "Git, para descargar el código desde GitHub.",
        "Java JDK 17 o superior. La API está hecha con Java 17. Sirve el JDK 17 o el 22.",
        "Node.js con npm, para la ventana de la aplicación.",
        "PostgreSQL 16, en el puerto 5432. Ahí vive la base biblioteca_fau.",
        "El proyecto clonado desde https://github.com/Lisangely/Pasantia_Dependencia.git",
    ])
    p(doc, "La aplicación no es una página que se abra en el navegador. Una ventana de escritorio habla con una API en el puerto 8081, y esa API guarda todo en PostgreSQL.")
    p(doc, "El archivo Biblioteca FAU.exe que viene dentro del repositorio no sirve en el equipo nuevo: busca la carpeta de la computadora donde se compiló. La primera vez que abras la ventana con npm run desktop, el proyecto crea un ejecutable nuevo con la carpeta de esta computadora y lo copia al escritorio. El paso 14 explica dónde queda y qué hacer si no aparece.")

    titulo(doc, "2. Abrir PowerShell")
    p(doc, "Pulsa la tecla Windows, escribe PowerShell y ábrelo. Déjalo abierto. Cada bloque de comandos se pega ahí y se confirma con Enter. Si un comando pide permiso de administrador, acepta.")

    titulo(doc, "3. Instalar Git")
    p(doc, "Entra a https://git-scm.com/download/win y descarga el instalador de 64 bits. En el asistente deja las opciones que vienen marcadas y pulsa Next hasta el final.")
    p(doc, "Cierra PowerShell, ábrelo de nuevo y comprueba:")
    comando(doc, "git --version")
    p(doc, "Tiene que mostrar un número de versión, por ejemplo git version 2.47.0. Si dice que no reconoce el comando, cierra PowerShell y ábrelo otra vez.")

    titulo(doc, "4. Instalar Java")
    p(doc, "Entra a https://adoptium.net/ y descarga Temurin JDK 17 para Windows x64, el archivo .msi. Instálalo con las opciones de defecto. Si el instalador ofrece «Set JAVA_HOME» o «Add to PATH», márcalas.")
    p(doc, "Cierra PowerShell, ábrelo de nuevo y comprueba:")
    comando(doc, "java -version")
    p(doc, "Tiene que decir openjdk version «17» o una versión mayor. Después localiza la carpeta del JDK:")
    comando(doc, 'Get-ChildItem "C:\\Program Files\\Eclipse Adoptium"')
    comando(doc, 'Get-ChildItem "C:\\Program Files\\Java"')
    p(doc, "Una de las dos carpetas existe. Anota el nombre completo, por ejemplo C:\\Program Files\\Eclipse Adoptium\\jdk-17.0.16.8-hotspot. En esta misma ventana de PowerShell define JAVA_HOME. Cambia la ruta por la que te salió:")
    comando(doc, '$env:JAVA_HOME = "C:\\Program Files\\Eclipse Adoptium\\jdk-17.0.16.8-hotspot"')
    comando(doc, '$env:Path = "$env:JAVA_HOME\\bin;" + $env:Path')
    comando(doc, "java -version")
    p(doc, "JAVA_HOME de esta forma solo dura mientras esa ventana de PowerShell siga abierta. Cada vez que abras una ventana nueva para arrancar la API, vuelve a ejecutar esas dos líneas de $env:JAVA_HOME y $env:Path.")

    titulo(doc, "5. Instalar Node.js")
    p(doc, "Entra a https://nodejs.org/ y descarga la versión LTS para Windows, el instalador .msi de 64 bits. Instálalo con las opciones de defecto. Eso incluye npm.")
    p(doc, "Cierra PowerShell, ábrelo de nuevo y comprueba:")
    comando(doc, "node -v")
    comando(doc, "npm -v")
    p(doc, "Los dos tienen que mostrar un número de versión. Si acabas de definir JAVA_HOME en la ventana anterior, esta ventana nueva no lo tiene. No importa todavía: Node y la API se arrancan en ventanas distintas.")

    titulo(doc, "6. Instalar PostgreSQL 16")
    p(doc, "Entra a https://www.postgresql.org/download/windows/ y descarga el instalador de EDB para PostgreSQL 16, Windows x86-64.")
    p(doc, "En el asistente:")
    bullets(doc, [
        "Deja el directorio de instalación que propone, normalmente C:\\Program Files\\PostgreSQL\\16.",
        "Deja el puerto 5432.",
        "Cuando pida la contraseña del usuario postgres, elige una y anótala. Esa clave es solo de la base de datos. No es la clave para entrar a la biblioteca.",
        "Locale: deja el que viene o elige Spanish, Venezuela, si aparece.",
        "Al final puedes desmarcar Stack Builder. No hace falta.",
    ])
    p(doc, "Comprueba que el programa responde. Te va a pedir la contraseña de postgres que acabas de elegir:")
    comando(doc, '& "C:\\Program Files\\PostgreSQL\\16\\bin\\psql.exe" -U postgres -h localhost -p 5432 -c "SELECT version();"')
    p(doc, "Si ves un texto que empieza por PostgreSQL 16, la base está instalada. Escribe \\q y Enter si te deja dentro de psql. El comando de arriba ya cierra solo.")

    titulo(doc, "7. Descargar el proyecto")
    p(doc, "En PowerShell:")
    comando(doc, "cd $env:USERPROFILE\\Documents")
    comando(doc, "git clone https://github.com/Lisangely/Pasantia_Dependencia.git")
    comando(doc, "cd Pasantia_Dependencia")
    comando(doc, "pwd")
    p(doc, "pwd tiene que terminar en \\Pasantia_Dependencia. Esa carpeta contiene pom.xml, mvnw.cmd y la carpeta frontend. Todos los comandos que siguen se ejecutan desde ahí, salvo cuando se indique entrar a frontend.")

    titulo(doc, "8. Poner la contraseña de PostgreSQL en la API")
    p(doc, "El repositorio no trae la contraseña de la base. Hay un ejemplo. Cópialo y ábrelo:")
    comando(doc, "Copy-Item src\\main\\resources\\application.properties.example src\\main\\resources\\application.properties")
    comando(doc, "notepad src\\main\\resources\\application.properties")
    p(doc, "En el bloc de notas cambia solo esta línea. Pon la contraseña que elegiste al instalar PostgreSQL, sin comillas:")
    comando(doc, "spring.datasource.password=TU_CLAVE")
    p(doc, "Tiene que quedar, por ejemplo, spring.datasource.password=clavequeelegi. No cambies el puerto 8081 ni el nombre biblioteca_fau. Guarda el archivo y cierra el bloc de notas.")
    p(doc, "Ese archivo se queda solo en esta computadora. No lo subas a GitHub.")

    titulo(doc, "9. Crear la base y las tablas")
    p(doc, "Sigue estando en la carpeta Pasantia_Dependencia. Estos dos comandos piden la contraseña de postgres. Escríbela cuando la pida y pulsa Enter. No se ve mientras la escribes.")
    comando(doc, '& "C:\\Program Files\\PostgreSQL\\16\\bin\\psql.exe" -U postgres -h localhost -p 5432 -c "CREATE DATABASE biblioteca_fau;"')
    comando(doc, '& "C:\\Program Files\\PostgreSQL\\16\\bin\\psql.exe" -U postgres -h localhost -p 5432 -d biblioteca_fau -f "src\\main\\resources\\db\\schema.sql"')
    p(doc, "El primero responde CREATE DATABASE. El segundo responde CREATE TABLE varias veces y un INSERT 0 1. Ese INSERT crea el usuario de la aplicación.")
    p(doc, "Si el primero dice que la base ya existe, no la borres. Sigue con el segundo. El esquema no pisa datos si las tablas ya estaban.")
    p(doc, "Comprueba que el usuario quedó creado:")
    comando(doc, '& "C:\\Program Files\\PostgreSQL\\16\\bin\\psql.exe" -U postgres -h localhost -p 5432 -d biblioteca_fau -c "SELECT usuario, rol FROM usuarios_sistema;"')
    p(doc, "Tiene que salir una fila: admin, administrador.")

    titulo(doc, "10. Arrancar la API")
    p(doc, "Abre una ventana de PowerShell y no la cierres. Esta ventana se queda trabajando mientras uses la biblioteca.")
    comando(doc, "cd $env:USERPROFILE\\Documents\\Pasantia_Dependencia")
    comando(doc, '$env:JAVA_HOME = "C:\\Program Files\\Eclipse Adoptium\\jdk-17.0.16.8-hotspot"')
    comando(doc, '$env:Path = "$env:JAVA_HOME\\bin;" + $env:Path')
    comando(doc, ".\\mvnw.cmd spring-boot:run")
    p(doc, "La primera vez Maven descarga dependencias. Puede tardar varios minutos y necesita internet. Espera a ver esta línea:")
    comando(doc, "Tomcat started on port 8081")
    p(doc, "Cuando aparezca, la API ya está lista. No cierres esa ventana y no pulses Ctrl+C. Si el puerto 8081 ya está ocupado, no arranques una segunda copia.")

    titulo(doc, "11. Abrir la ventana")
    p(doc, "Abre otra ventana de PowerShell, distinta de la anterior.")
    comando(doc, "cd $env:USERPROFILE\\Documents\\Pasantia_Dependencia\\frontend")
    comando(doc, "npm install --legacy-peer-deps")
    p(doc, "npm install se hace solo la primera vez, o cuando cambien las dependencias. Cuando termine:")
    comando(doc, "npm run desktop")
    p(doc, "Se abre la ventana Biblioteca FAU. La primera vez también levanta la interfaz en http://127.0.0.1:5173. Deja esta segunda ventana abierta mientras uses el programa.")

    titulo(doc, "12. Entrar")
    bullets(doc, [
        "Usuario: admin",
        "Clave: 123",
    ])
    p(doc, "Ese usuario es el administrador. Puede crear ayudantes, cargar el Excel del inventario y devolver una obra a una versión anterior. La clave 123 es la inicial. Conviene cambiarla desde Usuarios después de registrar las tres preguntas de seguridad.")
    p(doc, "La base empieza vacía. No hay obras, ejemplares ni lectores hasta que se carguen o se registren.")

    titulo(doc, "13. Cómo apagarlo")
    p(doc, "Cierra la ventana de la biblioteca. En las dos ventanas de PowerShell pulsa Ctrl+C y confirma con S o Y si lo pide. Primero la de npm run desktop y después la de mvnw.cmd. PostgreSQL puede quedarse encendido: es el servicio de Windows y no estorba.")

    titulo(doc, "14. El ejecutable se crea solo la primera vez")
    p(doc, "No hay que editar archivos ni copiar nada a mano. Cuando en el paso 11 ejecutas npm run desktop por primera vez, antes de abrir la ventana el proyecto hace tres cosas:")
    bullets(doc, [
        "Detecta la carpeta donde está el proyecto, la que contiene mvnw.cmd.",
        "Crea Biblioteca FAU.exe dentro de esa carpeta, con esa ruta guardada adentro.",
        "Copia ese archivo nuevo al escritorio.",
    ])
    p(doc, "En la ventana de PowerShell tiene que aparecer un texto como este:")
    comando(doc, "Se creó Biblioteca FAU.exe en la carpeta del proyecto y se copió al escritorio.")
    p(doc, "Debajo muestra la carpeta que quedó guardada. Si clonaste en Documentos de la cuenta Ana, será:")
    comando(doc, "C:\\Users\\Ana\\Documents\\Pasantia_Dependencia")
    p(doc, "El archivo del escritorio es el que se usa las próximas veces, con doble clic. El que venía dentro del repositorio queda reemplazado por este. Java, Node.js y PostgreSQL tienen que seguir instalados. Si más adelante mueves la carpeta del proyecto, vuelve a ejecutar npm run desktop una vez: se genera de nuevo con la ruta nueva y se vuelve a copiar al escritorio.")
    sub(doc, "Si no apareció en el escritorio")
    p(doc, "Desde la carpeta del proyecto puedes crearlo con este comando. Usa la misma carpeta que contiene mvnw.cmd:")
    comando(doc, "cd $env:USERPROFILE\\Documents\\Pasantia_Dependencia")
    comando(doc, "powershell -NoProfile -ExecutionPolicy Bypass -File .\\scripts\\crear_ejecutable.ps1")
    p(doc, "Eso deja Biblioteca FAU.exe al lado de mvnw.cmd y otra copia en el escritorio. No hace falta escribir la ruta en ningún archivo: el script la toma de la carpeta donde está el proyecto.")

    titulo(doc, "15. Volver a abrirlo otro día")
    p(doc, "Si el paso 14 dejó Biblioteca FAU.exe en el escritorio, ábrelo con doble clic. No hace falta abrir PowerShell.")
    p(doc, "Si todavía no existe ese ejecutable, abre las dos ventanas.")
    p(doc, "Ventana 1, la API:")
    comando(doc, "cd $env:USERPROFILE\\Documents\\Pasantia_Dependencia")
    comando(doc, '$env:JAVA_HOME = "C:\\Program Files\\Eclipse Adoptium\\jdk-17.0.16.8-hotspot"')
    comando(doc, '$env:Path = "$env:JAVA_HOME\\bin;" + $env:Path')
    comando(doc, ".\\mvnw.cmd spring-boot:run")
    p(doc, "Espera «Tomcat started on port 8081».")
    p(doc, "Ventana 2, la aplicación:")
    comando(doc, "cd $env:USERPROFILE\\Documents\\Pasantia_Dependencia\\frontend")
    comando(doc, "npm run desktop")

    titulo(doc, "16. Si algo falla")
    sub(doc, "«java no se reconoce» o Maven no arranca")
    p(doc, "JAVA_HOME no está definido en esa ventana. Repite las dos líneas de $env:JAVA_HOME y $env:Path del paso 10, con la carpeta real del JDK, y vuelve a lanzar .\\mvnw.cmd spring-boot:run.")
    sub(doc, "«npm no se reconoce»")
    p(doc, "Cierra PowerShell después de instalar Node.js y ábrelo de nuevo. Comprueba con npm -v.")
    sub(doc, "La API dice que no conecta a PostgreSQL")
    p(doc, "El servicio no está iniciado o la contraseña del archivo no coincide. Revisa spring.datasource.password. Para iniciar el servicio:")
    comando(doc, "Start-Service postgresql-x64-16")
    p(doc, "Si el nombre del servicio es otro, lista los que contienen postgres:")
    comando(doc, "Get-Service *postgres*")
    sub(doc, "psql no se encuentra")
    p(doc, "PostgreSQL quedó en otra carpeta. Busca psql.exe:")
    comando(doc, 'Get-ChildItem "C:\\Program Files\\PostgreSQL" -Recurse -Filter psql.exe')
    p(doc, "Usa esa ruta completa en lugar de C:\\Program Files\\PostgreSQL\\16\\bin\\psql.exe.")
    sub(doc, "La ventana dice que no llega a la API")
    p(doc, "La primera ventana tiene que seguir mostrando la API en marcha, con el puerto 8081. No la cierres. Si se detuvo, vuelve al paso 10.")
    sub(doc, "El usuario admin no entra")
    p(doc, "Vuelve a aplicar el esquema. No borra las tablas si ya existen, y crea el usuario admin si todavía no está:")
    comando(doc, '& "C:\\Program Files\\PostgreSQL\\16\\bin\\psql.exe" -U postgres -h localhost -p 5432 -d biblioteca_fau -f "src\\main\\resources\\db\\schema.sql"')
    p(doc, "Entra con admin y 123.")

    SALIDA.parent.mkdir(parents=True, exist_ok=True)
    candidatos = [
        SALIDA,
        SALIDA.with_name("Correr_en_otra_computadora_actualizado.docx"),
        SALIDA.with_name("Correr_en_otra_computadora_nuevo.docx"),
    ]
    for ruta in candidatos:
        try:
            doc.save(ruta)
            print(ruta)
            return
        except PermissionError:
            continue
    raise PermissionError("Cierra el Word de la guía y vuelve a generar el documento.")


if __name__ == "__main__":
    main()
