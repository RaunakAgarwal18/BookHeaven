import { useState } from 'react';
import { useSearchParams, Link, useNavigate } from 'react-router-dom';
import { KeyRound, Eye, EyeOff, Loader2, CheckCircle } from 'lucide-react';
import apiClient from '../api/axios';

const ResetPasswordPage = () => {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const navigate = useNavigate();

  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showNew, setShowNew] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState('');

  if (!token) {
    return (
      <div className="container" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 'calc(100vh - 80px)' }}>
        <div className="card glass-panel" style={{ width: '100%', maxWidth: '420px', textAlign: 'center' }}>
          <h2 style={{ color: 'var(--danger-color)', marginBottom: '1rem' }}>Invalid Link</h2>
          <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem' }}>This password reset link is invalid or has expired.</p>
          <Link to="/forgot-password" style={{ color: 'var(--primary-color)' }}>Request a new link</Link>
        </div>
      </div>
    );
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (newPassword !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }
    setLoading(true);
    setError('');
    try {
      await apiClient.post(`/user/auth/reset-password?token=${token}`, { newPassword, confirmPassword });
      setSuccess(true);
      setTimeout(() => navigate('/login'), 3000);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to reset password. The link may have expired.');
    } finally {
      setLoading(false);
    }
  };

  const eyeBtn = (visible, toggle) => (
    <button
      type="button"
      onClick={toggle}
      style={{ position: 'absolute', right: '0.75rem', top: '50%', transform: 'translateY(-50%)', background: 'transparent', border: 'none', padding: 0, color: 'var(--text-muted)', cursor: 'pointer', display: 'flex', alignItems: 'center' }}
    >
      {visible ? <EyeOff size={18} /> : <Eye size={18} />}
    </button>
  );

  return (
    <div className="container" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 'calc(100vh - 80px)' }}>
      <div className="card glass-panel" style={{ width: '100%', maxWidth: '420px' }}>
        <div style={{ textAlign: 'center', marginBottom: '1.5rem' }}>
          <div style={{ display: 'inline-flex', padding: '1rem', borderRadius: '50%', backgroundColor: 'rgba(59,130,246,0.1)', marginBottom: '1rem' }}>
            <KeyRound size={28} style={{ color: 'var(--primary-color)' }} />
          </div>
          <h2 style={{ marginBottom: '0.4rem' }}>Reset Password</h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
            Choose a strong new password for your account.
          </p>
        </div>

        {success ? (
          <div style={{ textAlign: 'center', padding: '1.5rem', backgroundColor: 'rgba(34,197,94,0.1)', borderRadius: 'var(--radius)', border: '1px solid rgba(34,197,94,0.3)', color: '#22c55e' }}>
            <CheckCircle size={36} style={{ marginBottom: '0.75rem' }} />
            <p style={{ fontWeight: '600', marginBottom: '0.4rem' }}>Password reset successfully!</p>
            <p style={{ fontSize: '0.9rem', color: 'var(--text-muted)' }}>Redirecting you to login...</p>
          </div>
        ) : (
          <form onSubmit={handleSubmit}>
            {error && (
              <div style={{ marginBottom: '1rem', padding: '0.75rem', backgroundColor: 'rgba(239,68,68,0.1)', borderRadius: 'var(--radius)', color: 'var(--danger-color)', fontSize: '0.9rem', textAlign: 'center' }}>
                {error}
              </div>
            )}

            <div className="form-group">
              <label className="form-label">New Password</label>
              <div style={{ position: 'relative' }}>
                <input
                  type={showNew ? 'text' : 'password'}
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  required
                  placeholder="Min 8 chars, upper, lower, digit, special"
                  style={{ paddingRight: '2.8rem' }}
                />
                {eyeBtn(showNew, () => setShowNew(p => !p))}
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Confirm New Password</label>
              <div style={{ position: 'relative' }}>
                <input
                  type={showConfirm ? 'text' : 'password'}
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  required
                  placeholder="Repeat your new password"
                  style={{ paddingRight: '2.8rem' }}
                />
                {eyeBtn(showConfirm, () => setShowConfirm(p => !p))}
              </div>
            </div>

            <p style={{ color: 'var(--text-muted)', fontSize: '0.8rem', marginBottom: '1rem' }}>
              Must be 8–128 characters and include uppercase, lowercase, a digit, and a special character (@$!%*?&#).
            </p>

            <button type="submit" disabled={loading} style={{ width: '100%', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '0.5rem' }}>
              {loading ? <Loader2 className="animate-spin" size={18} /> : <KeyRound size={18} />}
              {loading ? 'Resetting...' : 'Reset Password'}
            </button>
          </form>
        )}
      </div>
    </div>
  );
};

export default ResetPasswordPage;
