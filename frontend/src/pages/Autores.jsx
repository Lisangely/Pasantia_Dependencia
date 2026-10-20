import { useEffect, useState } from 'react'
import { api } from '../api'
import { useAuth } from '../auth'
import { puede } from '../permisos'
import { Aviso, AvisarVacios, Busqueda, Campo, Confirmar, Pagina, Vacio, algunoEmpieza, vacios } from '../ui'

const VACIO = { nombre: '', apellido: '', nombreCompleto: '' }

export default function Autores() {
  const { usuario } = useAuth()
  const crear = puede(usuario, 'puedeCrearAutores')
  const editarPermiso = puede(usuario, 'puedeActualizar')
  const borrar = puede(usuario, 'puedeEliminar')
  const [autores, setAutores] = useState([])
  const [form, setForm] = useState(VACIO)
  const [editando, setEditando] = useState(null)
  const [consulta, setConsulta] = useState('')
  const [confirmar, setConfirmar] = useState(null)
  const [avisarVacios, setAvisarVacios] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => { cargar() }, [])

  async function cargar() {
    try {
      setAutores(await api('/api/autores'))
    } catch (err) {
      setError(err.message)
    }
  }

  function cambiar(evento) {
    const { name, value } = evento.target
    setForm((actual) => ({ ...actual, [name]: value }))
  }

  function editar(autor) {
    setEditando(autor.idAutor)
    setForm({
      nombre: autor.nombre || '',
      apellido: autor.apellido || '',
      nombreCompleto: autor.nombreCompleto || '',
    })
  }

  async function guardar(evento, forzar = false) {
    if (evento) evento.preventDefault()
    const faltan = vacios([
      ['Apellido', form.apellido],
      ['Nombre completo', form.nombreCompleto],
    ])
    if (!forzar && faltan.length) {
      setAvisarVacios(faltan)
      return
    }
    setAvisarVacios(null)
    setError('')
    try {
      if (editando) {
        await api(`/api/autores/${editando}`, { method: 'PUT', body: JSON.stringify(form) })
      } else {
        await api('/api/autores', { method: 'POST', body: JSON.stringify(form) })
      }
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
      await api(`/api/autores/${id}`, { method: 'DELETE' })
      await cargar()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <Pagina titulo="Autores" texto="Nombre y apellido se conservan por separado. El nombre completo se arma si lo dejas vacío.">
      <Aviso>{error}</Aviso>
      <Busqueda valor={consulta} onChange={setConsulta} placeholder="Nombre o apellido" />
      <div className="rejilla">
        {(crear || (editarPermiso && editando)) && <form className="panel" onSubmit={guardar}>
          <h2>{editando ? 'Editar autor' : 'Nuevo autor'}</h2>
          <Campo etiqueta="Nombre">
            <input name="nombre" value={form.nombre} onChange={cambiar} required />
          </Campo>
          <Campo etiqueta="Apellido">
            <input name="apellido" value={form.apellido} onChange={cambiar} />
          </Campo>
          <Campo etiqueta="Nombre completo">
            <input name="nombreCompleto" value={form.nombreCompleto} onChange={cambiar} />
          </Campo>
          <div className="acciones">
            <button className="boton" type="submit">{editando ? 'Actualizar' : 'Guardar'}</button>
            {editando && <button className="boton boton-secundario" type="button" onClick={() => { setEditando(null); setForm(VACIO) }}>Cancelar</button>}
          </div>
        </form>}
        <div className="tabla-contenedor">
          {autores.filter((autor) => algunoEmpieza(consulta, [autor.nombre, autor.apellido, autor.nombreCompleto, `${autor.nombre} ${autor.apellido || ''}`])).length === 0 ? <Vacio>No hay autores que empiecen por ese texto.</Vacio> : (
            <table>
              <thead>
                <tr><th>Nombre</th><th>Apellido</th><th>Nombre completo</th><th></th></tr>
              </thead>
              <tbody>
                {autores.filter((autor) => algunoEmpieza(consulta, [autor.nombre, autor.apellido, autor.nombreCompleto, `${autor.nombre} ${autor.apellido || ''}`])).map((autor) => (
                  <tr key={autor.idAutor}>
                    <td>{autor.nombre}</td>
                    <td>{autor.apellido || '—'}</td>
                    <td>{autor.nombreCompleto || '—'}</td>
                    <td className="acciones-tabla">
                      {editarPermiso && <button type="button" onClick={() => editar(autor)}>Editar</button>}
                      {borrar && <button type="button" onClick={() => setConfirmar(autor.idAutor)}>Eliminar</button>}
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
      {confirmar && (
        <Confirmar texto="Se quitará este autor del registro." onCancelar={() => setConfirmar(null)} onConfirmar={() => eliminar(confirmar)} />
      )}
    </Pagina>
  )
}
