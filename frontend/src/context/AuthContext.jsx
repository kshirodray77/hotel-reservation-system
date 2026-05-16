import { createContext, useContext, useEffect, useState } from 'react';
import { authApi } from '../services/api.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const raw = localStorage.getItem('hotel.user');
    return raw ? JSON.parse(raw) : null;
  });

  useEffect(() => {
    if (user) localStorage.setItem('hotel.user', JSON.stringify(user));
    else localStorage.removeItem('hotel.user');
  }, [user]);

  async function login(email, password) {
    const r = await authApi.login({ email, password });
    localStorage.setItem('hotel.token', r.token);
    setUser({ id: r.userId, email: r.email, fullName: r.fullName, role: r.role });
    return r;
  }

  async function register(payload) {
    const r = await authApi.register(payload);
    localStorage.setItem('hotel.token', r.token);
    setUser({ id: r.userId, email: r.email, fullName: r.fullName, role: r.role });
    return r;
  }

  function logout() {
    localStorage.removeItem('hotel.token');
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
