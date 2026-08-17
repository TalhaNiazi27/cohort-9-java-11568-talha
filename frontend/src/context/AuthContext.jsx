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
        // Expected if not logged in
        if (isActive) {
          setUser(null);
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
    } catch (err) {
      console.error('Logout request failed', err);
    }
    
    safeStorage.removeItem('user');
    setUser(null);
  };

  const value = {
    user,
    // We don't expose token anymore since it's an HttpOnly cookie
    login,
    register,
    logout,
    loading
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  );
};

