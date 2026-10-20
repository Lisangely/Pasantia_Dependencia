import { useEffect, useState } from 'react'
import { api } from '../api'
import { useAuth } from '../auth'
import { puede } from '../permisos'
import { Aviso, Busqueda, Campo, Confirmar, Pagina, Vacio, algunoEmpieza } from '../ui'

const VACIO = { nombre: '', apellido: '', cedula: '', correo: '', telefono: '', semestre: '', carrera: '' }

export default function Lectores() {
  const { usuario } = useAuth()
  const crear = puede(usuario, 'puedeCrearLectores')
  const editarPermiso = puede(usuario, 'puedeActualizar')
  const borrar = puede(usuario, 'puedeEliminar')
  const [lectores, setLectores] = useState([])
  const [form, setForm] = useState(VACIO)
  const [editando, setEditando] = useState(null)
  const [consulta, setConsulta] = useState('')
  const [confirmar, setConfirmar] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => { cargar() }, [])

  async function cargar() {
    try {
      setLectores(await api('/api/lectores'))
    } catch (err) {
      setError(err.message)
    }
  }

  function cambiar(evento) {
    const { name, value } = evento.target
    setForm((actual) => ({ ...actual, [name]: value }))
  }

  async function guardar(evento) {
    evento.preventDefault()
    setError('')
    try {
      if (editando) await api(`/api/lectores/${editando}`, { method: 'PUT', body: JSON.stringify(form) })
      else await api('/api/lectores', { method: 'POST', body: JSON.stringify(form) })
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
      await api(`/api/lectores/${id}`, { method: 'DELETE' })
      await cargar()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <Pagina titulo="Lectores" texto="Los lectores no ingresan al sistema. Se registran solo para préstamos y devoluciones.">
      <Aviso>{error}</Aviso>
      <Busqueda valor={consulta} onChange={setConsulta} placeholder="Nombre, apellido o cédula" />
      <div className="rejilla">
        {(crear || (editarPermiso && editando)) && <form className="panel" onSubmit={guardar}>
          <h2>{editando ? 'Editar lector' : 'Nuevo lector'}</h2>
          <div className="fila">
            <Campo etiqueta="Nombre"><input name="nombre" value={form.nombre} onChange={cambiar} required /></Campo>
            <Campo etiqueta="Apellido"><input name="apellido" value={form.apellido} onChange={cambiar} required /></Campo>
          </div>
          <Campo etiqueta="Cédula"><input name="cedula" value={form.cedula} onChange={cambiar} required /></Campo>
          <Campo etiqueta="Correo"><input name="correo" type="email" value={form.correo} onChange={cambiar} required /></Campo>
          <Campo etiqueta="Teléfono"><input name="telefono" value={form.telefono} onChange={cambiar} required /></Campo>
          <div className="fila">
            <Campo etiqueta="Semestre (opcional)"><input name="semestre" value={form.semestre} onChange={cambiar} /></Campo>
            <Campo etiqueta="Carrera (opcional)"><input name="carrera" value={form.carrera} onChange={cambiar} /></Campo>
          </div>
          <div className="acciones">
            <button className="boton" type="submit">{editando ? 'Actualizar' : 'Guardar'}</button>
            {editando && <button className="boton boton-secundario" type="button" onClick={() => { setEditando(null); setForm(VACIO) }}>Cancelar</button>}
          </div>
        </form>}
        <div className="tabla-contenedor">
          {lectores.filter((lector) => algunoEmpieza(consulta, [lector.nombre, lector.apellido, `${lector.nombre} ${lector.apellido}`, lector.cedula])).length === 0 ? <Vacio>No hay lectores que empiecen por ese texto.</Vacio> : (
            <table>
              <thead><tr><th>Nombre</th><th>Cédula</th><th>Correo</th><th>Teléfono</th><th>Semestre</th><th>Carrera</th><th></th></tr></thead>
              <tbody>
                {lectores.filter((lector) => algunoEmpieza(consulta, [lector.nombre, lector.apellido, `${lector.nombre} ${lector.apellido}`, lector.cedula])).map((lector) => (
                  <tr key={lector.idLector}>
                    <td>{lector.nombre} {lector.apellido}</td>
                    <td>{lector.cedula || '—'}</td>
                    <td>{lector.correo || '—'}</td>
                    <td>{lector.telefono || '—'}</td>
                    <td>{lector.semestre || '—'}</td>
                    <td>{lector.carrera || '—'}</td>
                    <td className="acciones-tabla">
                      {editarPermiso && <button type="button" onClick={() => { setEditando(lector.idLector); setForm({ nombre: lector.nombre || '', apellido: lector.apellido || '', cedula: lector.cedula || '', correo: lector.correo || '', telefono: lector.telefono || '', semestre: lector.semestre || '', carrera: lector.carrera || '' }) }}>Editar</button>}
                      {borrar && <button type="button" onClick={() => setConfirmar(lector.idLector)}>Eliminar</button>}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
      {confirmar && <Confirmar texto="No se borrará si tiene préstamos." onCancelar={() => setConfirmar(null)} onConfirmar={() => eliminar(confirmar)} />}
    </Pagina>
  )
}
