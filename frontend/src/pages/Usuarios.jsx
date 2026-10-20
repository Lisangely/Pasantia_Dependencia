import { useEffect, useState } from 'react'
import { api } from '../api'
import { Aviso, Busqueda, Campo, Pagina, Vacio, algunoEmpieza } from '../ui'
import { CREENCIAS } from '../permisos'

const PERMISOS_VACIOS = Object.fromEntries(CREENCIAS.map(([clave]) => [clave, false]))

const VACIO = {
  nombre: '',
  apellido: '',
  usuario: '',
  correo: '',
  passwordHash: '',
  rol: '',
  activo: true,
  puedeEliminar: false,
  puedeActualizar: false,
  puedeCrear: false,
  pregunta1: '',
  pregunta2: '',
  pregunta3: '',
  respuesta1: '',
  respuesta2: '',
  respuesta3: '',
  ...PERMISOS_VACIOS,
}

export default function Usuarios() {
  const [usuarios, setUsuarios] = useState([])
  const [form, setForm] = useState(VACIO)
  const [editando, setEditando] = useState(null)
  const [esAdministrador, setEsAdministrador] = useState(false)
  const [faltanRespuestas, setFaltanRespuestas] = useState(false)
  const [preguntasAbiertas, setPreguntasAbiertas] = useState(false)
  const [preguntasListas, setPreguntasListas] = useState(false)
  const [respaldoPreguntas, setRespaldoPreguntas] = useState(null)
  const [consulta, setConsulta] = useState('')
  const [error, setError] = useState('')

  useEffect(() => { cargar() }, [])

  async function cargar() {
    try {
      setUsuarios(await api('/api/usuarios'))
    } catch (err) {
      setError(err.message)
    }
  }

  function cambiar(evento) {
    const { name, value, type, checked } = evento.target
    setForm((actual) => {
      const siguiente = { ...actual, [name]: type === 'checkbox' ? checked : value }
      if (name === 'puedeCrear' && !checked) {
        CREENCIAS.forEach(([clave]) => { siguiente[clave] = false })
      }
      return siguiente
    })
  }

  function preguntasDe(datos) {
    return {
      pregunta1: datos.pregunta1,
      pregunta2: datos.pregunta2,
      pregunta3: datos.pregunta3,
      respuesta1: datos.respuesta1,
      respuesta2: datos.respuesta2,
      respuesta3: datos.respuesta3,
    }
  }

  function preguntasCompletas(datos) {
    return ['pregunta1', 'pregunta2', 'pregunta3', 'respuesta1', 'respuesta2', 'respuesta3']
      .every((clave) => String(datos[clave] || '').trim())
  }

  function abrirPreguntas(datos) {
    setRespaldoPreguntas(preguntasDe(datos))
    setPreguntasAbiertas(true)
  }

  function elegirPerfil(evento) {
    const rol = evento.target.value
    if (rol === 'administrador') {
      setRespaldoPreguntas(preguntasDe(form))
      setForm((actual) => ({ ...actual, rol }))
      setPreguntasAbiertas(true)
      return
    }
    setForm((actual) => ({ ...actual, rol }))
    setPreguntasListas(false)
  }

  function cancelarPreguntas() {
    setPreguntasAbiertas(false)
    if (preguntasListas) {
      setForm((actual) => ({ ...actual, ...(respaldoPreguntas || {}) }))
      return
    }
    setForm((actual) => ({
      ...actual,
      rol: '',
      pregunta1: '',
      pregunta2: '',
      pregunta3: '',
      respuesta1: '',
      respuesta2: '',
      respuesta3: '',
    }))
  }

  function confirmarPreguntas(evento) {
    evento.preventDefault()
    if (!preguntasCompletas(form)) return
    setPreguntasListas(true)
    setPreguntasAbiertas(false)
  }

  function limpiar() {
    setForm(VACIO)
    setEditando(null)
    setEsAdministrador(false)
    setPreguntasAbiertas(false)
    setPreguntasListas(false)
    setRespaldoPreguntas(null)
  }

  async function guardar(evento) {
    evento.preventDefault()
    setError('')
    if (!editando && form.rol === 'administrador' && !preguntasCompletas(form)) {
      abrirPreguntas(form)
      return
    }
    const cuerpo = { ...form }
    if (editando && !cuerpo.passwordHash) delete cuerpo.passwordHash
    if (editando || cuerpo.rol !== 'administrador') {
      if (!esAdministrador) {
        delete cuerpo.pregunta1
        delete cuerpo.pregunta2
        delete cuerpo.pregunta3
        delete cuerpo.respuesta1
        delete cuerpo.respuesta2
        delete cuerpo.respuesta3
      }
    }
    if (!editando) cuerpo.rol = form.rol
    try {
      if (editando) await api(`/api/usuarios/${editando}`, { method: 'PUT', body: JSON.stringify(cuerpo) })
      else await api('/api/usuarios', { method: 'POST', body: JSON.stringify(cuerpo) })
      limpiar()
      await cargar()
    } catch (err) {
      setError(err.message)
    }
  }

  function empezarEdicion(usuario) {
    const admin = usuario.rol === 'administrador'
    setEditando(usuario.idUsuario)
    setEsAdministrador(admin)
    setFaltanRespuestas(admin && !usuario.pregunta1)
    setPreguntasAbiertas(false)
    setForm({
      nombre: usuario.nombre || '',
      apellido: usuario.apellido || '',
      usuario: usuario.usuario || '',
      correo: usuario.correo || '',
      passwordHash: '',
      activo: Boolean(usuario.activo),
      puedeEliminar: admin || Boolean(usuario.puedeEliminar),
      puedeActualizar: admin || Boolean(usuario.puedeActualizar),
      puedeCrear: admin || Boolean(usuario.puedeCrear),
      pregunta1: usuario.pregunta1 || '',
      pregunta2: usuario.pregunta2 || '',
      pregunta3: usuario.pregunta3 || '',
      respuesta1: '',
      respuesta2: '',
      respuesta3: '',
      ...Object.fromEntries(CREENCIAS.map(([clave]) => [clave, admin || Boolean(usuario[clave])])),
    })
  }

  const visibles = usuarios.filter((usuario) => algunoEmpieza(consulta, [
    usuario.nombre, usuario.apellido, `${usuario.nombre} ${usuario.apellido}`, usuario.usuario,
  ]))

  return (
    <Pagina titulo="Usuarios" texto="Solo el administrador crea cuentas, asigna permisos y cambia contraseñas.">
      <Aviso>{error}</Aviso>
      <Busqueda valor={consulta} onChange={setConsulta} placeholder="Nombre o usuario" />
      <div className="rejilla">
        <form className="panel" onSubmit={guardar}>
          <h2>{editando ? 'Editar usuario' : 'Nuevo usuario'}</h2>
          <div className="fila">
            <Campo etiqueta="Nombre"><input name="nombre" value={form.nombre} onChange={cambiar} required /></Campo>
            <Campo etiqueta="Apellido"><input name="apellido" value={form.apellido} onChange={cambiar} required /></Campo>
          </div>
          <Campo etiqueta="Usuario"><input name="usuario" value={form.usuario} onChange={cambiar} required /></Campo>
          <Campo etiqueta="Correo"><input name="correo" type="email" value={form.correo} onChange={cambiar} required /></Campo>
          <Campo etiqueta={editando ? 'Nueva contraseña' : 'Contraseña'}>
            <input name="passwordHash" type="password" value={form.passwordHash} onChange={cambiar} required={!editando} autoComplete="new-password" />
          </Campo>
          {!editando && (
            <fieldset className="panel-campos">
              <span className="campo"><span>Perfil</span></span>
              <label className="chequeo">
                <input type="radio" name="rol" value="ayudante" checked={form.rol === 'ayudante'} onChange={elegirPerfil} required />
                Ayudante
              </label>
              <label className="chequeo">
                <input type="radio" name="rol" value="administrador" checked={form.rol === 'administrador'} onChange={elegirPerfil} required />
                Administrador
              </label>
            </fieldset>
          )}
          {!editando && form.rol === 'administrador' && preguntasListas && (
            <p className="nota">
              Preguntas de seguridad listas.
              {' '}
              <button className="boton boton-secundario" type="button" onClick={() => abrirPreguntas(form)}>Modificar</button>
            </p>
          )}
          {esAdministrador ? (
            <>
              <p className="nota">Estas tres preguntas permiten que el administrador reestablezca su propia contraseña.</p>
              <Campo etiqueta="Pregunta 1"><input name="pregunta1" value={form.pregunta1} onChange={cambiar} required /></Campo>
              <Campo etiqueta="Respuesta 1"><input name="respuesta1" value={form.respuesta1} onChange={cambiar} required={faltanRespuestas} placeholder={faltanRespuestas ? '' : 'En blanco si no cambia'} /></Campo>
              <Campo etiqueta="Pregunta 2"><input name="pregunta2" value={form.pregunta2} onChange={cambiar} required /></Campo>
              <Campo etiqueta="Respuesta 2"><input name="respuesta2" value={form.respuesta2} onChange={cambiar} required={faltanRespuestas} /></Campo>
              <Campo etiqueta="Pregunta 3"><input name="pregunta3" value={form.pregunta3} onChange={cambiar} required /></Campo>
              <Campo etiqueta="Respuesta 3"><input name="respuesta3" value={form.respuesta3} onChange={cambiar} required={faltanRespuestas} /></Campo>
            </>
          ) : (editando || form.rol === 'ayudante') && (
            <>
              <label className="chequeo">
                <input name="puedeEliminar" type="checkbox" checked={form.puedeEliminar} onChange={cambiar} />
                Puede eliminar
              </label>
              <label className="chequeo">
                <input name="puedeActualizar" type="checkbox" checked={form.puedeActualizar} onChange={cambiar} />
                Puede actualizar
              </label>
              <label className="chequeo">
                <input name="puedeCrear" type="checkbox" checked={form.puedeCrear} onChange={cambiar} />
                Puede crear
              </label>
              {form.puedeCrear && (
                <div className="subchequeos">
                  {CREENCIAS.map(([clave, etiqueta]) => (
                    <label className="chequeo" key={clave}>
                      <input name={clave} type="checkbox" checked={form[clave]} onChange={cambiar} />
                      {etiqueta}
                    </label>
                  ))}
                </div>
              )}
            </>
          )}
          {(editando ? !esAdministrador : form.rol === 'ayudante') && (
            <label className="chequeo">
              <input name="activo" type="checkbox" checked={form.activo} onChange={cambiar} />
              Activo
            </label>
          )}
          <div className="acciones">
            <button className="boton" type="submit">{editando ? 'Actualizar' : 'Guardar'}</button>
            {editando && <button className="boton boton-secundario" type="button" onClick={limpiar}>Cancelar</button>}
          </div>
        </form>
        <div className="tabla-contenedor">
          {visibles.length === 0 ? <Vacio>No hay usuarios que empiecen por ese texto.</Vacio> : (
            <table>
              <thead><tr><th>Nombre</th><th>Usuario</th><th>Perfil</th><th>Estado</th><th></th></tr></thead>
              <tbody>
                {visibles.map((usuario) => (
                  <tr key={usuario.idUsuario}>
                    <td>{usuario.nombre} {usuario.apellido}</td>
                    <td>{usuario.usuario}</td>
                    <td>{usuario.rol === 'administrador' ? 'Administrador' : 'Ayudante'}</td>
                    <td>{usuario.activo ? 'Activo' : 'Inactivo'}</td>
                    <td>
                      <button type="button" onClick={() => empezarEdicion(usuario)}>Editar</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
      {preguntasAbiertas && (
        <div className="velo" onMouseDown={cancelarPreguntas}>
          <form className="dialogo dialogo-ancho" onMouseDown={(evento) => evento.stopPropagation()} onSubmit={confirmarPreguntas}>
            <h2>Preguntas de seguridad</h2>
            <p>Este administrador las usará para reestablecer su propia contraseña. Las tres preguntas y las tres respuestas son obligatorias.</p>
            <Campo etiqueta="Pregunta 1"><input name="pregunta1" value={form.pregunta1} onChange={cambiar} required /></Campo>
            <Campo etiqueta="Respuesta 1"><input name="respuesta1" value={form.respuesta1} onChange={cambiar} required /></Campo>
            <Campo etiqueta="Pregunta 2"><input name="pregunta2" value={form.pregunta2} onChange={cambiar} required /></Campo>
            <Campo etiqueta="Respuesta 2"><input name="respuesta2" value={form.respuesta2} onChange={cambiar} required /></Campo>
            <Campo etiqueta="Pregunta 3"><input name="pregunta3" value={form.pregunta3} onChange={cambiar} required /></Campo>
            <Campo etiqueta="Respuesta 3"><input name="respuesta3" value={form.respuesta3} onChange={cambiar} required /></Campo>
            <div className="acciones">
              <button className="boton" type="submit">Guardar preguntas</button>
              <button className="boton boton-secundario" type="button" onClick={cancelarPreguntas}>Cancelar</button>
            </div>
          </form>
        </div>
      )}
    </Pagina>
  )
}
