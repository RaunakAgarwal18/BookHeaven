import { useState, useEffect } from 'react';
import apiClient from '../api/axios';
import BookCard from '../components/BookCard';
import useCartStore from '../store/useCartStore';
import useAuthStore from '../store/useAuthStore';
import useWishlistStore from '../store/useWishlistStore';
import { useNavigate } from 'react-router-dom';
import { Loader2 } from 'lucide-react';

const WishlistPage = () => {
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [cartMessage, setCartMessage] = useState(null);
  
  const { addToCart } = useCartStore();
  const { isAuthenticated } = useAuthStore();
  const { wishlistIds, fetchWishlistIds, toggleWishlist } = useWishlistStore();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login', { replace: true });
      return;
    }
    fetchWishlistIds();
    fetchWishlistDetails();
  }, [isAuthenticated]);

  const fetchWishlistDetails = async () => {
    setLoading(true);
    try {
      const response = await apiClient.get('/user/wishlist/details');
      setBooks(response.data || []);
    } catch (error) {
      console.error('Failed to fetch wishlist details', error);
    } finally {
      setLoading(false);
    }
  };

  const handleAddToCart = async (bookId) => {
    await addToCart(bookId, 1);
    const error = useCartStore.getState().error;
    if (error) {
      setCartMessage({ type: 'error', text: error });
    } else {
      setCartMessage({ type: 'success', text: 'Added to cart!' });
    }
    setTimeout(() => setCartMessage(null), 3000);
  };

  const handleToggleWishlist = async (bookId) => {
    await toggleWishlist(bookId);
    // Remove from local display immediately
    setBooks(prev => prev.filter(b => b.bookId !== bookId));
  };

  return (
    <div style={{ width: '100%', maxWidth: '1500px', margin: '2rem auto', padding: '0 2rem 4rem 2rem' }}>
      {cartMessage && (
        <div style={{
          position: 'fixed',
          top: '5rem',
          right: '1.5rem',
          zIndex: 9999,
          padding: '0.75rem 1.25rem',
          borderRadius: 'var(--radius)',
          backgroundColor: cartMessage.type === 'success' ? 'rgba(34,197,94,0.15)' : 'rgba(239,68,68,0.15)',
          border: `1px solid ${cartMessage.type === 'success' ? 'rgba(34,197,94,0.4)' : 'rgba(239,68,68,0.4)'}`,
          color: cartMessage.type === 'success' ? '#22c55e' : 'var(--danger-color)',
          fontWeight: '500',
          boxShadow: '0 4px 12px rgba(0,0,0,0.3)',
          maxWidth: '320px',
        }}>
          {cartMessage.text}
        </div>
      )}

      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2.5rem', marginBottom: '0.5rem', color: 'var(--text-color)' }}>
          My Wishlist
        </h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '1.1rem' }}>
          Books you've saved for later.
        </p>
      </div>

      {loading ? (
        <div className="text-center mt-8">
          <Loader2 className="animate-spin" size={40} style={{ color: 'var(--primary-color)' }} />
        </div>
      ) : books.length > 0 ? (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: '1.5rem' }}>
          {books.map((book) => (
            <BookCard 
              key={book.bookId} 
              book={book} 
              onAddToCart={handleAddToCart} 
              isWishlisted={true} // In wishlist page, they are all wishlisted
              onToggleWishlist={handleToggleWishlist}
            />
          ))}
        </div>
      ) : (
        <div className="card text-center py-12 glass-panel">
          <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>📚</div>
          <h3 style={{ color: 'var(--text-color)', marginBottom: '0.5rem', fontSize: '1.5rem' }}>Your wishlist is empty</h3>
          <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem' }}>Start browsing and save your favorite books!</p>
          <button onClick={() => navigate('/')} className="button" style={{ padding: '0.75rem 2rem' }}>
            Explore Books
          </button>
        </div>
      )}
    </div>
  );
};

export default WishlistPage;
