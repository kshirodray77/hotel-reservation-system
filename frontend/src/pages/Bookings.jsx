import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { bookingsApi, reviewsApi } from '../services/api.js';
import StatusBadge from '../components/StatusBadge.jsx';

export default function Bookings() {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reviewFor, setReviewFor] = useState(null);
  const [reviewForm, setReviewForm] = useState({ rating: 5, comment: '' });

  function refresh() {
    setLoading(true);
    bookingsApi.mine().then(setBookings).finally(() => setLoading(false));
  }
  useEffect(refresh, []);

  async function cancel(id) {
    if (!confirm('Cancel this booking?')) return;
    try { await bookingsApi.cancel(id); refresh(); }
    catch (e) { setError(e.response?.data?.message || 'Cancel failed'); }
  }

  async function submitReview(e) {
    e.preventDefault();
    try {
      await reviewsApi.create({
        bookingId: reviewFor.id,
        rating: Number(reviewForm.rating),
        comment: reviewForm.comment,
      });
      setReviewFor(null);
      setReviewForm({ rating: 5, comment: '' });
      alert('Thanks for your review!');
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to submit review');
    }
  }

  return (
    <div className="mx-auto max-w-5xl px-4 py-10">
      <h1 className="text-2xl font-bold text-slate-900">My bookings</h1>
      {error && <div className="mt-4 rounded-lg bg-rose-50 p-3 text-sm text-rose-700">{error}</div>}
      {loading && <p className="mt-4 text-slate-500">Loading...</p>}
      {!loading && bookings.length === 0 && (
        <div className="card mt-6 p-8 text-center">
          <p className="text-slate-600">You haven't made any bookings yet.</p>
          <Link to="/rooms" className="btn-primary mt-4">Browse rooms</Link>
        </div>
      )}
      <ul className="mt-6 space-y-4">
        {bookings.map((b) => (
          <li key={b.id} className="card flex flex-col gap-4 p-4 sm:flex-row">
            <img src={b.roomImageUrl} alt={b.roomNumber} className="h-32 w-full rounded-lg object-cover sm:w-48" />
            <div className="flex-1">
              <div className="flex items-start justify-between">
                <div>
                  <p className="text-sm text-slate-500">Booking #{b.id}</p>
                  <h3 className="font-semibold text-slate-900">{b.roomType} — Room {b.roomNumber}</h3>
                </div>
                <StatusBadge status={b.status} />
              </div>
              <p className="mt-2 text-sm text-slate-600">{b.checkIn} → {b.checkOut} • {b.guests} guest(s)</p>
              <p className="mt-1 text-sm font-semibold text-slate-900">${Number(b.totalPrice).toFixed(2)}</p>
              <div className="mt-3 flex flex-wrap gap-2">
                {b.status === 'PENDING' && (
                  <Link to={`/checkout/${b.id}`} className="btn-primary">Pay now</Link>
                )}
                {(b.status === 'PENDING' || b.status === 'CONFIRMED') && (
                  <button className="btn-secondary" onClick={() => cancel(b.id)}>Cancel</button>
                )}
                {(b.status === 'COMPLETED' || b.status === 'CONFIRMED') && (
                  <button className="btn-secondary" onClick={() => setReviewFor(b)}>Leave a review</button>
                )}
              </div>
            </div>
          </li>
        ))}
      </ul>

      {reviewFor && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4">
          <form onSubmit={submitReview} className="card w-full max-w-md p-6">
            <h2 className="text-lg font-semibold text-slate-900">Review your stay</h2>
            <p className="text-sm text-slate-600">{reviewFor.roomType} — Room {reviewFor.roomNumber}</p>
            <div className="mt-4">
              <label className="label">Rating</label>
              <select className="input" value={reviewForm.rating}
                      onChange={(e) => setReviewForm({ ...reviewForm, rating: e.target.value })}>
                {[5,4,3,2,1].map((n) => <option key={n} value={n}>{'★'.repeat(n)} ({n})</option>)}
              </select>
            </div>
            <div className="mt-3">
              <label className="label">Comment</label>
              <textarea className="input" rows={4} value={reviewForm.comment}
                        onChange={(e) => setReviewForm({ ...reviewForm, comment: e.target.value })} />
            </div>
            <div className="mt-4 flex justify-end gap-2">
              <button type="button" className="btn-secondary" onClick={() => setReviewFor(null)}>Cancel</button>
              <button type="submit" className="btn-primary">Submit</button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}
