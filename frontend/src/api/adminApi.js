import { api, query } from './client';

export const authApi = {
  login: (email, password) => api.post('/api/auth/login', { email, password }),
  me: () => api.get('/api/auth/me'),
  logout: () => api.post('/api/auth/logout'),
};

export const adminApi = {
  dashboard: () => api.get('/api/admin/dashboard'),
  roles: () => api.get('/api/admin/roles'),

  listUsers: (params) => api.get('/api/admin/users' + query(params)),
  getUser: (id) => api.get(`/api/admin/users/${id}`),
  createUser: (data) => api.post('/api/admin/users', data),
  updateUser: (id, data) => api.put(`/api/admin/users/${id}`, data),
  toggleStatus: (id) => api.patch(`/api/admin/users/${id}/status`),
  resetPassword: (id, newPassword, confirmPassword) =>
    api.post(`/api/admin/users/${id}/reset-password`, { newPassword, confirmPassword }),
  deleteUser: (id) => api.del(`/api/admin/users/${id}`),

  settings: () => api.get('/api/admin/settings'),
  saveSettings: (values) => api.put('/api/admin/settings', values),

  audit: (params) => api.get('/api/admin/audit' + query(params)),
  auditActions: () => api.get('/api/admin/audit/actions'),
};
