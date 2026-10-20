using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Net.Sockets;
using System.ServiceProcess;
using System.Text;
using System.Threading;
using System.Windows.Forms;

static class Programa
{
    const string Raiz = @"c:\Users\jesus\Documents\pasantia lis\Pasantia_Dependencia-main\Pasantia_Dependencia-main";
    const string BaseDatos = "biblioteca_fau";

    [STAThread]
    static void Main()
    {
        Application.EnableVisualStyles();
        var ventana = new Form();
        ventana.Text = "Biblioteca FAU";
        ventana.FormBorderStyle = FormBorderStyle.FixedDialog;
        ventana.StartPosition = FormStartPosition.CenterScreen;
        ventana.MaximizeBox = false;
        ventana.MinimizeBox = false;
        ventana.ClientSize = new System.Drawing.Size(460, 110);
        var aviso = new Label();
        aviso.Dock = DockStyle.Fill;
        aviso.TextAlign = System.Drawing.ContentAlignment.MiddleCenter;
        aviso.Font = new System.Drawing.Font("Segoe UI", 11f);
        aviso.Text = "Revisando la biblioteca...";
        ventana.Controls.Add(aviso);

        ventana.Shown += delegate
        {
            ThreadPool.QueueUserWorkItem(delegate
            {
                try
                {
                    var hechos = new List<string>();
                    string javaHome = BuscarJava();
                    string npm = BuscarNpm();
                    string psql = BuscarPsql();
                    string clave = LeerClaveBase();

                    PrepararInterfaz(npm, hechos, ventana, aviso);
                    PrepararBase(psql, clave, hechos, ventana, aviso);

                    if (hechos.Count > 0)
                    {
                        string texto = "Se configuró lo siguiente para poder abrir la biblioteca:\n\n• "
                            + string.Join("\n• ", hechos.ToArray());
                        Avisar(ventana, texto);
                    }

                    if (!PuertoAbierto(8081))
                    {
                        CambiarTexto(ventana, aviso, "Iniciando el servidor...");
                        IniciarApi(javaHome);
                        if (!EsperarPuerto(8081, 180))
                        {
                            throw new Exception("El servidor no respondió en el puerto 8081. Comprueba que PostgreSQL siga en marcha.");
                        }
                    }

                    CambiarTexto(ventana, aviso, "Abriendo la ventana...");
                    IniciarEscritorio(npm);
                    ventana.BeginInvoke(new Action(ventana.Close));
                }
                catch (Exception error)
                {
                    ventana.BeginInvoke(new Action(delegate
                    {
                        MessageBox.Show(ventana, error.Message, "Biblioteca FAU", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                        ventana.Close();
                    }));
                }
            });
        };

        Application.Run(ventana);
    }

    static void PrepararInterfaz(string npm, List<string> hechos, Form ventana, Label aviso)
    {
        string electron = Path.Combine(Raiz, "frontend", "node_modules", "electron", "package.json");
        string vite = Path.Combine(Raiz, "frontend", "node_modules", "vite", "package.json");
        if (File.Exists(electron) && File.Exists(vite))
        {
            return;
        }

        CambiarTexto(ventana, aviso, "Instalando la ventana de la biblioteca...");
        int codigo = Ejecutar(npm, "install --legacy-peer-deps", Path.Combine(Raiz, "frontend"), null, 600000);
        if (codigo != 0 || !File.Exists(electron))
        {
            throw new Exception("No se pudieron instalar los componentes de la ventana. Revisa Node.js y vuelve a abrir este acceso.");
        }
        hechos.Add("Se instalaron los componentes de la ventana.");
    }

    static void PrepararBase(string psql, string clave, List<string> hechos, Form ventana, Label aviso)
    {
        if (!PuertoAbierto(5432))
        {
            CambiarTexto(ventana, aviso, "Iniciando PostgreSQL...");
            IniciarServicioPostgres();
            if (!EsperarPuerto(5432, 40))
            {
                throw new Exception("PostgreSQL no está en marcha. Ábrelo e intenta de nuevo. Si Windows pide permiso, acéptalo para iniciar el servicio.");
            }
            hechos.Add("Se inició PostgreSQL.");
        }

        CambiarTexto(ventana, aviso, "Revisando la base de datos...");
        var entorno = new Dictionary<string, string>();
        entorno["PGPASSWORD"] = clave;

        string existe = EjecutarTexto(
            psql,
            "-h localhost -p 5432 -U postgres -d postgres -tAc \"SELECT 1 FROM pg_database WHERE datname = '" + BaseDatos + "'\"",
            Raiz,
            entorno,
            30000);

        if (!ContieneLinea(existe, "1"))
        {
            CambiarTexto(ventana, aviso, "Creando la base de datos...");
            int creada = Ejecutar(
                psql,
                "-h localhost -p 5432 -U postgres -d postgres -v ON_ERROR_STOP=1 -c \"CREATE DATABASE " + BaseDatos + "\"",
                Raiz,
                entorno,
                60000);
            if (creada != 0)
            {
                throw new Exception("PostgreSQL está activo, pero no se pudo crear la base de datos. Revisa la contraseña guardada en la configuración de la API.");
            }
            hechos.Add("Se creó la base de datos de la biblioteca.");
        }

        string tabla = EjecutarTexto(
            psql,
            "-h localhost -p 5432 -U postgres -d " + BaseDatos + " -tAc \"SELECT to_regclass('public.usuarios_sistema')\"",
            Raiz,
            entorno,
            30000);

        if (tabla.IndexOf("usuarios_sistema", StringComparison.OrdinalIgnoreCase) < 0)
        {
            CambiarTexto(ventana, aviso, "Preparando las tablas...");
            string esquema = Path.Combine(Raiz, "src", "main", "resources", "db", "schema.sql");
            int aplicado = Ejecutar(
                psql,
                "-h localhost -p 5432 -U postgres -d " + BaseDatos + " -v ON_ERROR_STOP=1 -f \"" + esquema + "\"",
                Raiz,
                entorno,
                120000);
            if (aplicado != 0)
            {
                throw new Exception("No se pudo preparar el esquema de la biblioteca.");
            }
            hechos.Add("Se prepararon las tablas y el usuario admin. La contraseña inicial es 123.");
        }
    }

    static void IniciarServicioPostgres()
    {
        ServiceController elegido = null;
        foreach (ServiceController servicio in ServiceController.GetServices())
        {
            if (servicio.ServiceName.IndexOf("postgres", StringComparison.OrdinalIgnoreCase) < 0)
            {
                continue;
            }
            if (servicio.ServiceName.IndexOf("16", StringComparison.OrdinalIgnoreCase) >= 0)
            {
                elegido = servicio;
                break;
            }
            if (elegido == null)
            {
                elegido = servicio;
            }
        }
        if (elegido == null)
        {
            throw new Exception("No está instalado PostgreSQL. Instálalo y vuelve a abrir este acceso.");
        }
        if (elegido.Status == ServiceControllerStatus.Running)
        {
            return;
        }
        elegido.Start();
        elegido.WaitForStatus(ServiceControllerStatus.Running, TimeSpan.FromSeconds(30));
    }

    static string BuscarJava()
    {
        string desdeEntorno = Environment.GetEnvironmentVariable("JAVA_HOME");
        if (JavaValido(desdeEntorno))
        {
            return desdeEntorno;
        }

        string carpeta = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles), "Java");
        if (!Directory.Exists(carpeta))
        {
            throw new Exception("Falta Java. Instala un JDK y vuelve a abrir este acceso.");
        }

        string preferido = Path.Combine(carpeta, "jdk-22");
        if (JavaValido(preferido))
        {
            return preferido;
        }

        foreach (string directorio in Directory.GetDirectories(carpeta))
        {
            string nombre = Path.GetFileName(directorio);
            if (nombre.StartsWith("jdk", StringComparison.OrdinalIgnoreCase) && JavaValido(directorio))
            {
                return directorio;
            }
        }

        throw new Exception("Falta Java. Instala un JDK y vuelve a abrir este acceso.");
    }

    static bool JavaValido(string carpeta)
    {
        return !string.IsNullOrEmpty(carpeta) && File.Exists(Path.Combine(carpeta, "bin", "java.exe"));
    }

    static string BuscarNpm()
    {
        string[] fijos = new string[]
        {
            @"C:\nvm4w\nodejs\npm.cmd",
            Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles), "nodejs", "npm.cmd")
        };
        foreach (string candidato in fijos)
        {
            if (File.Exists(candidato))
            {
                return candidato;
            }
        }

        string path = Environment.GetEnvironmentVariable("PATH") ?? "";
        foreach (string parte in path.Split(';'))
        {
            if (parte.Trim().Length == 0)
            {
                continue;
            }
            string candidato = Path.Combine(parte.Trim(), "npm.cmd");
            if (File.Exists(candidato))
            {
                return candidato;
            }
        }

        throw new Exception("Falta Node.js. Instálalo y vuelve a abrir este acceso.");
    }

    static string BuscarPsql()
    {
        string carpeta = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles), "PostgreSQL");
        string preferido = Path.Combine(carpeta, "16", "bin", "psql.exe");
        if (File.Exists(preferido))
        {
            return preferido;
        }
        if (Directory.Exists(carpeta))
        {
            foreach (string directorio in Directory.GetDirectories(carpeta))
            {
                string candidato = Path.Combine(directorio, "bin", "psql.exe");
                if (File.Exists(candidato))
                {
                    return candidato;
                }
            }
        }
        throw new Exception("No está instalado PostgreSQL. Instálalo y vuelve a abrir este acceso.");
    }

    static string LeerClaveBase()
    {
        string archivo = Path.Combine(Raiz, "src", "main", "resources", "application.properties");
        if (!File.Exists(archivo))
        {
            throw new Exception("No se encontró la configuración de la API.");
        }
        foreach (string linea in File.ReadAllLines(archivo))
        {
            const string marca = "spring.datasource.password=";
            if (linea.StartsWith(marca, StringComparison.Ordinal))
            {
                return linea.Substring(marca.Length).Trim();
            }
        }
        throw new Exception("La configuración de la API no tiene la contraseña de la base de datos.");
    }

    static void IniciarApi(string javaHome)
    {
        var proceso = new ProcessStartInfo();
        proceso.FileName = "cmd.exe";
        proceso.Arguments = "/c mvnw.cmd spring-boot:run";
        proceso.WorkingDirectory = Raiz;
        proceso.UseShellExecute = false;
        proceso.CreateNoWindow = true;
        proceso.EnvironmentVariables["JAVA_HOME"] = javaHome;
        proceso.EnvironmentVariables["PATH"] = Path.Combine(javaHome, "bin") + ";" + proceso.EnvironmentVariables["PATH"];
        Process.Start(proceso);
    }

    static void IniciarEscritorio(string npm)
    {
        string node = Path.GetDirectoryName(npm);
        var proceso = new ProcessStartInfo();
        proceso.FileName = npm;
        proceso.Arguments = "run desktop";
        proceso.WorkingDirectory = Path.Combine(Raiz, "frontend");
        proceso.UseShellExecute = false;
        proceso.CreateNoWindow = true;
        proceso.EnvironmentVariables["PATH"] = node + ";" + proceso.EnvironmentVariables["PATH"];
        Process.Start(proceso);
    }

    static int Ejecutar(string archivo, string argumentos, string directorio, Dictionary<string, string> entorno, int esperaMs)
    {
        EjecutarTexto(archivo, argumentos, directorio, entorno, esperaMs);
        return ultimoCodigo;
    }

    static int ultimoCodigo;

    static string EjecutarTexto(string archivo, string argumentos, string directorio, Dictionary<string, string> entorno, int esperaMs)
    {
        var info = new ProcessStartInfo();
        info.FileName = archivo;
        info.Arguments = argumentos;
        info.WorkingDirectory = directorio;
        info.UseShellExecute = false;
        info.CreateNoWindow = true;
        info.RedirectStandardOutput = true;
        info.RedirectStandardError = true;
        info.StandardOutputEncoding = Encoding.UTF8;
        info.StandardErrorEncoding = Encoding.UTF8;
        if (entorno != null)
        {
            foreach (KeyValuePair<string, string> par in entorno)
            {
                info.EnvironmentVariables[par.Key] = par.Value;
            }
        }

        var salida = new StringBuilder();
        using (Process proceso = Process.Start(info))
        {
            proceso.OutputDataReceived += delegate(object sender, DataReceivedEventArgs evento)
            {
                if (evento.Data != null)
                {
                    salida.AppendLine(evento.Data);
                }
            };
            proceso.ErrorDataReceived += delegate(object sender, DataReceivedEventArgs evento)
            {
                if (evento.Data != null)
                {
                    salida.AppendLine(evento.Data);
                }
            };
            proceso.BeginOutputReadLine();
            proceso.BeginErrorReadLine();
            if (!proceso.WaitForExit(esperaMs))
            {
                try { proceso.Kill(); } catch { }
                throw new Exception("Una preparación tardó demasiado: " + Path.GetFileName(archivo));
            }
            proceso.WaitForExit();
            ultimoCodigo = proceso.ExitCode;
        }
        return salida.ToString();
    }

    static bool ContieneLinea(string salida, string esperado)
    {
        foreach (string linea in salida.Split(new char[] { '\r', '\n' }))
        {
            if (linea.Trim() == esperado)
            {
                return true;
            }
        }
        return false;
    }

    static void Avisar(Form ventana, string texto)
    {
        ventana.Invoke(new Action(delegate
        {
            MessageBox.Show(ventana, texto, "Biblioteca FAU", MessageBoxButtons.OK, MessageBoxIcon.Information);
        }));
    }

    static void CambiarTexto(Form ventana, Label aviso, string texto)
    {
        ventana.BeginInvoke(new Action(delegate { aviso.Text = texto; }));
    }

    static bool PuertoAbierto(int puerto)
    {
        try
        {
            using (var cliente = new TcpClient())
            {
                var resultado = cliente.BeginConnect("127.0.0.1", puerto, null, null);
                bool conecto = resultado.AsyncWaitHandle.WaitOne(TimeSpan.FromMilliseconds(700));
                if (!conecto)
                {
                    return false;
                }
                cliente.EndConnect(resultado);
                return true;
            }
        }
        catch
        {
            return false;
        }
    }

    static bool EsperarPuerto(int puerto, int segundos)
    {
        var limite = DateTime.UtcNow.AddSeconds(segundos);
        while (DateTime.UtcNow < limite)
        {
            if (PuertoAbierto(puerto))
            {
                return true;
            }
            Thread.Sleep(1000);
        }
        return false;
    }
}
