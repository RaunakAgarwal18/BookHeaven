import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import apiClient from '../api/axios';
import { 
  Plus, 
  Edit2, 
  Trash2, 
  X, 
  BookOpen, 
  DollarSign, 
  Layers, 
  Package, 
  User, 
  MapPin, 
  Calendar, 
  Check, 
  Clock, 
  CheckCircle,
  Truck, 
  AlertTriangle,
  Search
} from 'lucide-react';

const BOOK_CATEGORIES = [
  "Art & Photography",
  "Biography & Memoir",
  "Business & Economics",
  "Children's Books",
  "Cookbooks, Food & Wine",
  "Fantasy",
  "Fiction",
  "Graphic Novels & Comics",
  "Health & Fitness",
  "History",
  "Law",
  "Mystery & Thriller",
  "Non-Fiction",
  "Philosophy",
  "Poetry",
  "Politics & Social Sciences",
  "Religion & Spirituality",
  "Romance",
  "Science & Math",
  "Science Fiction",
  "Self-Help",
  "Technology & Computer Science",
  "Travel",
  "Young Adult (YA)",
  "Other"
];

const mapCategory = (fetchedCategory) => {
  if (!fetchedCategory) return "Other";
  const lower = fetchedCategory.toLowerCase();
  if (lower.includes('science fiction') || lower.includes('sci-fi') || lower.includes('sci fi')) return "Science Fiction";
  if (lower.includes('fantasy')) return "Fantasy";
  if (lower.includes('mystery') || lower.includes('thriller') || lower.includes('suspense')) return "Mystery & Thriller";
  if (lower.includes('romance')) return "Romance";
  if (lower.includes('biography') || lower.includes('memoir') || lower.includes('autobiography')) return "Biography & Memoir";
  if (lower.includes('comput') || lower.includes('tech') || lower.includes('programming') || lower.includes('software')) return "Technology & Computer Science";
  if (lower.includes('science') || lower.includes('math') || lower.includes('physics')) return "Science & Math";
  if (lower.includes('history')) return "History";
  if (lower.includes('business') || lower.includes('economic') || lower.includes('finance')) return "Business & Economics";
  if (lower.includes('self-help') || lower.includes('self help') || lower.includes('motivation')) return "Self-Help";
  if (lower.includes('children') || lower.includes('juvenile')) return "Children's Books";
  if (lower.includes('art') || lower.includes('photo')) return "Art & Photography";
  if (lower.includes('cook') || lower.includes('food') || lower.includes('wine') || lower.includes('culinary')) return "Cookbooks, Food & Wine";
  if (lower.includes('health') || lower.includes('fitness') || lower.includes('diet')) return "Health & Fitness";
  if (lower.includes('travel')) return "Travel";
  if (lower.includes('graphic novel') || lower.includes('comic')) return "Graphic Novels & Comics";
  if (lower.includes('poetry') || lower.includes('poem')) return "Poetry";
  if (lower.includes('young adult') || lower.includes('ya')) return "Young Adult (YA)";
  if (lower.includes('religion') || lower.includes('spiritual')) return "Religion & Spirituality";
  if (lower.includes('politic') || lower.includes('social science')) return "Politics & Social Sciences";
  if (lower.includes('philosophy')) return "Philosophy";
  if (lower.includes('law') || lower.includes('legal')) return "Law";
  if (lower.includes('non-fiction') || lower.includes('nonfiction')) return "Non-Fiction";
  if (lower.includes('fiction')) return "Fiction";
  return "Other";
};

const SellerDashboardPage = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('inventory');
  const [books, setBooks] = useState([]);
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [ordersLoading, setOrdersLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingBook, setEditingBook] = useState(null);
  const [error, setError] = useState('');
  const [isFetchingDetails, setIsFetchingDetails] = useState(false);
  
  const [formData, setFormData] = useState({
    title: '',
    author: '',
    description: '',
    category: 'Fiction', // default category
    img: '',
    copies: 5,
    isbn: '',
    price: 99.00,
    currency: 'INR'
  });

  useEffect(() => {
    fetchSellerCatalog();
    fetchSellerOrders();
  }, []);

  const fetchSellerCatalog = async () => {
    setLoading(true);
    try {
      const res = await apiClient.get('/book/seller');
      setBooks(res.data.content || []);
    } catch (err) {
      console.error('Failed to load seller catalog', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchSellerOrders = async () => {
    setOrdersLoading(true);
    try {
      const res = await apiClient.get('/order/seller');
      setOrders(res.data.content || []);
    } catch (err) {
      console.error('Failed to load seller orders', err);
    } finally {
      setOrdersLoading(false);
    }
  };

  const handleOpenAdd = () => {
    setEditingBook(null);
    setFormData({
      title: '',
      author: '',
      description: '',
      category: 'Fiction',
      img: '',
      copies: 5,
      isbn: '',
      price: 99.00,
      currency: 'INR'
    });
    setError('');
    setShowModal(true);
  };

  const handleOpenEdit = (book) => {
    setEditingBook(book);
    setFormData({
      title: book.title,
      author: book.author,
      description: book.description || '',
      category: book.category,
      img: book.img || '',
      copies: book.copies,
      isbn: book.isbn,
      price: book.price,
      currency: book.currency
    });
    setError('');
    setShowModal(true);
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setError('');
    try {
      if (editingBook) {
        // Update pricing/inventory for existing listing
        await apiClient.put(`/book/listing/${editingBook.listingId}`, {
          copies: formData.copies,
          price: formData.price,
          currency: formData.currency
        });
        
        // Also update canonical book metadata
        await apiClient.put(`/book/${editingBook.bookId}`, {
          title: formData.title,
          author: formData.author,
          description: formData.description,
          category: formData.category,
          isbn: formData.isbn,
          img: formData.img
        });
      } else {
        await apiClient.post('/book', formData);
      }
      setShowModal(false);
      fetchSellerCatalog();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save book listing.');
    }
  };

  const handleDelete = async (listingId) => {
    if (!window.confirm('Are you sure you want to delete this listing?')) return;
    try {
      await apiClient.delete(`/book/listing/${listingId}`);
      fetchSellerCatalog();
    } catch (err) {
      alert('Failed to delete listing.');
    }
  };

  const handleUpdateOrderStatus = async (orderId, newStatus) => {
    if (!window.confirm(`Are you sure you want to mark this order as ${newStatus}?`)) return;
    try {
      await apiClient.put(`/order/seller/${orderId}/status?status=${newStatus}`);
      alert(`Order updated to ${newStatus}!`);
      fetchSellerOrders();
    } catch (err) {
      alert(err.response?.data?.message || `Failed to update status to ${newStatus}.`);
    }
  };

  const handleAutoFill = async () => {
    if (!formData.isbn || formData.isbn.trim() === '') {
      setError('Please enter an ISBN first to auto-fill details.');
      return;
    }
    
    setError('');
    setIsFetchingDetails(true);
    try {
      const isbn = formData.isbn.trim();
      let newDetails = {
        title: '',
        author: '',
        description: '',
        category: '',
        img: ''
      };

      // 1. Try Google Books API
      try {
        const gRes = await fetch(`https://www.googleapis.com/books/v1/volumes?q=isbn:${isbn}`);
        const gData = await gRes.json();
        
        if (gData.items && gData.items.length > 0) {
          const vi = gData.items[0].volumeInfo;
          newDetails.title = vi.title || '';
          newDetails.author = vi.authors ? vi.authors.join(', ') : '';
          newDetails.description = vi.description || '';
          newDetails.category = (vi.categories && vi.categories.length > 0) ? mapCategory(vi.categories[0]) : '';
          newDetails.img = vi.imageLinks ? (vi.imageLinks.thumbnail || vi.imageLinks.smallThumbnail).replace('http:', 'https:') : '';
        }
      } catch (e) {
        console.error('Google Books API failed:', e);
      }

      // 2. Try Open Library API to fill in any missing gaps
      try {
        const olRes = await fetch(`https://openlibrary.org/search.json?isbn=${isbn}`);
        const olData = await olRes.json();
        
        if (olData.docs && olData.docs.length > 0) {
          const doc = olData.docs[0];
          
          if (!newDetails.title) newDetails.title = doc.title || '';
          if (!newDetails.author) newDetails.author = doc.author_name ? doc.author_name.join(', ') : '';
          if (!newDetails.category && doc.subject && doc.subject.length > 0) newDetails.category = mapCategory(doc.subject[0]);
          if (!newDetails.img && doc.cover_i) newDetails.img = `https://covers.openlibrary.org/b/id/${doc.cover_i}-M.jpg`;

          // If description is still missing, make the extra call to Open Library's Work endpoint
          if (!newDetails.description && doc.key) {
            const workRes = await fetch(`https://openlibrary.org${doc.key}.json`);
            const workData = await workRes.json();
            if (workData.description) {
              newDetails.description = typeof workData.description === 'string' 
                ? workData.description 
                : workData.description.value || '';
            }
          }
        }
      } catch (e) {
        console.error('Open Library API failed:', e);
      }

      // 3. Apply the combined data to the form
      if (newDetails.title || newDetails.author || newDetails.description || newDetails.category || newDetails.img) {
        setFormData(prev => ({
          ...prev,
          title: newDetails.title || prev.title,
          author: newDetails.author || prev.author,
          description: newDetails.description || prev.description,
          category: newDetails.category || prev.category,
          img: newDetails.img || prev.img,
        }));
      } else {
        setError('No book found for this ISBN in any database. Please enter details manually.');
      }
      
    } catch (err) {
      console.error('Error in auto-fill process:', err);
      setError('Failed to execute auto-fill process.');
    } finally {
      setIsFetchingDetails(false);
    }
  };

  // Status badge styling helper
  const getStatusStyle = (status) => {
    switch (status) {
      case 'PENDING':
        return { bg: 'rgba(234, 179, 8, 0.1)', color: '#eab308', icon: <Clock size={14} /> };
      case 'CONFIRMED':
        return { bg: 'rgba(59, 130, 246, 0.1)', color: '#3b82f6', icon: <CheckCircle size={14} /> };
      case 'SHIPPED':
        return { bg: 'rgba(168, 85, 247, 0.1)', color: '#a855f7', icon: <Truck size={14} /> };
      case 'DELIVERED':
        return { bg: 'rgba(34, 197, 94, 0.1)', color: '#22c55e', icon: <Check size={14} /> };
      case 'FAILED':
        return { bg: 'rgba(239, 68, 68, 0.1)', color: '#ef4444', icon: <AlertTriangle size={14} /> };
      case 'CANCELLED':
        return { bg: 'rgba(239, 68, 68, 0.1)', color: '#ef4444', icon: <X size={14} /> };
      case 'REFUNDED':
        return { bg: 'rgba(249, 115, 22, 0.1)', color: '#f97316', icon: <Clock size={14} /> };
      default:
        return { bg: 'rgba(255, 255, 255, 0.1)', color: 'var(--text-color)', icon: null };
    }
  };

  return (
    <div className="container mt-8 mb-8">
      {/* Header Section */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '2.2rem', marginBottom: '0.25rem', background: 'linear-gradient(90deg, var(--primary-color), #8b5cf6)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
            Merchant Dashboard
          </h1>
          <p style={{ color: 'var(--text-muted)' }}>Manage catalog listings, track incoming customer orders, and handle fulfillment.</p>
        </div>
        {activeTab === 'inventory' && (
          <button onClick={handleOpenAdd} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Plus size={18} /> Add New Listing
          </button>
        )}
      </div>

      {/* Navigation Tabs */}
      <div style={{ display: 'flex', gap: '1rem', marginBottom: '2rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.1rem' }}>
        <button 
          onClick={() => setActiveTab('inventory')}
          style={{
            background: 'transparent',
            border: 'none',
            borderBottom: activeTab === 'inventory' ? '2px solid var(--primary-color)' : 'none',
            borderRadius: 0,
            padding: '0.75rem 1.25rem',
            color: activeTab === 'inventory' ? 'var(--primary-color)' : 'var(--text-muted)',
            fontWeight: '600',
            cursor: 'pointer',
            transition: 'all 0.2s',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem'
          }}
        >
          <Layers size={16} /> Book Inventory
        </button>
        <button 
          onClick={() => setActiveTab('sales')}
          style={{
            background: 'transparent',
            border: 'none',
            borderBottom: activeTab === 'sales' ? '2px solid var(--primary-color)' : 'none',
            borderRadius: 0,
            padding: '0.75rem 1.25rem',
            color: activeTab === 'sales' ? 'var(--primary-color)' : 'var(--text-muted)',
            fontWeight: '600',
            cursor: 'pointer',
            transition: 'all 0.2s',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem'
          }}
        >
          <Package size={16} /> Orders Received
        </button>
      </div>

      {/* TAB CONTENT: INVENTORY */}
      {activeTab === 'inventory' && (
        loading ? (
          <div className="text-center py-8">Loading catalog...</div>
        ) : books.length > 0 ? (
          <div className="card glass-panel" style={{ padding: '0', overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
              <thead>
                <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-muted)', fontSize: '0.9rem' }}>
                  <th style={{ padding: '1rem' }}>Book Info</th>
                  <th style={{ padding: '1rem' }}>ISBN</th>
                  <th style={{ padding: '1rem' }}>Category</th>
                  <th style={{ padding: '1rem' }}>Stock Available</th>
                  <th style={{ padding: '1rem' }}>Price</th>
                  <th style={{ padding: '1rem', textAlign: 'center' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {books.map(book => (
                  <tr 
                    key={book.listingId} 
                    onClick={() => navigate(`/books/${book.bookId}`)}
                    style={{ borderBottom: '1px solid rgba(255,255,255,0.05)', fontSize: '0.95rem', cursor: 'pointer', transition: 'background-color 0.2s' }}
                    onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'rgba(255,255,255,0.02)'}
                    onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                  >
                    <td style={{ padding: '1rem', display: 'flex', alignItems: 'center', gap: '1rem' }}>
                      {book.img ? (
                        <img src={book.img} alt={book.title} style={{ width: '38px', height: '56px', borderRadius: '4px', objectFit: 'cover' }} />
                      ) : (
                        <div style={{ width: '38px', height: '56px', borderRadius: '4px', backgroundColor: 'rgba(255,255,255,0.05)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                          <BookOpen size={16} />
                        </div>
                      )}
                      <div>
                        <div style={{ fontWeight: '600' }}>{book.title}</div>
                        <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem' }}>by {book.author}</div>
                      </div>
                    </td>
                    <td style={{ padding: '1rem' }}>{book.isbn}</td>
                    <td style={{ padding: '1rem' }}>{book.category}</td>
                    <td style={{ padding: '1rem' }}>{book.copiesAvailable} / {book.copies}</td>
                    <td style={{ padding: '1rem', fontWeight: '600' }}>{book.currency} {book.price.toFixed(2)}</td>
                    <td style={{ padding: '1rem', textAlign: 'center' }}>
                      <div style={{ display: 'flex', justifyContent: 'center', gap: '0.75rem' }}>
                        <button onClick={(e) => { e.stopPropagation(); handleOpenEdit(book); }} style={{ padding: '0.4rem', background: 'rgba(139, 92, 246, 0.1)', border: 'none', color: 'var(--primary-color)', cursor: 'pointer' }}>
                          <Edit2 size={16} />
                        </button>
                        <button onClick={(e) => { e.stopPropagation(); handleDelete(book.listingId); }} style={{ padding: '0.4rem', background: 'rgba(239, 68, 68, 0.1)', border: 'none', color: 'var(--danger-color)', cursor: 'pointer' }}>
                          <Trash2 size={16} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="card text-center py-8">
            <h3 style={{ color: 'var(--text-muted)' }}>You have no active book listings.</h3>
            <button onClick={handleOpenAdd} style={{ marginTop: '1rem' }}>List Your First Book</button>
          </div>
        )
      )}

      {/* TAB CONTENT: ORDERS RECEIVED */}
      {activeTab === 'sales' && (
        ordersLoading ? (
          <div className="text-center py-8">Loading sales orders...</div>
        ) : orders.length > 0 ? (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
            {orders.filter(o => o.status !== 'FAILED').map(order => {
              const statusStyle = getStatusStyle(order.status);
              return (
                <div key={order.id} className="card glass-panel" style={{ padding: '1.5rem' }}>
                  {/* Order Card Header */}
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid rgba(255,255,255,0.05)', paddingBottom: '1rem', marginBottom: '1rem', flexWrap: 'wrap', gap: '1rem' }}>
                    <div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', flexWrap: 'wrap' }}>
                        <span style={{ fontWeight: '700', fontSize: '1.1rem' }}>Order #{order.orderReference || (order.id.substring(0, 8) + '...')}</span>
                        <span style={{ 
                          display: 'inline-flex', 
                          alignItems: 'center', 
                          gap: '0.25rem', 
                          padding: '0.25rem 0.6rem', 
                          borderRadius: '20px', 
                          backgroundColor: statusStyle.bg, 
                          color: statusStyle.color, 
                          fontSize: '0.8rem',
                          fontWeight: '600'
                        }}>
                          {statusStyle.icon}
                          {order.status}
                        </span>
                      </div>
                      <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.25rem', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                        <Calendar size={12} /> Ordered: {new Date(order.createdAt).toLocaleString()}
                      </div>
                    </div>
                    
                    {/* Action Panel for fulfillment */}
                    <div>
                      {order.status === 'CONFIRMED' && (
                        <button 
                          onClick={() => handleUpdateOrderStatus(order.id, 'SHIPPED')}
                          style={{
                            display: 'inline-flex',
                            alignItems: 'center',
                            gap: '0.4rem',
                            padding: '0.5rem 1rem',
                            backgroundColor: 'rgba(168, 85, 247, 0.2)',
                            color: '#a855f7',
                            border: '1px solid rgba(168, 85, 247, 0.4)',
                            fontSize: '0.85rem'
                          }}
                        >
                          <Truck size={14} /> Ship Order Items
                        </button>
                      )}
                      {order.status === 'SHIPPED' && (
                        <button 
                          onClick={() => handleUpdateOrderStatus(order.id, 'DELIVERED')}
                          style={{
                            display: 'inline-flex',
                            alignItems: 'center',
                            gap: '0.4rem',
                            padding: '0.5rem 1rem',
                            backgroundColor: 'rgba(34, 197, 94, 0.2)',
                            color: '#22c55e',
                            border: '1px solid rgba(34, 197, 94, 0.4)',
                            fontSize: '0.85rem'
                          }}
                        >
                          <Check size={14} /> Mark as Delivered
                        </button>
                      )}
                      {['DELIVERED', 'CANCELLED', 'FAILED', 'REFUNDED', 'PENDING'].includes(order.status) && (
                        <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontStyle: 'italic' }}>
                          Fulfillment locked ({order.status.toLowerCase()})
                        </span>
                      )}
                    </div>
                  </div>

                  {/* Order Details Body */}
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.5rem' }}>
                    {/* Customer & Shipping column */}
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: '600', color: 'var(--primary-color)', fontSize: '0.95rem' }}>
                        <User size={16} /> Customer Details
                      </div>
                      <div style={{ padding: '0.75rem', backgroundColor: 'rgba(255,255,255,0.02)', borderRadius: '6px', fontSize: '0.9rem' }}>
                        <div><strong>Username:</strong> {order.username}</div>
                      </div>

                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: '600', color: 'var(--primary-color)', fontSize: '0.95rem', marginTop: '0.25rem' }}>
                        <MapPin size={16} /> Shipping Address
                      </div>
                      <div style={{ padding: '0.75rem', backgroundColor: 'rgba(255,255,255,0.02)', borderRadius: '6px', fontSize: '0.9rem', lineHeight: '1.4' }}>
                        <div>{order.shippingAddress?.street}</div>
                        <div>{order.shippingAddress?.city}, {order.shippingAddress?.state} {order.shippingAddress?.zipCode}</div>
                        <div>{order.shippingAddress?.country}</div>
                        {order.shippingAddress?.phoneNumber && (
                          <div style={{ marginTop: '0.5rem', color: 'var(--text-muted)' }}>
                            <strong>Phone:</strong> {order.shippingAddress.phoneNumber}
                          </div>
                        )}
                      </div>
                    </div>

                    {/* Ordered Items list column */}
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: '600', color: 'var(--primary-color)', fontSize: '0.95rem' }}>
                        <BookOpen size={16} /> Your Items Sold
                      </div>
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                        {order.items.map((item, idx) => (
                          <div key={idx} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.75rem', backgroundColor: 'rgba(255,255,255,0.02)', borderRadius: '6px', fontSize: '0.9rem' }}>
                            <div>
                              <div style={{ fontWeight: '600' }}>{item.title}</div>
                              <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem' }}>Qty: {item.quantity} x {item.currency} {item.price.toFixed(2)}</div>
                            </div>
                            <div style={{ fontWeight: '600', color: 'var(--primary-color)' }}>
                              {item.currency} {(item.price * item.quantity).toFixed(2)}
                            </div>
                          </div>
                        ))}
                      </div>

                      {/* Total sales section */}
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '1rem 0.75rem', borderTop: '1px solid rgba(255,255,255,0.05)', marginTop: '0.5rem' }}>
                        <span style={{ fontWeight: '600' }}>Your Earnings subtotal:</span>
                        <span style={{ fontSize: '1.25rem', fontWeight: '700', color: 'var(--primary-color)' }}>
                          {order.currency} {order.totalAmount.toFixed(2)}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        ) : (
          <div className="card text-center py-8">
            <h3 style={{ color: 'var(--text-muted)' }}>You haven't received any orders yet.</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginTop: '0.5rem' }}>When customers buy your book offers, they will show up here for you to fulfill!</p>
          </div>
        )
      )}

      {/* Modal Dialog */}
      {showModal && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.6)', backdropFilter: 'blur(4px)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 2000 }}>
          <div className="card glass-panel" style={{ width: '100%', maxWidth: '600px', margin: '1rem', maxHeight: '90vh', overflowY: 'auto', position: 'relative' }}>
            <button onClick={() => setShowModal(false)} style={{ position: 'absolute', top: '1rem', right: '1rem', background: 'transparent', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}>
              <X size={20} />
            </button>
            <h2 className="mb-4">{editingBook ? 'Edit Book Listing' : 'List New Book'}</h2>
            
            {error && <div className="form-error text-center mb-4">{error}</div>}

            <form onSubmit={handleSave}>
              <div className="form-group">
                <label className="form-label">Book Title *</label>
                <input type="text" value={formData.title} onChange={e => setFormData({...formData, title: e.target.value})} required />
              </div>
              <div className="form-group">
                <label className="form-label">Author *</label>
                <input type="text" value={formData.author} onChange={e => setFormData({...formData, author: e.target.value})} required />
              </div>
              <div className="form-group">
                <label className="form-label">Description</label>
                <textarea rows="3" value={formData.description} onChange={e => setFormData({...formData, description: e.target.value})} style={{ resize: 'none' }} />
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div className="form-group">
                  <label className="form-label">Category *</label>
                  <select 
                    value={formData.category} 
                    onChange={e => setFormData({...formData, category: e.target.value})} 
                    required 
                    style={{
                      width: '100%', 
                      backgroundColor: '#1e293b', 
                      color: 'var(--text-color)', 
                      border: '1px solid var(--border-color)', 
                      borderRadius: '8px', 
                      padding: '0.75rem', 
                      outline: 'none',
                      appearance: 'none',
                      WebkitAppearance: 'none',
                      MozAppearance: 'none',
                      backgroundImage: `url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='24' height='24' viewBox='0 0 24 24' fill='none' stroke='%2394a3b8' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><polyline points='6 9 12 15 18 9'></polyline></svg>")`,
                      backgroundRepeat: 'no-repeat',
                      backgroundPosition: 'right 0.75rem center',
                      backgroundSize: '1rem'
                    }}
                  >
                    {BOOK_CATEGORIES.map(cat => (
                      <option key={cat} value={cat} style={{ backgroundColor: '#1e293b', color: '#f8fafc' }}>
                        {cat}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="form-group">
                  <label className="form-label">ISBN *</label>
                  <div style={{ display: 'flex', gap: '0.5rem' }}>
                    <input type="text" value={formData.isbn} onChange={e => setFormData({...formData, isbn: e.target.value})} required style={{ flex: 1 }} />
                    <button 
                      type="button" 
                      onClick={handleAutoFill} 
                      disabled={isFetchingDetails || !formData.isbn}
                      style={{ 
                        padding: '0 1rem', 
                        display: 'flex', 
                        alignItems: 'center', 
                        gap: '0.5rem',
                        backgroundColor: 'var(--primary-color)',
                        opacity: (isFetchingDetails || !formData.isbn) ? 0.7 : 1,
                        cursor: (isFetchingDetails || !formData.isbn) ? 'not-allowed' : 'pointer'
                      }}
                    >
                      <Search size={16} />
                      {isFetchingDetails ? 'Fetching...' : 'Auto-Fill'}
                    </button>
                  </div>
                </div>
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div className="form-group">
                  <label className="form-label">Stock Copies *</label>
                  <input type="number" min="0" value={formData.copies} onChange={e => setFormData({...formData, copies: parseInt(e.target.value) || 0})} required />
                </div>
                <div className="form-group">
                  <label className="form-label">Pricing *</label>
                  <div style={{ display: 'flex', gap: '0.5rem' }}>
                    <select 
                      value={formData.currency} 
                      onChange={e => setFormData({...formData, currency: e.target.value})} 
                      style={{ 
                        width: '85px', 
                        backgroundColor: '#1e293b', 
                        color: 'var(--text-color)', 
                        border: '1px solid var(--border-color)', 
                        borderRadius: '8px', 
                        padding: '0.5rem 1.8rem 0.5rem 0.5rem', 
                        outline: 'none',
                        appearance: 'none',
                        WebkitAppearance: 'none',
                        MozAppearance: 'none',
                        backgroundImage: `url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='24' height='24' viewBox='0 0 24 24' fill='none' stroke='%2394a3b8' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><polyline points='6 9 12 15 18 9'></polyline></svg>")`,
                        backgroundRepeat: 'no-repeat',
                        backgroundPosition: 'right 0.5rem center',
                        backgroundSize: '0.95rem'
                      }}
                    >
                      <option value="USD" style={{ backgroundColor: '#1e293b', color: '#f8fafc' }}>USD</option>
                      <option value="INR" style={{ backgroundColor: '#1e293b', color: '#f8fafc' }}>INR</option>
                    </select>
                    <input type="number" step="0.01" min="0.01" value={formData.price} onChange={e => setFormData({...formData, price: parseFloat(e.target.value) || 0.01})} required style={{ flex: 1 }} />
                  </div>
                </div>
              </div>
              <div className="form-group">
                <label className="form-label">Image URL</label>
                <input type="text" value={formData.img} onChange={e => setFormData({...formData, img: e.target.value})} />
              </div>
              
              <button type="submit" style={{ width: '100%', marginTop: '1.5rem', cursor: 'pointer' }}>
                {editingBook ? 'Save Changes' : 'Submit Book Listing'}
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default SellerDashboardPage;
