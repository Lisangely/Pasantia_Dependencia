export const MODULOS = [
  ['/catalogo', 'Catálogo', 'puedeCrearLibros'],
  ['/autores', 'Autores', 'puedeCrearAutores'],
  ['/editoriales', 'Editoriales', 'puedeCrearEditoriales'],
  ['/ubicaciones', 'Ubicaciones', 'puedeCrearUbicaciones'],
  ['/ejemplares', 'Ejemplares', 'puedeCrearEjemplares'],
  ['/lectores', 'Lectores', 'puedeCrearLectores'],
  ['/prestamos', 'Préstamos', 'puedeCrearPrestamos'],
  ['/incidencias', 'Incidencias', 'puedeCrearIncidencias'],
]

export const CREENCIAS = [
  ['puedeCrearLibros', 'Puede crear libros'],
  ['puedeCrearAutores', 'Puede crear autores'],
  ['puedeCrearEditoriales', 'Puede crear editoriales'],
  ['puedeCrearUbicaciones', 'Puede crear ubicaciones'],
  ['puedeCrearEjemplares', 'Puede crear ejemplares'],
  ['puedeCrearLectores', 'Puede crear lectores'],
  ['puedeCrearPrestamos', 'Puede crear préstamos'],
  ['puedeCrearIncidencias', 'Puede crear incidencias'],
]

export function esAdmin(usuario) {
  return usuario?.rol === 'administrador'
}

export function puede(usuario, permiso) {
  if (!permiso || esAdmin(usuario)) return true
  return Boolean(usuario?.[permiso])
}

export function primeraRuta() {
  return '/catalogo'
}
