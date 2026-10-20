# Crea Biblioteca FAU.exe con la carpeta de esta computadora y lo copia al escritorio.
# Si ya existe para esta misma carpeta, no vuelve a compilar.
# Siempre termina bien para no impedir que se abra la ventana.

$ErrorActionPreference = "Stop"

try {
    $raiz = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
    $launcher = Join-Path $raiz "frontend\launcher\Launcher.cs"
    $exe = Join-Path $raiz "Biblioteca FAU.exe"
    $marca = Join-Path $raiz "Biblioteca FAU.raiz"
    $escritorio = Join-Path ([Environment]::GetFolderPath("Desktop")) "Biblioteca FAU.exe"

    $listo = (Test-Path -LiteralPath $exe) -and (Test-Path -LiteralPath $marca)
    if ($listo) {
        $guardada = (Get-Content -LiteralPath $marca -Raw).Trim()
        if ($guardada -eq $raiz) {
            if (-not (Test-Path -LiteralPath $escritorio)) {
                Copy-Item -LiteralPath $exe -Destination $escritorio -Force
                Write-Host "Se copió Biblioteca FAU.exe al escritorio."
            }
            exit 0
        }
    }

    if (-not (Test-Path -LiteralPath $launcher)) {
        Write-Host "No está frontend\launcher\Launcher.cs. No se creó el ejecutable."
        exit 0
    }

    $csc = Join-Path $env:WINDIR "Microsoft.NET\Framework64\v4.0.30319\csc.exe"
    if (-not (Test-Path -LiteralPath $csc)) {
        $csc = Join-Path $env:WINDIR "Microsoft.NET\Framework\v4.0.30319\csc.exe"
    }
    if (-not (Test-Path -LiteralPath $csc)) {
        Write-Host "No se encontró el compilador de Windows. El programa se abre igual, sin ejecutable nuevo."
        exit 0
    }

    $texto = [System.IO.File]::ReadAllText($launcher)
    $coincidencia = [regex]::Match($texto, 'const string Raiz = @".*?";')
    if (-not $coincidencia.Success) {
        Write-Host "Launcher.cs no tiene la línea de la carpeta. No se creó el ejecutable."
        exit 0
    }
    $linea = 'const string Raiz = @"' + $raiz + '";'
    $cambiado = $texto.Remove($coincidencia.Index, $coincidencia.Length).Insert($coincidencia.Index, $linea)

    $temporal = Join-Path ([System.IO.Path]::GetTempPath()) ("LauncherFAU-" + [guid]::NewGuid().ToString("N") + ".cs")
    $utf8 = New-Object System.Text.UTF8Encoding $true
    [System.IO.File]::WriteAllText($temporal, $cambiado, $utf8)

    & $csc /nologo /target:winexe /r:System.ServiceProcess.dll /r:System.Windows.Forms.dll /r:System.Drawing.dll ("/out:" + $exe) $temporal
    $codigo = $LASTEXITCODE
    Remove-Item -LiteralPath $temporal -Force -ErrorAction SilentlyContinue
    if ($codigo -ne 0 -or -not (Test-Path -LiteralPath $exe)) {
        Write-Host "No se pudo compilar Biblioteca FAU.exe. El programa se abre igual."
        exit 0
    }

    Set-Content -LiteralPath $marca -Value $raiz -Encoding ASCII
    Copy-Item -LiteralPath $exe -Destination $escritorio -Force
    Write-Host "Se creó Biblioteca FAU.exe en la carpeta del proyecto y se copió al escritorio."
    Write-Host "Carpeta guardada dentro del ejecutable:"
    Write-Host $raiz
    exit 0
}
catch {
    Write-Host ("No se pudo crear el ejecutable: " + $_.Exception.Message)
    exit 0
}
