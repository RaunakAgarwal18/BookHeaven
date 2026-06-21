import { useState, useEffect, useRef } from 'react';
import apiClient from '../api/axios';
import BookCard from '../components/BookCard';
import useCartStore from '../store/useCartStore';
import useAuthStore from '../store/useAuthStore';
import useWishlistStore from '../store/useWishlistStore';
import { useNavigate } from 'react-router-dom';
import { Search } from 'lucide-react';


const HomePage = () => {
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [pageNumber, setPageNumber] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [cartMessage, setCartMessage] = useState(null); // { type: 'success'|'error', text: '' }
  const isSearchActive = useRef(false);
  const debounceRef = useRef(null);
  
  const { addToCart } = useCartStore();
  const { isAuthenticated } = useAuthStore();
  const { wishlistIds, fetchWishlistIds, toggleWishlist } = useWishlistStore();
  const navigate = useNavigate();

  useEffect(() => {
    fetchBooks(searchTerm, pageNumber);
  }, [pageNumber]);

  useEffect(() => {
    if (isAuthenticated) {
      fetchWishlistIds();
    }
  }, [isAuthenticated]);

  // Debounced auto-search: fires 0.75s after user stops typing
  useEffect(() => {
    if (!searchTerm.trim()) {
      clearTimeout(debounceRef.current);
      if (isSearchActive.current) {
        setPageNumber(0);
        fetchBooks('', 0);
        isSearchActive.current = false;
      }
      return;
    }

    isSearchActive.current = true;
    clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => {
      setPageNumber(0);
      fetchBooks(searchTerm, 0);
    }, 750);
    return () => clearTimeout(debounceRef.current);
  }, [searchTerm]);

  const fetchBooks = async (query = '', page = 0) => {
    setLoading(true);
    try {
      const endpoint = query
        ? `/search?q=${encodeURIComponent(query)}&pageNumber=${page}&pageSize=10`
        : `/book?pageNumber=${page}&pageSize=10`;
      const response = await apiClient.get(endpoint);
      setBooks(response.data.content || []);
      setTotalPages(response.data.totalPages || 0);
    } catch (error) {
      console.error('Failed to fetch books', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    setPageNumber(0);
    fetchBooks(searchTerm, 0);
  };

  const handleAddToCart = async (bookId) => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    
    await addToCart(bookId, 1);
    
    const error = useCartStore.getState().error;
    if (error) {
      setCartMessage({ type: 'error', text: error });
    } else {
      setCartMessage({ type: 'success', text: 'Added to cart!' });
    }
    setTimeout(() => setCartMessage(null), 3000);
  };

  const handleToggleWishlist = (bookId) => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    toggleWishlist(bookId);
  };

  return (
    <div className="container mt-8 mb-8">
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
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '2.5rem', marginBottom: '0.5rem', background: 'linear-gradient(90deg, var(--primary-color), #8b5cf6)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
            Discover Your Next Great Read
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '1.1rem' }}>Explore our vast collection of books across all genres.</p>
        </div>
        
        <form onSubmit={handleSearch} style={{ display: 'flex', gap: '0.5rem', width: '100%', maxWidth: '520px' }}>
          <div style={{ position: 'relative', flex: 1, minWidth: 0 }}>
            <input 
              type="text" 
              placeholder="Search for books, authors, categories, or ISBNs..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              style={{ paddingLeft: '2.5rem', width: '100%' }}
            />
            <Search size={18} style={{ position: 'absolute', left: '10px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
          </div>
          <button type="submit">Search</button>
        </form>
      </div>

      {loading ? (
        <div className="text-center mt-8">
          <div style={{ display: 'inline-block', width: '40px', height: '40px', border: '3px solid rgba(255,255,255,0.1)', borderRadius: '50%', borderTopColor: 'var(--primary-color)', animation: 'spin 1s ease-in-out infinite' }} />
          <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
        </div>
      ) : books.length > 0 ? (
        <>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '2rem' }}>
            {books.map((book) => (
              <BookCard 
                key={book.bookId} 
                book={book} 
                onAddToCart={handleAddToCart} 
                isWishlisted={wishlistIds?.has(book.bookId)}
                onToggleWishlist={handleToggleWishlist}
              />
            ))}
          </div>
          
          {/* Pagination Controls */}
          {totalPages > 1 && (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '1rem', marginTop: '3rem' }}>
              <button 
                onClick={() => setPageNumber(prev => Math.max(0, prev - 1))}
                disabled={pageNumber === 0}
                style={{ padding: '0.5rem 1rem', background: 'var(--card-bg)' }}
              >
                Previous
              </button>
              <span style={{ color: 'var(--text-muted)' }}>
                Page {pageNumber + 1} of {totalPages}
              </span>
              <button 
                onClick={() => setPageNumber(prev => Math.min(totalPages - 1, prev + 1))}
                disabled={pageNumber === totalPages - 1}
                style={{ padding: '0.5rem 1rem', background: 'var(--card-bg)' }}
              >
                Next
              </button>
            </div>
          )}
        </>
      ) : (
        <div className="card text-center py-8">
          <h3 style={{ color: 'var(--text-muted)' }}>No books found</h3>
        </div>
      )}
    </div>
  );
};

export default HomePage;
