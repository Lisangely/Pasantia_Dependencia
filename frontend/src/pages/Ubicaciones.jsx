import { useEffect, useState } from 'react'
import { api } from '../api'
import { useAuth } from '../auth'
import { puede } from '../permisos'
import { Aviso, AvisarVacios, Busqueda, Campo, Confirmar, Pagina, Vacio, algunoEmpieza, vacios } from '../ui'

const VACIO = { ubicacionOriginal: '', estanteria: '', posicion: '', requiereRevision: false }

export default function Ubicaciones() {
  const { usuario } = useAuth()
  const crear = puede(usuario, 'puedeCrearUbicaciones')
  const editarPermiso = puede(usuario, 'puedeActualizar')
  const borrar = puede(usuario, 'puedeEliminar')
  const [ubicaciones, setUbicaciones] = useState([])
  const [form, setForm] = useState(VACIO)
  const [editando, setEditando] = useState(null)
  const [consulta, setConsulta] = useState('')
  const [confirmar, setConfirmar] = useState(null)
  const [avisarVacios, setAvisarVacios] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => { cargar() }, [])

  async function cargar() {
    try {
      setUbicaciones(await api('/api/ubicaciones'))
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
      ['Estantería', form.estanteria],
      ['Posición', form.posicion],
    ])
    if (!forzar && faltan.length) {
      setAvisarVacios(faltan)
      return
    }
    setAvisarVacios(null)
    setError('')
    try {
      const cuerpo = JSON.stringify(form)
      if (editando) await api(`/api/ubicaciones/${editando}`, { method: 'PUT', body: cuerpo })
      else await api('/api/ubicaciones', { method: 'POST', body: cuerpo })
      setForm(VACIO)
      setEditando(null)
      await cargar()
    } catch (err) {
      setError(err.message)
    }
  }

  async function eliminar(id) {
    setConfirmar(null)
    setError('')
    try {
      await api(`/api/ubicaciones/${id}`, { method: 'DELETE' })
      await cargar()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <Pagina titulo="Ubicaciones" texto="El valor original de la fila se conserva aunque la estantería no se pueda interpretar.">
      <Aviso>{error}</Aviso>
      <Busqueda valor={consulta} onChange={setConsulta} placeholder="Ubicación o estantería" />
      <div className="rejilla">
        {(crear || (editarPermiso && editando)) && <form className="panel" onSubmit={guardar}>
          <h2>{editando ? 'Editar ubicación' : 'Nueva ubicación'}</h2>
          <Campo etiqueta="Ubicación original">
            <input name="ubicacionOriginal" value={form.ubicacionOriginal} onChange={cambiar} required />
          </Campo>
          <div className="fila">
            <Campo etiqueta="Estantería">
              <input name="estanteria" value={form.estanteria} onChange={cambiar} />
            </Campo>
            <Campo etiqueta="Posición">
              <input name="posicion" value={form.posicion} onChange={cambiar} />
            </Campo>
          </div>
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
          {ubicaciones.filter((ubicacion) => algunoEmpieza(consulta, [ubicacion.ubicacionOriginal, ubicacion.estanteria, ubicacion.posicion])).length === 0 ? <Vacio>No hay ubicaciones que empiecen por ese texto.</Vacio> : (
            <table>
              <thead>
                <tr><th>Original</th><th>Estantería</th><th>Posición</th><th>Revisión</th><th></th></tr>
              </thead>
              <tbody>
                {ubicaciones.filter((ubicacion) => algunoEmpieza(consulta, [ubicacion.ubicacionOriginal, ubicacion.estanteria, ubicacion.posicion])).map((ubicacion) => (
                  <tr key={ubicacion.idUbicacion}>
                    <td>{ubicacion.ubicacionOriginal}</td>
                    <td>{ubicacion.estanteria || '—'}</td>
                    <td>{ubicacion.posicion || '—'}</td>
                    <td>{ubicacion.requiereRevision ? 'Sí' : 'No'}</td>
                    <td className="acciones-tabla">
                      {editarPermiso && <button type="button" onClick={() => { setEditando(ubicacion.idUbicacion); setForm({ ubicacionOriginal: ubicacion.ubicacionOriginal || '', estanteria: ubicacion.estanteria || '', posicion: ubicacion.posicion || '', requiereRevision: Boolean(ubicacion.requiereRevision) }) }}>Editar</button>}
                      {borrar && <button type="button" onClick={() => setConfirmar(ubicacion.idUbicacion)}>Eliminar</button>}
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
      {confirmar && <Confirmar texto="Se quitará esta ubicación del registro." onCancelar={() => setConfirmar(null)} onConfirmar={() => eliminar(confirmar)} />}
    </Pagina>
  )
}
