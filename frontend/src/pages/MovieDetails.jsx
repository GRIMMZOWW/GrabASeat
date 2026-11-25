import React, { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { apiFetch } from '../api';
import { formatCurrency, formatDateHeading, formatShowTime } from '../utils/format';
import './MovieDetails.css';

function groupShowsByDateAndTheatre(shows) {
  const sorted = [...shows].sort((a, b) => a.startTime.localeCompare(b.startTime));
  const dateMap = new Map();

  for (const show of sorted) {
    const dateKey = show.startTime.split('T')[0];
    if (!dateMap.has(dateKey)) {
      dateMap.set(dateKey, new Map());
    }
    const theatreMap = dateMap.get(dateKey);
    const theatreKey = `${show.theatreName}__${show.city}`;
    if (!theatreMap.has(theatreKey)) {
      theatreMap.set(theatreKey, {
        theatreName: show.theatreName,
        city: show.city,
        shows: []
      });
    }
    theatreMap.get(theatreKey).shows.push(show);
  }

  return Array.from(dateMap.entries()).map(([dateKey, theatreMap]) => ({
    dateKey,
    theatres: Array.from(theatreMap.values())
  }));
}

export default function MovieDetails() {
  const { id } = useParams();
  const [movie, setMovie] = useState(null);
  const [shows, setShows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let isMounted = true;
    setLoading(true);
    setError(null);

    Promise.all([
      apiFetch(`/api/movies/${id}`),
      apiFetch(`/api/movies/${id}/shows`)
    ])
      .then(([movieData, showsData]) => {
        if (isMounted) {
          setMovie(movieData);
          setShows(showsData || []);
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
  }, [id]);

  const groupedDates = groupShowsByDateAndTheatre(shows);

  return (
    <main className="movie-details-page">
      <Link to="/" className="back-link">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
          <polyline points="15 18 9 12 15 6" />
        </svg>
        <span>Back to movies</span>
      </Link>

      {loading && (
        <div className="details-state" role="status">
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
          <p>Loading movie and showtimes...</p>
        </div>
      )}

      {error && !loading && (
        <div className="details-error" role="alert">
          <p>{error}</p>
        </div>
      )}

      {!loading && !error && movie && (
        <>
          <header className="movie-hero">
            <div className="movie-hero-tags">
              <span className="movie-tag">{movie.language}</span>
              <span className="movie-tag">{movie.genre}</span>
            </div>
            <h1 className="movie-hero-title">{movie.title}</h1>
            <div className="movie-hero-meta">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                <circle cx="12" cy="12" r="10" />
                <polyline points="12 6 12 12 16 14" />
              </svg>
              <span>{movie.durationMinutes} mins</span>
            </div>
          </header>

          <section className="shows-section" aria-label="Showtimes">
            {groupedDates.length === 0 ? (
              <div className="details-empty">
                <h2>No shows scheduled</h2>
                <p>There are no upcoming showtimes available for this movie right now.</p>
              </div>
            ) : (
              groupedDates.map((dateGroup) => (
                <div key={dateGroup.dateKey} className="date-group">
                  <h2 className="date-heading">{formatDateHeading(dateGroup.dateKey)}</h2>
                  <div className="theatre-list">
                    {dateGroup.theatres.map((theatre) => (
                      <div key={`${theatre.theatreName}-${theatre.city}`} className="theatre-row">
                        <div className="theatre-info">
                          <h3 className="theatre-name">{theatre.theatreName}</h3>
                          <p className="theatre-city">{theatre.city}</p>
                        </div>
                        <div className="showtime-list">
                          {theatre.shows.map((show) => (
                            <Link
                              key={show.id}
                              to={`/shows/${show.id}`}
                              className="showtime-link"
                            >
                              <span className="showtime-clock">{formatShowTime(show.startTime)}</span>
                              <span className="showtime-meta">
                                Screen {show.screenNumber} · {formatCurrency(show.price)}
                              </span>
                            </Link>
                          ))}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              ))
            )}
          </section>
        </>
      )}
    </main>
  );
}
