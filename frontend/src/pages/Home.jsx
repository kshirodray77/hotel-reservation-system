import { Link, useNavigate } from 'react-router-dom';
import { useState } from 'react';

export default function Home() {
  const today = new Date().toISOString().slice(0, 10);
  const tomorrow = new Date(Date.now() + 86400_000).toISOString().slice(0, 10);
  const [form, setForm] = useState({ checkIn: today, checkOut: tomorrow, guests: 2 });
  const navigate = useNavigate();

  function submit(e) {
    e.preventDefault();
    const params = new URLSearchParams(form).toString();
    navigate(`/rooms?${params}`);
  }

  return (
    <div>
      {/* Hero */}
      <section className="relative overflow-hidden bg-gradient-to-br from-brand-700 via-brand-600 to-brand-500 text-white">
        <div className="mx-auto max-w-7xl px-4 py-20 sm:py-28">
          <h1 className="max-w-2xl text-4xl font-bold sm:text-5xl">
            Find your perfect stay.
          </h1>
          <p className="mt-4 max-w-xl text-lg text-brand-50">
            Comfortable rooms, transparent pricing, instant confirmation.
          </p>
          <form onSubmit={submit} className="mt-10 grid grid-cols-1 gap-3 rounded-2xl bg-white p-4 shadow-lg sm:grid-cols-4">
            <div>
              <label className="label text-slate-700">Check-in</label>
              <input type="date" className="input" value={form.checkIn}
                     onChange={(e) => setForm({ ...form, checkIn: e.target.value })} required />
            </div>
            <div>
              <label className="label text-slate-700">Check-out</label>
              <input type="date" className="input" value={form.checkOut}
                     onChange={(e) => setForm({ ...form, checkOut: e.target.value })} required />
            </div>
            <div>
              <label className="label text-slate-700">Guests</label>
              <input type="number" min="1" max="10" className="input" value={form.guests}
                     onChange={(e) => setForm({ ...form, guests: e.target.value })} />
            </div>
            <div className="flex items-end">
              <button type="submit" className="btn-primary w-full">Search rooms</button>
            </div>
          </form>
        </div>
      </section>

      {/* Features */}
      <section className="mx-auto max-w-7xl px-4 py-16">
        <h2 className="text-2xl font-bold text-slate-900">Why book with us?</h2>
        <div className="mt-8 grid gap-6 sm:grid-cols-3">
          {[
            { t: 'Best rate guarantee',  d: 'No hidden fees. Pay what you see.' },
            { t: 'Instant confirmation', d: 'Get your booking confirmed by email in seconds.' },
            { t: 'Free cancellation',    d: 'Plans change — cancel before check-in at no cost.' },
          ].map((f) => (
            <div key={f.t} className="card p-6">
              <h3 className="font-semibold text-slate-900">{f.t}</h3>
              <p className="mt-2 text-sm text-slate-600">{f.d}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="bg-slate-100">
        <div className="mx-auto max-w-7xl px-4 py-12 text-center">
          <h2 className="text-xl font-semibold text-slate-900">Ready to book?</h2>
          <p className="mt-2 text-slate-600">Browse our full inventory and find a room that fits.</p>
          <Link to="/rooms" className="btn-primary mt-4">Browse rooms</Link>
        </div>
      </section>
    </div>
  );
}
