import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import { ContactModal, DeleteConfirmModal } from '../components/ContactModals';

const Dashboard = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  
  const [contacts, setContacts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  
  // Modal states
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [contactToEdit, setContactToEdit] = useState(null);
  const [contactToDelete, setContactToDelete] = useState(null);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const fetchContacts = async (query = '', page = 0, isActive = { current: true }) => {
    try {
      setLoading(true);
      setError(null);
      let url = `/contacts?page=${page}&size=10`;
      if (query.trim() !== '') {
        url = `/contacts/search?q=${encodeURIComponent(query)}&page=${page}&size=10`;
      }
      const response = await api.get(url);
      
      if (!isActive.current) return;

      const data = response.data.content || response.data;
      setContacts(data);
      
      if (response.data.totalElements !== undefined) {
        setTotalElements(response.data.totalElements);
        setTotalPages(response.data.totalPages);
      } else {
        setTotalElements(data.length);
        setTotalPages(1);
      }
    } catch (err) {
      if (!isActive.current) return;
      console.error('Error fetching contacts:', err);
      setError('Failed to load contacts. Please check your connection.');
    } finally {
      if (isActive.current) {
        setLoading(false);
      }
    }
  };

  // Coordinated effect for search and pagination
  useEffect(() => {
    let isActive = { current: true };
    
    const delayDebounceFn = setTimeout(() => {
      fetchContacts(searchQuery, currentPage, isActive);
    }, 300);

    return () => {
      clearTimeout(delayDebounceFn);
      isActive.current = false;
    };
  }, [searchQuery, currentPage]);

  const handleAddClick = () => {
    setContactToEdit(null);
    setIsModalOpen(true);
  };

  const handleEditClick = (contact) => {
    setContactToEdit(contact);
    setIsModalOpen(true);
  };

  const handleDeleteClick = (contact) => {
    setContactToDelete(contact);
    setIsDeleteModalOpen(true);
  };

  const confirmDelete = async () => {
    if (!contactToDelete) return;
    try {
      await api.delete(`/contacts/${contactToDelete.id}`);
      fetchContacts(searchQuery, currentPage); // Refresh list and preserve active view
    } catch (err) {
      console.error('Error deleting contact:', err);
    } finally {
      setIsDeleteModalOpen(false);
      setContactToDelete(null);
    }
  };

  const workCount = contacts.filter(c => c.emails?.some(e => e.label === 'Work') || c.phones?.some(p => p.label === 'Work')).length;
  const personalCount = contacts.filter(c => c.emails?.some(e => e.label === 'Personal') || c.phones?.some(p => p.label === 'Personal')).length;
  const familyCount = contacts.filter(c => c.emails?.some(e => e.label === 'Family') || c.phones?.some(p => p.label === 'Family')).length;

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      {/* Navbar */}
      <nav style={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center', 
        padding: '1rem 2rem',
        borderBottom: '1px solid var(--border-color)',
        backgroundColor: 'var(--bg-secondary)'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <h1 style={{ fontSize: '1.5rem', fontWeight: 'bold', color: 'var(--accent-primary)' }}>
            Lumina
          </h1>
        </div>
        <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
          <button className="btn" onClick={() => navigate('/profile')}>My Profile</button>
          <button className="btn btn-danger" onClick={handleLogout}>Sign Out</button>
        </div>
      </nav>

      {/* Main Content Area */}
      <main style={{ padding: '2rem', flex: 1, display: 'flex', flexDirection: 'column', gap: '2rem' }}>
        
        {/* Metrics/Widgets Section */}
        <section style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '1.5rem' }}>
          <div className="glass-card animate-fade-in" style={{ padding: '1.5rem' }}>
            <h3 style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>Total Contacts</h3>
            <p style={{ fontSize: '2rem', fontWeight: 'bold', marginTop: '0.5rem' }}>
              {loading ? '...' : totalElements}
            </p>
          </div>
          
          <div className="glass-card animate-fade-in" style={{ padding: '1.5rem', animationDelay: '0.1s' }}>
            <h3 style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>Labels</h3>
            <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem', flexWrap: 'wrap' }}>
              <span style={{ padding: '0.25rem 0.75rem', backgroundColor: 'var(--accent-light)', color: 'var(--accent-primary)', borderRadius: '999px', fontSize: '0.75rem', fontWeight: '500' }}>Work: {workCount}</span>
              <span style={{ padding: '0.25rem 0.75rem', backgroundColor: 'var(--accent-light)', color: 'var(--accent-primary)', borderRadius: '999px', fontSize: '0.75rem', fontWeight: '500' }}>Personal: {personalCount}</span>
              <span style={{ padding: '0.25rem 0.75rem', backgroundColor: 'var(--accent-light)', color: 'var(--accent-primary)', borderRadius: '999px', fontSize: '0.75rem', fontWeight: '500' }}>Family: {familyCount}</span>
            </div>
          </div>
        </section>

        {/* Contact Table \u0026 Actions */}
        <section className="glass-card animate-fade-in" style={{ flex: 1, display: 'flex', flexDirection: 'column', animationDelay: '0.2s' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
            <h2>Contacts</h2>
            
            <div style={{ display: 'flex', gap: '1rem' }}>
              <input 
                type="text" 
                placeholder="Search contacts..." 
                className="input-field"
                style={{ width: '250px', marginBottom: 0 }}
                value={searchQuery}
                onChange={(e) => {
                  setSearchQuery(e.target.value);
                  setCurrentPage(0); // Reset page on new search
                }}
              />
              <button className="btn btn-primary" onClick={handleAddClick}>
                + Add Contact
              </button>
            </div>
          </div>

          {/* Table Placeholder */}
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
              <thead>
                <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-secondary)' }}>
                  <th style={{ padding: '1rem' }}>Name</th>
                  <th style={{ padding: '1rem' }}>Email</th>
                  <th style={{ padding: '1rem' }}>Phone</th>
                  <th style={{ padding: '1rem' }}>Label</th>
                  <th style={{ padding: '1rem', textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr>
                    <td colSpan="5" style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-secondary)' }}>
                      Loading contacts...
                    </td>
                  </tr>
                ) : error ? (
                  <tr>
                    <td colSpan="5" style={{ padding: '2rem', textAlign: 'center', color: 'var(--danger)' }}>
                      <p style={{ marginBottom: '1rem' }}>{error}</p>
                      <button className="btn btn-primary" onClick={() => fetchContacts(searchQuery, currentPage)}>
                        Retry
                      </button>
                    </td>
                  </tr>
                ) : contacts.length === 0 ? (
                  <tr>
                    <td colSpan="5" style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-secondary)' }}>
                      No contacts found. Click "Add Contact" to get started!
                    </td>
                  </tr>
                ) : (
                  contacts.map((contact) => (
                    <tr key={contact.id} style={{ borderBottom: '1px solid var(--glass-border)' }}>
                      <td style={{ padding: '1rem' }}>{contact.name || `${contact.firstName} ${contact.lastName}`}</td>
                      <td style={{ padding: '1rem' }}>{contact.emails && contact.emails.length > 0 ? contact.emails[0].emailAddress : ''}</td>
                      <td style={{ padding: '1rem' }}>{contact.phones && contact.phones.length > 0 ? contact.phones[0].phoneNumber : ''}</td>
                      <td style={{ padding: '1rem' }}>
                        <span style={{ padding: '0.25rem 0.5rem', backgroundColor: 'var(--accent-light)', color: 'var(--accent-primary)', borderRadius: '4px', fontSize: '0.75rem' }}>
                          {(contact.emails && contact.emails.length > 0) ? contact.emails[0].label : ((contact.phones && contact.phones.length > 0) ? contact.phones[0].label : 'None')}
                        </span>
                      </td>
                      <td style={{ padding: '1rem', textAlign: 'right' }}>
                        <button className="btn" onClick={() => handleEditClick(contact)} style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem', marginRight: '0.5rem' }}>Edit</button>
                        <button className="btn btn-danger" onClick={() => handleDeleteClick(contact)} style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem' }}>Delete</button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
          
          {/* Pagination */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '1.5rem' }}>
            <span style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
              Showing page {currentPage + 1} of {totalPages} ({totalElements} total contacts)
            </span>
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              <button 
                className="btn" 
                onClick={() => setCurrentPage(prev => Math.max(0, prev - 1))}
                disabled={currentPage === 0 || loading}
                style={{ backgroundColor: 'transparent', border: '1px solid var(--border-color)' }}
              >
                Previous
              </button>
              <button 
                className="btn" 
                onClick={() => setCurrentPage(prev => Math.min(totalPages - 1, prev + 1))}
                disabled={currentPage >= totalPages - 1 || loading}
                style={{ backgroundColor: 'transparent', border: '1px solid var(--border-color)' }}
              >
                Next
              </button>
            </div>
          </div>
        </section>

      </main>

      {/* Modals */}
      <ContactModal 
        isOpen={isModalOpen} 
        onClose={() => setIsModalOpen(false)} 
        onSaved={() => fetchContacts(searchQuery, currentPage)}
        contactToEdit={contactToEdit}
      />
      
      <DeleteConfirmModal 
        isOpen={isDeleteModalOpen}
        onClose={() => setIsDeleteModalOpen(false)}
        onConfirm={confirmDelete}
        contactName={contactToDelete ? (contactToDelete.name || `${contactToDelete.firstName} ${contactToDelete.lastName}`) : ''}
      />
    </div>
  );
};

export default Dashboard;
