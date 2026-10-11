import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// The dev server forwards /api calls to Spring Boot, so the session cookie works without CORS issues.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
});
