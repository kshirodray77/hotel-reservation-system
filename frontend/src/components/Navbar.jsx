import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const linkCls = ({ isActive }) =>
    `px-3 py-2 rounded-md text-sm font-medium ${
      isActive ? 'text-brand-700 bg-brand-50' : 'text-slate-600 hover:text-slate-900'
    }`;

  return (
    <header className="sticky top-0 z-30 border-b border-slate-200 bg-white/80 backdrop-blur">
      <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-3">
        <Link to="/" className="flex items-center gap-2">
          <span className="grid h-8 w-8 place-items-center rounded-lg bg-brand-600 text-white font-bold">H</span>
          <span className="text-lg font-semibold text-slate-900">Hotel Reserve</span>
        </Link>
        <nav className="flex items-center gap-1">
          <NavLink to="/" end className={linkCls}>Home</NavLink>
          <NavLink to="/rooms" className={linkCls}>Rooms</NavLink>
          {user && <NavLink to="/bookings" className={linkCls}>My bookings</NavLink>}
          {user?.role === 'ADMIN' && <NavLink to="/admin" className={linkCls}>Admin</NavLink>}
        </nav>
        <div className="flex items-center gap-2">
          {!user ? (
            <>
              <Link to="/login" className="btn-secondary">Sign in</Link>
              <Link to="/register" className="btn-primary">Sign up</Link>
            </>
          ) : (
            <>
              <span className="hidden sm:block text-sm text-slate-600">Hi, {user.fullName.split(' ')[0]}</span>
              <button className="btn-secondary" onClick={() => { logout(); navigate('/'); }}>Logout</button>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
