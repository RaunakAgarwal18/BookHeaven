import { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, useLocation } from 'react-router-dom';
import { AnimatePresence, motion } from 'framer-motion';
import Navbar from './components/Navbar';
import ContactFooter from './components/ContactFooter';
import SetPasswordModal from './components/SetPasswordModal';
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

const PageWrapper = ({ children }) => {
  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -15 }}
      transition={{ duration: 0.3 }}
      style={{ display: 'flex', flexDirection: 'column', flex: 1 }}
    >
      {children}
    </motion.div>
  );
};

const AnimatedRoutes = () => {
  const location = useLocation();
  return (
    <AnimatePresence mode="wait">
      <Routes location={location} key={location.pathname}>
        <Route path="/" element={<PageWrapper><HomePage /></PageWrapper>} />
        <Route path="/login" element={<PageWrapper><LoginPage /></PageWrapper>} />
        <Route path="/signup" element={<PageWrapper><SignupPage /></PageWrapper>} />
        <Route path="/verify-otp" element={<PageWrapper><VerifyOtpPage /></PageWrapper>} />
        <Route path="/cart" element={<PageWrapper><CartPage /></PageWrapper>} />
        <Route path="/books/:id" element={<PageWrapper><BookDetailsPage /></PageWrapper>} />
        <Route path="/order-success" element={<PageWrapper><OrderSuccessPage /></PageWrapper>} />
        <Route path="/profile" element={<PageWrapper><ProfilePage /></PageWrapper>} />
        <Route path="/forgot-password" element={<PageWrapper><ForgotPasswordPage /></PageWrapper>} />
        <Route path="/reset-password" element={<PageWrapper><ResetPasswordPage /></PageWrapper>} />
        <Route path="/seller/dashboard" element={<PageWrapper><SellerDashboardPage /></PageWrapper>} />
        <Route path="/seller/coupons" element={<PageWrapper><SellerCouponsPage /></PageWrapper>} />
        <Route path="/orders" element={<PageWrapper><MyOrdersPage /></PageWrapper>} />
        <Route path="/wishlist" element={<PageWrapper><WishlistPage /></PageWrapper>} />
        <Route path="/admin/dashboard" element={<PageWrapper><AdminDashboardPage /></PageWrapper>} />
        <Route path="/admin/coupons" element={<PageWrapper><AdminCouponsPage /></PageWrapper>} />
        <Route path="/oauth2/callback" element={<PageWrapper><OAuth2CallbackPage /></PageWrapper>} />
      </Routes>
    </AnimatePresence>
  );
};

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
        <AnimatedRoutes />
      </main>
      
      <SetPasswordModal />
      <ContactFooter />
    </Router>
  );
}

export default App;
