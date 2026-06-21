import React, { useState } from 'react';
import { useStripe, useElements, PaymentElement } from '@stripe/react-stripe-js';
import { Loader2 } from 'lucide-react';

const StripeCheckoutForm = ({ onSuccess, onCancel, amount, currency }) => {
  const stripe = useStripe();
  const elements = useElements();
  const [errorMessage, setErrorMessage] = useState(null);
  const [isProcessing, setIsProcessing] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!stripe || !elements) {
      // Stripe.js has not yet loaded.
      return;
    }

    setIsProcessing(true);
    setErrorMessage(null);

    const { error } = await stripe.confirmPayment({
      elements,
      redirect: 'if_required', // Avoid redirect if possible
      confirmParams: {
        // Return URL is required even with redirect: 'if_required' in some cases
        return_url: window.location.origin + '/order-success',
      },
    });

    if (error) {
      setErrorMessage(error.message);
      setIsProcessing(false);
    } else {
      // Payment succeeded!
      onSuccess();
    }
  };

  return (
    <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '2rem', marginTop: '1rem' }}>
      <PaymentElement />
      {errorMessage && (
        <div style={{ 
          color: 'var(--danger-color)', 
          fontSize: '0.9rem', 
          backgroundColor: 'rgba(239, 68, 68, 0.1)', 
          border: '1px solid rgba(239, 68, 68, 0.2)',
          padding: '1rem', 
          borderRadius: '8px',
          display: 'flex',
          alignItems: 'center',
          gap: '0.5rem'
        }}>
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
          {errorMessage}
        </div>
      )}
      <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end', marginTop: '0.5rem' }}>
        <button 
          type="button" 
          onClick={onCancel} 
          disabled={isProcessing}
          style={{ 
            padding: '0.75rem 1.5rem', 
            background: 'rgba(255,255,255,0.05)', 
            color: 'var(--text-color)', 
            border: '1px solid var(--border-color)', 
            borderRadius: 'var(--radius)',
            transition: 'all 0.2s ease',
            cursor: isProcessing ? 'not-allowed' : 'pointer'
          }}
          onMouseOver={(e) => { if (!isProcessing) { e.currentTarget.style.background = 'rgba(255,255,255,0.1)'; e.currentTarget.style.borderColor = '#475569'; } }}
          onMouseOut={(e) => { if (!isProcessing) { e.currentTarget.style.background = 'rgba(255,255,255,0.05)'; e.currentTarget.style.borderColor = 'var(--border-color)'; } }}
        >
          Cancel
        </button>
        <button 
          type="submit" 
          disabled={!stripe || isProcessing}
          style={{ 
            padding: '0.75rem 2rem', 
            display: 'flex', 
            alignItems: 'center', 
            justifyContent: 'center', 
            minWidth: '150px',
            background: 'linear-gradient(135deg, #3b82f6 0%, #2563eb 100%)',
            color: 'white',
            border: 'none',
            borderRadius: 'var(--radius)',
            fontWeight: '600',
            boxShadow: '0 4px 14px 0 rgba(59, 130, 246, 0.39)',
            transition: 'all 0.2s ease',
            cursor: (!stripe || isProcessing) ? 'not-allowed' : 'pointer',
            opacity: (!stripe || isProcessing) ? 0.7 : 1
          }}
          onMouseOver={(e) => { if (stripe && !isProcessing) { e.currentTarget.style.transform = 'translateY(-2px)'; e.currentTarget.style.boxShadow = '0 6px 20px rgba(59, 130, 246, 0.5)'; } }}
          onMouseOut={(e) => { if (stripe && !isProcessing) { e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = '0 4px 14px 0 rgba(59, 130, 246, 0.39)'; } }}
        >
          {isProcessing ? <Loader2 className="animate-spin" size={20} /> : `Pay ${currency === 'usd' ? '$' : currency} ${Number(amount || 0).toFixed(2)}`}
        </button>
      </div>
    </form>
  );
};

export default StripeCheckoutForm;
