const { app, BrowserWindow } = require('electron')
const { spawn } = require('child_process')
const http = require('http')
const path = require('path')

const URL_APP = 'http://127.0.0.1:5173'
const RAIZ = path.join(__dirname, '..')
let procesoVite = null

function estaActiva(url) {
  return new Promise((resolve) => {
    const solicitud = http.get(url, (respuesta) => {
      respuesta.resume()
      resolve(Boolean(respuesta.statusCode && respuesta.statusCode < 500))
    })
    solicitud.on('error', () => resolve(false))
    solicitud.setTimeout(1500, () => {
      solicitud.destroy()
      resolve(false)
    })
  })
}

function esperar(url) {
  return new Promise((resolve) => {
    const intentar = () => {
      estaActiva(url).then((activa) => {
        if (activa) resolve()
        else setTimeout(intentar, 400)
      })
    }
    intentar()
  })
}

async function prepararInterfaz() {
  if (await estaActiva(URL_APP)) return

  procesoVite = spawn('npm', ['run', 'dev', '--', '--host', '127.0.0.1', '--port', '5173'], {
    cwd: RAIZ,
    shell: true,
    stdio: 'inherit',
  })

  await esperar(URL_APP)
}

function crearVentana() {
  const ventana = new BrowserWindow({
    width: 1280,
    height: 840,
    minWidth: 980,
    minHeight: 680,
    title: 'Biblioteca FAU',
    show: false,
    autoHideMenuBar: true,
    backgroundColor: '#f3ecdf',
    webPreferences: {
      contextIsolation: true,
      nodeIntegration: false,
    },
  })

  ventana.loadURL(URL_APP)
  ventana.once('ready-to-show', () => {
    ventana.show()
    console.log('Ventana lista')
  })
}

function cerrarInterfaz() {
  if (!procesoVite || !procesoVite.pid) return
  spawn('taskkill', ['/pid', String(procesoVite.pid), '/t', '/f'], { shell: true })
  procesoVite = null
}

app.whenReady().then(async () => {
  await prepararInterfaz()
  crearVentana()
})

app.on('window-all-closed', () => {
  cerrarInterfaz()
  app.quit()
})
