import axios from 'axios';

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const api = axios.create({ baseURL });

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('hotel.token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (r) => r,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('hotel.token');
      localStorage.removeItem('hotel.user');
      // soft redirect
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login';
      }
    }
    return Promise.reject(err);
  },
);

export const authApi = {
  login: (data)    => api.post('/auth/login', data).then((r) => r.data),
  register: (data) => api.post('/auth/register', data).then((r) => r.data),
};

export const roomsApi = {
  search: (params) => api.get('/rooms', { params }).then((r) => r.data),
  get: (id)        => api.get(`/rooms/${id}`).then((r) => r.data),
  create: (data)   => api.post('/rooms', data).then((r) => r.data),
  update: (id, d)  => api.put(`/rooms/${id}`, d).then((r) => r.data),
  remove: (id)     => api.delete(`/rooms/${id}`).then((r) => r.data),
};

export const bookingsApi = {
  create: (data) => api.post('/bookings', data).then((r) => r.data),
  mine:   ()     => api.get('/bookings/me').then((r) => r.data),
  cancel: (id)   => api.delete(`/bookings/${id}`).then((r) => r.data),
};

export const paymentsApi = {
  checkout: (data) => api.post('/payments/checkout', data).then((r) => r.data),
};

export const reviewsApi = {
  forRoom: (roomId) => api.get(`/reviews/room/${roomId}`).then((r) => r.data),
  create:  (data)   => api.post('/reviews', data).then((r) => r.data),
};

export const adminApi = {
  bookings: () => api.get('/admin/bookings').then((r) => r.data),
  revenue:  (params) => api.get('/admin/reports/revenue', { params }).then((r) => r.data),
};

export default api;
