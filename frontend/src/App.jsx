import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider, useAuth } from './auth'
import { puede, primeraRuta, esAdmin } from './permisos'
import Shell from './Shell'
import Auditoria from './pages/Auditoria'
import Autores from './pages/Autores'
import Catalogo from './pages/Catalogo'
import Editoriales from './pages/Editoriales'
import Ejemplares from './pages/Ejemplares'
import Incidencias from './pages/Incidencias'
import Lectores from './pages/Lectores'
import Libro from './pages/Libro'
import Login from './pages/Login'
import Prestamos from './pages/Prestamos'
import Ubicaciones from './pages/Ubicaciones'
import Usuarios from './pages/Usuarios'

function Modulo({ permiso, children }) {
  const { usuario } = useAuth()
  if (!puede(usuario, permiso)) return <Navigate to={primeraRuta(usuario)} replace />
  return children
}

function SoloAdmin({ children }) {
  const { usuario } = useAuth()
  if (!esAdmin(usuario)) return <Navigate to={primeraRuta(usuario)} replace />
  return children
}

function Inicio() {
  const { usuario } = useAuth()
  if (!usuario) return <Navigate to="/login" replace />
  return <Navigate to={primeraRuta(usuario)} replace />
}

function Rutas() {
  const { listo } = useAuth()
  if (!listo) return <p className="cargando">Abriendo la biblioteca…</p>
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route element={<Shell />}>
        <Route path="/catalogo" element={<Catalogo />} />
        <Route path="/libros/nuevo" element={<Modulo permiso="puedeCrearLibros"><Libro /></Modulo>} />
        <Route path="/libros/:id" element={<Libro />} />
        <Route path="/autores" element={<Autores />} />
        <Route path="/editoriales" element={<Editoriales />} />
        <Route path="/ubicaciones" element={<Ubicaciones />} />
        <Route path="/ejemplares" element={<Ejemplares />} />
        <Route path="/lectores" element={<Lectores />} />
        <Route path="/prestamos" element={<Prestamos />} />
        <Route path="/incidencias" element={<Incidencias />} />
        <Route path="/auditoria" element={<Auditoria />} />
        <Route path="/usuarios" element={<SoloAdmin><Usuarios /></SoloAdmin>} />
      </Route>
      <Route path="*" element={<Inicio />} />
    </Routes>
  )
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Rutas />
      </BrowserRouter>
    </AuthProvider>
  )
}
