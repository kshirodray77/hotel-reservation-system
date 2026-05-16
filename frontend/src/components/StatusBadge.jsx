const COLORS = {
  PENDING:   'bg-amber-100 text-amber-800',
  CONFIRMED: 'bg-emerald-100 text-emerald-800',
  CANCELLED: 'bg-rose-100 text-rose-800',
  COMPLETED: 'bg-slate-200 text-slate-700',
  SUCCESS:   'bg-emerald-100 text-emerald-800',
  FAILED:    'bg-rose-100 text-rose-800',
};

export default function StatusBadge({ status }) {
  return <span className={`badge ${COLORS[status] || 'bg-slate-100 text-slate-700'}`}>{status}</span>;
}
