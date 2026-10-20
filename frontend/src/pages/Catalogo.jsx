import { useEffect, useMemo, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../auth'
import { esAdmin, puede } from '../permisos'
import { Aviso, Campo, Confirmar, Pagina, Vacio, algunoEmpieza, empiezaPor, formatoFecha } from '../ui'

const FILTRO_INICIAL = { titulo: '', autor: '', cota: '', tipo: '' }

function textoAutores(lista) {
  if (!lista?.length) return '—'
  return lista
    .map((autor) => autor.nombreCompleto || `${autor.nombre || ''} ${autor.apellido || ''}`.trim())
    .filter(Boolean)
    .join('; ')
}

export default function Catalogo() {
  const { usuario } = useAuth()
  const crear = puede(usuario, 'puedeCrearLibros')
  const admin = esAdmin(usuario)
  const [filtro, setFiltro] = useState(FILTRO_INICIAL)
  const [libros, setLibros] = useState([])
  const [autores, setAutores] = useState({})
  const [error, setError] = useState('')
  const [aviso, setAviso] = useState('')
  const [cargando, setCargando] = useState(true)
  const [subiendo, setSubiendo] = useState(false)
  const [historial, setHistorial] = useState(null)
  const [versionElegida, setVersionElegida] = useState(null)
  const [confirmarVersion, setConfirmarVersion] = useState(false)
  const archivo = useRef(null)

  useEffect(() => { cargar() }, [])

  function cargar() {
    setCargando(true)
    Promise.all([api('/api/libros'), api('/api/libro-autor/resumen')])
      .then(([lista, resumen]) => {
        const mapa = {}
        resumen.forEach((autor) => {
          if (!mapa[autor.idLibro]) mapa[autor.idLibro] = []
          mapa[autor.idLibro].push(autor)
        })
        setLibros(lista)
        setAutores(mapa)
      })
      .catch((err) => setError(err.message))
      .finally(() => setCargando(false))
  }

  async function subirExcel(evento) {
    const elegido = evento.target.files?.[0]
    evento.target.value = ''
    if (!elegido) return
    setError('')
    setAviso('')
    setSubiendo(true)
    try {
      const datos = new FormData()
      datos.append('archivo', elegido)
      const resultado = await api('/api/libros/importar', { method: 'POST', body: datos })
      setAviso(`Se cargaron ${resultado.cargadas} obras. Se ignoraron ${resultado.ignoradas} porque ya estaban en el catálogo.`)
      cargar()
    } catch (err) {
      setError(err.message)
    } finally {
      setSubiendo(false)
    }
  }

  async function abrirHistorial(libro) {
    setVersionElegida(null)
    setConfirmarVersion(false)
    setHistorial({ libro, eventos: [], cargando: true, error: '' })
    try {
      const eventos = await api(`/api/auditoria?entidad=LIBROS&idEntidad=${libro.idLibro}`)
      setHistorial({ libro, eventos, cargando: false, error: '' })
    } catch (err) {
      setHistorial({ libro, eventos: [], cargando: false, error: err.message })
    }
  }

  async function restaurarVersion() {
    setError('')
    setAviso('')
    try {
      await api(`/api/libros/${historial.libro.idLibro}/restaurar?idAuditoria=${versionElegida}`, { method: 'POST' })
      setAviso('La obra volvió a la versión seleccionada.')
      setHistorial(null)
      setVersionElegida(null)
      setConfirmarVersion(false)
      cargar()
    } catch (err) {
      setHistorial((actual) => ({ ...actual, error: err.message }))
      setConfirmarVersion(false)
    }
  }

  function cambiar(evento) {
    const { name, value } = evento.target
    setFiltro((actual) => ({ ...actual, [name]: value }))
  }

  const visibles = useMemo(() => libros.filter((libro) => {
    const delLibro = autores[libro.idLibro] || []
    return empiezaPor(libro.titulo, filtro.titulo)
      && empiezaPor(libro.tipo, filtro.tipo)
      && empiezaPor(libro.cota, filtro.cota)
      && algunoEmpieza(filtro.autor, delLibro.flatMap((autor) => [
        autor.nombreCompleto,
        autor.nombre,
        autor.apellido,
        `${autor.nombre || ''} ${autor.apellido || ''}`,
      ]))
  }), [libros, autores, filtro])

  return (
    <Pagina
      titulo="Catálogo"
      texto="Escribe y la lista se reduce a lo que empieza por ese texto: título, autor, cota o tipo."
      acciones={crear ? (
        <>
          <button className="boton boton-secundario" type="button" onClick={() => archivo.current?.click()} disabled={subiendo}>
            {subiendo ? 'Cargando Excel…' : 'Cargar Excel con datos'}
          </button>
          <input ref={archivo} type="file" accept=".xlsx" hidden onChange={subirExcel} />
          <Link className="boton" to="/libros/nuevo">Nueva obra</Link>
        </>
      ) : null}
    >
      <form className="filtros" onSubmit={(evento) => evento.preventDefault()}>
        <Campo etiqueta="Título">
          <input name="titulo" value={filtro.titulo} onChange={cambiar} placeholder="Empieza por…" />
        </Campo>
        <Campo etiqueta="Autor">
          <input name="autor" value={filtro.autor} onChange={cambiar} placeholder="Empieza por…" />
        </Campo>
        <Campo etiqueta="Cota">
          <input name="cota" value={filtro.cota} onChange={cambiar} placeholder="Empieza por…" />
        </Campo>
        <Campo etiqueta="Tipo">
          <input name="tipo" value={filtro.tipo} onChange={cambiar} placeholder="Empieza por…" />
        </Campo>
      </form>
      <Aviso>{error}</Aviso>
      <Aviso tipo="ok">{aviso}</Aviso>
      {cargando ? <p className="nota">Cargando catálogo…</p> : visibles.length === 0 ? (
        <Vacio>No hay obras que empiecen por ese texto.</Vacio>
      ) : (
        <div className="tabla-contenedor">
          <table>
            <thead>
              <tr>
                <th>Título</th>
                <th>Autores</th>
                <th>Tipo</th>
                <th>Año</th>
                <th>Cota</th>
                <th>Editorial</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {visibles.map((libro) => (
                <tr key={libro.idLibro}>
                  <td>
                    <Link to={`/libros/${libro.idLibro}`}>{libro.titulo}</Link>
                    {libro.requiereRevisionFecha && <span className="marca-estado pendiente">Fecha por revisar</span>}
                  </td>
                  <td>{textoAutores(autores[libro.idLibro])}</td>
                  <td>{libro.tipo || '—'}</td>
                  <td>{libro.fechaPublicacion || libro.fechaOriginal || '—'}</td>
                  <td>{libro.cota || '—'}</td>
                  <td>{libro.editorial?.nombre || '—'}</td>
                  <td className="acciones-tabla">
                    <button type="button" className="icono" title="Historial" aria-label="Historial de cambios" onClick={() => abrirHistorial(libro)}>
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
                        <circle cx="12" cy="12" r="8" />
                        <path d="M12 8v5l3 2" />
                      </svg>
                    </button>
                    <Link to={`/libros/${libro.idLibro}`}>Abrir</Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      {historial && (
        <div className="velo" onMouseDown={() => setHistorial(null)}>
          <div className="dialogo dialogo-ancho" onMouseDown={(evento) => evento.stopPropagation()}>
            <h2>Historial</h2>
            <p className="nota">{historial.libro.titulo}</p>
            {admin && historial.eventos?.length > 0 && (
              <p className="nota">Selecciona una fila para devolver la obra a ese momento.</p>
            )}
            <Aviso>{historial.error}</Aviso>
            {historial.cargando ? <p className="nota">Cargando cambios…</p> : historial.eventos.length === 0 ? (
              <Vacio>Esta obra todavía no tiene cambios registrados.</Vacio>
            ) : (
              <div className="tabla-contenedor">
                <table>
                  <thead>
                    <tr><th>Fecha</th><th>Quién</th><th>Dónde</th><th>Antes</th><th>Después</th></tr>
                  </thead>
                  <tbody>
                    {historial.eventos.map((evento) => (
                      <tr
                        key={evento.idAuditoria}
                        className={admin ? `elegible${versionElegida === evento.idAuditoria ? ' seleccionada' : ''}` : undefined}
                        onClick={admin ? () => setVersionElegida(evento.idAuditoria) : undefined}
                      >
                        <td>{formatoFecha(evento.fechaEvento)}</td>
                        <td>{evento.usuario?.usuario || '—'}</td>
                        <td>{evento.observaciones || etiquetaAccion(evento.accion)}</td>
                        <td>{evento.valorAnterior || '—'}</td>
                        <td>{evento.valorNuevo || '—'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
            <div className="acciones">
              {admin && historial.eventos?.length > 0 && (
                <button className="boton" type="button" disabled={!versionElegida} onClick={() => setConfirmarVersion(true)}>
                  Devolver a esta versión
                </button>
              )}
              <button className="boton boton-secundario" type="button" onClick={() => setHistorial(null)}>Cerrar</button>
            </div>
            {confirmarVersion && (
              <Confirmar
                titulo="¿Seguro que vas a devolver la obra a esta versión?"
                texto="Se recuperan el título, los datos bibliográficos y los autores de ese momento. Los ejemplares y los préstamos se quedan como están."
                accion="Devolver"
                peligro={false}
                onCancelar={() => setConfirmarVersion(false)}
                onConfirmar={restaurarVersion}
              />
            )}
          </div>
        </div>
      )}
    </Pagina>
  )
}

function etiquetaAccion(accion) {
  return {
    INSERT: 'Alta',
    UPDATE: 'Cambio',
    DELETE: 'Eliminación',
    RESOLVER_INCIDENCIA: 'Resolución',
    PRESTAMO: 'Préstamo',
    DEVOLUCION: 'Devolución',
  }[accion] || accion || '—'
}
