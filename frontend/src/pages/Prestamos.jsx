import { useEffect, useState } from 'react'
import { api } from '../api'
import { useAuth } from '../auth'
import { puede } from '../permisos'
import { Aviso, Busqueda, Campo, Pagina, Selector, Vacio, algunoEmpieza, formatoFecha } from '../ui'

const LECTOR_VACIO = { nombre: '', apellido: '', cedula: '', correo: '', telefono: '', semestre: '', carrera: '' }

export default function Prestamos() {
  const { usuario } = useAuth()
  const crear = puede(usuario, 'puedeCrearPrestamos')
  const crearLector = puede(usuario, 'puedeCrearLectores')
  const editarPermiso = puede(usuario, 'puedeActualizar')
  const [prestamos, setPrestamos] = useState([])
  const [ejemplares, setEjemplares] = useState([])
  const [lectores, setLectores] = useState([])
  const [form, setForm] = useState({ idEjemplar: '', idLector: '', fechaDevolucionEsperada: '' })
  const [consulta, setConsulta] = useState('')
  const [popup, setPopup] = useState(false)
  const [nuevoLector, setNuevoLector] = useState(LECTOR_VACIO)
  const [error, setError] = useState('')
  const [errorLector, setErrorLector] = useState('')
  const [aviso, setAviso] = useState('')

  useEffect(() => { cargar() }, [])

  async function cargar() {
    setError('')
    try {
      const [listaPrestamos, listaEjemplares, listaLectores] = await Promise.all([
        api('/api/prestamos'),
        api('/api/ejemplares'),
        api('/api/lectores'),
      ])
      setPrestamos(listaPrestamos)
      setEjemplares(listaEjemplares)
      setLectores(listaLectores)
    } catch (err) {
      setError(err.message)
    }
  }

  async function crearPrestamo(evento) {
    evento.preventDefault()
    setError('')
    setAviso('')
    try {
      await api('/api/prestamos', {
        method: 'POST',
        body: JSON.stringify({
          idEjemplar: Number(form.idEjemplar),
          idLector: Number(form.idLector),
          fechaDevolucionEsperada: form.fechaDevolucionEsperada || null,
        }),
      })
      setForm({ idEjemplar: '', idLector: '', fechaDevolucionEsperada: '' })
      setAviso('Préstamo registrado.')
      await cargar()
    } catch (err) {
      setError(err.message)
    }
  }

  function abrirLector() {
    setNuevoLector(LECTOR_VACIO)
    setErrorLector('')
    setPopup(true)
  }

  function cambiarLector(evento) {
    const { name, value } = evento.target
    setNuevoLector((actual) => ({ ...actual, [name]: value }))
  }

  async function guardarLector(evento) {
    evento.preventDefault()
    setErrorLector('')
    try {
      const creado = await api('/api/lectores', {
        method: 'POST',
        body: JSON.stringify(nuevoLector),
      })
      setLectores((actual) => [...actual, creado])
      setForm((actual) => ({ ...actual, idLector: String(creado.idLector) }))
      setPopup(false)
      setAviso('Lector registrado y elegido para el préstamo.')
    } catch (err) {
      setErrorLector(err.message)
    }
  }

  async function devolver(id) {
    setError('')
    setAviso('')
    try {
      await api(`/api/prestamos/${id}/devolver`, { method: 'PUT' })
      setAviso('Devolución registrada.')
      await cargar()
    } catch (err) {
      setError(err.message)
    }
  }

  const disponibles = ejemplares.filter((ejemplar) => ejemplar.disponible)
  const ordenados = [...prestamos]
    .sort((a, b) => String(b.fechaPrestamo).localeCompare(String(a.fechaPrestamo)))
    .filter((prestamo) => algunoEmpieza(consulta, [
      prestamo.ejemplar?.libro?.titulo,
      prestamo.lector ? `${prestamo.lector.nombre} ${prestamo.lector.apellido}` : '',
      prestamo.lector?.nombre,
      prestamo.lector?.apellido,
      prestamo.estado,
    ]))

  return (
    <Pagina titulo="Préstamos" texto="El préstamo queda a nombre de quien inició sesión. Un ejemplar no puede tener dos préstamos activos.">
      <Aviso>{error}</Aviso>
      <Aviso tipo="ok">{aviso}</Aviso>
      {crear && <form className="panel" onSubmit={crearPrestamo}>
        <h2>Registrar préstamo</h2>
        <div className="fila">
          <Campo etiqueta="Ejemplar disponible">
            <Selector
              requerido
              placeholder="Escribe el título"
              valor={form.idEjemplar}
              onChange={(idEjemplar) => setForm({ ...form, idEjemplar })}
              opciones={disponibles.map((ejemplar) => ({
                valor: ejemplar.idEjemplar,
                etiqueta: `${ejemplar.libro?.titulo || 'Obra'} · Nº ${ejemplar.numeroEjemplar ?? ejemplar.idEjemplar}`,
                claves: [ejemplar.libro?.titulo, ejemplar.libro?.cota],
              }))}
            />
          </Campo>
          <Campo etiqueta="Lector">
            <Selector
              requerido
              placeholder="Escribe el nombre"
              valor={form.idLector}
              onChange={(idLector) => setForm({ ...form, idLector })}
              opciones={lectores.map((lector) => ({
                valor: lector.idLector,
                etiqueta: `${lector.nombre} ${lector.apellido}`,
                claves: [lector.nombre, lector.apellido, lector.cedula],
              }))}
            />
          </Campo>
          <Campo etiqueta="Devolución estimada">
            <input type="datetime-local" value={form.fechaDevolucionEsperada} onChange={(evento) => setForm({ ...form, fechaDevolucionEsperada: evento.target.value })} required />
          </Campo>
        </div>
        <div className="acciones">
          <button className="boton" type="submit">Prestar</button>
          {crearLector && <button className="boton boton-secundario" type="button" onClick={abrirLector}>Registrar lector nuevo</button>}
        </div>
      </form>}
      {popup && (
        <div className="velo" onMouseDown={() => setPopup(false)}>
          <form className="dialogo" onMouseDown={(evento) => evento.stopPropagation()} onSubmit={guardarLector}>
            <h2>Registrar lector</h2>
            <p className="nota">Queda elegido para este préstamo en cuanto lo guardes.</p>
            <Aviso>{errorLector}</Aviso>
            <div className="fila">
              <Campo etiqueta="Nombre"><input name="nombre" value={nuevoLector.nombre} onChange={cambiarLector} required /></Campo>
              <Campo etiqueta="Apellido"><input name="apellido" value={nuevoLector.apellido} onChange={cambiarLector} required /></Campo>
            </div>
            <Campo etiqueta="Cédula"><input name="cedula" value={nuevoLector.cedula} onChange={cambiarLector} required /></Campo>
            <Campo etiqueta="Correo"><input name="correo" type="email" value={nuevoLector.correo} onChange={cambiarLector} required /></Campo>
            <Campo etiqueta="Teléfono"><input name="telefono" value={nuevoLector.telefono} onChange={cambiarLector} required /></Campo>
            <div className="fila">
              <Campo etiqueta="Semestre (opcional)"><input name="semestre" value={nuevoLector.semestre} onChange={cambiarLector} /></Campo>
              <Campo etiqueta="Carrera (opcional)"><input name="carrera" value={nuevoLector.carrera} onChange={cambiarLector} /></Campo>
            </div>
            <div className="acciones">
              <button className="boton" type="submit">Guardar y elegir</button>
              <button className="boton boton-secundario" type="button" onClick={() => setPopup(false)}>Cancelar</button>
            </div>
          </form>
        </div>
      )}
      <Busqueda valor={consulta} onChange={setConsulta} placeholder="Obra o lector" />
      {ordenados.length === 0 ? <Vacio>No hay préstamos que empiecen por ese texto.</Vacio> : (
        <div className="tabla-contenedor">
          <table>
            <thead>
              <tr><th>Obra</th><th>Lector</th><th>Prestado</th><th>Estimada</th><th>Devuelto</th><th>Registró</th><th>Estado</th><th></th></tr>
            </thead>
            <tbody>
              {ordenados.map((prestamo) => (
                <tr key={prestamo.idPrestamo}>
                  <td>{prestamo.ejemplar?.libro?.titulo || '—'} · Nº {prestamo.ejemplar?.numeroEjemplar ?? prestamo.ejemplar?.idEjemplar}</td>
                  <td>{prestamo.lector ? `${prestamo.lector.nombre} ${prestamo.lector.apellido}` : '—'}</td>
                  <td>{formatoFecha(prestamo.fechaPrestamo)}</td>
                  <td>{formatoFecha(prestamo.fechaDevolucionEsperada)}</td>
                  <td>{formatoFecha(prestamo.fechaDevolucionReal)}</td>
                  <td>{prestamo.registradoPor?.usuario || '—'}</td>
                  <td><span className={`marca-estado ${prestamo.estado}`}>{prestamo.estado}</span></td>
                  <td>
                    {editarPermiso && prestamo.estado === 'prestado' && (
                      <button type="button" onClick={() => devolver(prestamo.idPrestamo)}>Devolver</button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </Pagina>
  )
}
