import { useEffect, useRef, useState } from 'react'

export function normalizar(valor) {
  return String(valor ?? '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLocaleLowerCase('es')
}

export function empiezaPor(texto, consulta) {
  const busqueda = normalizar(consulta).trim()
  if (!busqueda) return true
  return normalizar(texto).startsWith(busqueda)
}

export function algunoEmpieza(consulta, textos) {
  const busqueda = normalizar(consulta).trim()
  if (!busqueda) return true
  return textos.some((texto) => normalizar(texto).startsWith(busqueda))
}

export function Busqueda({ valor, onChange, placeholder = 'Escribe para filtrar' }) {
  return (
    <div className="filtros">
      <Campo etiqueta="Buscar">
        <input
          value={valor}
          onChange={(evento) => onChange(evento.target.value)}
          placeholder={placeholder}
        />
      </Campo>
    </div>
  )
}

export function Selector({ opciones, valor, onChange, placeholder = 'Escribe para buscar', requerido = false }) {
  const seleccionado = opciones.find((opcion) => String(opcion.valor) === String(valor))
  const [texto, setTexto] = useState(seleccionado?.etiqueta || '')
  const [abierto, setAbierto] = useState(false)
  const [activo, setActivo] = useState(0)
  const caja = useRef(null)
  const campo = useRef(null)
  const etiquetaElegida = useRef('')
  etiquetaElegida.current = seleccionado?.etiqueta || ''

  useEffect(() => {
    setTexto(seleccionado?.etiqueta || '')
  }, [seleccionado?.etiqueta])

  useEffect(() => {
    campo.current?.setCustomValidity(requerido && !valor ? 'Elige una opción de la lista' : '')
  }, [requerido, valor])

  useEffect(() => {
    function cerrar(evento) {
      if (caja.current && !caja.current.contains(evento.target)) setAbierto(false)
    }
    document.addEventListener('mousedown', cerrar)
    return () => document.removeEventListener('mousedown', cerrar)
  }, [])

  const busqueda = texto.trim()
  const encontradas = busqueda
    ? opciones.filter((opcion) => algunoEmpieza(busqueda, [opcion.etiqueta, ...(opcion.claves || [])]))
    : []
  const coincidencias = encontradas.slice(0, 30)

  function elegir(opcion) {
    onChange(String(opcion.valor))
    setTexto(opcion.etiqueta)
    setAbierto(false)
  }

  function escribir(evento) {
    const escrito = evento.target.value
    setTexto(escrito)
    setActivo(0)
    setAbierto(Boolean(escrito.trim()))
    if (!seleccionado || escrito !== seleccionado.etiqueta) onChange('')
  }

  function teclas(evento) {
    if (!abierto || coincidencias.length === 0) return
    if (evento.key === 'ArrowDown') {
      evento.preventDefault()
      setActivo((indice) => (indice + 1) % coincidencias.length)
    } else if (evento.key === 'ArrowUp') {
      evento.preventDefault()
      setActivo((indice) => (indice - 1 + coincidencias.length) % coincidencias.length)
    } else if (evento.key === 'Enter' && coincidencias[activo]) {
      evento.preventDefault()
      elegir(coincidencias[activo])
    } else if (evento.key === 'Escape') {
      setAbierto(false)
    }
  }

  return (
    <div className="selector" ref={caja}>
      <input
        ref={campo}
        value={texto}
        placeholder={placeholder}
        onChange={escribir}
        onFocus={() => busqueda && setAbierto(true)}
        onBlur={() => {
          window.setTimeout(() => {
            setAbierto(false)
            setTexto(etiquetaElegida.current)
          }, 150)
        }}
        onKeyDown={teclas}
        autoComplete="off"
        required={requerido}
      />
      {abierto && busqueda && (
        <ul className="sugerencias" role="listbox">
          {coincidencias.length === 0 ? (
            <li className="sugerencia-vacia">Ninguno empieza por ese texto</li>
          ) : (
            <>
              {coincidencias.map((opcion, indice) => (
                <li key={opcion.valor}>
                  <button
                    type="button"
                    className={indice === activo ? 'activa' : undefined}
                    onMouseDown={(evento) => evento.preventDefault()}
                    onClick={() => elegir(opcion)}
                  >
                    {opcion.etiqueta}
                  </button>
                </li>
              ))}
              {encontradas.length > coincidencias.length && (
                <li className="sugerencia-vacia">Sigue escribiendo para ver el resto</li>
              )}
            </>
          )}
        </ul>
      )}
    </div>
  )
}

export function formatoFecha(valor) {
  if (!valor) return '—'
  const fecha = new Date(valor)
  if (Number.isNaN(fecha.getTime())) return String(valor)
  return fecha.toLocaleString('es-VE', {
    dateStyle: 'medium',
    timeStyle: 'short',
  })
}

export function Aviso({ tipo = 'error', children }) {
  if (!children) return null
  return (
    <p className={`aviso aviso-${tipo}`} role={tipo === 'error' ? 'alert' : 'status'}>
      {children}
    </p>
  )
}

export function Campo({ etiqueta, children }) {
  return (
    <label className="campo">
      <span>{etiqueta}</span>
      {children}
    </label>
  )
}

export function Vacio({ children }) {
  return <p className="vacio">{children}</p>
}

export function vacios(pares) {
  return pares
    .filter(([, valor]) => valor == null || String(valor).trim() === '')
    .map(([etiqueta]) => etiqueta)
}

export function AvisarVacios({ campos, onConfirmar, onCancelar }) {
  return (
    <div className="velo" onMouseDown={onCancelar}>
      <div className="dialogo" onMouseDown={(evento) => evento.stopPropagation()}>
        <h2>¿Seguro que vas a guardar esto sin la siguiente información?</h2>
        <ul className="lista">
          {campos.map((campo) => <li key={campo}>{campo}</li>)}
        </ul>
        <div className="acciones">
          <button className="boton" type="button" onClick={onConfirmar}>Guardar de todos modos</button>
          <button className="boton boton-secundario" type="button" onClick={onCancelar}>Devolver</button>
        </div>
      </div>
    </div>
  )
}

export function Confirmar({ titulo = '¿Seguro que vas a eliminar esto?', texto, accion = 'Eliminar', peligro = true, onConfirmar, onCancelar }) {
  return (
    <div className="velo" onMouseDown={onCancelar}>
      <div className="dialogo" onMouseDown={(evento) => evento.stopPropagation()}>
        <h2>{titulo}</h2>
        {texto && <p>{texto}</p>}
        <div className="acciones">
          <button className={peligro ? 'boton boton-peligro' : 'boton'} type="button" onClick={onConfirmar}>{accion}</button>
          <button className="boton boton-secundario" type="button" onClick={onCancelar}>Cancelar</button>
        </div>
      </div>
    </div>
  )
}

export function Pagina({ titulo, texto, acciones, children }) {
  return (
    <section className="pagina">
      <header className="pagina-cabecera">
        <div>
          <h1>{titulo}</h1>
          {texto && <p>{texto}</p>}
        </div>
        {acciones && <div className="acciones">{acciones}</div>}
      </header>
      {children}
    </section>
  )
}
