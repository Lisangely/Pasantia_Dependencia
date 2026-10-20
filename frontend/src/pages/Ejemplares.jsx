import { useEffect, useState } from 'react'
import { api } from '../api'
import { useAuth } from '../auth'
import { puede } from '../permisos'
import { Aviso, AvisarVacios, Busqueda, Campo, Pagina, Selector, Vacio, algunoEmpieza, vacios } from '../ui'

const VACIO = { idLibro: '', idUbicacion: '', numeroEjemplar: '', disponible: true, requiereRevision: false }

export default function Ejemplares() {
  const { usuario } = useAuth()
  const crear = puede(usuario, 'puedeCrearEjemplares')
  const editarPermiso = puede(usuario, 'puedeActualizar')
  const [ejemplares, setEjemplares] = useState([])
  const [libros, setLibros] = useState([])
  const [ubicaciones, setUbicaciones] = useState([])
  const [form, setForm] = useState(VACIO)
  const [editando, setEditando] = useState(null)
  const [consulta, setConsulta] = useState('')
  const [avisarVacios, setAvisarVacios] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([api('/api/libros'), api('/api/ubicaciones')])
      .then(([listaLibros, listaUbicaciones]) => {
        setLibros(listaLibros)
        setUbicaciones(listaUbicaciones)
      })
      .catch((err) => setError(err.message))
    cargar()
  }, [])

  async function cargar() {
    setError('')
    try {
      setEjemplares(await api('/api/ejemplares'))
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
      ['Ubicación', form.idUbicacion],
      ['Número de ejemplar', form.numeroEjemplar],
    ])
    if (!forzar && faltan.length) {
      setAvisarVacios(faltan)
      return
    }
    setAvisarVacios(null)
    setError('')
    const cuerpo = {
      idLibro: Number(form.idLibro),
      idUbicacion: form.idUbicacion ? Number(form.idUbicacion) : null,
      numeroEjemplar: form.numeroEjemplar === '' ? null : Number(form.numeroEjemplar),
      disponible: form.disponible,
      requiereRevision: form.requiereRevision,
    }
    try {
      if (editando) await api(`/api/ejemplares/${editando}`, { method: 'PUT', body: JSON.stringify(cuerpo) })
      else await api('/api/ejemplares', { method: 'POST', body: JSON.stringify(cuerpo) })
      setForm(VACIO)
      setEditando(null)
      await cargar()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <Pagina titulo="Ejemplares" texto="Cada copia física se presta por separado y tiene su propia ubicación.">
      <Aviso>{error}</Aviso>
      <Busqueda valor={consulta} onChange={setConsulta} placeholder="Título de la obra" />
      <div className="rejilla">
        {(crear || (editarPermiso && editando)) && <form className="panel" onSubmit={guardar}>
          <h2>{editando ? 'Editar ejemplar' : 'Nuevo ejemplar'}</h2>
          <Campo etiqueta="Obra">
            <Selector
              requerido
              placeholder="Escribe el título"
              valor={form.idLibro}
              onChange={(idLibro) => setForm((actual) => ({ ...actual, idLibro }))}
              opciones={libros.map((libro) => ({ valor: libro.idLibro, etiqueta: libro.titulo, claves: [libro.cota] }))}
            />
          </Campo>
          <Campo etiqueta="Ubicación">
            <Selector
              placeholder="Escribe la ubicación"
              valor={form.idUbicacion}
              onChange={(idUbicacion) => setForm((actual) => ({ ...actual, idUbicacion }))}
              opciones={ubicaciones.map((ubicacion) => ({ valor: ubicacion.idUbicacion, etiqueta: ubicacion.ubicacionOriginal }))}
            />
          </Campo>
          <Campo etiqueta="Número de ejemplar">
            <input name="numeroEjemplar" value={form.numeroEjemplar} onChange={cambiar} inputMode="numeric" />
          </Campo>
          <label className="chequeo">
            <input name="disponible" type="checkbox" checked={form.disponible} onChange={cambiar} />
            Disponible
          </label>
          <label className="chequeo">
            <input name="requiereRevision" type="checkbox" checked={form.requiereRevision} onChange={cambiar} />
            Requiere revisión
          </label>
          <div className="acciones">
            <button className="boton" type="submit">{editando ? 'Actualizar' : 'Guardar'}</button>
            {editando && <button className="boton boton-secundario" type="button" onClick={() => { setEditando(null); setForm(VACIO) }}>Cancelar</button>}
          </div>
        </form>}
        <div className="tabla-contenedor">
          {ejemplares.filter((ejemplar) => algunoEmpieza(consulta, [ejemplar.libro?.titulo, ejemplar.ubicacion?.ubicacionOriginal])).length === 0 ? <Vacio>No hay ejemplares que empiecen por ese texto.</Vacio> : (
            <table>
              <thead>
                <tr><th>Obra</th><th>Nº</th><th>Ubicación</th><th>Estado</th><th></th></tr>
              </thead>
              <tbody>
                {ejemplares.filter((ejemplar) => algunoEmpieza(consulta, [ejemplar.libro?.titulo, ejemplar.ubicacion?.ubicacionOriginal])).map((ejemplar) => (
                  <tr key={ejemplar.idEjemplar}>
                    <td>{ejemplar.libro?.titulo || '—'}</td>
                    <td>{ejemplar.numeroEjemplar ?? '—'}</td>
                    <td>{ejemplar.ubicacion?.ubicacionOriginal || '—'}</td>
                    <td><span className={`marca-estado ${ejemplar.disponible ? 'resuelto' : 'pendiente'}`}>{ejemplar.disponible ? 'Disponible' : 'No disponible'}</span></td>
                    <td>
                      {editarPermiso && (
                        <button type="button" onClick={() => {
                          setEditando(ejemplar.idEjemplar)
                          setForm({
                            idLibro: ejemplar.libro?.idLibro ?? '',
                            idUbicacion: ejemplar.ubicacion?.idUbicacion ?? '',
                            numeroEjemplar: ejemplar.numeroEjemplar ?? '',
                            disponible: Boolean(ejemplar.disponible),
                            requiereRevision: Boolean(ejemplar.requiereRevision),
                          })
                        }}>Editar</button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
      {avisarVacios && (
        <AvisarVacios campos={avisarVacios} onCancelar={() => setAvisarVacios(null)} onConfirmar={() => guardar(null, true)} />
      )}
    </Pagina>
  )
}
