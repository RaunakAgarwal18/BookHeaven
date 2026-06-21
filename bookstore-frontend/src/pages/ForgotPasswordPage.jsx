import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Mail, ArrowLeft, Loader2 } from 'lucide-react';
import apiClient from '../api/axios';

const ForgotPasswordPage = () => {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      await apiClient.post('/user/auth/forgot-password', { email });
      setSuccess(true);
    } catch (err) {
      setError(err.response?.data?.message || 'Something went wrong. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 'calc(100vh - 80px)' }}>
      <div className="card glass-panel" style={{ width: '100%', maxWidth: '420px' }}>
        <Link to="/login" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem', color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '1.5rem' }}>
          <ArrowLeft size={16} /> Back to Login
        </Link>

        <div style={{ textAlign: 'center', marginBottom: '1.5rem' }}>
          <div style={{ display: 'inline-flex', padding: '1rem', borderRadius: '50%', backgroundColor: 'rgba(59,130,246,0.1)', marginBottom: '1rem' }}>
            <Mail size={28} style={{ color: 'var(--primary-color)' }} />
          </div>
          <h2 style={{ marginBottom: '0.4rem' }}>Forgot Password?</h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
            Enter your email address and we'll send you a link to reset your password.
          </p>
        </div>

        {success ? (
          <div style={{ textAlign: 'center', padding: '1.5rem', backgroundColor: 'rgba(34,197,94,0.1)', borderRadius: 'var(--radius)', border: '1px solid rgba(34,197,94,0.3)', color: '#22c55e' }}>
            <p style={{ fontWeight: '600', marginBottom: '0.5rem' }}>Check your inbox!</p>
            <p style={{ fontSize: '0.9rem', color: 'var(--text-muted)' }}>
              A password reset link has been sent to <strong style={{ color: 'var(--text-color)' }}>{email}</strong>.
            </p>
          </div>
        ) : (
          <form onSubmit={handleSubmit}>
            {error && (
              <div style={{ marginBottom: '1rem', padding: '0.75rem', backgroundColor: 'rgba(239,68,68,0.1)', borderRadius: 'var(--radius)', color: 'var(--danger-color)', fontSize: '0.9rem', textAlign: 'center' }}>
                {error}
              </div>
            )}
            <div className="form-group">
              <label className="form-label">Email Address</label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                placeholder="Enter your registered email"
                autoFocus
              />
            </div>
            <button type="submit" disabled={loading} style={{ width: '100%', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '0.5rem', marginTop: '0.5rem' }}>
              {loading ? <Loader2 className="animate-spin" size={18} /> : <Mail size={18} />}
              {loading ? 'Sending...' : 'Send Reset Link'}
            </button>
          </form>
        )}
      </div>
    </div>
  );
};

export default ForgotPasswordPage;
