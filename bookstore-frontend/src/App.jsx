import { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import ContactFooter from './components/ContactFooter';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import VerifyOtpPage from './pages/VerifyOtpPage';
import CartPage from './pages/CartPage';
import BookDetailsPage from './pages/BookDetailsPage';
import OrderSuccessPage from './pages/OrderSuccessPage';
import ProfilePage from './pages/ProfilePage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import ResetPasswordPage from './pages/ResetPasswordPage';
import SellerDashboardPage from './pages/SellerDashboardPage';
import MyOrdersPage from './pages/MyOrdersPage';
import WishlistPage from './pages/WishlistPage';
import AdminCouponsPage from './pages/AdminCouponsPage';
import SellerCouponsPage from './pages/SellerCouponsPage';
import AdminDashboardPage from './pages/AdminDashboardPage';
import OAuth2CallbackPage from './pages/OAuth2CallbackPage';

function App() {
  const [rateLimitMessage, setRateLimitMessage] = useState(null);

  useEffect(() => {
    const handleRateLimit = (event) => {
      setRateLimitMessage(event.detail);
      setTimeout(() => setRateLimitMessage(null), 5000);
    };
    window.addEventListener("rate-limit-exceeded", handleRateLimit);
    return () => window.removeEventListener("rate-limit-exceeded", handleRateLimit);
  }, []);

  return (
    <Router>
      <Navbar />
      
      {/* Floating Rate Limit Warning Toast */}
      {rateLimitMessage && (
        <div style={{
          position: 'fixed',
          top: '5.5rem',
          left: '50%',
          transform: 'translateX(-50%)',
          zIndex: 10000,
          padding: '0.75rem 1.5rem',
          borderRadius: '12px',
          backgroundColor: 'rgba(239, 68, 68, 0.95)',
          border: '1px solid rgba(239, 68, 68, 0.4)',
          color: '#ffffff',
          fontWeight: '600',
          fontSize: '0.95rem',
          boxShadow: '0 10px 25px -5px rgba(0, 0, 0, 0.4)',
          backdropFilter: 'blur(8px)',
          display: 'flex',
          alignItems: 'center',
          gap: '0.5rem',
          animation: 'fadeInOut 5s ease-in-out forwards',
        }}>
          <span>⚠️</span>
          <span>{rateLimitMessage}</span>
        </div>
      )}

      <main>

        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
          <Route path="/verify-otp" element={<VerifyOtpPage />} />
          <Route path="/cart" element={<CartPage />} />
          <Route path="/books/:id" element={<BookDetailsPage />} />
          <Route path="/order-success" element={<OrderSuccessPage />} />
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route path="/reset-password" element={<ResetPasswordPage />} />
          <Route path="/seller/dashboard" element={<SellerDashboardPage />} />
          <Route path="/seller/coupons" element={<SellerCouponsPage />} />
          <Route path="/orders" element={<MyOrdersPage />} />
          <Route path="/wishlist" element={<WishlistPage />} />
          <Route path="/admin/dashboard" element={<AdminDashboardPage />} />
          <Route path="/admin/coupons" element={<AdminCouponsPage />} />
          <Route path="/oauth2/callback" element={<OAuth2CallbackPage />} />
        </Routes>
      </main>
      
      <ContactFooter />
    </Router>
  );
}

export default App;
