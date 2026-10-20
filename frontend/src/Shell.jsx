import { Navigate, NavLink, Outlet } from 'react-router-dom'
import { useAuth } from './auth'
import { MODULOS, esAdmin } from './permisos'

export default function Shell() {
  const { usuario, salir } = useAuth()

  if (!usuario) return <Navigate to="/login" replace />

  return (
    <div className="shell">
      <aside className="barra">
        <div className="marca">
          <span>FAU · UCV</span>
          <strong>Biblioteca</strong>
        </div>
        <nav className="menu">
          {MODULOS.map(([ruta, texto]) => (
            <NavLink key={ruta} to={ruta}>
              {texto}
            </NavLink>
          ))}
          <NavLink to="/auditoria">Auditoría</NavLink>
          {esAdmin(usuario) && (
            <NavLink to="/usuarios">Usuarios</NavLink>
          )}
        </nav>
        <div className="sesion">
          <strong>{usuario.nombre} {usuario.apellido}</strong>
          <span>{esAdmin(usuario) ? 'Administrador' : 'Personal'}</span>
          <button type="button" onClick={salir}>Cerrar sesión</button>
        </div>
      </aside>
      <main className="contenido">
        <Outlet />
      </main>
    </div>
  )
}
