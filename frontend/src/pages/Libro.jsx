import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../auth'
import { puede } from '../permisos'
import { Aviso, AvisarVacios, Campo, Confirmar, Pagina, Selector, Vacio, vacios } from '../ui'

const VACIO = {
  titulo: '',
  tipo: '',
  fechaPublicacion: '',
  fechaOriginal: '',
  requiereRevisionFecha: false,
  cota: '',
  observaciones: '',
  idEditorial: '',
}

function aCuerpo(form) {
  return {
    titulo: form.titulo.trim(),
    tipo: form.tipo.trim() || null,
    fechaPublicacion: form.fechaPublicacion === '' ? null : Number(form.fechaPublicacion),
    fechaOriginal: form.fechaOriginal.trim() || null,
    requiereRevisionFecha: form.requiereRevisionFecha,
    cota: form.cota.trim() || null,
    observaciones: form.observaciones.trim() || null,
    idEditorial: form.idEditorial ? Number(form.idEditorial) : null,
  }
}

export default function Libro() {
  const { usuario } = useAuth()
  const crearAutores = puede(usuario, 'puedeCrearAutores')
  const crearEjemplares = puede(usuario, 'puedeCrearEjemplares')
  const crearIncidencias = puede(usuario, 'puedeCrearIncidencias')
  const editarPermiso = puede(usuario, 'puedeActualizar')
  const borrar = puede(usuario, 'puedeEliminar')
  const { id } = useParams()
  const navegar = useNavigate()
  const esNuevo = !id
  const [form, setForm] = useState(VACIO)
  const [editoriales, setEditoriales] = useState([])
  const [autores, setAutores] = useState([])
  const [vinculos, setVinculos] = useState([])
  const [ejemplares, setEjemplares] = useState([])
  const [ubicaciones, setUbicaciones] = useState([])
  const [idAutor, setIdAutor] = useState('')
  const [copia, setCopia] = useState({ numeroEjemplar: '', idUbicacion: '', requiereRevision: false })
  const [incidencia, setIncidencia] = useState({ campo: '', valorOriginal: '', descripcion: '' })
  const [error, setError] = useState('')
  const [aviso, setAviso] = useState('')
  const [guardando, setGuardando] = useState(false)
  const [confirmar, setConfirmar] = useState(false)
  const [avisarVacios, setAvisarVacios] = useState(null)
  const [avisarExtra, setAvisarExtra] = useState(null)
  const puedeGuardar = esNuevo ? puede(usuario, 'puedeCrearLibros') : editarPermiso

  useEffect(() => {
    cargar()
  }, [id])

  async function cargar() {
    setError('')
    try {
      const [listaEditoriales, listaAutores, listaUbicaciones] = await Promise.all([
        api('/api/editoriales'),
        api('/api/autores'),
        api('/api/ubicaciones'),
      ])
      setEditoriales(listaEditoriales)
      setAutores(listaAutores)
      setUbicaciones(listaUbicaciones)
      if (!esNuevo) {
        const [libro, relaciones, copias] = await Promise.all([
          api(`/api/libros/${id}`),
          api(`/api/libro-autor?idLibro=${id}`),
          api(`/api/ejemplares?idLibro=${id}`),
        ])
        setForm({
          titulo: libro.titulo || '',
          tipo: libro.tipo || '',
          fechaPublicacion: libro.fechaPublicacion ?? '',
          fechaOriginal: libro.fechaOriginal || '',
          requiereRevisionFecha: Boolean(libro.requiereRevisionFecha),
          cota: libro.cota || '',
          observaciones: libro.observaciones || '',
          idEditorial: libro.editorial?.idEditorial ?? '',
        })
        setVinculos(relaciones)
        setEjemplares(copias)
      }
    } catch (err) {
      setError(err.message)
    }
  }

  function cambiar(evento) {
    const { name, value, type, checked } = evento.target
    setForm((actual) => ({ ...actual, [name]: type === 'checkbox' ? checked : value }))
  }

  async function guardar(evento, forzar = false) {
    if (evento) evento.preventDefault()
    const faltan = vacios([
      ['Tipo', form.tipo],
      ['Año validado', form.fechaPublicacion],
      ['Fecha original del inventario', form.fechaOriginal],
      ['Cota', form.cota],
      ['Editorial', form.idEditorial],
      ['Observaciones', form.observaciones],
    ])
    if (!forzar && faltan.length) {
      setAvisarVacios(faltan)
      return
    }
    setAvisarVacios(null)
    setGuardando(true)
    setError('')
    setAviso('')
    try {
      if (esNuevo) {
        const creado = await api('/api/libros', { method: 'POST', body: JSON.stringify(aCuerpo(form)) })
        navegar(`/libros/${creado.idLibro}`)
      } else {
        await api(`/api/libros/${id}`, { method: 'PUT', body: JSON.stringify(aCuerpo(form)) })
        setAviso('Obra actualizada.')
      }
    } catch (err) {
      setError(err.message)
    } finally {
      setGuardando(false)
    }
  }

  async function eliminar() {
    setConfirmar(false)
    try {
      await api(`/api/libros/${id}`, { method: 'DELETE' })
      navegar('/catalogo')
    } catch (err) {
      setError(err.message)
    }
  }

  async function asociarAutor(evento) {
    evento.preventDefault()
    setError('')
    try {
      await api('/api/libro-autor', {
        method: 'POST',
        body: JSON.stringify({ idLibro: Number(id), idAutor: Number(idAutor) }),
      })
      setIdAutor('')
      setVinculos(await api(`/api/libro-autor?idLibro=${id}`))
    } catch (err) {
      setError(err.message)
    }
  }

  async function quitarAutor(idAutorVinculo) {
    setError('')
    try {
      await api(`/api/libro-autor?idLibro=${id}&idAutor=${idAutorVinculo}`, { method: 'DELETE' })
      setVinculos(await api(`/api/libro-autor?idLibro=${id}`))
    } catch (err) {
      setError(err.message)
    }
  }

  async function agregarCopia(evento, forzar = false) {
    if (evento) evento.preventDefault()
    const faltan = vacios([
      ['Número', copia.numeroEjemplar],
      ['Ubicación', copia.idUbicacion],
    ])
    if (!forzar && faltan.length) {
      setAvisarExtra({ campos: faltan, accion: 'copia' })
      return
    }
    setAvisarExtra(null)
    setError('')
    try {
      await api('/api/ejemplares', {
        method: 'POST',
        body: JSON.stringify({
          idLibro: Number(id),
          idUbicacion: copia.idUbicacion ? Number(copia.idUbicacion) : null,
          numeroEjemplar: copia.numeroEjemplar === '' ? null : Number(copia.numeroEjemplar),
          disponible: true,
          requiereRevision: copia.requiereRevision,
        }),
      })
      setCopia({ numeroEjemplar: '', idUbicacion: '', requiereRevision: false })
      setEjemplares(await api(`/api/ejemplares?idLibro=${id}`))
    } catch (err) {
      setError(err.message)
    }
  }

  async function registrarIncidencia(evento, forzar = false) {
    if (evento) evento.preventDefault()
    const faltan = vacios([['Valor original', incidencia.valorOriginal]])
    if (!forzar && faltan.length) {
      setAvisarExtra({ campos: faltan, accion: 'incidencia' })
      return
    }
    setAvisarExtra(null)
    setError('')
    try {
      await api('/api/incidencias', {
        method: 'POST',
        body: JSON.stringify({
          idLibro: Number(id),
          campo: incidencia.campo,
          valorOriginal: incidencia.valorOriginal,
          descripcion: incidencia.descripcion,
        }),
      })
      setIncidencia({ campo: '', valorOriginal: '', descripcion: '' })
      setAviso('Incidencia registrada.')
    } catch (err) {
      setError(err.message)
    }
  }

  const idsVinculados = new Set(vinculos.map((vinculo) => vinculo.autor.idAutor))

  return (
    <Pagina
      titulo={esNuevo ? 'Nueva obra' : 'Obra'}
      texto="Datos bibliográficos, autores, ejemplares e incidencias de la obra."
      acciones={<Link className="boton boton-secundario" to="/catalogo">Volver al catálogo</Link>}
    >
      <Aviso>{error}</Aviso>
      <Aviso tipo="ok">{aviso}</Aviso>
      <div className="rejilla">
        <form className="panel" onSubmit={guardar}>
          <h2>Ficha</h2>
          <fieldset disabled={!puedeGuardar} className="panel-campos">
          <Campo etiqueta="Título">
            <input name="titulo" value={form.titulo} onChange={cambiar} required />
          </Campo>
          <div className="fila">
            <Campo etiqueta="Tipo">
              <input name="tipo" value={form.tipo} onChange={cambiar} />
            </Campo>
            <Campo etiqueta="Año validado">
              <input name="fechaPublicacion" value={form.fechaPublicacion} onChange={cambiar} inputMode="numeric" />
            </Campo>
          </div>
          <Campo etiqueta="Fecha original del inventario">
            <input name="fechaOriginal" value={form.fechaOriginal} onChange={cambiar} />
          </Campo>
          <label className="chequeo">
            <input name="requiereRevisionFecha" type="checkbox" checked={form.requiereRevisionFecha} onChange={cambiar} />
            La fecha requiere revisión
          </label>
          <div className="fila">
            <Campo etiqueta="Cota">
              <input name="cota" value={form.cota} onChange={cambiar} />
            </Campo>
            <Campo etiqueta="Editorial">
              <Selector
                placeholder="Escribe la editorial"
                valor={form.idEditorial}
                onChange={(idEditorial) => setForm((actual) => ({ ...actual, idEditorial }))}
                opciones={editoriales.map((editorial) => ({ valor: editorial.idEditorial, etiqueta: editorial.nombre }))}
              />
            </Campo>
          </div>
          <Campo etiqueta="Observaciones">
            <textarea name="observaciones" value={form.observaciones} onChange={cambiar} rows="4" />
          </Campo>
          </fieldset>
          <div className="acciones">
            {puedeGuardar && (
              <button className="boton" type="submit" disabled={guardando}>
                {guardando ? 'Guardando…' : 'Guardar'}
              </button>
            )}
            {!esNuevo && borrar && (
              <button className="boton boton-peligro" type="button" onClick={() => setConfirmar(true)}>Eliminar</button>
            )}
          </div>
        </form>

        {!esNuevo && (
          <div className="columna">
            <section className="panel">
              <h2>Autores</h2>
              {vinculos.length === 0 ? <Vacio>Esta obra todavía no tiene autores.</Vacio> : (
                <ul className="lista">
                  {vinculos.map((vinculo) => (
                    <li key={vinculo.autor.idAutor}>
                      <span>{vinculo.autor.nombreCompleto || `${vinculo.autor.nombre} ${vinculo.autor.apellido || ''}`}</span>
                      {borrar && <button type="button" onClick={() => quitarAutor(vinculo.autor.idAutor)}>Quitar</button>}
                    </li>
                  ))}
                </ul>
              )}
              {crearAutores && <form className="fila" onSubmit={asociarAutor}>
                <Selector
                  requerido
                  placeholder="Escribe el autor"
                  valor={idAutor}
                  onChange={setIdAutor}
                  opciones={autores.filter((autor) => !idsVinculados.has(autor.idAutor)).map((autor) => ({
                    valor: autor.idAutor,
                    etiqueta: autor.nombreCompleto || `${autor.nombre} ${autor.apellido || ''}`.trim(),
                    claves: [autor.nombre, autor.apellido, autor.nombreCompleto],
                  }))}
                />
                <button className="boton" type="submit">Asociar</button>
              </form>}
            </section>

            <section className="panel">
              <h2>Ejemplares</h2>
              {ejemplares.length === 0 ? <Vacio>No hay copias registradas.</Vacio> : (
                <ul className="lista">
                  {ejemplares.map((ejemplar) => (
                    <li key={ejemplar.idEjemplar}>
                      <span>
                        Nº {ejemplar.numeroEjemplar ?? '—'} · {ejemplar.ubicacion?.ubicacionOriginal || 'Sin ubicación'} · {ejemplar.disponible ? 'Disponible' : 'Prestado'}
                      </span>
                    </li>
                  ))}
                </ul>
              )}
              {crearEjemplares && <form onSubmit={agregarCopia}>
                <div className="fila">
                  <Campo etiqueta="Número">
                    <input value={copia.numeroEjemplar} onChange={(evento) => setCopia({ ...copia, numeroEjemplar: evento.target.value })} inputMode="numeric" />
                  </Campo>
                  <Campo etiqueta="Ubicación">
                    <Selector
                      placeholder="Escribe la ubicación"
                      valor={copia.idUbicacion}
                      onChange={(idUbicacion) => setCopia({ ...copia, idUbicacion })}
                      opciones={ubicaciones.map((ubicacion) => ({ valor: ubicacion.idUbicacion, etiqueta: ubicacion.ubicacionOriginal }))}
                    />
                  </Campo>
                </div>
                <label className="chequeo">
                  <input type="checkbox" checked={copia.requiereRevision} onChange={(evento) => setCopia({ ...copia, requiereRevision: evento.target.checked })} />
                  Requiere revisión
                </label>
                <button className="boton" type="submit">Agregar ejemplar</button>
              </form>}
            </section>

            {crearIncidencias && <section className="panel">
              <h2>Incidencia de esta obra</h2>
              <form onSubmit={registrarIncidencia}>
                <Campo etiqueta="Campo afectado">
                  <input value={incidencia.campo} onChange={(evento) => setIncidencia({ ...incidencia, campo: evento.target.value })} required />
                </Campo>
                <Campo etiqueta="Valor original">
                  <input value={incidencia.valorOriginal} onChange={(evento) => setIncidencia({ ...incidencia, valorOriginal: evento.target.value })} />
                </Campo>
                <Campo etiqueta="Descripción">
                  <textarea value={incidencia.descripcion} onChange={(evento) => setIncidencia({ ...incidencia, descripcion: evento.target.value })} rows="3" required />
                </Campo>
                <button className="boton" type="submit">Registrar incidencia</button>
              </form>
            </section>}
          </div>
        )}
      </div>
      {avisarVacios && (
        <AvisarVacios campos={avisarVacios} onCancelar={() => setAvisarVacios(null)} onConfirmar={() => guardar(null, true)} />
      )}
      {avisarExtra && (
        <AvisarVacios
          campos={avisarExtra.campos}
          onCancelar={() => setAvisarExtra(null)}
          onConfirmar={() => (avisarExtra.accion === 'copia' ? agregarCopia(null, true) : registrarIncidencia(null, true))}
        />
      )}
      {confirmar && (
        <Confirmar
          texto="No se borrará si tiene ejemplares, préstamos o incidencias."
          onCancelar={() => setConfirmar(false)}
          onConfirmar={eliminar}
        />
      )}
    </Pagina>
  )
}
