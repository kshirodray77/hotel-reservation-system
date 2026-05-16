import { useEffect, useState } from 'react';
import { adminApi, roomsApi } from '../services/api.js';
import StatusBadge from '../components/StatusBadge.jsx';

export default function Admin() {
  const [tab, setTab] = useState('bookings');
  const [bookings, setBookings] = useState([]);
  const [report, setReport] = useState(null);
  const [rooms, setRooms] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({
    roomNumber: '', type: 'DOUBLE', description: '', capacity: 2,
    pricePerNight: 100, imageUrl: '', amenities: '',
  });

  useEffect(() => {
    if (tab === 'bookings')  adminApi.bookings().then(setBookings);
    if (tab === 'reports')   adminApi.revenue().then(setReport);
    if (tab === 'rooms')     roomsApi.search({}).then(setRooms);
  }, [tab]);

  async function createRoom(e) {
    e.preventDefault();
    try {
      await roomsApi.create({ ...form,
        capacity: Number(form.capacity), pricePerNight: Number(form.pricePerNight) });
      setShowForm(false);
      const list = await roomsApi.search({});
      setRooms(list);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed');
    }
  }

  async function deleteRoom(id) {
    if (!confirm('Disable this room?')) return;
    await roomsApi.remove(id);
    setRooms((rs) => rs.filter((r) => r.id !== id));
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-10">
      <h1 className="text-2xl font-bold text-slate-900">Admin dashboard</h1>

      <div className="mt-6 flex gap-2 border-b border-slate-200">
        {['bookings','rooms','reports'].map((t) => (
          <button key={t}
                  onClick={() => setTab(t)}
                  className={`-mb-px border-b-2 px-4 py-2 text-sm font-medium capitalize ${
                    tab === t ? 'border-brand-600 text-brand-700' : 'border-transparent text-slate-500'
                  }`}>{t}</button>
        ))}
      </div>

      {tab === 'bookings' && (
        <div className="mt-6 overflow-x-auto card">
          <table className="min-w-full text-sm">
            <thead className="bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
              <tr>
                <th className="px-4 py-3">#</th>
                <th className="px-4 py-3">Guest</th>
                <th className="px-4 py-3">Room</th>
                <th className="px-4 py-3">Stay</th>
                <th className="px-4 py-3">Total</th>
                <th className="px-4 py-3">Status</th>
              </tr>
            </thead>
            <tbody>
              {bookings.map((b) => (
                <tr key={b.id} className="border-t border-slate-100">
                  <td className="px-4 py-3">{b.id}</td>
                  <td className="px-4 py-3">{b.userFullName}<div className="text-xs text-slate-500">{b.userEmail}</div></td>
                  <td className="px-4 py-3">{b.roomType} — {b.roomNumber}</td>
                  <td className="px-4 py-3">{b.checkIn} → {b.checkOut}</td>
                  <td className="px-4 py-3">${Number(b.totalPrice).toFixed(2)}</td>
                  <td className="px-4 py-3"><StatusBadge status={b.status} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {tab === 'rooms' && (
        <div className="mt-6">
          <div className="mb-4 flex justify-end">
            <button className="btn-primary" onClick={() => setShowForm(true)}>+ Add room</button>
          </div>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {rooms.map((r) => (
              <div key={r.id} className="card p-4">
                <div className="flex items-start justify-between">
                  <div>
                    <p className="text-xs text-slate-500">#{r.roomNumber}</p>
                    <h3 className="font-semibold">{r.type}</h3>
                  </div>
                  <p className="font-bold">${Number(r.pricePerNight).toFixed(2)}</p>
                </div>
                <p className="mt-2 line-clamp-2 text-sm text-slate-600">{r.description}</p>
                <button className="btn-danger mt-3 text-xs" onClick={() => deleteRoom(r.id)}>Disable</button>
              </div>
            ))}
          </div>

          {showForm && (
            <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4">
              <form onSubmit={createRoom} className="card w-full max-w-lg space-y-3 p-6">
                <h2 className="text-lg font-semibold">New room</h2>
                <div className="grid grid-cols-2 gap-3">
                  <div><label className="label">Room number</label>
                    <input className="input" required value={form.roomNumber}
                           onChange={(e) => setForm({ ...form, roomNumber: e.target.value })} /></div>
                  <div><label className="label">Type</label>
                    <select className="input" value={form.type}
                            onChange={(e) => setForm({ ...form, type: e.target.value })}>
                      <option>SINGLE</option><option>DOUBLE</option><option>SUITE</option>
                      <option>DELUXE</option><option>FAMILY</option>
                    </select></div>
                  <div><label className="label">Capacity</label>
                    <input type="number" className="input" value={form.capacity}
                           onChange={(e) => setForm({ ...form, capacity: e.target.value })} /></div>
                  <div><label className="label">Price/night</label>
                    <input type="number" className="input" value={form.pricePerNight}
                           onChange={(e) => setForm({ ...form, pricePerNight: e.target.value })} /></div>
                </div>
                <div><label className="label">Image URL</label>
                  <input className="input" value={form.imageUrl}
                         onChange={(e) => setForm({ ...form, imageUrl: e.target.value })} /></div>
                <div><label className="label">Amenities (comma-separated)</label>
                  <input className="input" value={form.amenities}
                         onChange={(e) => setForm({ ...form, amenities: e.target.value })} /></div>
                <div><label className="label">Description</label>
                  <textarea className="input" rows={3} value={form.description}
                            onChange={(e) => setForm({ ...form, description: e.target.value })} /></div>
                <div className="flex justify-end gap-2">
                  <button type="button" className="btn-secondary" onClick={() => setShowForm(false)}>Cancel</button>
                  <button type="submit" className="btn-primary">Create</button>
                </div>
              </form>
            </div>
          )}
        </div>
      )}

      {tab === 'reports' && report && (
        <div className="mt-6 grid gap-4 sm:grid-cols-3">
          <div className="card p-6"><p className="text-sm text-slate-500">Total revenue</p><p className="mt-2 text-3xl font-bold text-slate-900">${Number(report.totalRevenue).toFixed(2)}</p><p className="mt-1 text-xs text-slate-500">{report.from} → {report.to}</p></div>
          <div className="card p-6"><p className="text-sm text-slate-500">Total bookings</p><p className="mt-2 text-3xl font-bold text-slate-900">{report.totalBookings}</p></div>
          <div className="card p-6"><p className="text-sm text-slate-500">Active rooms</p><p className="mt-2 text-3xl font-bold text-slate-900">{report.totalRooms}</p></div>
        </div>
      )}
    </div>
  );
}
