import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Layout = () => {
  const { user, logout } = useAuth();

  return (
    <div className="app-shell">
      <header className="app-header">
        <div className="brand">
          <NavLink to="/catalog" className="brand-link">
            Storefront
          </NavLink>
        </div>
        <nav className="nav-links">
          <NavLink to="/catalog" className="nav-link">
            Catalogue
          </NavLink>
          {user ? (
            <NavLink to="/orders" className="nav-link">
              My Orders
            </NavLink>
          ) : null}
        </nav>
        <div className="auth-actions">
          {user ? (
            <>
              <span className="user-pill">{user.username}</span>
              <button type="button" onClick={logout} className="btn ghost">
                Log out
              </button>
            </>
          ) : (
            <>
              <NavLink to="/login" className="btn ghost">
                Log in
              </NavLink>
              <NavLink to="/register" className="btn primary">
                Register
              </NavLink>
            </>
          )}
        </div>
      </header>
      <main className="app-main">
        <Outlet />
      </main>
      <footer className="app-footer">
        <small>
          Demo store UI for COMP4348 &mdash; configure backend URL via
          VITE_API_BASE_URL.
        </small>
      </footer>
    </div>
  );
};

export default Layout;
