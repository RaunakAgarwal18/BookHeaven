import { Link } from 'react-router-dom';
import { CheckCircle } from 'lucide-react';

const OrderSuccessPage = () => {
  return (
    <div className="container mt-8 text-center" style={{ minHeight: '60vh', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
      <CheckCircle size={80} style={{ color: '#22c55e', marginBottom: '2rem' }} />
      <h1 style={{ fontSize: '2.5rem', marginBottom: '1rem', color: 'var(--text-color)' }}>
        Payment Successful!
      </h1>
      <p style={{ fontSize: '1.2rem', color: 'var(--text-muted)', maxWidth: '600px', marginBottom: '2rem' }}>
        Thank you for your purchase. Your order has been placed successfully and is now being processed. 
        You will receive an email confirmation shortly.
      </p>
      
      <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center' }}>
        <Link to="/" className="button" style={{ 
          backgroundColor: 'transparent', 
          border: '1px solid var(--border-color)', 
          color: 'var(--text-color)',
          padding: '0.8rem 1.5rem', 
          borderRadius: 'var(--radius)'
        }}>
          Continue Shopping
        </Link>
        <Link to="/orders" className="button" style={{ 
          backgroundColor: 'var(--primary-color)', 
          color: 'white', 
          border: 'none',
          padding: '0.8rem 1.5rem', 
          borderRadius: 'var(--radius)'
        }}>
          View My Orders
        </Link>
      </div>
    </div>
  );
};

export default OrderSuccessPage;
