import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ShoppingCart, ArrowLeft, Loader2, Star, Send, ChevronLeft, ChevronRight, Heart, ThumbsUp, Camera, X } from 'lucide-react';
import apiClient from '../api/axios';
import useCartStore from '../store/useCartStore';
import useAuthStore from '../store/useAuthStore';
import useWishlistStore from '../store/useWishlistStore';
import BookCard from '../components/BookCard';

const DEFAULT_COVER = '/default-book-cover.png';

/* ── Related Books Component ──────────────────────────────────────── */
const RelatedBooks = ({ category, currentBookId }) => {
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const { addToCart } = useCartStore();
  const { wishlistIds, toggleWishlist } = useWishlistStore();

  useEffect(() => {
    const fetchRelated = async () => {
      try {
        const res = await apiClient.get(`/book/search?category=${encodeURIComponent(category)}&pageNumber=0&pageSize=5`);
        const filtered = (res.data.content || []).filter(b => b.bookId !== currentBookId).slice(0, 4);
        setBooks(filtered);
      } catch (err) {
        console.error('Failed to fetch related books', err);
      } finally {
        setLoading(false);
      }
    };
    if (category) fetchRelated();
  }, [category, currentBookId]);

  if (loading || books.length === 0) return null;

  return (
    <div className="card glass-panel" style={{ marginTop: '2rem', padding: '1.5rem' }}>
      <h2 style={{ fontSize: '1.4rem', marginBottom: '1.5rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.5rem' }}>
        You might also like...
      </h2>
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))',
        gap: '1.5rem'
      }}>
        {books.map(book => (
          <div key={book.bookId}>
            <BookCard
              book={book}
              onAddToCart={(id) => addToCart(id, 1)}
              isWishlisted={wishlistIds.has(book.bookId)}
              onToggleWishlist={toggleWishlist}
            />
          </div>
        ))}
      </div>
    </div>
  );
};

/* ── Star Rating (interactive or read-only) ────────────────────────── */
const StarRating = ({ rating, onRate, size = 18, interactive = false }) => {
  const [hovered, setHovered] = useState(0);
  return (
    <div style={{ display: 'inline-flex', gap: '2px' }}>
      {[1, 2, 3, 4, 5].map((star) => {
        const filled = interactive ? (hovered || rating) >= star : rating >= star;
        const half = !filled && (interactive ? false : rating >= star - 0.5);
        return (
          <Star
            key={star}
            size={size}
            onClick={interactive ? () => onRate(star) : undefined}
            onMouseEnter={interactive ? () => setHovered(star) : undefined}
            onMouseLeave={interactive ? () => setHovered(0) : undefined}
            style={{
              cursor: interactive ? 'pointer' : 'default',
              color: filled || half ? '#facc15' : 'var(--border-color)',
              fill: filled ? '#facc15' : half ? 'url(#halfGrad)' : 'none',
              transition: 'color 0.15s, fill 0.15s',
            }}
          />
        );
      })}
      {/* SVG gradient for half-stars (read-only) */}
      {!interactive && (
        <svg width="0" height="0" style={{ position: 'absolute' }}>
          <defs>
            <linearGradient id="halfGrad">
              <stop offset="50%" stopColor="#facc15" />
              <stop offset="50%" stopColor="transparent" />
            </linearGradient>
          </defs>
        </svg>
      )}
    </div>
  );
};

/* ── Time-ago helper ───────────────────────────────────────────────── */
const timeAgo = (dateStr) => {
  const diff = Date.now() - new Date(dateStr).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return 'Just now';
  if (mins < 60) return `${mins}m ago`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return `${hrs}h ago`;
  const days = Math.floor(hrs / 24);
  if (days < 30) return `${days}d ago`;
  const months = Math.floor(days / 30);
  return `${months}mo ago`;
};

/* ── Review Section Component ──────────────────────────────────────── */
const ReviewSection = ({ bookId }) => {
  const { isAuthenticated, user } = useAuthStore();
  const [reviews, setReviews] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [reviewsLoading, setReviewsLoading] = useState(true);

  // New review form
  const [newRating, setNewRating] = useState(0);
  const [newText, setNewText] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState(null);
  const [submitSuccess, setSubmitSuccess] = useState(false);
  const [alreadyReviewed, setAlreadyReviewed] = useState(false);

  // Edit review state
  const [editingId, setEditingId] = useState(null);
  const [editRating, setEditRating] = useState(0);
  const [editText, setEditText] = useState('');
  const [editSubmitting, setEditSubmitting] = useState(false);
  const [editError, setEditError] = useState(null);

  // Photos & Lightbox states
  const [selectedPhotos, setSelectedPhotos] = useState([]);
  const [editPhotos, setEditPhotos] = useState([]);
  const [activeLightboxImage, setActiveLightboxImage] = useState(null);

  const handlePhotoChange = (e, isEdit = false) => {
    const files = Array.from(e.target.files);
    const targetLimit = 3;
    const currentCount = isEdit ? editPhotos.length : selectedPhotos.length;
    
    if (currentCount + files.length > targetLimit) {
      alert(`You can only upload up to ${targetLimit} photos.`);
      return;
    }

    files.forEach(file => {
      if (!file.type.startsWith('image/')) {
        alert('Only image files are allowed.');
        return;
      }
      if (file.size > 1 * 1024 * 1024) {
        alert('Each image must be less than 1MB in size.');
        return;
      }

      const reader = new FileReader();
      reader.onloadend = () => {
        if (isEdit) {
          setEditPhotos(prev => [...prev, reader.result]);
        } else {
          setSelectedPhotos(prev => [...prev, reader.result]);
        }
      };
      reader.readAsDataURL(file);
    });
    e.target.value = '';
  };

  const removePhoto = (index, isEdit = false) => {
    if (isEdit) {
      setEditPhotos(prev => prev.filter((_, i) => i !== index));
    } else {
      setSelectedPhotos(prev => prev.filter((_, i) => i !== index));
    }
  };

  const fetchReviews = async (p = 0) => {
    setReviewsLoading(true);
    try {
      const res = await apiClient.get(`/reviews/${bookId}?pageNumber=${p}&pageSize=5`);
      setReviews(res.data.content || []);
      setTotalPages(res.data.totalPages || 0);
    } catch {
      setReviews([]);
    } finally {
      setReviewsLoading(false);
    }
  };

  useEffect(() => {
    fetchReviews(page);
  }, [bookId, page]);

  const avgRating = reviews.length
    ? (reviews.reduce((sum, r) => sum + r.rating, 0) / reviews.length).toFixed(1)
    : null;

  const handleSubmitReview = async () => {
    if (newRating === 0) { setSubmitError('Please select a rating.'); return; }
    setSubmitting(true);
    setSubmitError(null);
    try {
      await apiClient.post('/reviews', {
        bookId: Number(bookId),
        rating: newRating,
        reviewDescription: newText.trim() || null,
        photos: selectedPhotos,
      });
      setSubmitSuccess(true);
      setNewRating(0);
      setNewText('');
      setSelectedPhotos([]);
      setPage(0);
      await fetchReviews(0);
      setTimeout(() => setSubmitSuccess(false), 3000);
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to submit review.';
      if (msg.toLowerCase().includes('already')) {
        setAlreadyReviewed(true);
      }
      setSubmitError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  const handleStartEdit = (review) => {
    setEditingId(review.id);
    setEditRating(review.rating);
    setEditText(review.reviewDescription || '');
    setEditPhotos(review.photos || []);
    setEditError(null);
  };

  const handleCancelEdit = () => {
    setEditingId(null);
    setEditRating(0);
    setEditText('');
    setEditPhotos([]);
    setEditError(null);
  };

  const handleSaveEdit = async (reviewId) => {
    if (editRating === 0) { setEditError('Please select a rating.'); return; }
    setEditSubmitting(true);
    setEditError(null);
    try {
      await apiClient.put(`/reviews/${reviewId}`, {
        rating: editRating,
        reviewDescription: editText.trim() || null,
        photos: editPhotos,
      });
      setReviews(prev => prev.map(r =>
        r.id === reviewId
          ? { ...r, rating: editRating, reviewDescription: editText.trim() || null, photos: editPhotos }
          : r
      ));
      setEditingId(null);
      setEditPhotos([]);
    } catch (err) {
      setEditError(err.response?.data?.message || 'Failed to update review.');
    } finally {
      setEditSubmitting(false);
    }
  };

  const handleDeleteReview = async (reviewId) => {
    if (!window.confirm('Are you sure you want to delete this review?')) return;
    try {
      await apiClient.delete(`/reviews/${reviewId}`);
      setReviews(prev => prev.filter(r => r.id !== reviewId));
      setAlreadyReviewed(false);
      await fetchReviews(0);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete review.');
    }
  };

  const handleToggleUpvote = async (reviewId) => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    try {
      const res = await apiClient.post(`/reviews/${reviewId}/upvote`);
      setReviews(prev => prev.map(r =>
        r.id === reviewId
          ? { ...r, upvoteCount: res.data.upvoteCount, upvotedByCurrentUser: res.data.upvotedByCurrentUser }
          : r
      ));
    } catch (err) {
      console.error('Failed to toggle upvote', err);
    }
  };

  return (
    <div className="card glass-panel" style={{ marginTop: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '1rem' }}>
        <h2 style={{ fontSize: '1.4rem', margin: 0 }}>Reviews</h2>
        {avgRating && (
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <StarRating rating={parseFloat(avgRating)} size={16} />
            <span style={{ fontWeight: '600', fontSize: '1.1rem' }}>{avgRating}</span>
            <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
              ({reviews.length}{totalPages > 1 ? '+' : ''} review{reviews.length !== 1 ? 's' : ''})
            </span>
          </div>
        )}
      </div>

      {/* Write a Review */}
      {isAuthenticated ? (
        !alreadyReviewed && !submitSuccess && (
          <div style={{ marginBottom: '2rem', padding: '1.25rem', backgroundColor: 'rgba(255,255,255,0.03)', borderRadius: 'var(--radius)', border: '1px solid var(--border-color)' }}>
            <h3 style={{ fontSize: '1rem', marginBottom: '0.75rem' }}>Write a Review</h3>
            <div style={{ marginBottom: '0.75rem' }}>
              <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginRight: '0.5rem' }}>Your rating:</span>
              <StarRating rating={newRating} onRate={setNewRating} size={22} interactive />
            </div>
            <textarea
              placeholder="Share your thoughts about this book (optional)..."
              value={newText}
              onChange={(e) => setNewText(e.target.value)}
              style={{
                width: '100%',
                minHeight: '80px',
                padding: '0.75rem',
                borderRadius: 'var(--radius)',
                border: '1px solid var(--border-color)',
                backgroundColor: 'var(--bg-color)',
                color: 'var(--text-color)',
                fontSize: '0.95rem',
                resize: 'vertical',
                fontFamily: 'inherit',
              }}
            />
            {/* Review Photos Upload */}
            <div style={{ marginTop: '0.75rem', display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <input
                  type="file"
                  accept="image/*"
                  multiple
                  onChange={(e) => handlePhotoChange(e, false)}
                  id="review-photo-upload-new"
                  style={{ display: 'none' }}
                />
                <label
                  htmlFor="review-photo-upload-new"
                  style={{
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: '0.5rem',
                    padding: '0.5rem 1rem',
                    border: '1px dashed var(--border-color)',
                    borderRadius: 'var(--radius)',
                    cursor: 'pointer',
                    fontSize: '0.85rem',
                    color: 'var(--text-muted)',
                    backgroundColor: 'rgba(255, 255, 255, 0.02)',
                    transition: 'all 0.2s',
                  }}
                  onMouseOver={(e) => e.currentTarget.style.borderColor = 'var(--primary-color)'}
                  onMouseOut={(e) => e.currentTarget.style.borderColor = 'var(--border-color)'}
                >
                  <Camera size={16} />
                  Add Photos ({selectedPhotos.length}/3)
                </label>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Max 1MB per photo</span>
              </div>
              
              {selectedPhotos.length > 0 && (
                <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                  {selectedPhotos.map((photo, index) => (
                    <div key={index} style={{ position: 'relative', width: '70px', height: '70px', borderRadius: 'var(--radius)', overflow: 'hidden', border: '1px solid var(--border-color)' }}>
                      <img src={photo} alt="preview" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                      <button
                        type="button"
                        onClick={() => removePhoto(index, false)}
                        style={{
                          position: 'absolute',
                          top: '2px',
                          right: '2px',
                          background: 'rgba(0,0,0,0.6)',
                          border: 'none',
                          borderRadius: '50%',
                          width: '18px',
                          height: '18px',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          color: '#fff',
                          cursor: 'pointer',
                          padding: 0,
                        }}
                      >
                        <X size={12} />
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>
            {submitError && (
              <p style={{ color: 'var(--danger-color)', fontSize: '0.85rem', marginTop: '0.5rem' }}>{submitError}</p>
            )}
            <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '0.75rem' }}>
              <button
                onClick={handleSubmitReview}
                disabled={submitting || newRating === 0}
                style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', padding: '0.6rem 1.2rem' }}
              >
                {submitting ? <Loader2 className="animate-spin" size={16} /> : <Send size={16} />}
                {submitting ? 'Posting…' : 'Post Review'}
              </button>
            </div>
          </div>
        )
      ) : (
        <div style={{ marginBottom: '1.5rem', padding: '1rem', backgroundColor: 'rgba(255,255,255,0.03)', borderRadius: 'var(--radius)', border: '1px solid var(--border-color)', textAlign: 'center' }}>
          <p style={{ color: 'var(--text-muted)', margin: 0, fontSize: '0.9rem' }}>
            <a href="/login" style={{ color: 'var(--primary-color)', fontWeight: '600' }}>Log in</a> to write a review.
          </p>
        </div>
      )}

      {submitSuccess && (
        <div style={{ marginBottom: '1.5rem', padding: '0.75rem 1rem', backgroundColor: 'rgba(34,197,94,0.1)', color: '#22c55e', borderRadius: 'var(--radius)', border: '1px solid rgba(34,197,94,0.3)' }}>
          ✓ Your review has been posted!
        </div>
      )}

      {alreadyReviewed && !submitSuccess && (
        <div style={{ marginBottom: '1.5rem', padding: '0.75rem 1rem', backgroundColor: 'rgba(59,130,246,0.1)', color: 'var(--primary-color)', borderRadius: 'var(--radius)', border: '1px solid rgba(59,130,246,0.3)', fontSize: '0.9rem' }}>
          You have already reviewed this book.
        </div>
      )}

      {/* Reviews List */}
      {reviewsLoading ? (
        <div style={{ textAlign: 'center', padding: '2rem' }}>
          <Loader2 className="animate-spin" size={24} style={{ color: 'var(--primary-color)' }} />
        </div>
      ) : reviews.length === 0 ? (
        <p style={{ color: 'var(--text-muted)', textAlign: 'center', padding: '2rem 0' }}>
          No reviews yet. Be the first to review this book!
        </p>
      ) : (
        <>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {reviews.map((r) => (
              <div key={r.id} style={{ padding: '1rem 1.25rem', backgroundColor: 'rgba(255,255,255,0.02)', borderRadius: 'var(--radius)', border: '1px solid var(--border-color)' }}>
                {editingId === r.id ? (
                  /* ── Inline Edit Form ── */
                  <div>
                    <div style={{ marginBottom: '0.75rem' }}>
                      <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginRight: '0.5rem' }}>Rating:</span>
                      <StarRating rating={editRating} onRate={setEditRating} size={20} interactive />
                    </div>
                    <textarea
                      value={editText}
                      onChange={(e) => setEditText(e.target.value)}
                      style={{
                        width: '100%',
                        minHeight: '70px',
                        padding: '0.65rem',
                        borderRadius: 'var(--radius)',
                        border: '1px solid var(--border-color)',
                        backgroundColor: 'var(--bg-color)',
                        color: 'var(--text-color)',
                        fontSize: '0.9rem',
                        resize: 'vertical',
                        fontFamily: 'inherit',
                      }}
                    />
                    {/* Review Photos Edit */}
                    <div style={{ marginTop: '0.75rem', display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                        <input
                          type="file"
                          accept="image/*"
                          multiple
                          onChange={(e) => handlePhotoChange(e, true)}
                          id="review-photo-upload-edit"
                          style={{ display: 'none' }}
                        />
                        <label
                          htmlFor="review-photo-upload-edit"
                          style={{
                            display: 'inline-flex',
                            alignItems: 'center',
                            gap: '0.5rem',
                            padding: '0.5rem 1rem',
                            border: '1px dashed var(--border-color)',
                            borderRadius: 'var(--radius)',
                            cursor: 'pointer',
                            fontSize: '0.85rem',
                            color: 'var(--text-muted)',
                            backgroundColor: 'rgba(255, 255, 255, 0.02)',
                            transition: 'all 0.2s',
                          }}
                          onMouseOver={(e) => e.currentTarget.style.borderColor = 'var(--primary-color)'}
                          onMouseOut={(e) => e.currentTarget.style.borderColor = 'var(--border-color)'}
                        >
                          <Camera size={16} />
                          Add Photos ({editPhotos.length}/3)
                        </label>
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Max 1MB per photo</span>
                      </div>
                      
                      {editPhotos.length > 0 && (
                        <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                          {editPhotos.map((photo, index) => (
                            <div key={index} style={{ position: 'relative', width: '60px', height: '60px', borderRadius: 'var(--radius)', overflow: 'hidden', border: '1px solid var(--border-color)' }}>
                              <img src={photo} alt="preview" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                              <button
                                type="button"
                                onClick={() => removePhoto(index, true)}
                                style={{
                                  position: 'absolute',
                                  top: '2px',
                                  right: '2px',
                                  background: 'rgba(0,0,0,0.6)',
                                  border: 'none',
                                  borderRadius: '50%',
                                  width: '16px',
                                  height: '16px',
                                  display: 'flex',
                                  alignItems: 'center',
                                  justifyContent: 'center',
                                  color: '#fff',
                                  cursor: 'pointer',
                                  padding: 0,
                                }}
                              >
                                <X size={10} />
                              </button>
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                    {editError && (
                      <p style={{ color: 'var(--danger-color)', fontSize: '0.82rem', marginTop: '0.4rem' }}>{editError}</p>
                    )}
                    <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end', marginTop: '0.65rem' }}>
                      <button
                        onClick={handleCancelEdit}
                        disabled={editSubmitting}
                        style={{ padding: '0.4rem 1rem', fontSize: '0.85rem', background: 'transparent', border: '1px solid var(--border-color)', color: 'var(--text-muted)' }}
                      >
                        Cancel
                      </button>
                      <button
                        onClick={() => handleSaveEdit(r.id)}
                        disabled={editSubmitting || editRating === 0}
                        style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', padding: '0.4rem 1rem', fontSize: '0.85rem' }}
                      >
                        {editSubmitting ? <Loader2 className="animate-spin" size={14} /> : null}
                        {editSubmitting ? 'Saving…' : 'Save'}
                      </button>
                    </div>
                  </div>
                ) : (
                  /* ── Normal Review Card ── */
                  <>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                        <div style={{
                          width: '32px', height: '32px', borderRadius: '50%',
                          background: 'linear-gradient(135deg, var(--primary-color), #8b5cf6)',
                          display: 'flex', alignItems: 'center', justifyContent: 'center',
                          color: '#fff', fontWeight: '600', fontSize: '0.8rem',
                        }}>
                          {r.username?.[0]?.toUpperCase() || '?'}
                        </div>
                        <span style={{ fontWeight: '600' }}>{r.username}</span>
                        <StarRating rating={r.rating} size={14} />
                      </div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                        <span style={{ color: 'var(--text-muted)', fontSize: '0.8rem' }}>{timeAgo(r.createdAt)}</span>
                        {isAuthenticated && user?.username === r.username && (
                          <div style={{ display: 'flex', gap: '0.5rem' }}>
                            <button
                              onClick={() => handleStartEdit(r)}
                              style={{
                                padding: '0.2rem 0.65rem',
                                fontSize: '0.78rem',
                                background: 'transparent',
                                border: '1px solid var(--border-color)',
                                color: 'var(--primary-color)',
                                borderRadius: 'var(--radius)',
                                cursor: 'pointer',
                              }}
                            >
                              Edit
                            </button>
                            <button
                              onClick={() => handleDeleteReview(r.id)}
                              style={{
                                padding: '0.2rem 0.65rem',
                                fontSize: '0.78rem',
                                background: 'transparent',
                                border: '1px solid var(--border-color)',
                                color: 'var(--danger-color)',
                                borderRadius: 'var(--radius)',
                                cursor: 'pointer',
                              }}
                            >
                              Delete
                            </button>
                          </div>
                        )}
                      </div>
                    </div>
                    {r.reviewDescription && (
                      <p style={{ color: 'var(--text-muted)', lineHeight: '1.5', margin: 0, fontSize: '0.95rem' }}>
                        {r.reviewDescription}
                      </p>
                    )}
                    
                    {/* Render Photos if any */}
                    {r.photos && r.photos.length > 0 && (
                      <div style={{ display: 'flex', gap: '0.5rem', marginTop: '0.75rem', flexWrap: 'wrap' }}>
                        {r.photos.map((photo, idx) => (
                          <div 
                            key={idx} 
                            style={{ 
                              width: '80px', 
                              height: '80px', 
                              borderRadius: 'var(--radius)', 
                              overflow: 'hidden', 
                              border: '1px solid var(--border-color)',
                              cursor: 'pointer',
                            }}
                            onClick={() => setActiveLightboxImage(photo)}
                          >
                            <img 
                              src={photo} 
                              alt={`review-photo-${idx}`} 
                              style={{ width: '100%', height: '100%', objectFit: 'cover', transition: 'transform 0.2s' }} 
                              onMouseOver={(e) => e.currentTarget.style.transform = 'scale(1.08)'} 
                              onMouseOut={(e) => e.currentTarget.style.transform = 'scale(1)'} 
                            />
                          </div>
                        ))}
                      </div>
                    )}

                    {/* Upvote Button */}
                    <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginTop: '0.75rem' }}>
                      <button
                        onClick={() => handleToggleUpvote(r.id)}
                        style={{
                          display: 'flex',
                          alignItems: 'center',
                          gap: '0.35rem',
                          padding: '0.3rem 0.75rem',
                          background: r.upvotedByCurrentUser ? 'rgba(139, 92, 246, 0.15)' : 'transparent',
                          border: `1px solid ${r.upvotedByCurrentUser ? 'var(--primary-color)' : 'var(--border-color)'}`,
                          color: r.upvotedByCurrentUser ? 'var(--primary-color)' : 'var(--text-muted)',
                          borderRadius: '20px',
                          cursor: 'pointer',
                          fontSize: '0.8rem',
                          fontWeight: '600',
                          transition: 'all 0.2s',
                        }}
                        onMouseOver={(e) => {
                          if (!r.upvotedByCurrentUser) {
                            e.currentTarget.style.borderColor = 'var(--primary-color)';
                            e.currentTarget.style.color = 'var(--primary-color)';
                          }
                        }}
                        onMouseOut={(e) => {
                          if (!r.upvotedByCurrentUser) {
                            e.currentTarget.style.borderColor = 'var(--border-color)';
                            e.currentTarget.style.color = 'var(--text-muted)';
                          }
                        }}
                      >
                        <ThumbsUp size={12} fill={r.upvotedByCurrentUser ? 'var(--primary-color)' : 'none'} />
                        <span>{r.upvoteCount || 0} Upvote{r.upvoteCount !== 1 ? 's' : ''}</span>
                      </button>
                    </div>
                  </>
                )}
              </div>
            ))}
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '1rem', marginTop: '1.5rem' }}>
              <button
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
                style={{ padding: '0.4rem 0.8rem', background: 'var(--card-bg)', display: 'flex', alignItems: 'center', gap: '0.25rem' }}
              >
                <ChevronLeft size={16} /> Prev
              </button>
              <span style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
                {page + 1} / {totalPages}
              </span>
              <button
                onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                disabled={page === totalPages - 1}
                style={{ padding: '0.4rem 0.8rem', background: 'var(--card-bg)', display: 'flex', alignItems: 'center', gap: '0.25rem' }}
              >
                Next <ChevronRight size={16} />
              </button>
            </div>
          )}
        </>
      )}

      {/* Lightbox Modal */}
      {activeLightboxImage && (
        <div 
          onClick={() => setActiveLightboxImage(null)}
          style={{
            position: 'fixed',
            top: 0, left: 0, right: 0, bottom: 0,
            backgroundColor: 'rgba(0,0,0,0.85)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 99999,
            cursor: 'zoom-out',
            animation: 'fadeIn 0.2s ease-out',
          }}
        >
          <img 
            src={activeLightboxImage} 
            alt="Expanded Review" 
            style={{ 
              maxWidth: '90%', 
              maxHeight: '90%', 
              borderRadius: 'var(--radius)',
              boxShadow: '0 8px 32px rgba(0,0,0,0.5)',
            }} 
          />
        </div>
      )}
    </div>
  );
};

/* ── Main BookDetailsPage ──────────────────────────────────────────── */
const BookDetailsPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuthStore();
  const { addToCart } = useCartStore();
  const { wishlistIds, toggleWishlist } = useWishlistStore();
  
  const [book, setBook] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [addingToCartId, setAddingToCartId] = useState(null); // listingId being added
  const [cartMessage, setCartMessage] = useState(null);

  useEffect(() => {
    const fetchBookAndOffers = async () => {
      setLoading(true);
      try {
        const bookResponse = await apiClient.get(`/book/${id}`);
        setBook(bookResponse.data);
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load book details');
      } finally {
        setLoading(false);
      }
    };
    fetchBookAndOffers();
  }, [id]);

  const handleAddToCart = async (targetBookId) => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    
    setAddingToCartId(targetBookId);
    await addToCart(targetBookId, 1);
    setAddingToCartId(null);
    
    const cartError = useCartStore.getState().error;
    if (cartError) {
      setCartMessage({ type: 'error', text: cartError });
    } else {
      setCartMessage({ type: 'success', text: 'Added to cart!' });
    }
    setTimeout(() => setCartMessage(null), 3000);
  };

  if (loading) {
    return (
      <div className="container mt-8" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '50vh' }}>
        <Loader2 className="animate-spin" size={32} style={{ color: 'var(--primary-color)' }} />
      </div>
    );
  }

  if (error || !book) {
    return (
      <div className="container mt-8 text-center">
        <h2 style={{ color: 'var(--danger-color)' }}>{error || 'Book not found'}</h2>
        <button onClick={() => navigate(-1)} style={{ marginTop: '1rem' }} className="button">Go Back</button>
      </div>
    );
  }



  const isWishlisted = book ? wishlistIds.has(book.bookId) : false;

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
      <button 
        onClick={() => navigate(-1)} 
        style={{ background: 'transparent', border: 'none', display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '2rem', color: 'var(--text-color)', cursor: 'pointer' }}
      >
        <ArrowLeft size={20} /> Back
      </button>

      <div className="card glass-panel" style={{ position: 'relative', display: 'flex', gap: '3rem', alignItems: 'flex-start', flexWrap: 'wrap', marginBottom: '2rem' }}>
        
        {/* Wishlist Button */}
        {isAuthenticated && book && (
          <button
            onClick={() => toggleWishlist(book.bookId)}
            style={{
              position: 'absolute',
              top: '1.5rem',
              right: '1.5rem',
              background: 'transparent',
              border: '1px solid var(--border-color)',
              borderRadius: 'var(--radius)',
              width: '42px',
              height: '42px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              cursor: 'pointer',
              transition: 'all 0.2s',
              padding: 0,
              zIndex: 10
            }}
            onMouseOver={(e) => e.currentTarget.style.borderColor = isWishlisted ? '#ef4444' : 'var(--primary-color)'}
            onMouseOut={(e) => e.currentTarget.style.borderColor = 'var(--border-color)'}
          >
            <Heart 
              size={22} 
              color={isWishlisted ? '#ef4444' : 'var(--text-muted)'} 
              fill={isWishlisted ? '#ef4444' : 'none'} 
              style={{ transition: 'all 0.2s' }}
            />
          </button>
        )}

        {/* Book Cover */}
        <div style={{ flex: '1 1 300px', maxWidth: '400px', backgroundColor: 'var(--bg-color)', borderRadius: 'var(--radius)', overflow: 'hidden' }}>
          <img
            src={book.img || DEFAULT_COVER}
            alt={book.title}
            onError={(e) => { e.target.onerror = null; e.target.src = DEFAULT_COVER; }}
            style={{ width: '100%', height: 'auto', objectFit: 'cover' }}
          />
        </div>

        {/* Book Details */}
        <div style={{ flex: '2 1 400px' }}>
          <div style={{ marginBottom: '1.5rem' }}>
            <h1 style={{ fontSize: '2.5rem', marginBottom: '0.5rem', color: 'var(--text-color)' }}>{book.title}</h1>
            <p style={{ fontSize: '1.2rem', color: 'var(--text-muted)' }}>By {book.author}</p>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1.5rem' }}>
            {book.listings && book.listings.length > 0 && book.listings[0].copiesAvailable > 0 ? (
              <>
                <span style={{ fontSize: '2rem', fontWeight: 'bold', color: 'var(--primary-color)' }}>
                  {book.listings[0].currency || '$'} {book.listings[0].price?.toFixed(2)}
                </span>
                <span style={{
                  padding: '0.3rem 0.8rem',
                  backgroundColor: 'rgba(34, 197, 94, 0.1)',
                  color: '#22c55e',
                  borderRadius: '20px',
                  fontWeight: '500',
                  fontSize: '0.85rem'
                }}>
                  {book.listings.reduce((sum, l) => sum + l.copiesAvailable, 0)} total in stock
                </span>
              </>
            ) : (
              <span style={{ padding: '0.3rem 0.8rem', backgroundColor: 'rgba(239, 68, 68, 0.1)', color: 'var(--danger-color)', borderRadius: '20px', fontWeight: '500', fontSize: '0.85rem' }}>
                Out of Stock
              </span>
            )}
          </div>

          <div style={{ marginBottom: '1.5rem' }}>
            <h3 style={{ marginBottom: '0.5rem', fontSize: '1.1rem' }}>Description</h3>
            <p style={{ color: 'var(--text-muted)', lineHeight: '1.6', fontSize: '0.95rem' }}>
              {book.description || 'No description available for this book.'}
            </p>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '2rem', padding: '1rem 1.5rem', backgroundColor: 'rgba(255, 255, 255, 0.03)', borderRadius: 'var(--radius)' }}>
            <div>
              <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Category</span>
              <p style={{ fontWeight: '500', margin: '2px 0 0 0' }}>{book.category}</p>
            </div>
            <div>
              <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>ISBN</span>
              <p style={{ fontWeight: '500', margin: '2px 0 0 0' }}>{book.isbn}</p>
            </div>
          </div>

          {book.listings && book.listings.length > 0 && book.listings[0].copiesAvailable > 0 && (
            <button
              onClick={() => handleAddToCart(book.listings[0].listingId)}
              disabled={addingToCartId !== null}
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '0.75rem',
                padding: '1rem 2rem',
                fontSize: '1.1rem',
                width: '100%',
                maxWidth: '300px'
              }}
            >
              {addingToCartId === book.listings[0].listingId ? (
                <Loader2 className="animate-spin" size={20} />
              ) : (
                <ShoppingCart size={20} />
              )}
              {addingToCartId === book.listings[0].listingId ? 'Adding...' : 'Add to Cart'}
            </button>
          )}
        </div>
      </div>

      {/* Sellers Offers Table */}
      <div className="card glass-panel" style={{ marginTop: '2rem', padding: '1.5rem' }}>
        <h2 style={{ fontSize: '1.4rem', marginBottom: '1rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.5rem' }}>
          Compare Sellers & Offers
        </h2>
        {book.listings && book.listings.length > 0 ? (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
              <thead>
                <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-muted)' }}>
                  <th style={{ padding: '0.75rem 1rem' }}>Seller</th>
                  <th style={{ padding: '0.75rem 1rem' }}>Availability</th>
                  <th style={{ padding: '0.75rem 1rem' }}>Price</th>
                  <th style={{ padding: '0.75rem 1rem', textAlign: 'right' }}>Action</th>
                </tr>
              </thead>
              <tbody>
                {book.listings.map((offer, idx) => {
                  const isCheapest = idx === 0 && offer.copiesAvailable > 0;
                  return (
                    <tr 
                      key={offer.listingId} 
                      style={{ 
                        borderBottom: '1px solid rgba(255,255,255,0.03)',
                        transition: 'background-color 0.2s'
                      }}
                    >
                      <td style={{ padding: '1rem' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', flexWrap: 'wrap' }}>
                          <span style={{ fontWeight: '600' }}>{offer.sellerUsername || 'Library Direct'}</span>
                          {isCheapest && (
                            <span style={{ fontSize: '0.7rem', padding: '0.1rem 0.4rem', backgroundColor: 'rgba(34, 197, 94, 0.15)', color: '#22c55e', borderRadius: '4px', fontWeight: '600' }}>
                              Best Price
                            </span>
                          )}
                        </div>
                      </td>
                      <td style={{ padding: '1rem' }}>
                        {offer.copiesAvailable > 0 ? (
                          <span style={{ color: '#22c55e', fontSize: '0.9rem', fontWeight: '500' }}>
                            {offer.copiesAvailable} in stock
                          </span>
                        ) : (
                          <span style={{ color: 'var(--danger-color)', fontSize: '0.9rem', fontWeight: '500' }}>
                            Out of stock
                          </span>
                        )}
                      </td>
                      <td style={{ padding: '1rem', fontWeight: '600', color: 'var(--primary-color)', fontSize: '1.05rem' }}>
                        {offer.currency || '$'} {offer.price?.toFixed(2)}
                      </td>
                      <td style={{ padding: '1rem', textAlign: 'right' }}>
                        <button
                          onClick={() => handleAddToCart(offer.listingId)}
                          disabled={offer.copiesAvailable <= 0 || addingToCartId !== null}
                          style={{
                            padding: '0.4rem 1rem',
                            fontSize: '0.85rem',
                            background: 'var(--primary-color)',
                            border: '1px solid var(--primary-color)',
                            color: '#fff',
                            borderRadius: 'var(--radius)',
                            cursor: 'pointer',
                            transition: 'all 0.2s'
                          }}
                        >
                          {addingToCartId === offer.listingId ? 'Adding…' : 'Add to Cart'}
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        ) : (
          <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem', margin: 0 }}>
            No active listings found for this book.
          </p>
        )}
      </div>

      {/* Related Books Section */}
      <RelatedBooks category={book.category} currentBookId={book.bookId} />

      {/* Reviews Section */}
      <ReviewSection bookId={id} />
    </div>
  );
};

export default BookDetailsPage;
