import axios from 'axios';
import { safeStorage } from '../utils/storage';

const defaultBaseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
const isProd = import.meta.env.PROD;

// Create an Axios instance with base URL
const api = axios.create({
  baseURL: isProd ? defaultBaseURL.replace(/^http:\/\//i, 'https://') : defaultBaseURL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // Send cookies with every request
  xsrfCookieName: 'XSRF-TOKEN', // The cookie name Spring Security uses
  xsrfHeaderName: 'X-XSRF-TOKEN', // The header name Spring Security expects
});

// Request Interceptor: No longer needed for Bearer tokens as we use HttpOnly cookies.
// We keep the interceptor shell in case future config is needed.
api.interceptors.request.use(
  (config) => {
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response Interceptor: Handle 401 Unauthorized globally
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    // If we receive a 401 (Unauthorized), it means the token is expired or invalid
    if (error.response && error.response.status === 401) {
      // Clear localStorage user and redirect to login (if not already on login)
      if (window.location.pathname !== '/login' && window.location.pathname !== '/register') {
        safeStorage.removeItem('user');
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default api;
