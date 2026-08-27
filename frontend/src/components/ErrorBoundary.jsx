import React from 'react';

class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    // Update state so the next render will show the fallback UI.
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    // You can also log the error to an error reporting service here
    console.error('ErrorBoundary caught an error:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      // You can render any custom fallback UI
      return (
        <div style={{ padding: '2rem', textAlign: 'center', fontFamily: 'system-ui, sans-serif' }}>
          <h2>Something went wrong.</h2>
          <p style={{ color: 'red', marginTop: '1rem' }}>
            {this.state.error?.toString()}
          </p>
          <button 
            onClick={() => window.location.reload()} 
            style={{ 
              padding: '0.75rem 1.5rem', 
              marginTop: '1.5rem', 
              cursor: 'pointer',
              backgroundColor: '#3b82f6',
              color: 'white',
              border: 'none',
              borderRadius: '0.375rem',
              fontWeight: '600'
            }}
          >
            Reload Page
          </button>
        </div>
      );
    }

    return this.props.children; 
  }
}

export default ErrorBoundary;
