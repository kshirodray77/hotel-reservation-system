import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { bookingsApi, paymentsApi } from '../services/api.js';

export default function Checkout() {
  const { bookingId } = useParams();
  const navigate = useNavigate();
  const [booking, setBooking] = useState(null);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const [form, setForm] = useState({
    method: 'CARD',
    cardNumber: '4242 4242 4242 4242',
    cardHolder: '',
    expiry: '12/29',
    cvv: '123',
  });

  useEffect(() => {
    bookingsApi.mine().then((list) => {
      const b = list.find((x) => x.id === Number(bookingId));
      if (!b) setError('Booking not found');
      else setBooking(b);
    });
  }, [bookingId]);

  async function pay(e) {
    e.preventDefault();
    setBusy(true); setError('');
    try {
      await paymentsApi.checkout({
        bookingId: Number(bookingId),
        method: form.method,
        cardNumber: form.cardNumber.replace(/\s+/g, ''),
        cardHolder: form.cardHolder,
        expiry: form.expiry,
        cvv: form.cvv,
      });
      navigate('/bookings');
    } catch (err) {
      setError(err.response?.data?.message || 'Payment failed');
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="mx-auto max-w-2xl px-4 py-10">
      <h1 className="text-2xl font-bold text-slate-900">Checkout</h1>
      {error && <div className="mt-4 rounded-lg bg-rose-50 p-3 text-sm text-rose-700">{error}</div>}
      {!booking ? (
        <p className="mt-4 text-slate-500">Loading...</p>
      ) : (
        <div className="mt-6 grid gap-6 md:grid-cols-2">
          <div className="card p-5">
            <h2 className="font-semibold text-slate-900">Order summary</h2>
            <dl className="mt-4 space-y-2 text-sm">
              <div className="flex justify-between"><dt>Booking</dt><dd>#{booking.id}</dd></div>
              <div className="flex justify-between"><dt>Room</dt><dd>{booking.roomType} • {booking.roomNumber}</dd></div>
              <div className="flex justify-between"><dt>Stay</dt><dd>{booking.checkIn} → {booking.checkOut}</dd></div>
              <div className="flex justify-between"><dt>Guests</dt><dd>{booking.guests}</dd></div>
            </dl>
            <hr className="my-4 border-slate-200" />
            <div className="flex items-center justify-between">
              <span className="text-sm text-slate-600">Total</span>
              <span className="text-2xl font-bold text-slate-900">${Number(booking.totalPrice).toFixed(2)}</span>
            </div>
          </div>

          <form onSubmit={pay} className="card space-y-4 p-5">
            <h2 className="font-semibold text-slate-900">Payment details</h2>
            <p className="text-xs text-slate-500">Demo: any card not ending in 0000 succeeds. No real charges.</p>
            <div>
              <label className="label">Method</label>
              <select className="input" value={form.method}
                      onChange={(e) => setForm({ ...form, method: e.target.value })}>
                <option value="CARD">Credit/debit card</option>
                <option value="PAYPAL">PayPal</option>
              </select>
            </div>
            <div>
              <label className="label">Card holder</label>
              <input className="input" required value={form.cardHolder}
                     onChange={(e) => setForm({ ...form, cardHolder: e.target.value })} />
            </div>
            <div>
              <label className="label">Card number</label>
              <input className="input" required value={form.cardNumber}
                     onChange={(e) => setForm({ ...form, cardNumber: e.target.value })} />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="label">Expiry</label>
                <input className="input" required placeholder="MM/YY" value={form.expiry}
                       onChange={(e) => setForm({ ...form, expiry: e.target.value })} />
              </div>
              <div>
                <label className="label">CVV</label>
                <input className="input" required value={form.cvv}
                       onChange={(e) => setForm({ ...form, cvv: e.target.value })} />
              </div>
            </div>
            <button type="submit" disabled={busy} className="btn-primary w-full">
              {busy ? 'Processing...' : `Pay $${Number(booking.totalPrice).toFixed(2)}`}
            </button>
          </form>
        </div>
      )}
    </div>
  );
}
