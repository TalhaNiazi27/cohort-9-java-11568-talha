import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../api/axiosConfig';
import { safeStorage } from '../utils/storage';

const AuthContext = createContext();

export const useAuth = () => {
  return useContext(AuthContext);
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [serviceError, setServiceError] = useState(false);

  // Check if user is authenticated by hitting the /me endpoint
  useEffect(() => {
    let isActive = true;

    const initAuth = async () => {
      try {
        const response = await api.get('/auth/me');
        if (isActive) {
          setUser(response.data);
        }
      } catch (error) {
        if (isActive) {
          if (error.code === 'ECONNABORTED' || !error.response) {
            // Timeout or Network Error
            setServiceError(true);
          } else {
            // Expected if not logged in (401 Unauthorized)
            setUser(null);
          }
        }
      }
      if (isActive) {
        setLoading(false);
      }
    };

    initAuth();

    return () => {
      isActive = false;
    };
  }, []);

  const login = async (username, password) => {
    try {
      const response = await api.post('/auth/login', { username, password });
      
      // Cookie is automatically set by the backend
      const { id, email, phone } = response.data.user || response.data;
      setUser({ id, email, phone });
      
      return { success: true };
    } catch (error) {
      console.error('Login error:', error);
      return { 
        success: false, 
        message: error.response?.data?.message || 'Login failed. Please check your credentials.' 
      };
    }
  };

  const register = async (userData) => {
    try {
      const response = await api.post('/auth/register', userData);
      // Registration sets the cookie automatically now
      const { id, email, phone } = response.data;
      setUser({ id, email, phone });
      
      return { success: true, data: response.data };
    } catch (error) {
      console.error('Registration error:', error);
      return { 
        success: false, 
        message: error.response?.data?.message || 'Registration failed.' 
      };
    }
  };

  const logout = async () => {
    try {
      await api.post('/auth/logout');
      safeStorage.removeItem('user');
      setUser(null);
      return { success: true };
    } catch (err) {
      console.error('Logout request failed', err);
      return { success: false, message: 'Logout failed.' };
    }
  };

  const value = {
    user,
    // We don't expose token anymore since it's an HttpOnly cookie
    login,
    register,
    logout,
    loading
  };

  if (serviceError) {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100vh', textAlign: 'center', fontFamily: 'sans-serif' }}>
        <h2 style={{ color: '#ff4d4f', marginBottom: '1rem' }}>Service Unavailable</h2>
        <p style={{ color: '#666', marginBottom: '1.5rem' }}>The server is taking too long to respond or is currently unreachable.</p>
        <button 
          onClick={() => window.location.reload()} 
          style={{ padding: '10px 20px', backgroundColor: '#1890ff', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
        >
          Try Again
        </button>
      </div>
    );
  }

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  );
};

