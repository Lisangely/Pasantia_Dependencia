import { useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../auth'
import { Aviso, Campo } from '../ui'
import { primeraRuta } from '../permisos'

export default function Login() {
  const { usuario, entrar } = useAuth()
  const navegar = useNavigate()
  const [form, setForm] = useState({ usuario: '', password: '' })
  const [error, setError] = useState('')
  const [aviso, setAviso] = useState('')
  const [enviando, setEnviando] = useState(false)
  const [recuperar, setRecuperar] = useState(false)
  const [preguntas, setPreguntas] = useState([])
  const [respuestas, setRespuestas] = useState(['', '', ''])
  const [nueva, setNueva] = useState('')

  if (usuario) return <Navigate to={primeraRuta(usuario)} replace />

  function cambiar(evento) {
    const { name, value } = evento.target
    setForm((actual) => ({ ...actual, [name]: value }))
  }

  async function enviar(evento) {
    evento.preventDefault()
    setError('')
    setEnviando(true)
    try {
      const sesion = await entrar(form)
      navegar(primeraRuta(sesion))
    } catch (err) {
      setError(err.message)
    } finally {
      setEnviando(false)
    }
  }

  async function pedirPreguntas(evento) {
    evento.preventDefault()
    setError('')
    setAviso('')
    try {
      const data = await api('/api/auth/recuperar/preguntas', {
        method: 'POST',
        body: JSON.stringify({ usuario: form.usuario }),
      })
      setPreguntas(data.preguntas || [])
      setRespuestas(['', '', ''])
    } catch (err) {
      setPreguntas([])
      setError(err.message)
    }
  }

  async function restablecer(evento) {
    evento.preventDefault()
    setError('')
    setAviso('')
    try {
      await api('/api/auth/recuperar', {
        method: 'POST',
        body: JSON.stringify({ usuario: form.usuario, respuestas, password: nueva }),
      })
      setAviso('Contraseña actualizada. Ya puedes entrar.')
      setRecuperar(false)
      setPreguntas([])
      setNueva('')
      setForm((actual) => ({ ...actual, password: '' }))
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div className="acceso">
      <section className="acceso-presentacion">
        <p>Universidad Central de Venezuela</p>
        <h1>Biblioteca de la Facultad de Arquitectura y Urbanismo</h1>
        <p>Catálogo, préstamos, incidencias y trazabilidad del inventario.</p>
      </section>
      {recuperar ? (
        <form className="acceso-form" onSubmit={preguntas.length ? restablecer : pedirPreguntas}>
          <h2>Reestablecer contraseña</h2>
          <p className="nota">Solo el administrador puede reestablecer la suya, respondiendo las tres preguntas que él mismo registró. La contraseña de cualquier otro perfil la cambia el administrador.</p>
          <Aviso>{error}</Aviso>
          <Aviso tipo="ok">{aviso}</Aviso>
          <Campo etiqueta="Usuario">
            <input name="usuario" value={form.usuario} onChange={cambiar} required />
          </Campo>
          {preguntas.map((pregunta, indice) => (
            <Campo key={pregunta} etiqueta={pregunta}>
              <input
                value={respuestas[indice]}
                onChange={(evento) => setRespuestas((actual) => actual.map((valor, i) => i === indice ? evento.target.value : valor))}
                required
              />
            </Campo>
          ))}
          {preguntas.length > 0 && (
            <Campo etiqueta="Nueva contraseña">
              <input type="password" value={nueva} onChange={(evento) => setNueva(evento.target.value)} required autoComplete="new-password" />
            </Campo>
          )}
          <button className="boton" type="submit">{preguntas.length ? 'Guardar contraseña' : 'Ver preguntas'}</button>
          <button className="boton boton-secundario" type="button" onClick={() => { setRecuperar(false); setPreguntas([]); setError('') }}>Volver</button>
        </form>
      ) : (
        <form className="acceso-form" onSubmit={enviar}>
          <h2>Ingreso del personal</h2>
          <Aviso>{error}</Aviso>
          <Aviso tipo="ok">{aviso}</Aviso>
          <Campo etiqueta="Usuario">
            <input name="usuario" value={form.usuario} onChange={cambiar} autoComplete="username" required />
          </Campo>
          <Campo etiqueta="Contraseña">
            <input name="password" type="password" value={form.password} onChange={cambiar} autoComplete="current-password" required />
          </Campo>
          <button className="boton" type="submit" disabled={enviando}>
            {enviando ? 'Ingresando…' : 'Entrar'}
          </button>
          <button className="boton boton-secundario" type="button" onClick={() => { setRecuperar(true); setError(''); setAviso('') }}>
            Reestablecer contraseña
          </button>
        </form>
      )}
    </div>
  )
}
