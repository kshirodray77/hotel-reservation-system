import { Link } from 'react-router-dom';

export default function RoomCard({ room }) {
  return (
    <Link to={`/rooms/${room.id}`} className="card group hover:shadow-md transition">
      <div className="aspect-[4/3] overflow-hidden bg-slate-200">
        {room.imageUrl ? (
          <img src={room.imageUrl} alt={room.roomNumber} className="h-full w-full object-cover transition group-hover:scale-105" />
        ) : (
          <div className="flex h-full items-center justify-center text-slate-400">No image</div>
        )}
      </div>
      <div className="p-4">
        <div className="flex items-start justify-between gap-2">
          <div>
            <p className="text-sm text-slate-500">Room {room.roomNumber}</p>
            <h3 className="text-base font-semibold text-slate-900">{room.type}</h3>
          </div>
          <p className="text-right">
            <span className="text-xl font-bold text-slate-900">${Number(room.pricePerNight).toFixed(2)}</span>
            <span className="block text-xs text-slate-500">per night</span>
          </p>
        </div>
        <p className="mt-2 line-clamp-2 text-sm text-slate-600">{room.description}</p>
        <div className="mt-3 flex items-center justify-between text-xs text-slate-500">
          <span>Sleeps {room.capacity}</span>
          {room.averageRating > 0 && (
            <span className="badge bg-amber-100 text-amber-800">★ {Number(room.averageRating).toFixed(1)}</span>
          )}
        </div>
      </div>
    </Link>
  );
}
