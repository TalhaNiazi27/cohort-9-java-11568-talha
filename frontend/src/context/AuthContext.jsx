import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../api/axiosConfig';
import { safeStorage } from '../utils/storage';

const AuthContext = createContext();

export const useAuth = () => {
  return useContext(AuthContext);
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(safeStorage.getItem('token') || null);
  const [loading, setLoading] = useState(true);

  // Set up initial state from localStorage and fetch current user profile if token exists
  useEffect(() => {
    let isActive = true;

    const initAuth = async () => {
      if (token) {
        try {
          // If we have a token, optionally fetch the current user profile from the backend
          const response = await api.get('/auth/me');
          if (isActive) {
            setUser(response.data);
          }
        } catch (error) {
          console.error('Failed to authenticate stored token:', error);
          if (isActive) {
            logout();
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
  }, [token]);

  const login = async (username, password) => {
    try {
      const response = await api.post('/auth/login', { username, password });
      const { token: jwt, id, email, phone } = response.data;
      
      safeStorage.setItem('token', jwt);
      setToken(jwt);
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
      // Registration typically doesn't log you in directly, but in some apps it does.
      // Assuming we need to manually log in after, or just return success.
      return { success: true, data: response.data };
    } catch (error) {
      console.error('Registration error:', error);
      return { 
        success: false, 
        message: error.response?.data?.message || 'Registration failed.' 
      };
    }
  };

  const logout = () => {
    safeStorage.removeItem('token');
    safeStorage.removeItem('user');
    setToken(null);
    setUser(null);
    // Note: React Router handles redirecting if we wrap routes properly
  };

  const value = {
    user,
    token,
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
