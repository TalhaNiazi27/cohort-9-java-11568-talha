import React from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

const Profile = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (!user) {
    return <div style={{ display: 'flex', justifyContent: 'center', marginTop: '3rem' }}>Loading profile...</div>;
  }

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', padding: '1rem' }}>
      <div className="glass-card animate-fade-in" style={{ width: '100%', maxWidth: '500px' }}>
        <h2 style={{ marginBottom: '1.5rem', fontSize: '1.5rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '1rem' }}>
          My Profile
        </h2>
        
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginBottom: '2rem' }}>
          <div>
            <span style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>User ID</span>
            <p style={{ fontWeight: '500' }}>#{user.id}</p>
          </div>
          
          <div>
            <span style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>Email Address</span>
            <p style={{ fontWeight: '500' }}>{user.email || 'Not provided'}</p>
          </div>
          
          <div>
            <span style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>Phone Number</span>
            <p style={{ fontWeight: '500' }}>{user.phone || 'Not provided'}</p>
          </div>
        </div>

        <div style={{ display: 'flex', gap: '1rem' }}>
          <button className="btn" style={{ flex: 1, backgroundColor: 'var(--bg-secondary)', border: '1px solid var(--border-color)' }}>
            Change Password
          </button>
          <button className="btn btn-danger" onClick={handleLogout} style={{ flex: 1 }}>
            Sign Out
          </button>
        </div>
      </div>
    </div>
  );
};

export default Profile;
