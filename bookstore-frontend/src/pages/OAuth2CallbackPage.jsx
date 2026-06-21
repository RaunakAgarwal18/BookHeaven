import { useEffect, useState, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import apiClient from '../api/axios';
import useAuthStore from '../store/useAuthStore';
import useCartStore from '../store/useCartStore';

const OAuth2CallbackPage = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const login = useAuthStore(state => state.login);
  const fetchCart = useCartStore(state => state.fetchCart);
  const [error, setError] = useState('');

  const hasProcessed = useRef(false);

  useEffect(() => {
    if (hasProcessed.current) return;
    
    const code = searchParams.get('code');
    const state = searchParams.get('state'); // contains the provider name

    if (!code || !state) {
      setError('Invalid OAuth callback. Missing code or provider.');
      return;
    }

    hasProcessed.current = true;

    const processCallback = async () => {
      try {
        const response = await apiClient.post('/user/auth/oauth2/callback', {
          provider: state,
          code: code,
        });

        const authData = response.data.data || response.data;
        login({ email: authData.user.email, ...authData.user });
        await fetchCart();
        navigate('/');
      } catch (err) {
        console.error('OAuth2 callback error:', err);
        setError(err.response?.data?.message || 'Social login failed. Please try again.');
        hasProcessed.current = false; // Allow retry on error
      }
    };

    processCallback();
  }, [searchParams, navigate, login, fetchCart]);

  if (error) {
    return (
      <div className="container" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 'calc(100vh - 80px)' }}>
        <div className="card glass-panel" style={{ width: '100%', maxWidth: '400px', textAlign: 'center' }}>
          <h3 style={{ color: '#ef4444' }}>Login Failed</h3>
          <p style={{ color: 'var(--text-muted)', marginTop: '1rem' }}>{error}</p>
          <button onClick={() => navigate('/login')} style={{ marginTop: '1.5rem', width: '100%' }}>
            Back to Login
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="container" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 'calc(100vh - 80px)' }}>
      <div className="card glass-panel" style={{ textAlign: 'center', padding: '3rem' }}>
        <div style={{
          width: '40px', height: '40px', border: '3px solid var(--border-color)',
          borderTop: '3px solid var(--primary-color)', borderRadius: '50%',
          animation: 'spin 1s linear infinite', margin: '0 auto 1rem'
        }} />
        <p style={{ color: 'var(--text-muted)' }}>Completing sign in...</p>
      </div>
    </div>
  );
};

export default OAuth2CallbackPage;
