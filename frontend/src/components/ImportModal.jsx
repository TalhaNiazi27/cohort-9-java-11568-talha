import React, { useState, useRef } from 'react';
import api from '../api/axiosConfig';

const ImportModal = ({ onClose, onSuccess }) => {
  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const fileInputRef = useRef(null);

  const handleDragOver = (e) => {
    e.preventDefault();
  };

  const handleDrop = (e) => {
    e.preventDefault();
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      setFile(e.dataTransfer.files[0]);
    }
  };

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files.length > 0) {
      setFile(e.target.files[0]);
    }
  };

  const handleImport = async () => {
    if (!file) {
      setError('Please select a file to import.');
      return;
    }

    const formData = new FormData();
    formData.append('file', file);

    setLoading(true);
    setError(null);
    try {
      await api.post('/contacts/import', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      onSuccess();
    } catch (err) {
      console.error(err);
      setError(err.response?.data || 'Failed to import contacts. Please check the file format.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
      backgroundColor: 'rgba(0, 0, 0, 0.5)', display: 'flex',
      alignItems: 'center', justifyContent: 'center', zIndex: 1000, padding: '1rem'
    }}>
      <div className="glass-card animate-fade-in" style={{ width: '100%', maxWidth: '500px', backgroundColor: 'var(--bg-secondary)', padding: '2rem' }}>
        <h2 style={{ marginBottom: '1.5rem', color: 'var(--text-primary)' }}>Import Contacts</h2>
        
        {error && <div style={{ color: 'white', backgroundColor: 'var(--danger)', padding: '0.75rem', borderRadius: '8px', marginBottom: '1rem' }}>{error}</div>}
        
        <div 
          onDragOver={handleDragOver}
          onDrop={handleDrop}
          onClick={() => fileInputRef.current.click()}
          style={{
            border: '2px dashed var(--border-color)',
            borderRadius: '12px',
            padding: '3rem 2rem',
            textAlign: 'center',
            cursor: 'pointer',
            backgroundColor: 'var(--bg-primary)',
            transition: 'border-color 0.2s ease',
            marginBottom: '1.5rem'
          }}
        >
          <p style={{ color: 'var(--text-secondary)', marginBottom: '1rem' }}>
            {file ? file.name : 'Drag & drop a CSV or vCard file here, or click to browse'}
          </p>
          <input 
            type="file" 
            accept=".csv,.vcf" 
            ref={fileInputRef} 
            onChange={handleFileChange} 
            style={{ display: 'none' }} 
          />
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem' }}>
          <button className="btn" onClick={onClose} disabled={loading} style={{ backgroundColor: 'transparent', border: '1px solid var(--border-color)' }}>
            Cancel
          </button>
          <button className="btn btn-primary" onClick={handleImport} disabled={loading || !file}>
            {loading ? 'Importing...' : 'Import Contacts'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ImportModal;
