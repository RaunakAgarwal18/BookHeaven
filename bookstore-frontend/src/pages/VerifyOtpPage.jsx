import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import apiClient from '../api/axios';

const VerifyOtpPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [otp, setOtp] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);

  const email = location.state?.email;
  const username = location.state?.username;

  useEffect(() => {
    if (!email || !username) {
      // If accessed directly without state, redirect back to signup
      navigate('/signup');
    }
  }, [email, username, navigate]);

  const handleVerify = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      await apiClient.post('/user/auth/verify-otp', {
        email,
        userName: username,
        otp
      });
      setSuccess(true);
      setTimeout(() => navigate('/login'), 2000);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to verify OTP. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (!email) return null;

  return (
    <div className="container" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 'calc(100vh - 80px)' }}>
      <div className="card glass-panel" style={{ width: '100%', maxWidth: '400px' }}>
        <h2 className="text-center mb-4">Verify Your Account</h2>
        <p className="text-center mb-4" style={{ color: 'var(--text-muted)' }}>
          We sent a verification code to <br /><strong>{email}</strong>
        </p>
        
        {error && <div className="form-error text-center mb-4" style={{ padding: '0.5rem', backgroundColor: 'rgba(239, 68, 68, 0.1)', borderRadius: '4px' }}>{error}</div>}
        {success && <div className="text-center mb-4" style={{ padding: '0.5rem', backgroundColor: 'rgba(16, 185, 129, 0.1)', color: 'var(--success-color)', borderRadius: '4px' }}>Account verified! Redirecting to login...</div>}
        
        {!success && (
          <form onSubmit={handleVerify}>
            <div className="form-group">
              <label className="form-label text-center">Enter 6-digit OTP</label>
              <input 
                type="text" 
                value={otp} 
                onChange={(e) => setOtp(e.target.value)} 
                required 
                maxLength={6}
                placeholder="123456"
                style={{ textAlign: 'center', fontSize: '1.5rem', letterSpacing: '0.5rem' }}
              />
            </div>
            
            <button type="submit" disabled={loading || otp.length < 4} style={{ width: '100%', marginTop: '1rem' }}>
              {loading ? 'Verifying...' : 'Verify OTP'}
            </button>
          </form>
        )}
      </div>
    </div>
  );
};

export default VerifyOtpPage;
