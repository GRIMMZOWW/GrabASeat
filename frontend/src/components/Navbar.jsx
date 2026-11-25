import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './Navbar.css';

export default function Navbar({ user, onLogout }) {
  const navigate = useNavigate();

  function handleLogout() {
    onLogout();
    navigate('/login');
  }

  return (
    <header className="navbar">
      <div className="navbar-container">
        <Link to="/" className="navbar-brand">
          <svg className="navbar-logo-icon" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
            <rect x="2" y="3" width="20" height="18" rx="2" />
            <line x1="7" y1="3" x2="7" y2="21" />
            <line x1="17" y1="3" x2="17" y2="21" />
            <line x1="2" y1="12" x2="22" y2="12" />
            <line x1="2" y1="7" x2="7" y2="7" />
            <line x1="2" y1="17" x2="7" y2="17" />
            <line x1="17" y1="17" x2="22" y2="17" />
            <line x1="17" y1="7" x2="22" y2="7" />
          </svg>
          <span className="navbar-title">GrabASeat</span>
        </Link>

        <nav className="navbar-actions">
          {user ? (
            <div className="navbar-user">
              <span className="navbar-username">{user.name}</span>
              <button onClick={handleLogout} className="btn btn-secondary">
                Logout
              </button>
            </div>
          ) : (
            <div className="navbar-guest">
              <Link to="/login" className="btn btn-secondary">
                Login
              </Link>
              <Link to="/register" className="btn btn-primary">
                Register
              </Link>
            </div>
          )}
        </nav>
      </div>
    </header>
  );
}
