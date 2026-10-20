let alPerderSesion = () => {}

export function definirAlPerderSesion(funcion) {
  alPerderSesion = funcion
}

function mensajeDe(data, status) {
  if (!data) return `Error ${status}`
  if (typeof data === 'string') {
    if (data.includes('<html') || data.includes('<!DOCTYPE')) {
      return 'La API no respondió. Comprueba que la biblioteca esté iniciada en el puerto 8081.'
    }
    return data
  }
  return data.detail || data.message || data.error || data.title || `Error ${status}`
}

export async function api(path, options = {}) {
  const headers = { ...(options.headers || {}) }
  const esArchivo = typeof FormData !== 'undefined' && options.body instanceof FormData
  if (options.body != null && !esArchivo && !headers['Content-Type']) {
    headers['Content-Type'] = 'application/json'
  }

  let response
  try {
    response = await fetch(path, {
      credentials: 'include',
      ...options,
      headers,
    })
  } catch {
    throw new Error('No se pudo conectar con la API. Comprueba que el servidor esté en el puerto 8081.')
  }

  const text = await response.text()
  let data = null
  if (text) {
    try {
      data = JSON.parse(text)
    } catch {
      data = text
    }
  }

  if (response.status === 401 && path !== '/api/auth/login') {
    alPerderSesion()
  }

  if (!response.ok) {
    const mensaje = response.status === 502 || response.status === 503
      ? 'La API no está en marcha. Inicia el servidor de la biblioteca en el puerto 8081.'
      : mensajeDe(data, response.status)
    const error = new Error(mensaje)
    error.status = response.status
    throw error
  }

  return data
}
