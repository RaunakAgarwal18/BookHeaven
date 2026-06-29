import { useState, useRef, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ShoppingCart, User, LogOut, Package, ChevronDown, Heart, Tag, LayoutDashboard } from 'lucide-react';
import { motion, useScroll, useTransform } from 'framer-motion';
import useAuthStore from '../store/useAuthStore';
import useCartStore from '../store/useCartStore';

const MotionLink = motion(Link);

const Navbar = () => {
  const { isAuthenticated, user, logout } = useAuthStore();
  const { cart, clearCartState } = useCartStore();
  const navigate = useNavigate();
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const dropdownRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsDropdownOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const handleLogout = () => {
    logout();
    clearCartState();
    setIsDropdownOpen(false);
    navigate('/login');
  };

  const cartItemCount = cart?.items?.reduce((total, item) => total + item.quantity, 0) || 0;

  const { scrollY } = useScroll();
  const navBackground = useTransform(scrollY, [0, 50], ['rgba(15, 23, 42, 0)', 'rgba(15, 23, 42, 0.8)']);
  const navBackdropFilter = useTransform(scrollY, [0, 50], ['blur(0px)', 'blur(12px)']);
  const navBorder = useTransform(scrollY, [0, 50], ['1px solid transparent', '1px solid var(--border-color)']);

  return (
    <motion.nav style={{
      padding: '1rem 3rem',
      backgroundColor: navBackground,
      backdropFilter: navBackdropFilter,
      WebkitBackdropFilter: navBackdropFilter,
      borderBottom: navBorder,
      position: 'sticky',
      top: 0,
      zIndex: 1000,
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center'
    }}>
      <MotionLink 
          to="/" 
          style={{ 
            fontSize: '1.75rem', 
            fontWeight: 'bold', 
            background: 'var(--accent-gradient)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
            letterSpacing: '-0.5px'
          }}
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.95 }}
        >
          BookHeaven
        </MotionLink>

        <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem' }}>
          {isAuthenticated ? (
            <>
              {(user?.role === 'SELLER' || user?.role === 'ADMIN') && (
                <Link
                  to="/seller/dashboard"
                  style={{ color: 'var(--primary-color)', fontWeight: '600', textDecoration: 'none' }}
                >
                  Seller Dashboard
                </Link>
              )}

              <MotionLink
                to="/orders"
                style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: 'var(--text-muted)', textDecoration: 'none', fontSize: '0.95rem' }}
                whileHover={{ scale: 1.05, color: 'var(--text-color)' }}
              >
                <Package size={18} />
                My Orders
              </MotionLink>

              <MotionLink
                to="/wishlist"
                style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: 'var(--text-muted)', textDecoration: 'none', fontSize: '0.95rem' }}
                whileHover={{ scale: 1.05, color: '#ef4444' }}
              >
                <Heart size={18} />
                Wishlist
              </MotionLink>

              <MotionLink 
                to="/cart" 
                style={{ position: 'relative', color: 'var(--text-color)', display: 'flex', alignItems: 'center' }}
                whileHover={{ scale: 1.1 }}
                whileTap={{ scale: 0.95 }}
              >
                <ShoppingCart size={24} />
                {cartItemCount > 0 && (
                  <motion.span 
                    initial={{ scale: 0 }}
                    animate={{ scale: 1 }}
                    style={{
                      position: 'absolute',
                      top: '-8px',
                      right: '-8px',
                      backgroundColor: 'var(--danger-color)',
                      color: 'white',
                      fontSize: '0.75rem',
                      fontWeight: 'bold',
                      borderRadius: '50%',
                      width: '20px',
                      height: '20px',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center'
                    }}>
                    {cartItemCount}
                  </motion.span>
                )}
              </MotionLink>

              <div ref={dropdownRef} style={{ position: 'relative', marginLeft: '0.5rem' }}>
                <div
                  onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                  style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer', padding: '0.2rem 0.5rem', borderRadius: '20px', transition: 'background-color 0.2s', backgroundColor: isDropdownOpen ? 'rgba(255,255,255,0.05)' : 'transparent' }}
                >
                  {user?.profilePicture ? (
                    <img
                      src={user.profilePicture}
                      alt={user.username}
                      style={{
                        width: '32px', height: '32px', borderRadius: '50%',
                        objectFit: 'cover', border: '2px solid var(--primary-color)',
                      }}
                    />
                  ) : (
                    <div style={{
                      width: '32px', height: '32px', borderRadius: '50%',
                      background: 'linear-gradient(135deg, var(--primary-color), #8b5cf6)',
                      display: 'flex', alignItems: 'center', justifyContent: 'center',
                      color: '#fff', fontWeight: '600', fontSize: '0.85rem',
                    }}>
                      {user?.username?.[0]?.toUpperCase() || '?'}
                    </div>
                  )}
                  <ChevronDown size={14} style={{ color: 'var(--text-muted)', transition: 'transform 0.2s', transform: isDropdownOpen ? 'rotate(180deg)' : 'rotate(0deg)' }} />
                </div>

                {isDropdownOpen && (
                  <div style={{
                    position: 'absolute',
                    top: '120%',
                    right: '0',
                    width: '200px',
                    backgroundColor: 'var(--card-bg)',
                    border: '1px solid var(--border-color)',
                    borderRadius: 'var(--radius)',
                    boxShadow: '0 10px 25px -5px rgba(0, 0, 0, 0.5)',
                    overflow: 'hidden',
                    zIndex: 1001
                  }}>
                    <div style={{ padding: '0.75rem 1rem', borderBottom: '1px solid var(--border-color)', backgroundColor: 'rgba(0,0,0,0.2)' }}>
                      <div style={{ fontWeight: 'bold', color: 'var(--text-color)', fontSize: '0.9rem', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{user?.username}</div>
                      <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{user?.email || 'User Account'}</div>
                    </div>

                    <Link
                      to="/profile"
                      onClick={() => setIsDropdownOpen(false)}
                      style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', padding: '0.75rem 1rem', color: 'var(--text-color)', textDecoration: 'none', transition: 'background-color 0.2s', fontSize: '0.9rem' }}
                      onMouseOver={(e) => e.currentTarget.style.backgroundColor = 'rgba(255,255,255,0.05)'}
                      onMouseOut={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                    >
                      <User size={16} /> My Profile
                    </Link>

                    <Link
                      to="/wishlist"
                      onClick={() => setIsDropdownOpen(false)}
                      style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', padding: '0.75rem 1rem', color: 'var(--text-color)', textDecoration: 'none', transition: 'background-color 0.2s', fontSize: '0.9rem' }}
                      onMouseOver={(e) => e.currentTarget.style.backgroundColor = 'rgba(255,255,255,0.05)'}
                      onMouseOut={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                    >
                      <Heart size={16} /> My Wishlist
                    </Link>

                    {user?.role === 'ADMIN' && (
                      <>
                        <Link
                          to="/admin/dashboard"
                          onClick={() => setIsDropdownOpen(false)}
                          style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', padding: '0.75rem 1rem', color: 'var(--primary-color)', fontWeight: '600', textDecoration: 'none', transition: 'background-color 0.2s', fontSize: '0.9rem' }}
                          onMouseOver={(e) => e.currentTarget.style.backgroundColor = 'rgba(255,255,255,0.05)'}
                          onMouseOut={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                        >
                          <LayoutDashboard size={16} /> Admin Dashboard
                        </Link>
                      </>
                    )}

                    {(user?.role === 'SELLER' || user?.role === 'ADMIN') && (
                      <Link
                        to={user?.role === 'ADMIN' ? "/admin/coupons" : "/seller/coupons"}
                        onClick={() => setIsDropdownOpen(false)}
                        style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', padding: '0.75rem 1rem', color: 'var(--primary-color)', fontWeight: '600', textDecoration: 'none', transition: 'background-color 0.2s', fontSize: '0.9rem' }}
                        onMouseOver={(e) => e.currentTarget.style.backgroundColor = 'rgba(255,255,255,0.05)'}
                        onMouseOut={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                      >
                        <Tag size={16} /> My Promo Codes
                      </Link>
                    )}

                    <button
                      onClick={handleLogout}
                      style={{
                        width: '100%', textAlign: 'left', background: 'transparent', border: 'none', borderTop: '1px solid var(--border-color)',
                        padding: '0.75rem 1rem', display: 'flex', alignItems: 'center', gap: '0.75rem', color: 'var(--danger-color)',
                        borderRadius: '0', fontSize: '0.9rem', cursor: 'pointer', transition: 'background-color 0.2s'
                      }}
                      onMouseOver={(e) => e.currentTarget.style.backgroundColor = 'rgba(239, 68, 68, 0.1)'}
                      onMouseOut={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                    >
                      <LogOut size={16} /> Logout
                    </button>
                  </div>
                )}
              </div>
            </>
          ) : (
            <>
              <MotionLink 
                to="/cart" 
                style={{ position: 'relative', color: 'var(--text-color)', display: 'flex', alignItems: 'center' }}
                whileHover={{ scale: 1.1 }}
                whileTap={{ scale: 0.95 }}
              >
                <ShoppingCart size={24} />
                {cartItemCount > 0 && (
                  <motion.span 
                    initial={{ scale: 0 }}
                    animate={{ scale: 1 }}
                    style={{
                    position: 'absolute',
                    top: '-8px',
                    right: '-8px',
                    backgroundColor: 'var(--danger-color)',
                    color: 'white',
                    fontSize: '0.75rem',
                    fontWeight: 'bold',
                    borderRadius: '50%',
                    width: '20px',
                    height: '20px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center'
                  }}>
                    {cartItemCount}
                  </motion.span>
                )}
              </MotionLink>

              <div style={{ display: 'flex', gap: '1rem', marginLeft: '0.5rem' }}>
                <MotionLink 
                  to="/login" 
                  style={{ color: 'var(--text-color)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}
                  whileHover={{ scale: 1.05 }}
                >
                  <User size={18} /> Login
                </MotionLink>
                <MotionLink 
                  to="/signup" 
                  style={{
                    backgroundColor: 'var(--primary-color)',
                    color: 'white',
                    padding: '0.4rem 1rem',
                    borderRadius: 'var(--radius)',
                    fontWeight: '500'
                  }}
                  whileHover={{ scale: 1.05, backgroundColor: 'var(--primary-hover)' }}
                  whileTap={{ scale: 0.95 }}
                >
                  Sign Up
                </MotionLink>
              </div>
            </>
          )}
        </div>
    </motion.nav>
  );
};

export default Navbar;
