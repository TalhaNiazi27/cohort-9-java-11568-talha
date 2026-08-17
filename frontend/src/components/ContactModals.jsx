import React, { useState, useEffect } from 'react';
import api from '../api/axiosConfig';
import ModalWrapper from './ModalWrapper';

export const ContactModal = ({ isOpen, onClose, onSaved, contactToEdit }) => {
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    label: 'Personal' // Default label
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (contactToEdit) {
      setFormData({
        firstName: contactToEdit.firstName || '',
        lastName: contactToEdit.lastName || '',
        title: contactToEdit.title || '',
        email: (contactToEdit.emails && contactToEdit.emails.length > 0) ? contactToEdit.emails[0].emailAddress : '',
        phone: (contactToEdit.phones && contactToEdit.phones.length > 0) ? contactToEdit.phones[0].phoneNumber : '',
        label: (contactToEdit.emails && contactToEdit.emails.length > 0) ? contactToEdit.emails[0].label : ((contactToEdit.phones && contactToEdit.phones.length > 0) ? contactToEdit.phones[0].label : 'Personal')
      });
    } else {
      setFormData({ firstName: '', lastName: '', title: '', email: '', phone: '', label: 'Personal' });
    }
    setError(null);
  }, [contactToEdit, isOpen]);

  if (!isOpen) return null;

  const handleChange = (e) => {
    const { name, value } = e.target;
    if (name === 'phone') {
      // Only allow digits and plus sign
      const sanitized = value.replace(/[^0-9+]/g, '');
      setFormData(prev => ({ ...prev, [name]: sanitized }));
    } else {
      setFormData(prev => ({ ...prev, [name]: value }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      let emails = contactToEdit ? [...(contactToEdit.emails || [])] : [];
      if (formData.email) {
        if (emails.length > 0) {
          emails[0] = { ...emails[0], emailAddress: formData.email, label: formData.label };
        } else {
          emails.push({ emailAddress: formData.email, label: formData.label });
        }
      } else if (emails.length > 0) {
        emails.shift();
      }

      let phones = contactToEdit ? [...(contactToEdit.phones || [])] : [];
      if (formData.phone) {
        if (phones.length > 0) {
          phones[0] = { ...phones[0], phoneNumber: formData.phone, label: formData.label };
        } else {
          phones.push({ phoneNumber: formData.phone, label: formData.label });
        }
      } else if (phones.length > 0) {
        phones.shift();
      }

      const payload = {
        firstName: formData.firstName,
        lastName: formData.lastName,
        title: formData.title,
        emails,
        phones
      };

      if (contactToEdit) {
        await api.put(`/contacts/${contactToEdit.id}`, payload);
      } else {
        await api.post('/contacts', payload);
      }
      onSaved();
      onClose();
    } catch (err) {
      console.error('Error saving contact:', err);
      setError(err.response?.data?.message || 'Failed to save contact. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <ModalWrapper onClose={onClose}>
      <div className="glass-card animate-fade-in" style={{ width: '100%', maxWidth: '500px', backgroundColor: 'var(--modal-bg)' }}>
        <h2 id="modal-title" style={{ marginBottom: '1.5rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '1rem' }}>
          {contactToEdit ? 'Edit Contact' : 'Add New Contact'}
        </h2>
        
        {error && <div style={{ color: 'white', backgroundColor: 'var(--danger)', padding: '0.75rem', borderRadius: '8px', marginBottom: '1rem' }}>{error}</div>}
        
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <div style={{ display: 'flex', gap: '1rem' }}>
            <div className="input-group" style={{ flex: 1, marginBottom: 0 }}>
              <label className="input-label">First Name</label>
              <input type="text" name="firstName" className="input-field" value={formData.firstName} onChange={handleChange} required />
            </div>
            <div className="input-group" style={{ flex: 1, marginBottom: 0 }}>
              <label className="input-label">Last Name</label>
              <input type="text" name="lastName" className="input-field" value={formData.lastName} onChange={handleChange} />
            </div>
          </div>
          
          <div className="input-group" style={{ marginBottom: 0 }}>
            <label className="input-label">Job Title</label>
            <input type="text" name="title" className="input-field" value={formData.title} onChange={handleChange} placeholder="e.g. Software Engineer" />
          </div>
          
          <div className="input-group" style={{ marginBottom: 0 }}>
            <label className="input-label">Email</label>
            <input type="email" name="email" className="input-field" value={formData.email} onChange={handleChange} />
          </div>
          
          <div className="input-group" style={{ marginBottom: 0 }}>
            <label className="input-label">Phone Number</label>
            <input type="tel" name="phone" className="input-field" value={formData.phone} onChange={handleChange} />
          </div>
          
          <div className="input-group" style={{ marginBottom: 0 }}>
            <label className="input-label">Label</label>
            <select name="label" className="input-field" value={formData.label} onChange={handleChange}>
              <option value="Personal">Personal</option>
              <option value="Work">Work</option>
              <option value="Family">Family</option>
            </select>
          </div>
          
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '1rem' }}>
            <button type="button" className="btn" onClick={onClose} disabled={loading} style={{ backgroundColor: 'transparent', border: '1px solid var(--border-color)' }}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Saving...' : 'Save Contact'}
            </button>
          </div>
        </form>
      </div>
    </ModalWrapper>
  );
};

export const DeleteConfirmModal = ({ isOpen, onClose, onConfirm, contactName }) => {
  if (!isOpen) return null;

  return (
    <ModalWrapper onClose={onClose}>
      <div className="glass-card animate-fade-in" style={{ width: '100%', maxWidth: '400px', backgroundColor: 'var(--modal-bg)', textAlign: 'center' }}>
        <h2 id="modal-title" style={{ marginBottom: '1rem', color: 'var(--danger)' }}>Delete Contact</h2>
        <p style={{ marginBottom: '2rem', color: 'var(--text-secondary)' }}>
          Are you sure you want to delete <strong>{contactName}</strong>? This action cannot be undone.
        </p>
        <div style={{ display: 'flex', justifyContent: 'center', gap: '1rem' }}>
          <button className="btn" onClick={onClose} style={{ backgroundColor: 'transparent', border: '1px solid var(--border-color)' }}>
            Cancel
          </button>
          <button className="btn btn-danger" onClick={onConfirm}>
            Yes, Delete
          </button>
        </div>
      </div>
    </ModalWrapper>
  );
};

export const ChangePasswordModal = ({ isOpen, onClose }) => {
  const [passwords, setPasswords] = useState({ oldPassword: '', newPassword: '', confirmPassword: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  if (!isOpen) return null;

  const handleChange = (e) => {
    setPasswords({ ...passwords, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (passwords.newPassword !== passwords.confirmPassword) {
      setError("New passwords do not match.");
      return;
    }
    
    setLoading(true);
    setError(null);
    try {
      await api.post('/auth/change-password', { 
        currentPassword: passwords.oldPassword, 
        newPassword: passwords.newPassword 
      });
      setSuccess(true);
      setTimeout(() => {
        onClose();
        setSuccess(false);
        setPasswords({ oldPassword: '', newPassword: '', confirmPassword: '' });
      }, 2000);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to change password.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <ModalWrapper onClose={onClose}>
      <div className="glass-card animate-fade-in" style={{ width: '100%', maxWidth: '400px', backgroundColor: 'var(--modal-bg)' }}>
        <h2 id="modal-title" style={{ marginBottom: '1.5rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '1rem' }}>
          Change Password
        </h2>
        
        {error && <div style={{ color: 'white', backgroundColor: 'var(--danger)', padding: '0.75rem', borderRadius: '8px', marginBottom: '1rem' }}>{error}</div>}
        {success && <div style={{ color: 'white', backgroundColor: 'var(--success)', padding: '0.75rem', borderRadius: '8px', marginBottom: '1rem' }}>Password updated successfully!</div>}
        
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <div className="input-group" style={{ marginBottom: 0 }}>
            <label className="input-label">Current Password</label>
            <input type="password" name="oldPassword" className="input-field" value={passwords.oldPassword} onChange={handleChange} required />
          </div>
          <div className="input-group" style={{ marginBottom: 0 }}>
            <label className="input-label">New Password</label>
            <input type="password" name="newPassword" className="input-field" value={passwords.newPassword} onChange={handleChange} required minLength={6} />
          </div>
          <div className="input-group" style={{ marginBottom: 0 }}>
            <label className="input-label">Confirm New Password</label>
            <input type="password" name="confirmPassword" className="input-field" value={passwords.confirmPassword} onChange={handleChange} required minLength={6} />
          </div>
          
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '1rem' }}>
            <button type="button" className="btn" onClick={onClose} disabled={loading} style={{ backgroundColor: 'transparent', border: '1px solid var(--border-color)' }}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading || success}>
              {loading ? 'Updating...' : 'Update Password'}
            </button>
          </div>
        </form>
      </div>
    </ModalWrapper>
  );
};
