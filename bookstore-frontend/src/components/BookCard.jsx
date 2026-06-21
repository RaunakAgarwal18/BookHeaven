import { ShoppingCart, Heart } from 'lucide-react';
import { Link } from 'react-router-dom';

const DEFAULT_COVER = '/default-book-cover.png';

const BookCard = ({ book, onAddToCart, isWishlisted = false, onToggleWishlist }) => {
  return (
    <div className="card" style={{ display: 'flex', flexDirection: 'column', height: '100%', padding: 0, overflow: 'hidden', position: 'relative' }}>
      
      <Link to={`/books/${book.bookId}`} style={{ textDecoration: 'none', color: 'inherit', display: 'flex', flexDirection: 'column', padding: '1.5rem 1.5rem 0.5rem 1.5rem', flex: 1 }}>
        <div style={{ height: '200px', backgroundColor: 'var(--bg-color)', borderRadius: 'var(--radius)', marginBottom: '1rem', overflow: 'hidden', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <img
            src={book.img || DEFAULT_COVER}
            alt={book.title}
            onError={(e) => { e.target.onerror = null; e.target.src = DEFAULT_COVER; }}
            style={{ width: '100%', height: '100%', objectFit: 'cover' }}
          />
        </div>
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
          <h3 style={{ fontSize: '1.2rem', marginBottom: '0.25rem', transition: 'color 0.2s' }}>{book.title}</h3>
          <p style={{ color: 'var(--text-muted)', marginBottom: '0.5rem', fontSize: '0.9rem' }}>By {book.author}</p>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.3rem', marginBottom: '0.5rem', fontSize: '0.9rem', color: 'var(--text-muted)' }}>
            {book.totalReviews > 0 ? (
              <>
                <span style={{ color: '#eab308', fontSize: '1rem' }}>★</span>
                <span style={{ fontWeight: '500', color: 'var(--text-color)' }}>{book.averageRating?.toFixed(1) || '0.0'}</span>
                <span>({book.totalReviews})</span>
              </>
            ) : (
              <span>No reviews</span>
            )}
          </div>
          <div style={{ marginTop: 'auto' }}>
            <span style={{ 
              display: 'inline-block', 
              padding: '0.2rem 0.5rem', 
              backgroundColor: 'rgba(59, 130, 246, 0.1)', 
              color: 'var(--primary-color)',
              borderRadius: '4px',
              fontSize: '0.8rem'
            }}>
              {book.category}
            </span>
          </div>
        </div>
      </Link>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.5rem 1.5rem 1.5rem 1.5rem', marginTop: 'auto' }}>
        <div style={{ fontSize: '1.25rem', fontWeight: 'bold' }}>
          {book.lowestCurrency || '$'} {(book.lowestPrice || 0).toFixed(2)}
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          {/* Wishlist Button */}
          {onToggleWishlist && (
            <button
              onClick={(e) => {
                e.preventDefault();
                e.stopPropagation();
                onToggleWishlist(book.bookId);
              }}
              style={{
                background: 'transparent',
                border: '1px solid var(--border-color)',
                borderRadius: 'var(--radius)',
                width: '38px',
                height: '38px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                cursor: 'pointer',
                transition: 'all 0.2s',
                padding: 0
              }}
              onMouseOver={(e) => e.currentTarget.style.borderColor = isWishlisted ? '#ef4444' : 'var(--primary-color)'}
              onMouseOut={(e) => e.currentTarget.style.borderColor = 'var(--border-color)'}
            >
              <Heart 
                size={18} 
                color={isWishlisted ? '#ef4444' : 'var(--text-muted)'} 
                fill={isWishlisted ? '#ef4444' : 'none'} 
                style={{ transition: 'all 0.2s' }}
              />
            </button>
          )}

          <button 
            onClick={() => onAddToCart(book.bookId)}
            disabled={book.totalCopiesAvailable <= 0}
            style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', padding: '0.5rem 1rem' }}
          >
            <ShoppingCart size={16} />
            {book.totalCopiesAvailable > 0 ? 'Add' : 'Out of Stock'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default BookCard;
