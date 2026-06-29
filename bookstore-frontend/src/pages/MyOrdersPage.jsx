import { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Package, ChevronDown, ChevronUp, ChevronLeft, ChevronRight,
  Loader2, ShoppingBag, MapPin, CreditCard, Calendar, X, BookOpen,
} from 'lucide-react';
import apiClient from '../api/axios';
import useAuthStore from '../store/useAuthStore';

/* ─── Status config ──────────────────────────────────────────── */
const STATUS = {
  PENDING:   { label: 'Pending',   color: '#f59e0b', bg: 'rgba(245,158,11,0.12)',   border: 'rgba(245,158,11,0.35)'   },
  CONFIRMED: { label: 'Confirmed', color: '#3b82f6', bg: 'rgba(59,130,246,0.12)',   border: 'rgba(59,130,246,0.35)'   },
  SHIPPED:   { label: 'Shipped',   color: '#8b5cf6', bg: 'rgba(139,92,246,0.12)',   border: 'rgba(139,92,246,0.35)'   },
  DELIVERED: { label: 'Delivered', color: '#22c55e', bg: 'rgba(34,197,94,0.12)',    border: 'rgba(34,197,94,0.35)'    },
  FAILED:    { label: 'Failed',    color: '#ef4444', bg: 'rgba(239,68,68,0.12)',    border: 'rgba(239,68,68,0.35)'    },
  CANCELLED: { label: 'Cancelled', color: '#6b7280', bg: 'rgba(107,114,128,0.12)', border: 'rgba(107,114,128,0.35)'  },
};

const statusCfg = (s) => STATUS[s] ?? STATUS.PENDING;

const fmt = {
  date: (d) => new Date(d).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' }),
  time: (d) => new Date(d).toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' }),
  money: (amount, cur = 'INR') =>
    new Intl.NumberFormat('en-IN', { style: 'currency', currency: cur, maximumFractionDigits: 0 }).format(amount),
};

const shortId = (id) => id?.toString().split('-')[0].toUpperCase();

/* ─── Status Badge ───────────────────────────────────────────── */
const Badge = ({ status }) => {
  const { label, color, bg, border } = statusCfg(status);
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', gap: '0.35rem',
      padding: '0.25rem 0.7rem', borderRadius: '999px', fontSize: '0.78rem',
      fontWeight: '600', color, backgroundColor: bg,
      border: `1px solid ${border}`, whiteSpace: 'nowrap',
    }}>
      <span style={{ width: 7, height: 7, borderRadius: '50%', backgroundColor: color, display: 'inline-block' }} />
      {label}
    </span>
  );
};

/* ─── Single Order Card ──────────────────────────────────────── */
const OrderCard = ({ order, onCancel }) => {
  const [expanded, setExpanded] = useState(false);
  const [cancelling, setCancelling] = useState(false);
  const cfg = statusCfg(order.status);

  const handleCancel = async (e) => {
    e.stopPropagation();
    if (!window.confirm('Cancel this order?')) return;
    setCancelling(true);
    await onCancel(order.id);
    setCancelling(false);
  };

  const address = order.shippingAddress;
  const addressStr = address
    ? [address.street, address.city, address.state, address.zipCode, address.country].filter(Boolean).join(', ')
    : '—';

  return (
    <div style={{
      backgroundColor: 'rgba(15,23,42,0.6)', border: `1px solid var(--border-color)`,
      borderRadius: '14px', overflow: 'hidden',
      transition: 'border-color 0.2s, box-shadow 0.2s',
      boxShadow: expanded ? `0 0 0 1px ${cfg.border}, 0 8px 32px rgba(0,0,0,0.3)` : 'none',
    }}>

      {/* ── Header row (always visible) ── */}
      <div
        onClick={() => setExpanded((v) => !v)}
        style={{
          display: 'flex', alignItems: 'center', justifyContent: 'space-between',
          padding: '1.1rem 1.4rem', cursor: 'pointer', gap: '1rem',
          flexWrap: 'wrap',
        }}
      >
        {/* Left: icon + order info */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', minWidth: 0 }}>
          <div style={{
            width: 44, height: 44, borderRadius: '10px', flexShrink: 0,
            backgroundColor: cfg.bg, border: `1px solid ${cfg.border}`,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <Package size={20} color={cfg.color} />
          </div>
          <div style={{ minWidth: 0 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', flexWrap: 'wrap' }}>
              <span style={{ fontWeight: '700', fontSize: '0.95rem', color: 'var(--text-color)' }}>
                #{order.orderReference || shortId(order.id)}
              </span>
              <Badge status={order.status} />
            </div>
            <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', marginTop: '0.2rem' }}>
              {fmt.date(order.createdAt)} · {fmt.time(order.createdAt)}
              &nbsp;·&nbsp;{order.items?.length} {order.items?.length === 1 ? 'item' : 'items'}
            </div>
          </div>
        </div>

        {/* Right: amount + cancel + chevron */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.8rem', flexShrink: 0 }}>
          <span style={{ fontWeight: '700', fontSize: '1.05rem', color: 'var(--text-color)' }}>
            {fmt.money(order.totalAmount, order.currency)}
          </span>
          {['PENDING', 'CONFIRMED', 'PARTIALLY_REFUNDED', 'PARTIALLY_CANCELLED'].includes(order.status) && (
            <button
              onClick={handleCancel}
              disabled={cancelling}
              title="Cancel order"
              style={{
                display: 'flex', alignItems: 'center', gap: '0.3rem',
                padding: '0.3rem 0.7rem', borderRadius: '6px', fontSize: '0.78rem',
                backgroundColor: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.35)',
                color: '#ef4444', cursor: 'pointer', fontWeight: '600',
              }}
            >
              {cancelling ? <Loader2 size={12} className="animate-spin" /> : <X size={12} />}
              Cancel
            </button>
          )}
          <div style={{ color: 'var(--text-muted)', display: 'flex', alignItems: 'center' }}>
            {expanded ? <ChevronUp size={18} /> : <ChevronDown size={18} />}
          </div>
        </div>
      </div>

      {/* ── Expanded details ── */}
      {expanded && (
        <div style={{ borderTop: '1px solid var(--border-color)', padding: '1.2rem 1.4rem' }}>

          {/* Books list */}
          <p style={{ fontSize: '0.8rem', fontWeight: '700', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '0.75rem' }}>
            Items Ordered
          </p>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.6rem', marginBottom: '1.2rem' }}>
            {order.items?.map((item, i) => (
              <div key={i} style={{
                display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                padding: '0.65rem 0.9rem', borderRadius: '8px',
                backgroundColor: 'rgba(255,255,255,0.03)', border: '1px solid var(--border-color)',
                gap: '0.75rem', flexWrap: 'wrap',
              }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem', minWidth: 0 }}>
                  <div style={{
                    width: 34, height: 34, borderRadius: '6px', flexShrink: 0,
                    background: 'linear-gradient(135deg, var(--primary-color), #8b5cf6)',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                  }}>
                    <BookOpen size={14} color="#fff" />
                  </div>
                  <div style={{ minWidth: 0 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', flexWrap: 'wrap' }}>
                      <Link
                        to={`/books/${item.bookId}`}
                        style={{ fontWeight: '600', fontSize: '0.9rem', color: 'var(--text-color)', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', maxWidth: '260px', textDecoration: 'none', transition: 'color 0.15s' }}
                        onMouseEnter={e => e.currentTarget.style.color = 'var(--primary-color)'}
                        onMouseLeave={e => e.currentTarget.style.color = 'var(--text-color)'}
                      >
                        {item.title}
                      </Link>
                      {item.status && item.status !== order.status && (
                        <div style={{ transform: 'scale(0.85)', transformOrigin: 'left center' }}>
                          <Badge status={item.status} />
                        </div>
                      )}
                    </div>
                    <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>by {item.author}</div>
                  </div>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '1.2rem', flexShrink: 0 }}>
                  <span style={{ fontSize: '0.82rem', color: 'var(--text-muted)' }}>× {item.quantity}</span>
                  <span style={{ fontWeight: '600', fontSize: '0.9rem', color: 'var(--text-color)' }}>
                    {fmt.money(item.price * item.quantity, item.currency)}
                  </span>
                </div>
              </div>
            ))}
          </div>

          {/* Order Totals Summary */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.4rem', marginBottom: '1.2rem', padding: '1rem', backgroundColor: 'rgba(255,255,255,0.02)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
              <span>Subtotal</span>
              <span>{fmt.money(order.subtotal || order.items.reduce((acc, item) => acc + item.price * item.quantity, 0), order.currency)}</span>
            </div>
            {order.discountAmount > 0 && (
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', color: 'var(--success-color)' }}>
                <span>Discount {order.couponCode ? `(${order.couponCode})` : ''}</span>
                <span>-{fmt.money(order.discountAmount, order.currency)}</span>
              </div>
            )}
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
              <span>Tax (GST)</span>
              <span>{fmt.money(order.taxAmount || 0, order.currency)}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
              <span>Shipping</span>
              <span>{fmt.money(order.shippingAmount || 0, order.currency)}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.95rem', fontWeight: 'bold', borderTop: '1px solid var(--border-color)', paddingTop: '0.5rem', marginTop: '0.5rem' }}>
              <span>Total</span>
              <span style={{ color: 'var(--primary-color)' }}>{fmt.money(order.totalAmount, order.currency)}</span>
            </div>
          </div>

          {/* Meta row */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '0.75rem' }}>
            <MetaBlock icon={<MapPin size={14} />} label="Shipping to" value={addressStr} />
            <MetaBlock icon={<CreditCard size={14} />} label="Payment" value={order.paymentMethod || '—'} />
            <MetaBlock icon={<Calendar size={14} />} label="Order ID" value={order.orderReference || order.id} mono />
          </div>
        </div>
      )}
    </div>
  );
};

const MetaBlock = ({ icon, label, value, mono }) => (
  <div style={{ padding: '0.7rem 0.9rem', borderRadius: '8px', backgroundColor: 'rgba(255,255,255,0.03)', border: '1px solid var(--border-color)' }}>
    <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: 'var(--text-muted)', fontSize: '0.75rem', marginBottom: '0.3rem' }}>
      {icon} {label}
    </div>
    <div style={{ fontSize: mono ? '0.72rem' : '0.85rem', fontWeight: '500', color: 'var(--text-color)', wordBreak: 'break-all', fontFamily: mono ? 'monospace' : undefined }}>
      {value}
    </div>
  </div>
);

/* ─── Pagination ─────────────────────────────────────────────── */
const Pagination = ({ page, totalPages, onChange }) => {
  if (totalPages <= 1) return null;
  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '0.5rem', marginTop: '2rem' }}>
      <button
        onClick={() => onChange(page - 1)} disabled={page === 0}
        style={{ padding: '0.45rem 0.8rem', borderRadius: '8px', border: '1px solid var(--border-color)', background: 'transparent', backgroundImage: 'none', boxShadow: 'none', color: page === 0 ? 'var(--text-muted)' : 'var(--text-color)', cursor: page === 0 ? 'not-allowed' : 'pointer', display: 'flex', alignItems: 'center' }}
      >
        <ChevronLeft size={16} />
      </button>

      {Array.from({ length: totalPages }, (_, i) => i)
        .filter(i => Math.abs(i - page) <= 2)
        .map(i => (
          <button
            key={i} onClick={() => onChange(i)}
            style={{
              padding: '0.45rem 0.85rem', borderRadius: '8px', fontSize: '0.9rem', fontWeight: i === page ? '700' : '400',
              border: `1px solid ${i === page ? 'var(--primary-color)' : 'var(--border-color)'}`,
              background: i === page ? 'var(--primary-color)' : 'transparent',
              backgroundImage: 'none',
              boxShadow: 'none',
              color: i === page ? '#fff' : 'var(--text-color)', cursor: 'pointer',
            }}
          >
            {i + 1}
          </button>
        ))
      }

      <button
        onClick={() => onChange(page + 1)} disabled={page === totalPages - 1}
        style={{ padding: '0.45rem 0.8rem', borderRadius: '8px', border: '1px solid var(--border-color)', background: 'transparent', backgroundImage: 'none', boxShadow: 'none', color: page === totalPages - 1 ? 'var(--text-muted)' : 'var(--text-color)', cursor: page === totalPages - 1 ? 'not-allowed' : 'pointer', display: 'flex', alignItems: 'center' }}
      >
        <ChevronRight size={16} />
      </button>
    </div>
  );
};

/* ─── Filter bar ─────────────────────────────────────────────── */
const FILTERS = ['ALL', 'PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'FAILED'];

const FilterBar = ({ active, onChange }) => (
  <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap', marginBottom: '1.5rem' }}>
    {FILTERS.map((f) => {
      const isActive = f === active;
      const cfg = f === 'ALL' ? null : statusCfg(f);
      return (
        <button
          key={f}
          onClick={() => onChange(f)}
          style={{
            padding: '0.35rem 0.9rem', borderRadius: '999px', fontSize: '0.82rem', fontWeight: '600',
            cursor: 'pointer', transition: 'all 0.15s',
            background: isActive ? (cfg?.bg ?? 'var(--primary-color)') : 'transparent',
            backgroundImage: 'none',
            boxShadow: 'none',
            border: isActive ? `1px solid ${cfg?.border ?? 'var(--primary-color)'}` : '1px solid var(--border-color)',
            color: isActive ? (cfg?.color ?? '#fff') : 'var(--text-muted)',
          }}
        >
          {f === 'ALL' ? 'All Orders' : STATUS[f].label}
        </button>
      );
    })}
  </div>
);

/* ─── Main Page ──────────────────────────────────────────────── */
const MyOrdersPage = () => {
  const { isAuthenticated } = useAuthStore();
  const navigate = useNavigate();

  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [filter, setFilter] = useState('ALL');

  useEffect(() => {
    if (!isAuthenticated) { navigate('/login', { replace: true }); return; }
  }, [isAuthenticated]);

  const fetchOrders = useCallback(async (pageNum, statusFilter) => {
    setLoading(true);
    setError(null);
    try {
      const params = new URLSearchParams({ pageNumber: pageNum, pageSize: 8 });
      if (statusFilter && statusFilter !== 'ALL') params.set('status', statusFilter);
      const res = await apiClient.get(`/order/my-orders?${params}`);
      setOrders(res.data.content ?? []);
      setTotalPages(res.data.totalPages ?? 0);
      setTotalElements(res.data.totalElements ?? 0);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load orders. Please try again.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchOrders(page, filter); }, [page, filter]);

  const handleFilterChange = (f) => { setFilter(f); setPage(0); };

  const handleCancel = async (orderId) => {
    try {
      await apiClient.delete(`/order/${orderId}/cancel`);
      fetchOrders(page, filter);
    } catch (err) {
      alert(err.response?.data?.message || 'Could not cancel order.');
    }
  };

  return (
    <div className="container mt-8 mb-8" style={{ maxWidth: '860px' }}>

      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginBottom: '1.75rem', flexWrap: 'wrap', gap: '0.75rem' }}>
        <div>
          <h1 style={{ fontSize: '2rem', marginBottom: '0.3rem', display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
            <ShoppingBag size={28} style={{ color: 'var(--primary-color)' }} />
            My Orders
          </h1>
          {!loading && (
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', margin: 0 }}>
              {totalElements === 0 ? 'No orders yet' : `${totalElements} order${totalElements !== 1 ? 's' : ''} total`}
            </p>
          )}
        </div>
      </div>

      {/* Filter bar */}
      <FilterBar active={filter} onChange={handleFilterChange} />

      {/* States */}
      {loading && (
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '300px' }}>
          <Loader2 size={36} className="animate-spin" style={{ color: 'var(--primary-color)' }} />
        </div>
      )}

      {error && !loading && (
        <div style={{ padding: '1rem', borderRadius: '10px', backgroundColor: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.3)', color: '#ef4444', textAlign: 'center' }}>
          {error}
        </div>
      )}

      {!loading && !error && orders.length === 0 && (
        <div style={{ textAlign: 'center', padding: '4rem 2rem', color: 'var(--text-muted)' }}>
          <ShoppingBag size={56} style={{ opacity: 0.2, marginBottom: '1rem' }} />
          <p style={{ fontSize: '1.1rem', fontWeight: '600', marginBottom: '0.5rem' }}>
            {filter === 'ALL' ? "You haven't placed any orders yet." : `No ${STATUS[filter]?.label} orders found.`}
          </p>
          <p style={{ fontSize: '0.9rem', marginBottom: '1.5rem' }}>
            {filter === 'ALL' ? 'Start exploring books and place your first order!' : 'Try a different filter.'}
          </p>
          {filter === 'ALL' && (
            <button onClick={() => navigate('/')} style={{ padding: '0.6rem 1.5rem' }}>
              Browse Books
            </button>
          )}
        </div>
      )}

      {/* Order cards */}
      {!loading && !error && orders.length > 0 && (
        <>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
            {orders.map((order) => (
              <OrderCard key={order.id} order={order} onCancel={handleCancel} />
            ))}
          </div>
          <Pagination page={page} totalPages={totalPages} onChange={setPage} />
        </>
      )}
    </div>
  );
};

export default MyOrdersPage;
