import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { bookingsApi, reviewsApi, roomsApi } from '../services/api.js';
import { useAuth } from '../context/AuthContext.jsx';

export default function RoomDetail() {
  const { id } = useParams();
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const { user } = useAuth();

  const [room, setRoom] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const today = new Date().toISOString().slice(0, 10);
  const tomorrow = new Date(Date.now() + 86400_000).toISOString().slice(0, 10);
  const [form, setForm] = useState({
    checkIn:  params.get('checkIn')  || today,
    checkOut: params.get('checkOut') || tomorrow,
    guests:   Number(params.get('guests') || 2),
    specialRequest: '',
  });

  const nights = useMemo(() => {
    const a = new Date(form.checkIn);
    const b = new Date(form.checkOut);
    return Math.max(0, Math.round((b - a) / 86400_000));
  }, [form.checkIn, form.checkOut]);

  const total = room ? (Number(room.pricePerNight) * nights).toFixed(2) : '0.00';

  useEffect(() => {
    Promise.all([roomsApi.get(id), reviewsApi.forRoom(id)])
      .then(([r, rv]) => { setRoom(r); setReviews(rv); })
      .catch((e) => setError(e.response?.data?.message || 'Failed to load'));
  }, [id]);

  async function book(e) {
    e.preventDefault();
    if (!user) return navigate('/login');
    setBusy(true); setError('');
    try {
      const booking = await bookingsApi.create({
        roomId: Number(id),
        checkIn: form.checkIn,
        checkOut: form.checkOut,
        guests: Number(form.guests),
        specialRequest: form.specialRequest,
      });
      navigate(`/checkout/${booking.id}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Booking failed');
    } finally {
      setBusy(false);
    }
  }

  if (!room) return <div className="mx-auto max-w-7xl px-4 py-10 text-slate-500">Loading...</div>;

  return (
    <div className="mx-auto max-w-7xl px-4 py-10">
      <div className="grid gap-8 lg:grid-cols-3">
        <div className="lg:col-span-2">
          <img src={room.imageUrl} alt={room.roomNumber} className="aspect-[16/9] w-full rounded-2xl object-cover" />
          <div className="mt-6 flex items-start justify-between">
            <div>
              <p className="text-sm text-slate-500">Room {room.roomNumber}</p>
              <h1 className="text-3xl font-bold text-slate-900">{room.type}</h1>
              {room.averageRating > 0 && (
                <p className="mt-1 text-amber-600">★ {Number(room.averageRating).toFixed(1)} average rating</p>
              )}
            </div>
            <p className="text-right">
              <span className="text-3xl font-bold text-slate-900">${Number(room.pricePerNight).toFixed(2)}</span>
              <span className="block text-sm text-slate-500">per night</span>
            </p>
          </div>
          <p className="mt-4 text-slate-700">{room.description}</p>
          {room.amenities && (
            <div className="mt-6">
              <h2 className="text-lg font-semibold text-slate-900">Amenities</h2>
              <ul className="mt-2 flex flex-wrap gap-2">
                {room.amenities.split(',').map((a) => (
                  <li key={a} className="badge bg-brand-50 text-brand-700">{a.trim()}</li>
                ))}
              </ul>
            </div>
          )}
          <div className="mt-10">
            <h2 className="text-lg font-semibold text-slate-900">Guest reviews</h2>
            {reviews.length === 0 ? (
              <p className="mt-2 text-sm text-slate-500">No reviews yet.</p>
            ) : (
              <ul className="mt-4 space-y-4">
                {reviews.map((r) => (
                  <li key={r.id} className="card p-4">
                    <div className="flex items-center justify-between">
                      <p className="font-medium text-slate-900">{r.userFullName}</p>
                      <p className="text-amber-600">{'★'.repeat(r.rating)}</p>
                    </div>
                    <p className="mt-2 text-sm text-slate-600">{r.comment}</p>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>

        <aside className="lg:col-span-1">
          <form onSubmit={book} className="card sticky top-24 space-y-3 p-5">
            <h3 className="text-lg font-semibold text-slate-900">Book this room</h3>
            {error && <div className="rounded-lg bg-rose-50 p-3 text-sm text-rose-700">{error}</div>}
            <div>
              <label className="label">Check-in</label>
              <input type="date" className="input" value={form.checkIn}
                     onChange={(e) => setForm({ ...form, checkIn: e.target.value })} required />
            </div>
            <div>
              <label className="label">Check-out</label>
              <input type="date" className="input" value={form.checkOut}
                     onChange={(e) => setForm({ ...form, checkOut: e.target.value })} required />
            </div>
            <div>
              <label className="label">Guests</label>
              <input type="number" min="1" max={room.capacity} className="input" value={form.guests}
                     onChange={(e) => setForm({ ...form, guests: e.target.value })} />
            </div>
            <div>
              <label className="label">Special request</label>
              <textarea className="input" rows={3} value={form.specialRequest}
                        onChange={(e) => setForm({ ...form, specialRequest: e.target.value })} />
            </div>
            <div className="rounded-lg bg-slate-50 p-3 text-sm">
              <div className="flex justify-between"><span>{nights} night(s)</span><span>${Number(room.pricePerNight).toFixed(2)} ea</span></div>
              <div className="mt-2 flex justify-between font-semibold text-slate-900"><span>Total</span><span>${total}</span></div>
            </div>
            <button type="submit" disabled={busy || nights === 0} className="btn-primary w-full">
              {busy ? 'Booking...' : 'Book now'}
            </button>
          </form>
        </aside>
      </div>
    </div>
  );
}
