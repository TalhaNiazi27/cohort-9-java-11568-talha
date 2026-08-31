import React, { useState, useRef } from 'react';
import api from '../api/axiosConfig';
import ModalWrapper from './ModalWrapper';

const ImportModal = ({ onClose, onSuccess }) => {
  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const fileInputRef = useRef(null);

  const ACCEPTED_EXTENSIONS = ['.csv', '.vcf'];

  const selectFile = (candidate) => {
    const name = candidate.name.toLowerCase();
    if (!ACCEPTED_EXTENSIONS.some(ext => name.endsWith(ext))) {
      setFile(null);
      setError('Select a .csv or .vcf file.');
      return;
    }
    setError(null);
    setFile(candidate);
  };

  const handleDragOver = (e) => {
    e.preventDefault();
  };

  const handleDrop = (e) => {
    e.preventDefault();
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      selectFile(e.dataTransfer.files[0]);
    }
  };

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files.length > 0) {
      selectFile(e.target.files[0]);
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
      const data = err.response?.data;
      const message = typeof data === 'string' ? data : data?.message;
      setError(message || 'Failed to import contacts. Please check the file format.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <ModalWrapper onClose={onClose}>
      <div className="glass-card animate-fade-in" style={{ width: '100%', maxWidth: '500px', backgroundColor: 'var(--modal-bg)', padding: '2rem' }}>
        <h2 id="modal-title" style={{ marginBottom: '1.5rem', color: 'var(--text-primary)' }}>Import Contacts</h2>
        
        {error && <div style={{ color: 'white', backgroundColor: 'var(--danger)', padding: '0.75rem', borderRadius: '8px', marginBottom: '1rem' }}>{error}</div>}
        
        <div 
          role="button"
          tabIndex={0}
          aria-label="Select a CSV or vCard file to import"
          onDragOver={handleDragOver}
          onDrop={handleDrop}
          onClick={() => fileInputRef.current.click()}
          onKeyDown={(e) => {
            if (e.key === 'Enter' || e.key === ' ') {
              e.preventDefault();
              fileInputRef.current.click();
            }
          }}
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
    </ModalWrapper>
  );
};

export default ImportModal;
