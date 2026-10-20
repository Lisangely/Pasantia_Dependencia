import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import { api, definirAlPerderSesion } from './api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(null)
  const [listo, setListo] = useState(false)

  useEffect(() => {
    definirAlPerderSesion(() => setUsuario(null))
    api('/api/auth/me')
      .then(setUsuario)
      .catch(() => setUsuario(null))
      .finally(() => setListo(true))
  }, [])

  const valor = useMemo(() => ({
    usuario,
    listo,
    async entrar(credenciales) {
      const sesion = await api('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify(credenciales),
      })
      setUsuario(sesion)
      return sesion
    },
    async salir() {
      try {
        await api('/api/auth/logout', { method: 'POST' })
      } finally {
        setUsuario(null)
      }
    },
  }), [usuario, listo])

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>
}

export function useAuth() {
  return useContext(AuthContext)
}
