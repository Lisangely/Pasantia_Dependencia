import { useEffect, useState } from 'react'
import { api } from '../api'
import { useAuth } from '../auth'
import { puede } from '../permisos'
import { Aviso, Busqueda, Campo, Confirmar, Pagina, Vacio, empiezaPor } from '../ui'

export default function Editoriales() {
  const { usuario } = useAuth()
  const crear = puede(usuario, 'puedeCrearEditoriales')
  const editarPermiso = puede(usuario, 'puedeActualizar')
  const borrar = puede(usuario, 'puedeEliminar')
  const [editoriales, setEditoriales] = useState([])
  const [nombre, setNombre] = useState('')
  const [editando, setEditando] = useState(null)
  const [consulta, setConsulta] = useState('')
  const [confirmar, setConfirmar] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => { cargar() }, [])

  async function cargar() {
    try {
      setEditoriales(await api('/api/editoriales'))
    } catch (err) {
      setError(err.message)
    }
  }

  async function guardar(evento) {
    evento.preventDefault()
    setError('')
    try {
      const cuerpo = JSON.stringify({ nombre })
      if (editando) await api(`/api/editoriales/${editando}`, { method: 'PUT', body: cuerpo })
      else await api('/api/editoriales', { method: 'POST', body: cuerpo })
      setNombre('')
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
      await api(`/api/editoriales/${id}`, { method: 'DELETE' })
      await cargar()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <Pagina titulo="Editoriales" texto="Catálogo de casas editoras asociadas a las obras.">
      <Aviso>{error}</Aviso>
      <Busqueda valor={consulta} onChange={setConsulta} placeholder="Nombre de la editorial" />
      <div className="rejilla">
        {(crear || (editarPermiso && editando)) && <form className="panel" onSubmit={guardar}>
          <h2>{editando ? 'Editar editorial' : 'Nueva editorial'}</h2>
          <Campo etiqueta="Nombre">
            <input value={nombre} onChange={(evento) => setNombre(evento.target.value)} required />
          </Campo>
          <div className="acciones">
            <button className="boton" type="submit">{editando ? 'Actualizar' : 'Guardar'}</button>
            {editando && <button className="boton boton-secundario" type="button" onClick={() => { setEditando(null); setNombre('') }}>Cancelar</button>}
          </div>
        </form>}
        <div className="tabla-contenedor">
          {editoriales.filter((editorial) => empiezaPor(editorial.nombre, consulta)).length === 0 ? <Vacio>No hay editoriales que empiecen por ese texto.</Vacio> : (
            <table>
              <thead><tr><th>Nombre</th><th></th></tr></thead>
              <tbody>
                {editoriales.filter((editorial) => empiezaPor(editorial.nombre, consulta)).map((editorial) => (
                  <tr key={editorial.idEditorial}>
                    <td>{editorial.nombre}</td>
                    <td className="acciones-tabla">
                      {editarPermiso && <button type="button" onClick={() => { setEditando(editorial.idEditorial); setNombre(editorial.nombre) }}>Editar</button>}
                      {borrar && <button type="button" onClick={() => setConfirmar(editorial.idEditorial)}>Eliminar</button>}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
      {confirmar && <Confirmar texto="Se quitará esta editorial del registro." onCancelar={() => setConfirmar(null)} onConfirmar={() => eliminar(confirmar)} />}
    </Pagina>
  )
}
