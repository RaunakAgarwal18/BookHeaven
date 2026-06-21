import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import apiClient from '../api/axios';
import useAuthStore from '../store/useAuthStore';
import { Tag, Plus, Loader2, Trash2, Power, AlertTriangle, CheckCircle, XCircle } from 'lucide-react';

const SellerCouponsPage = () => {
  const { user, isAuthenticated } = useAuthStore();
  const navigate = useNavigate();
  const [coupons, setCoupons] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [confirmDelete, setConfirmDelete] = useState(null); // coupon to delete
  const [togglingId, setTogglingId] = useState(null);
  const [deletingId, setDeletingId] = useState(null);
  const [toast, setToast] = useState(null); // { message, type: 'success' | 'error' }
  
  // New coupon form state
  const [code, setCode] = useState('');
  const [discountType, setDiscountType] = useState('PERCENTAGE');
  const [discountValue, setDiscountValue] = useState('');
  const [maxDiscountAmount, setMaxDiscountAmount] = useState('');
  const [minOrderAmount, setMinOrderAmount] = useState('');
  const [startDate, setStartDate] = useState('');
  const [expiryDate, setExpiryDate] = useState('');
  const [usageLimit, setUsageLimit] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login', { replace: true });
      return;
    }
    if (user?.role !== 'SELLER') {
      navigate('/');
      return;
    }
    fetchCoupons();
  }, [isAuthenticated, navigate, user]);

  // Auto-dismiss toast after 3 seconds
  useEffect(() => {
    if (toast) {
      const timer = setTimeout(() => setToast(null), 3000);
      return () => clearTimeout(timer);
    }
  }, [toast]);

  const showToast = (message, type = 'success') => {
    setToast({ message, type });
  };

  const fetchCoupons = async () => {
    setLoading(true);
    try {
      const res = await apiClient.get('/coupons'); // Admin endpoint mapped via gateway
      setCoupons(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleToggleStatus = async (coupon) => {
    setTogglingId(coupon.id);
    try {
      await apiClient.patch(`/coupons/${coupon.id}/toggle`);
      const newStatus = coupon.status === 'ACTIVE' ? 'deactivated' : 'activated';
      showToast(`Coupon "${coupon.code}" ${newStatus} successfully`);
      fetchCoupons();
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to toggle coupon status', 'error');
    } finally {
      setTogglingId(null);
    }
  };

  const handleDeleteCoupon = async (coupon) => {
    setDeletingId(coupon.id);
    try {
      await apiClient.delete(`/coupons/${coupon.id}`);
      showToast(`Coupon "${coupon.code}" deleted successfully`);
      setConfirmDelete(null);
      fetchCoupons();
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to delete coupon', 'error');
      setConfirmDelete(null);
    } finally {
      setDeletingId(null);
    }
  };

  const handleCreateCoupon = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      const getLocalISOString = () => {
        const tzoffset = (new Date()).getTimezoneOffset() * 60000;
        return (new Date(Date.now() - tzoffset)).toISOString().slice(0, 19); 
      };

      const payload = {
        code: code.toUpperCase(),
        discountType,
        discountValue: parseFloat(discountValue),
        maxDiscountAmount: maxDiscountAmount ? parseFloat(maxDiscountAmount) : null,
        minOrderAmount: minOrderAmount ? parseFloat(minOrderAmount) : null,
        startDate: startDate ? startDate + ':00' : getLocalISOString(),
        expiryDate: expiryDate + ':00',
        usageLimit: parseInt(usageLimit)
      };
      await apiClient.post('/coupons', payload);
      setShowModal(false);
      resetForm();
      showToast(`Coupon "${payload.code}" created successfully`);
      fetchCoupons();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create coupon');
    } finally {
      setSubmitting(false);
    }
  };

  const resetForm = () => {
    setCode('');
    setDiscountType('PERCENTAGE');
    setDiscountValue('');
    setMaxDiscountAmount('');
    setMinOrderAmount('');
    setStartDate('');
    setExpiryDate('');
    setUsageLimit('');
  };

  if (loading) return <div className="text-center mt-8">Loading coupons...</div>;

  return (
    <div className="container mt-8 mb-8" style={{ maxWidth: '1000px' }}>
      {/* Toast Notification */}
      {toast && (
        <div style={{
          position: 'fixed',
          top: 80,
          right: 24,
          zIndex: 2000,
          padding: '0.85rem 1.25rem',
          borderRadius: 'var(--radius)',
          display: 'flex',
          alignItems: 'center',
          gap: '0.6rem',
          fontSize: '0.9rem',
          fontWeight: '600',
          boxShadow: '0 8px 24px rgba(0,0,0,0.4)',
          animation: 'slideInRight 0.3s ease-out',
          backgroundColor: toast.type === 'success' ? 'rgba(34, 197, 94, 0.15)' : 'rgba(239, 68, 68, 0.15)',
          border: `1px solid ${toast.type === 'success' ? 'rgba(34, 197, 94, 0.3)' : 'rgba(239, 68, 68, 0.3)'}`,
          color: toast.type === 'success' ? '#22c55e' : '#ef4444',
          backdropFilter: 'blur(12px)',
        }}>
          {toast.type === 'success' ? <CheckCircle size={18} /> : <XCircle size={18} />}
          {toast.message}
        </div>
      )}

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1 style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <Tag size={28} style={{ color: 'var(--primary-color)' }} />
          Coupon Management
        </h1>
        <button 
          onClick={() => setShowModal(true)} 
          style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', padding: '0.6rem 1.2rem' }}
        >
          <Plus size={18} /> Create Coupon
        </button>
      </div>

      <div className="card glass-panel" style={{ overflowX: 'auto' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
          <thead>
            <tr style={{ borderBottom: '1px solid var(--border-color)' }}>
              <th style={{ padding: '1rem', color: 'var(--text-muted)' }}>Code</th>
              <th style={{ padding: '1rem', color: 'var(--text-muted)' }}>Type</th>
              <th style={{ padding: '1rem', color: 'var(--text-muted)' }}>Value</th>
              <th style={{ padding: '1rem', color: 'var(--text-muted)' }}>Min Order</th>
              <th style={{ padding: '1rem', color: 'var(--text-muted)' }}>Usage</th>
              <th style={{ padding: '1rem', color: 'var(--text-muted)' }}>Expiry</th>
              <th style={{ padding: '1rem', color: 'var(--text-muted)' }}>Status</th>
              <th style={{ padding: '1rem', color: 'var(--text-muted)', textAlign: 'right' }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {coupons.map(coupon => (
              <tr key={coupon.id} style={{ borderBottom: '1px solid var(--border-color)' }}>
                <td style={{ padding: '1rem', fontWeight: 'bold' }}>{coupon.code}</td>
                <td style={{ padding: '1rem' }}>{coupon.discountType}</td>
                <td style={{ padding: '1rem' }}>
                  {coupon.discountType === 'PERCENTAGE' ? `${coupon.discountValue}%` : `₹${coupon.discountValue}`}
                  {coupon.maxDiscountAmount && ` (Max: ₹${coupon.maxDiscountAmount})`}
                </td>
                <td style={{ padding: '1rem' }}>{coupon.minOrderAmount ? `₹${coupon.minOrderAmount}` : '-'}</td>
                <td style={{ padding: '1rem' }}>{coupon.usageCount} / {coupon.usageLimit}</td>
                <td style={{ padding: '1rem' }}>{new Date(coupon.expiryDate).toLocaleDateString()}</td>
                <td style={{ padding: '1rem' }}>
                  <span style={{ 
                    padding: '0.25rem 0.75rem', 
                    borderRadius: '999px', 
                    fontSize: '0.8rem',
                    backgroundColor: coupon.status === 'ACTIVE' ? 'rgba(34, 197, 94, 0.1)' : 
                                     coupon.status === 'EXPIRED' ? 'rgba(239, 68, 68, 0.1)' : 'rgba(249, 115, 22, 0.1)',
                    color: coupon.status === 'ACTIVE' ? '#22c55e' : 
                           coupon.status === 'EXPIRED' ? '#ef4444' : '#f97316'
                  }}>
                    {coupon.status === 'ACTIVE' ? 'Active' : 
                     coupon.status === 'EXPIRED' ? 'Expired' : 'Inactive'}
                  </span>
                </td>
                <td style={{ padding: '1rem', textAlign: 'right' }}>
                  <div style={{ display: 'flex', gap: '0.25rem', justifyContent: 'flex-end' }}>
                    {/* Toggle Button */}
                    <button 
                      onClick={() => handleToggleStatus(coupon)}
                      disabled={togglingId === coupon.id || coupon.status === 'EXPIRED'}
                      style={{ 
                        background: 'transparent', 
                        border: '1px solid transparent',
                        borderRadius: '6px',
                        color: 'var(--text-color)', 
                        padding: '0.4rem', 
                        cursor: coupon.status === 'EXPIRED' ? 'not-allowed' : (togglingId === coupon.id ? 'wait' : 'pointer'),
                        transition: 'all 0.2s',
                        display: 'flex',
                        alignItems: 'center',
                        opacity: togglingId === coupon.id ? 0.5 : (coupon.status === 'EXPIRED' ? 0.3 : 1),
                      }}
                      title={coupon.status === 'EXPIRED' ? "Expired coupons cannot be toggled" : (coupon.status === 'ACTIVE' ? "Pause coupon (deactivate)" : "Activate coupon")}
                      onMouseOver={(e) => {
                        if (coupon.status === 'EXPIRED') return;
                        e.currentTarget.style.backgroundColor = coupon.status === 'ACTIVE' ? 'rgba(239, 68, 68, 0.1)' : 'rgba(34, 197, 94, 0.1)';
                        e.currentTarget.style.borderColor = coupon.status === 'ACTIVE' ? 'rgba(239, 68, 68, 0.3)' : 'rgba(34, 197, 94, 0.3)';
                      }}
                      onMouseOut={(e) => {
                        if (coupon.status === 'EXPIRED') return;
                        e.currentTarget.style.backgroundColor = 'transparent';
                        e.currentTarget.style.borderColor = 'transparent';
                      }}
                    >
                      {togglingId === coupon.id ? (
                        <Loader2 size={18} className="animate-spin" style={{ color: 'var(--text-muted)' }} />
                      ) : (
                        <Power size={18} color={coupon.status === 'EXPIRED' ? '#6b7280' : (coupon.status === 'ACTIVE' ? '#ef4444' : '#22c55e')} />
                      )}
                    </button>

                    {/* Delete Button */}
                    <button 
                      onClick={() => setConfirmDelete(coupon)}
                      style={{ 
                        background: 'transparent', 
                        border: '1px solid transparent',
                        borderRadius: '6px',
                        color: 'var(--text-muted)', 
                        padding: '0.4rem', 
                        cursor: 'pointer',
                        transition: 'all 0.2s',
                        display: 'flex',
                        alignItems: 'center',
                      }}
                      title="Delete coupon permanently"
                      onMouseOver={(e) => {
                        e.currentTarget.style.backgroundColor = 'rgba(239, 68, 68, 0.1)';
                        e.currentTarget.style.borderColor = 'rgba(239, 68, 68, 0.3)';
                        e.currentTarget.style.color = '#ef4444';
                      }}
                      onMouseOut={(e) => {
                        e.currentTarget.style.backgroundColor = 'transparent';
                        e.currentTarget.style.borderColor = 'transparent';
                        e.currentTarget.style.color = 'var(--text-muted)';
                      }}
                    >
                      <Trash2 size={18} />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
            {coupons.length === 0 && (
              <tr>
                <td colSpan="8" style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                  No coupons found. Create one to get started.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Delete Confirmation Modal */}
      {confirmDelete && (
        <div 
          style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.6)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}
          onClick={(e) => e.target === e.currentTarget && setConfirmDelete(null)}
        >
          <div className="card glass-panel" style={{ width: '90%', maxWidth: '420px', textAlign: 'center' }}>
            <div style={{ 
              width: 48, height: 48, borderRadius: '50%', margin: '0 auto 1rem',
              background: 'rgba(239, 68, 68, 0.1)', display: 'flex', alignItems: 'center', justifyContent: 'center'
            }}>
              <AlertTriangle size={24} style={{ color: '#ef4444' }} />
            </div>
            <h3 style={{ marginBottom: '0.5rem' }}>Delete Coupon</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '1.5rem' }}>
              Are you sure you want to permanently delete coupon <strong style={{ color: 'var(--text-color)' }}>"{confirmDelete.code}"</strong>? This action cannot be undone.
            </p>
            <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'center' }}>
              <button 
                type="button" 
                onClick={() => setConfirmDelete(null)} 
                style={{ 
                  padding: '0.6rem 1.5rem', background: 'transparent', 
                  color: 'var(--text-color)', border: '1px solid var(--border-color)',
                  borderRadius: 'var(--radius)', cursor: 'pointer',
                }}
              >
                Cancel
              </button>
              <button 
                onClick={() => handleDeleteCoupon(confirmDelete)}
                disabled={deletingId === confirmDelete.id}
                style={{ 
                  padding: '0.6rem 1.5rem', 
                  backgroundColor: '#ef4444', color: '#fff', border: 'none',
                  borderRadius: 'var(--radius)', cursor: deletingId ? 'wait' : 'pointer',
                  display: 'flex', alignItems: 'center', gap: '0.5rem',
                  opacity: deletingId ? 0.7 : 1,
                }}
              >
                {deletingId === confirmDelete.id ? (
                  <><Loader2 size={16} className="animate-spin" /> Deleting...</>
                ) : (
                  <><Trash2 size={16} /> Delete</>
                )}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Create Coupon Modal */}
      {showModal && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div className="card glass-panel" style={{ width: '90%', maxWidth: '500px', maxHeight: '90vh', overflowY: 'auto' }}>
            <h2 style={{ marginBottom: '1.5rem' }}>Create New Coupon</h2>
            {error && <div style={{ color: 'var(--danger-color)', marginBottom: '1rem' }}>{error}</div>}
            
            <form onSubmit={handleCreateCoupon} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-muted)' }}>Coupon Code</label>
                <input required type="text" value={code} onChange={e => setCode(e.target.value)} placeholder="e.g., SUMMER50" style={{ width: '100%', padding: '0.75rem', borderRadius: 'var(--radius)', border: '1px solid var(--border-color)', backgroundColor: 'var(--bg-color)', color: 'var(--text-color)' }} />
              </div>
              
              <div style={{ display: 'flex', gap: '1rem' }}>
                <div style={{ flex: 1 }}>
                  <label style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-muted)' }}>Discount Type</label>
                  <select value={discountType} onChange={e => setDiscountType(e.target.value)} style={{ width: '100%', padding: '0.75rem', borderRadius: 'var(--radius)', border: '1px solid var(--border-color)', backgroundColor: 'var(--bg-color)', color: 'var(--text-color)' }}>
                    <option value="PERCENTAGE">Percentage (%)</option>
                    <option value="FLAT">Flat Amount</option>
                  </select>
                </div>
                <div style={{ flex: 1 }}>
                  <label style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-muted)' }}>Value</label>
                  <input required type="number" min="0" step="0.01" value={discountValue} onChange={e => setDiscountValue(e.target.value)} style={{ width: '100%', padding: '0.75rem', borderRadius: 'var(--radius)', border: '1px solid var(--border-color)', backgroundColor: 'var(--bg-color)', color: 'var(--text-color)' }} />
                </div>
              </div>

              <div style={{ display: 'flex', gap: '1rem' }}>
                <div style={{ flex: 1 }}>
                  <label style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-muted)' }}>Max Discount Amount</label>
                  <input type="number" min="0" step="0.01" value={maxDiscountAmount} onChange={e => setMaxDiscountAmount(e.target.value)} placeholder="Optional" style={{ width: '100%', padding: '0.75rem', borderRadius: 'var(--radius)', border: '1px solid var(--border-color)', backgroundColor: 'var(--bg-color)', color: 'var(--text-color)' }} />
                </div>
                <div style={{ flex: 1 }}>
                  <label style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-muted)' }}>Min Order Amount</label>
                  <input type="number" min="0" step="0.01" value={minOrderAmount} onChange={e => setMinOrderAmount(e.target.value)} placeholder="Optional" style={{ width: '100%', padding: '0.75rem', borderRadius: 'var(--radius)', border: '1px solid var(--border-color)', backgroundColor: 'var(--bg-color)', color: 'var(--text-color)' }} />
                </div>
              </div>

              <div style={{ display: 'flex', gap: '1rem' }}>
                <div style={{ flex: 1 }}>
                  <label style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-muted)' }}>Start Date</label>
                  <input type="datetime-local" value={startDate} onChange={e => setStartDate(e.target.value)} style={{ width: '100%', padding: '0.75rem', borderRadius: 'var(--radius)', border: '1px solid var(--border-color)', backgroundColor: 'var(--bg-color)', color: 'var(--text-color)' }} />
                </div>
                <div style={{ flex: 1 }}>
                  <label style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-muted)' }}>Expiry Date</label>
                  <input required type="datetime-local" value={expiryDate} onChange={e => setExpiryDate(e.target.value)} style={{ width: '100%', padding: '0.75rem', borderRadius: 'var(--radius)', border: '1px solid var(--border-color)', backgroundColor: 'var(--bg-color)', color: 'var(--text-color)' }} />
                </div>
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-muted)' }}>Usage Limit</label>
                <input required type="number" min="1" value={usageLimit} onChange={e => setUsageLimit(e.target.value)} placeholder="e.g., 100" style={{ width: '100%', padding: '0.75rem', borderRadius: 'var(--radius)', border: '1px solid var(--border-color)', backgroundColor: 'var(--bg-color)', color: 'var(--text-color)' }} />
              </div>

              <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end', marginTop: '1rem' }}>
                <button type="button" onClick={() => setShowModal(false)} style={{ padding: '0.5rem 1rem', background: 'transparent', color: 'var(--text-color)', border: '1px solid var(--border-color)' }}>Cancel</button>
                <button type="submit" disabled={submitting} style={{ padding: '0.5rem 1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  {submitting ? <Loader2 size={16} className="animate-spin" /> : 'Create Coupon'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      <style>{`
        @keyframes slideInRight {
          from { transform: translateX(100%); opacity: 0; }
          to { transform: translateX(0); opacity: 1; }
        }
      `}</style>
    </div>
  );
};

export default SellerCouponsPage;
