import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { roomsApi } from '../services/api.js';
import RoomCard from '../components/RoomCard.jsx';

export default function Rooms() {
  const [params, setParams] = useSearchParams();
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const filters = {
    checkIn:  params.get('checkIn')  || new Date().toISOString().slice(0, 10),
    checkOut: params.get('checkOut') || new Date(Date.now() + 86400_000).toISOString().slice(0, 10),
    guests:   params.get('guests')   || 1,
    minPrice: params.get('minPrice') || '',
    maxPrice: params.get('maxPrice') || '',
    type:     params.get('type')     || '',
  };

  useEffect(() => {
    setLoading(true);
    const cleaned = Object.fromEntries(Object.entries(filters).filter(([, v]) => v !== ''));
    roomsApi.search(cleaned)
      .then(setRooms)
      .catch((e) => setError(e.response?.data?.message || 'Failed to load rooms'))
      .finally(() => setLoading(false));
    // eslint-disable-next-line
  }, [params]);

  function applyFilters(e) {
    e.preventDefault();
    const next = {};
    new FormData(e.target).forEach((v, k) => { if (v) next[k] = v; });
    setParams(next);
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-10">
      <h1 className="text-2xl font-bold text-slate-900">Available rooms</h1>
      <p className="mt-1 text-sm text-slate-600">
        {filters.checkIn} → {filters.checkOut} • {filters.guests} guest(s)
      </p>

      <form onSubmit={applyFilters} className="card mt-6 grid grid-cols-2 gap-3 p-4 sm:grid-cols-6">
        <div>
          <label className="label">Check-in</label>
          <input type="date" name="checkIn" defaultValue={filters.checkIn} className="input" />
        </div>
        <div>
          <label className="label">Check-out</label>
          <input type="date" name="checkOut" defaultValue={filters.checkOut} className="input" />
        </div>
        <div>
          <label className="label">Guests</label>
          <input type="number" name="guests" min="1" max="10" defaultValue={filters.guests} className="input" />
        </div>
        <div>
          <label className="label">Type</label>
          <select name="type" defaultValue={filters.type} className="input">
            <option value="">Any</option>
            <option>SINGLE</option><option>DOUBLE</option><option>SUITE</option>
            <option>DELUXE</option><option>FAMILY</option>
          </select>
        </div>
        <div>
          <label className="label">Min price</label>
          <input type="number" name="minPrice" defaultValue={filters.minPrice} className="input" />
        </div>
        <div>
          <label className="label">Max price</label>
          <input type="number" name="maxPrice" defaultValue={filters.maxPrice} className="input" />
        </div>
        <div className="col-span-2 sm:col-span-6 flex justify-end">
          <button className="btn-primary" type="submit">Apply</button>
        </div>
      </form>

      <div className="mt-6">
        {loading && <p className="text-slate-500">Loading rooms...</p>}
        {error && <p className="text-rose-600">{error}</p>}
        {!loading && !error && rooms.length === 0 && (
          <p className="text-slate-500">No rooms match those filters.</p>
        )}
        <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {rooms.map((r) => <RoomCard key={r.id} room={r} />)}
        </div>
      </div>
    </div>
  );
}
