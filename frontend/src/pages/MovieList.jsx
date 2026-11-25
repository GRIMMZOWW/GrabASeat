import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { apiFetch } from '../api';
import './MovieList.css';

export default function MovieList() {
  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let isMounted = true;
    apiFetch('/api/movies')
      .then((data) => {
        if (isMounted) {
          setMovies(data || []);
          setLoading(false);
        }
      })
      .catch((err) => {
        if (isMounted) {
          setError(err.message);
          setLoading(false);
        }
      });

    return () => {
      isMounted = false;
    };
  }, []);

  return (
    <main className="movies-page">
      <div className="movies-header">
        <h1 className="movies-heading">Now Showing</h1>
        <p className="movies-subheading">Select a film to explore showtimes and book your seats.</p>
      </div>

      {loading && (
        <div className="movies-state" role="status">
          <svg className="spinner-icon" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
            <line x1="12" y1="2" x2="12" y2="6" />
            <line x1="12" y1="18" x2="12" y2="22" />
            <line x1="4.93" y1="4.93" x2="7.76" y2="7.76" />
            <line x1="16.24" y1="16.24" x2="19.07" y2="19.07" />
            <line x1="2" y1="12" x2="6" y2="12" />
            <line x1="18" y1="12" x2="22" y2="12" />
            <line x1="4.93" y1="19.07" x2="7.76" y2="16.24" />
            <line x1="16.24" y1="7.76" x2="19.07" y2="4.93" />
          </svg>
          <p>Loading available movies...</p>
        </div>
      )}

      {error && !loading && (
        <div className="movies-error" role="alert">
          <p>{error}</p>
        </div>
      )}

      {!loading && !error && movies.length === 0 && (
        <div className="movies-empty">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
            <rect x="2" y="3" width="20" height="18" rx="2" />
            <line x1="7" y1="3" x2="7" y2="21" />
            <line x1="17" y1="3" x2="17" y2="21" />
            <line x1="2" y1="12" x2="22" y2="12" />
          </svg>
          <h2>No movies showing right now</h2>
          <p>Please check back later for newly scheduled cinema releases.</p>
        </div>
      )}

      {!loading && !error && movies.length > 0 && (
        <div className="movies-grid">
          {movies.map((movie) => (
            <Link key={movie.id} to={`/movies/${movie.id}`} className="movie-card">
              <div className="movie-card-header">
                <span className="movie-tag">{movie.language}</span>
                <span className="movie-tag">{movie.genre}</span>
              </div>
              <h2 className="movie-title">{movie.title}</h2>
              <div className="movie-meta">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                  <circle cx="12" cy="12" r="10" />
                  <polyline points="12 6 12 12 16 14" />
                </svg>
                <span>{movie.durationMinutes} mins</span>
              </div>
            </Link>
          ))}
        </div>
      )}
    </main>
  );
}
