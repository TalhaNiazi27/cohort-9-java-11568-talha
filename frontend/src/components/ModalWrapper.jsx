import React, { useEffect, useRef } from 'react';

const ModalWrapper = ({ onClose, children }) => {
  const dialogRef = useRef(null);

  useEffect(() => {
    const dialog = dialogRef.current;
    if (dialog && !dialog.open) {
      dialog.showModal();
    }
    return () => {
      if (dialog && dialog.open) {
        dialog.close();
      }
    };
  }, []);

  const handleCancel = (e) => {
    e.preventDefault();
    if (onClose) onClose();
  };

  return (
    <dialog
      ref={dialogRef}
      onCancel={handleCancel}
      aria-labelledby="modal-title"
      style={{
        padding: 0,
        border: 'none',
        background: 'transparent',
        width: '100%',
        height: '100%',
        margin: 0,
        maxWidth: 'none',
        maxHeight: 'none',
        overflow: 'hidden',
        color: 'var(--text-primary)'
      }}
    >
      <div style={{
        position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
        backgroundColor: 'rgba(0, 0, 0, 0.5)', display: 'flex',
        alignItems: 'center', justifyContent: 'center', zIndex: 1000, padding: '1rem'
      }}>
        {children}
      </div>
    </dialog>
  );
};

export default ModalWrapper;
