import React, { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom';
import { apiFetch } from '../api';
import { formatCurrency, formatShowDateTime } from '../utils/format';
import './ShowPage.css';

const ROWS = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
const MAX_SEATS = 6;
const HOLD_SECONDS = 300;

function compareSeatNumbers(a, b) {
  const rowCmp = a.slice(0, 1).localeCompare(b.slice(0, 1));
  if (rowCmp !== 0) return rowCmp;
  return Number(a.slice(1)) - Number(b.slice(1));
}

function formatCountdown(totalSeconds) {
  const mins = Math.floor(totalSeconds / 60);
  const secs = totalSeconds % 60;
  return `${mins}:${String(secs).padStart(2, '0')}`;
}

export default function ShowPage({ user, onLogout }) {
  const { showId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();

  const [show, setShow] = useState(null);
  const [seats, setSeats] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);

  const [selectedSeats, setSelectedSeats] = useState([]);
  const [heldSeats, setHeldSeats] = useState(null);
  const [secondsLeft, setSecondsLeft] = useState(HOLD_SECONDS);
  const [paymentMethod, setPaymentMethod] = useState('UPI');
  const [submitting, setSubmitting] = useState(false);

  const [notice, setNotice] = useState(null);
  const [noticeType, setNoticeType] = useState('error');
  const [booking, setBooking] = useState(null);

  async function reloadSeats() {
    const seatsData = await apiFetch(`/api/shows/${showId}/seats`);
    setSeats(seatsData || []);
  }

  useEffect(() => {
    let isMounted = true;
    setLoading(true);
    setLoadError(null);

    Promise.all([
      apiFetch(`/api/shows/${showId}`),
      apiFetch(`/api/shows/${showId}/seats`)
    ])
      .then(([showData, seatsData]) => {
        if (isMounted) {
          setShow(showData);
          setSeats(seatsData || []);
          setLoading(false);
        }
      })
      .catch((err) => {
        if (isMounted) {
          setLoadError(err.message);
          setLoading(false);
        }
      });

    return () => {
      isMounted = false;
    };
  }, [showId]);

  useEffect(() => {
    if (!heldSeats) return;
    const timer = setInterval(() => {
      setSecondsLeft((prev) => {
        if (prev <= 1) {
          clearInterval(timer);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
    return () => clearInterval(timer);
  }, [heldSeats]);

  useEffect(() => {
    if (heldSeats && secondsLeft === 0) {
      setHeldSeats(null);
      setSelectedSeats([]);
      setNotice('Your hold expired');
      setNoticeType('error');
      reloadSeats().catch(() => {});
    }
  }, [secondsLeft, heldSeats]);

  function handleUnauthorized() {
    onLogout();
    navigate('/login', {
      state: {
        from: location.pathname,
        message: 'Your session expired. Please log in again.'
      }
    });
  }

  function handleSeatClick(seat) {
    if (heldSeats || submitting) return;
    setNotice(null);

    const seatNum = seat.seatNumber;
    if (selectedSeats.includes(seatNum)) {
      setSelectedSeats(selectedSeats.filter((s) => s !== seatNum));
      return;
    }

    if (selectedSeats.length >= MAX_SEATS) {
      setNotice('You can select up to 6 seats.');
      setNoticeType('error');
      return;
    }

    setSelectedSeats([...selectedSeats, seatNum].sort(compareSeatNumbers));
  }

  async function handleHoldSeats() {
    if (!user) {
      navigate('/login', { state: { from: location.pathname } });
      return;
    }
    if (selectedSeats.length === 0 || submitting) return;

    setSubmitting(true);
    setNotice(null);

    try {
      await apiFetch(`/api/shows/${showId}/lock`, {
        method: 'POST',
        body: JSON.stringify({ seatNumbers: selectedSeats })
      });
      setSecondsLeft(HOLD_SECONDS);
      setHeldSeats([...selectedSeats]);
    } catch (err) {
      if (err.status === 401) {
        handleUnauthorized();
        return;
      }
      if (err.status === 409) {
        setNotice(err.message);
        setNoticeType('error');
        setHeldSeats(null);
        setSelectedSeats([]);
        await reloadSeats().catch(() => {});
      } else {
        setNotice(err.message);
        setNoticeType('error');
      }
    } finally {
      setSubmitting(false);
    }
  }

  async function handleRelease() {
    if (submitting) return;
    setSubmitting(true);
    setHeldSeats(null);
    setSelectedSeats([]);
    setSecondsLeft(HOLD_SECONDS);
    setNotice(null);
    try {
      await reloadSeats();
    } catch (err) {
      setNotice(err.message);
      setNoticeType('error');
    } finally {
      setSubmitting(false);
    }
  }

  async function handlePayAndConfirm() {
    if (!heldSeats || heldSeats.length === 0 || submitting) return;

    setSubmitting(true);
    setNotice(null);

    try {
      const data = await apiFetch(`/api/shows/${showId}/book`, {
        method: 'POST',
        body: JSON.stringify({
          seatNumbers: heldSeats,
          paymentMethod
        })
      });
      setBooking(data);
      setHeldSeats(null);
      setSelectedSeats([]);
    } catch (err) {
      if (err.status === 401) {
        handleUnauthorized();
        return;
      }
      if (err.status === 409) {
        setNotice(err.message);
        setNoticeType('error');
        setHeldSeats(null);
        setSelectedSeats([]);
        await reloadSeats().catch(() => {});
      } else {
        setNotice(err.message);
        setNoticeType('error');
      }
    } finally {
      setSubmitting(false);
    }
  }

  const seatsByRow = ROWS.map((row) => ({
    row,
    items: seats
      .filter((s) => s.seatNumber.startsWith(row))
      .sort((a, b) => compareSeatNumbers(a.seatNumber, b.seatNumber))
  })).filter((group) => group.items.length > 0);

  const activeSeats = heldSeats || selectedSeats;
  const estimatedTotal = show ? Number(show.price) * activeSeats.length : 0;

  return (
    <main className="show-page">
      <Link to={show ? `/movies/${show.movieId}` : '/'} className="back-link">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
          <polyline points="15 18 9 12 15 6" />
        </svg>
        <span>{show ? 'Back to showtimes' : 'Back to movies'}</span>
      </Link>

      {loading && (
        <div className="show-state" role="status">
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
          <p>Loading seat map...</p>
        </div>
      )}

      {loadError && !loading && (
        <div className="show-error" role="alert">
          <p>{loadError}</p>
        </div>
      )}

      {!loading && !loadError && booking && (
        <section className="confirmation-panel" aria-live="polite">
          <div className="confirmation-header">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              <polyline points="20 6 9 17 4 12" />
            </svg>
            <h1 className="confirmation-title">Booking Confirmed</h1>
          </div>

          <dl className="confirmation-list">
            <div className="confirmation-row">
              <dt>Booking ID</dt>
              <dd>#{booking.bookingId}</dd>
            </div>
            <div className="confirmation-row">
              <dt>Movie</dt>
              <dd>{booking.movieTitle}</dd>
            </div>
            <div className="confirmation-row">
              <dt>Theatre</dt>
              <dd>{booking.theatreName}, {booking.city}</dd>
            </div>
            <div className="confirmation-row">
              <dt>Screen</dt>
              <dd>Screen {booking.screenNumber}</dd>
            </div>
            <div className="confirmation-row">
              <dt>Start Time</dt>
              <dd>{formatShowDateTime(booking.startTime)}</dd>
            </div>
            <div className="confirmation-row">
              <dt>Seats</dt>
              <dd>{booking.seatNumbers.join(', ')}</dd>
            </div>
            <div className="confirmation-row">
              <dt>Total</dt>
              <dd className="confirmation-total">{formatCurrency(booking.totalAmount)}</dd>
            </div>
            <div className="confirmation-row">
              <dt>Payment Method</dt>
              <dd>{booking.paymentMethod}</dd>
            </div>
            <div className="confirmation-row">
              <dt>Status</dt>
              <dd>{booking.status}</dd>
            </div>
          </dl>

          <div className="confirmation-actions">
            <Link to="/" className="btn btn-primary">
              Back to movies
            </Link>
          </div>
        </section>
      )}

      {!loading && !loadError && !booking && show && (
        <>
          <header className="show-header">
            <h1 className="show-movie-title">{show.movieTitle}</h1>
            <p className="show-meta-line">
              {show.theatreName}, {show.city} · Screen {show.screenNumber} · {formatShowDateTime(show.startTime)} · {formatCurrency(show.price)} per seat
            </p>
          </header>

          {notice && (
            <div className={`show-notice ${noticeType === 'error' ? 'show-notice-error' : ''}`} role="alert">
              {notice}
            </div>
          )}

          {seatsByRow.length === 0 ? (
            <div className="show-state">
              <p>No seats found for this show.</p>
            </div>
          ) : (
            <section className="seat-map-section" aria-label="Seat Map">
              <div className="screen-indicator" aria-hidden="true">
                <div className="screen-line" />
                <span className="screen-label">Screen</span>
              </div>

              <div className="seat-grid">
                {seatsByRow.map(({ row, items }) => (
                  <div key={row} className="seat-row">
                    <span className="seat-row-label" aria-hidden="true">{row}</span>
                    <div className="seat-row-buttons">
                      {items.map((seat) => {
                        const isBooked = seat.status === 'BOOKED';
                        const isHeldByMe = Boolean(heldSeats && heldSeats.includes(seat.seatNumber));
                        const isLockedByOther = seat.status === 'LOCKED' && !isHeldByMe;
                        const isSelected = isHeldByMe || selectedSeats.includes(seat.seatNumber);
                        const isDisabled = isBooked || isLockedByOther || Boolean(heldSeats) || submitting;

                        let stateText = 'available';
                        if (isBooked) stateText = 'booked';
                        else if (isHeldByMe) stateText = 'held by you';
                        else if (isLockedByOther) stateText = 'held';
                        else if (isSelected) stateText = 'selected';

                        return (
                          <button
                            key={seat.seatNumber}
                            type="button"
                            className={`seat-btn ${isSelected ? 'seat-selected' : ''} ${isBooked ? 'seat-booked' : ''} ${isLockedByOther ? 'seat-locked' : ''}`}
                            disabled={isDisabled}
                            onClick={() => handleSeatClick(seat)}
                            aria-label={`Seat ${seat.seatNumber}, ${stateText}`}
                            aria-pressed={isSelected}
                          >
                            {isSelected ? (
                              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                                <polyline points="20 6 9 17 4 12" />
                              </svg>
                            ) : isBooked ? (
                              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                                <line x1="18" y1="6" x2="6" y2="18" />
                                <line x1="6" y1="6" x2="18" y2="18" />
                              </svg>
                            ) : isLockedByOther ? (
                              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                                <line x1="5" y1="12" x2="19" y2="12" />
                              </svg>
                            ) : (
                              <span>{seat.seatNumber.slice(1)}</span>
                            )}
                          </button>
                        );
                      })}
                    </div>
                  </div>
                ))}
              </div>

              <div className="seat-legend" aria-label="Seat status legend">
                <div className="legend-item">
                  <span className="legend-box legend-available" aria-hidden="true" />
                  <span>Available</span>
                </div>
                <div className="legend-item">
                  <span className="legend-box legend-selected" aria-hidden="true">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
                      <polyline points="20 6 9 17 4 12" />
                    </svg>
                  </span>
                  <span>Selected</span>
                </div>
                <div className="legend-item">
                  <span className="legend-box legend-locked" aria-hidden="true">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                      <line x1="5" y1="12" x2="19" y2="12" />
                    </svg>
                  </span>
                  <span>Held</span>
                </div>
                <div className="legend-item">
                  <span className="legend-box legend-booked" aria-hidden="true">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                      <line x1="18" y1="6" x2="6" y2="18" />
                      <line x1="6" y1="6" x2="18" y2="18" />
                    </svg>
                  </span>
                  <span>Booked</span>
                </div>
              </div>
            </section>
          )}

          <section className="summary-bar" aria-label="Booking summary">
            <div className="summary-details">
              <div className="summary-seats">
                <span className="summary-label">Selected seats:</span>{' '}
                <span className="summary-value">
                  {activeSeats.length > 0 ? activeSeats.join(', ') : 'None'}
                </span>
              </div>
              <div className="summary-price">
                <span className="summary-label">Total (estimate):</span>{' '}
                <span className="summary-total">{formatCurrency(estimatedTotal)}</span>
              </div>
            </div>

            {!heldSeats ? (
              <div className="summary-actions">
                {!user ? (
                  <button
                    type="button"
                    className="btn btn-primary"
                    onClick={() => navigate('/login', { state: { from: location.pathname } })}
                  >
                    Log in to hold seats
                  </button>
                ) : (
                  <button
                    type="button"
                    className="btn btn-primary"
                    disabled={selectedSeats.length === 0 || submitting}
                    onClick={handleHoldSeats}
                  >
                    {submitting ? 'Holding seats...' : 'Hold seats'}
                  </button>
                )}
              </div>
            ) : (
              <div className="hold-panel">
                <div className="hold-timer" role="timer" aria-live="off">
                  <span>Hold active:</span>{' '}
                  <strong>{formatCountdown(secondsLeft)}</strong>
                </div>

                <fieldset className="payment-methods" disabled={submitting}>
                  <legend className="payment-legend">Payment method</legend>
                  <label className="payment-option">
                    <input
                      type="radio"
                      name="paymentMethod"
                      value="UPI"
                      checked={paymentMethod === 'UPI'}
                      onChange={() => setPaymentMethod('UPI')}
                    />
                    <span>UPI</span>
                  </label>
                  <label className="payment-option">
                    <input
                      type="radio"
                      name="paymentMethod"
                      value="CARD"
                      checked={paymentMethod === 'CARD'}
                      onChange={() => setPaymentMethod('CARD')}
                    />
                    <span>CARD</span>
                  </label>
                </fieldset>

                <div className="hold-actions">
                  <button
                    type="button"
                    className="btn btn-secondary"
                    disabled={submitting}
                    onClick={handleRelease}
                  >
                    Release
                  </button>
                  <button
                    type="button"
                    className="btn btn-primary"
                    disabled={submitting}
                    onClick={handlePayAndConfirm}
                  >
                    {submitting ? 'Confirming...' : 'Pay and confirm'}
                  </button>
                </div>
              </div>
            )}
          </section>
        </>
      )}
    </main>
  );
}
