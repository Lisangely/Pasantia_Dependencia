import { useEffect, useState } from 'react'
import { api } from '../api'
import { Aviso, Busqueda, Pagina, Vacio, algunoEmpieza, formatoFecha } from '../ui'

export default function Auditoria() {
  const [eventos, setEventos] = useState([])
  const [consulta, setConsulta] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    api('/api/auditoria')
      .then((data) => {
        setEventos([...data].sort((a, b) => String(b.fechaEvento).localeCompare(String(a.fechaEvento))))
      })
      .catch((err) => setError(err.message))
  }, [])

  return (
    <Pagina titulo="Auditoría" texto="Historial de inicios de sesión, cambios del catálogo, préstamos, devoluciones e incidencias.">
      <Aviso>{error}</Aviso>
      <Busqueda valor={consulta} onChange={setConsulta} placeholder="Acción, entidad o usuario" />
      {eventos.filter((evento) => algunoEmpieza(consulta, [evento.accion, evento.entidad, evento.usuario?.usuario, evento.observaciones])).length === 0 ? <Vacio>No hay eventos que empiecen por ese texto.</Vacio> : (
        <div className="tabla-contenedor">
          <table>
            <thead>
              <tr><th>Fecha</th><th>Usuario</th><th>Acción</th><th>Entidad</th><th>Anterior</th><th>Nuevo</th><th>Nota</th></tr>
            </thead>
            <tbody>
              {eventos.filter((evento) => algunoEmpieza(consulta, [evento.accion, evento.entidad, evento.usuario?.usuario, evento.observaciones])).map((evento) => (
                <tr key={evento.idAuditoria}>
                  <td>{formatoFecha(evento.fechaEvento)}</td>
                  <td>{evento.usuario?.usuario || '—'}</td>
                  <td>{evento.accion}</td>
                  <td>{evento.entidad} #{evento.idEntidad}</td>
                  <td>{evento.valorAnterior || '—'}</td>
                  <td>{evento.valorNuevo || '—'}</td>
                  <td>{evento.observaciones || '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </Pagina>
  )
}
