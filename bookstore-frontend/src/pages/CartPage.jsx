import { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import useCartStore from '../store/useCartStore';
import useAuthStore from '../store/useAuthStore';
import { Trash2, Plus, Minus, ArrowRight, Loader2 } from 'lucide-react';
import apiClient from '../api/axios';
import { loadStripe } from '@stripe/stripe-js';
import { Elements } from '@stripe/react-stripe-js';
import StripeCheckoutForm from '../components/StripeCheckoutForm';

// We will initialize Stripe inside the component by fetching the key from the backend

const loadRazorpayScript = () => {
  return new Promise((resolve) => {
    if (window.Razorpay) {
      resolve(true);
      return;
    }
    const script = document.createElement('script');
    script.src = 'https://checkout.razorpay.com/v1/checkout.js';
    script.onload = () => {
      resolve(true);
    };
    script.onerror = () => {
      resolve(false);
    };
    document.body.appendChild(script);
  });
};

const CartPage = () => {
  const { cart, loading, fetchCart, updateQuantity, removeFromCart, applyCoupon, removeCoupon } = useCartStore();
  const { isAuthenticated, user } = useAuthStore();
  const navigate = useNavigate();
  const [isCheckingOut, setIsCheckingOut] = useState(false);
  const [checkoutError, setCheckoutError] = useState(null);
  const [showAddressModal, setShowAddressModal] = useState(false);
  const [clientSecret, setClientSecret] = useState(null);
  const [addresses, setAddresses] = useState([]);
  const [selectedAddressId, setSelectedAddressId] = useState(null);
  const [isLoadingAddresses, setIsLoadingAddresses] = useState(false);

  const [couponInput, setCouponInput] = useState('');
  const [couponError, setCouponError] = useState(null);
  const [applyingCoupon, setApplyingCoupon] = useState(false);
  const [stripePromise, setStripePromise] = useState(null);
  const [razorpayKey, setRazorpayKey] = useState(null);
  const [selectedPaymentMethod, setSelectedPaymentMethod] = useState('stripe');

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login', { replace: true });
      return;
    }
    fetchCart();
    
    // Fetch Payment Keys from backend config
    apiClient.get(`/config/checkout-keys?t=${new Date().getTime()}`).then(res => {
      if (res.data) {
        if (res.data.stripePublishableKey) {
          setStripePromise(loadStripe(res.data.stripePublishableKey));
        }
        if (res.data.razorpayKeyId) {
          setRazorpayKey(res.data.razorpayKeyId);
        }
      }
    }).catch(err => console.error("Failed to fetch payment config", err));
  }, [isAuthenticated, navigate, fetchCart]);

  if (loading && !cart) {
    return <div className="text-center mt-8">Loading cart...</div>;
  }

  const handleUpdateQuantity = (listingId, currentQuantity, change) => {
    const newQuantity = currentQuantity + change;
    if (newQuantity <= 0) {
      removeFromCart(listingId);
    } else {
      updateQuantity(listingId, newQuantity);
    }
  };

  const handleRemove = (listingId) => {
    removeFromCart(listingId);
  };

  const handleProceedToCheckout = async () => {
    setCheckoutError(null);
    setIsLoadingAddresses(true);
    try {
      const res = await apiClient.get('/user/me');
      const userAddresses = res.data.addresses || [];
      if (userAddresses.length === 0) {
        setCheckoutError('Please add a delivery address in your profile first.');
        setIsLoadingAddresses(false);
        return;
      }
      setAddresses(userAddresses);
      setSelectedAddressId(userAddresses[0].id);
      setShowAddressModal(true);
    } catch (err) {
      setCheckoutError('Failed to fetch addresses');
    } finally {
      setIsLoadingAddresses(false);
    }
  };

  const handleCheckout = async () => {
    setCheckoutError(null);
    setIsCheckingOut(true);
    // Don't close address modal yet, we will replace its content with the Stripe form

    try {
      // 1. Call our backend checkout endpoint
      const response = await apiClient.post('/order/checkout', { paymentMethod: 'stripe', addressId: selectedAddressId });
      // For Stripe, the keyId field holds the client_secret returned by the backend
      const { keyId } = response.data;
      
      setClientSecret(keyId);
      setIsCheckingOut(false);
    } catch (err) {
      setCheckoutError(err.response?.data?.message || err.message || 'An error occurred during checkout');
      setIsCheckingOut(false);
    }
  };

  const handleRazorpayCheckout = async () => {
    setCheckoutError(null);
    setIsCheckingOut(true);

    try {
      // 1. Call our backend checkout endpoint
      const response = await apiClient.post('/order/checkout', { paymentMethod: 'razorpay', addressId: selectedAddressId });
      const { gatewayOrderId, totalAmount, keyId } = response.data;

      // 2. Load Razorpay script
      const res = await loadRazorpayScript();
      if (!res) {
        setCheckoutError('Razorpay SDK failed to load. Are you offline?');
        setIsCheckingOut(false);
        return;
      }

      if (!keyId) {
        setCheckoutError('Configuration Error: Razorpay Key is missing from backend response. Please check payment-service properties.');
        setIsCheckingOut(false);
        return;
      }

      // 3. Configure Razorpay options
      const options = {
        key: keyId,
        amount: Math.round(totalAmount * 100),
        currency: cart?.currency || 'INR',
        name: 'BookHeaven',
        description: 'Order Payment',
        order_id: gatewayOrderId,
        handler: async function (response) {
          try {
            const mockPayload = {
              event: 'payment.captured',
              payload: {
                payment: {
                  entity: {
                    order_id: response.razorpay_order_id,
                    id: response.razorpay_payment_id,
                    method: 'card',
                    amount: Math.round(totalAmount * 100)
                  }
                }
              }
            };

            await apiClient.post('/payment/webhook', mockPayload, {
              headers: {
                'X-Razorpay-Signature': 'bypass-signature-for-testing'
              }
            });

            fetchCart();
            setShowAddressModal(false);
            navigate('/order-success');
          } catch (err) {
            setCheckoutError('Payment verification failed.');
            setIsCheckingOut(false);
          }
        },
        prefill: {
          name: user?.name || '',
          email: user?.email || '',
        },
        theme: {
          color: '#3399cc'
        }
      };

      console.log("Razorpay Options: ", options);

      const paymentObject = new window.Razorpay(options);
      paymentObject.on('payment.failed', function (response) {
        alert("Razorpay Error: " + response.error.description + "\nReason: " + response.error.reason);
        console.error("Razorpay Error", response.error);
        setCheckoutError('Payment failed: ' + response.error.description);
        setIsCheckingOut(false);
      });
      paymentObject.open();
      setIsCheckingOut(false);
      
    } catch (err) {
      setCheckoutError(err.response?.data?.message || err.message || 'An error occurred during Razorpay checkout');
      setIsCheckingOut(false);
    }
  };

  const handleBypassPayment = async () => {
    setCheckoutError(null);
    setIsCheckingOut(true);

    try {
      // 1. Call checkout to get the order
      const response = await apiClient.post('/order/checkout', { paymentMethod: 'razorpay', addressId: selectedAddressId });
      const { gatewayOrderId, totalAmount } = response.data;

      // 2. Send mock webhook
      const mockPayload = {
        event: 'payment.captured',
        payload: {
          payment: {
            entity: {
              order_id: gatewayOrderId,
              id: 'pay_bypass_' + Math.floor(Math.random() * 1000000),
              method: 'card',
              amount: Math.round(totalAmount * 100)
            }
          }
        }
      };

      await apiClient.post('/payment/webhook', mockPayload, {
        headers: {
          'X-Razorpay-Signature': 'bypass-signature-for-testing'
        }
      });

      fetchCart();
      setShowAddressModal(false);
      navigate('/order-success');
    } catch (err) {
      setCheckoutError('Payment bypass failed.');
      setIsCheckingOut(false);
    }
  };

  const handleApplyCoupon = async () => {
    if (!couponInput.trim()) return;
    setApplyingCoupon(true);
    setCouponError(null);
    const result = await applyCoupon(couponInput.trim());
    if (!result.success) {
      setCouponError(result.message);
    } else {
      setCouponInput('');
    }
    setApplyingCoupon(false);
  };

  const handleRemoveCoupon = async () => {
    await removeCoupon();
  };

  const items = cart?.items || [];

  // Use backend calculated totals if available
  const hasSubtotal = cart && cart.subtotal !== undefined;
  const subtotal = hasSubtotal ? cart.subtotal : items.reduce((total, item) => total + ((item.price || 0) * item.quantity), 0);
  const discountAmount = cart?.discountAmount || 0;
  const couponCode = cart?.couponCode;

  const taxAmount = cart?.taxAmount !== undefined ? cart.taxAmount : (subtotal * 0.18);
  const shippingAmount = cart?.shippingAmount !== undefined ? cart.shippingAmount : (subtotal * 0.06);
  const finalTotal = cart?.totalAmount !== undefined ? cart.totalAmount : (subtotal - discountAmount + taxAmount + shippingAmount);

  return (
    <div style={{ width: '100%', maxWidth: '1500px', margin: '2rem auto', padding: '0 2rem 4rem 2rem' }}>
      <h1 className="mb-8" style={{ fontSize: '2.5rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '1rem' }}>Shopping Cart</h1>

      {items.length === 0 ? (
        <div className="card text-center py-8 glass-panel">
          <h2 style={{ marginBottom: '1rem' }}>Your cart is empty</h2>
          <p style={{ color: 'var(--text-muted)', marginBottom: '2rem' }}>Looks like you haven't added any books yet.</p>
          <Link to="/" className="button" style={{
            backgroundColor: 'var(--primary-color)',
            color: 'white',
            padding: '0.8rem 1.5rem',
            borderRadius: 'var(--radius)',
            display: 'inline-block'
          }}>
            Continue Shopping
          </Link>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 350px', gap: '2rem', alignItems: 'start' }}>
          {/* Cart Items List */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
            {items.map((item) => (
              <div key={item.listingId} className="card glass-panel" style={{ display: 'flex', gap: '1.5rem', alignItems: 'center' }}>
                <div style={{ width: '100px', height: '140px', backgroundColor: 'var(--bg-color)', borderRadius: '4px', overflow: 'hidden' }}>
                  <Link to={`/books/${item.bookId}`} style={{ display: 'block', width: '100%', height: '100%' }}>
                    <img
                      src={item.imageUrl || '/default-book-cover.png'}
                      alt={item.title}
                      onError={(e) => { e.target.onerror = null; e.target.src = '/default-book-cover.png'; }}
                      style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                    />
                  </Link>
                </div>

                <div style={{ flex: 1 }}>
                  <Link to={`/books/${item.bookId}`} style={{ textDecoration: 'none', color: 'inherit' }}>
                    <h3 style={{ fontSize: '1.2rem', marginBottom: '0.25rem' }}>{item.title}</h3>
                  </Link>
                  <p style={{ color: 'var(--text-muted)', marginBottom: '0.25rem' }}>{item.author}</p>
                  {item.sellerUsername && (
                    <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '0.75rem', marginTop: 0 }}>
                      Seller: <span style={{ color: 'var(--primary-color)', fontWeight: '600' }}>{item.sellerUsername}</span>
                    </p>
                  )}
                  <div style={{ fontWeight: 'bold', fontSize: '1.2rem', color: 'var(--primary-color)' }}>
                    {item.currency} {(item.price || 0).toFixed(2)}
                  </div>
                </div>

                <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '1rem' }}>
                  <div style={{ display: 'flex', alignItems: 'center', border: '1px solid var(--border-color)', borderRadius: 'var(--radius)', overflow: 'hidden' }}>
                    <button
                      onClick={() => handleUpdateQuantity(item.listingId, item.quantity, -1)}
                      style={{ background: 'transparent', border: 'none', padding: '0.5rem', color: 'var(--text-color)', borderRadius: 0 }}
                    >
                      <Minus size={16} />
                    </button>
                    <span style={{ padding: '0 1rem', minWidth: '40px', textAlign: 'center' }}>{item.quantity}</span>
                    <button
                      onClick={() => handleUpdateQuantity(item.listingId, item.quantity, 1)}
                      style={{ background: 'transparent', border: 'none', padding: '0.5rem', color: 'var(--text-color)', borderRadius: 0 }}
                    >
                      <Plus size={16} />
                    </button>
                  </div>

                  <button
                    onClick={() => handleRemove(item.listingId)}
                    style={{ background: 'transparent', border: 'none', color: 'var(--danger-color)', display: 'flex', alignItems: 'center', gap: '0.5rem', padding: '0.5rem' }}
                  >
                    <Trash2 size={16} /> Remove
                  </button>
                </div>
              </div>
            ))}
          </div>

          {/* Order Summary */}
          <div className="card glass-panel" style={{ position: 'sticky', top: '100px' }}>
            <h2 className="mb-4" style={{ fontSize: '1.5rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '1rem' }}>Order Summary</h2>

            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1rem' }}>
              <span style={{ color: 'var(--text-muted)' }}>Subtotal ({items.reduce((acc, item) => acc + item.quantity, 0)} items)</span>
              <span>{cart?.currency || items[0]?.currency || '$'} {subtotal.toFixed(2)}</span>
            </div>

            {/* Coupon Section */}
            <div style={{ marginBottom: '1.5rem', paddingBottom: '1.5rem', borderBottom: '1px solid var(--border-color)' }}>
              {couponCode ? (
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', backgroundColor: 'rgba(59, 130, 246, 0.1)', padding: '0.75rem', borderRadius: 'var(--radius)' }}>
                  <div>
                    <span style={{ fontWeight: 'bold', color: 'var(--primary-color)' }}>{couponCode}</span> applied
                  </div>
                  <button onClick={handleRemoveCoupon} style={{ background: 'transparent', border: 'none', color: 'var(--danger-color)', padding: '0.25rem' }}>
                    <Trash2 size={16} />
                  </button>
                </div>
              ) : (
                <div>
                  <div style={{ display: 'flex', gap: '0.5rem' }}>
                    <input
                      type="text"
                      placeholder="Promo Code"
                      value={couponInput}
                      onChange={(e) => setCouponInput(e.target.value.toUpperCase())}
                      style={{ flex: 1, padding: '0.5rem', borderRadius: 'var(--radius)', border: '1px solid var(--border-color)', backgroundColor: 'var(--bg-color)', color: 'var(--text-color)' }}
                    />
                    <button
                      onClick={handleApplyCoupon}
                      disabled={applyingCoupon || !couponInput.trim()}
                      style={{ padding: '0.5rem 1rem', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                    >
                      {applyingCoupon ? <Loader2 size={16} className="animate-spin" /> : 'Apply'}
                    </button>
                  </div>
                  {couponError && <div style={{ color: 'var(--danger-color)', fontSize: '0.85rem', marginTop: '0.5rem' }}>{couponError}</div>}
                </div>
              )}
            </div>

            {discountAmount > 0 && (
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem', color: 'var(--success-color)' }}>
                <span>Discount</span>
                <span>-{cart?.currency || items[0]?.currency || '$'} {discountAmount.toFixed(2)}</span>
              </div>
            )}

            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
              <span style={{ color: 'var(--text-muted)' }}>GST</span>
              <span>{cart?.currency || items[0]?.currency || '$'} {taxAmount.toFixed(2)}</span>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1.5rem' }}>
              <span style={{ color: 'var(--text-muted)' }}>Shipping</span>
              <span>{cart?.currency || items[0]?.currency || '$'} {shippingAmount.toFixed(2)}</span>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '2rem', borderTop: '1px solid var(--border-color)', paddingTop: '1rem', fontWeight: 'bold', fontSize: '1.25rem' }}>
              <span>Total</span>
              <span style={{ color: 'var(--primary-color)' }}>{cart?.currency || items[0]?.currency || '$'} {finalTotal.toFixed(2)}</span>
            </div>

            {checkoutError && (
              <div style={{ color: 'var(--danger-color)', marginBottom: '1rem', padding: '0.5rem', backgroundColor: 'rgba(239,68,68,0.1)', borderRadius: 'var(--radius)', fontSize: '0.9rem', textAlign: 'center' }}>
                {checkoutError}
              </div>
            )}

            <button
              onClick={handleProceedToCheckout}
              disabled={isCheckingOut || isLoadingAddresses}
              style={{ width: '100%', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '0.5rem', padding: '1rem', fontSize: '1.1rem' }}
            >
              {(isCheckingOut || isLoadingAddresses) ? <Loader2 className="animate-spin" size={20} /> : 'Proceed to Checkout'}
              {!(isCheckingOut || isLoadingAddresses) && <ArrowRight size={20} />}
            </button>
          </div>
        </div>
      )}
      {showAddressModal && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(15, 23, 42, 0.75)', backdropFilter: 'blur(8px)', WebkitBackdropFilter: 'blur(8px)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, animation: 'fadeIn 0.3s ease-out' }}>
          <div className="card glass-panel" style={{ width: '90%', maxWidth: '500px', maxHeight: '80vh', overflowY: 'auto', padding: '2rem', boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.5)' }}>
            {clientSecret ? (
              <>
                <h2 style={{ marginBottom: '1.5rem' }}>Secure Payment</h2>
                {!stripePromise ? (
                  <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--danger-color)' }}>
                    Failed to initialize payment system. Please make sure the backend is running and the API keys are configured.
                  </div>
                ) : (
                  <Elements stripe={stripePromise} options={{ 
                    clientSecret,
                    appearance: {
                      theme: 'night',
                      variables: {
                        colorPrimary: '#3b82f6',
                        colorBackground: '#1e293b',
                        colorText: '#f8fafc',
                        colorDanger: '#ef4444',
                        fontFamily: 'Inter, system-ui, Avenir, Helvetica, Arial, sans-serif',
                        borderRadius: '8px',
                        spacingGridRow: '1.5rem',
                      },
                      rules: {
                        '.Input': {
                          border: '1px solid #334155',
                          boxShadow: 'none',
                          backgroundColor: '#0f172a',
                          padding: '0.75rem 1rem',
                        },
                        '.Input:focus': {
                          borderColor: '#3b82f6',
                          boxShadow: '0 0 0 2px rgba(59, 130, 246, 0.2)',
                        },
                        '.Label': {
                          color: '#f8fafc',
                          fontWeight: '500',
                          marginBottom: '0.5rem',
                        }
                      }
                    }
                  }}>
                    <StripeCheckoutForm 
                      amount={finalTotal} 
                      currency={cart?.currency || items[0]?.currency || 'usd'} 
                      onSuccess={() => {
                        fetchCart();
                        navigate('/order-success');
                      }}
                      onCancel={() => {
                        setClientSecret(null);
                        setShowAddressModal(false);
                        setCheckoutError('Payment cancelled. Your items are still in the cart.');
                      }}
                    />
                  </Elements>
                )}
              </>
            ) : (
              <>
                <h2 style={{ marginBottom: '1.5rem' }}>Select Delivery Address</h2>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginBottom: '1.5rem' }}>
                  {addresses.map(addr => (
                    <label key={addr.id} style={{ display: 'flex', alignItems: 'center', gap: '1rem', padding: '1rem', border: `2px solid ${selectedAddressId === addr.id ? 'var(--primary-color)' : 'var(--border-color)'}`, borderRadius: 'var(--radius)', cursor: 'pointer' }}>
                      <input type="radio" name="address" value={addr.id} checked={selectedAddressId === addr.id} onChange={() => setSelectedAddressId(addr.id)} style={{ margin: 0 }} />
                      <div>
                        <div style={{ fontWeight: '500', marginBottom: '0.25rem' }}>{addr.street}</div>
                        <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                          {addr.city}, {addr.state} {addr.zipCode}, {addr.country}
                        </div>
                      </div>
                    </label>
                  ))}
                </div>
                <h3 style={{ marginBottom: '1rem', marginTop: '1rem' }}>Payment Method</h3>
                <div style={{ display: 'flex', gap: '2rem', marginBottom: '1.5rem', padding: '1rem', border: '1px solid var(--border-color)', borderRadius: 'var(--radius)' }}>
                  <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                    <input 
                      type="radio" 
                      name="paymentMethod" 
                      value="stripe" 
                      checked={selectedPaymentMethod === 'stripe'} 
                      onChange={() => setSelectedPaymentMethod('stripe')} 
                    />
                    <span style={{ fontWeight: '500' }}>Stripe (Cards, Link)</span>
                  </label>
                  <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                    <input 
                      type="radio" 
                      name="paymentMethod" 
                      value="razorpay" 
                      checked={selectedPaymentMethod === 'razorpay'} 
                      onChange={() => setSelectedPaymentMethod('razorpay')} 
                    />
                    <span style={{ fontWeight: '500' }}>Razorpay (UPI, Netbanking)</span>
                  </label>
                </div>
                {checkoutError && (
                  <div style={{ color: 'var(--danger-color)', marginBottom: '1rem', padding: '0.5rem', backgroundColor: 'rgba(239,68,68,0.1)', borderRadius: 'var(--radius)', fontSize: '0.9rem' }}>
                    {checkoutError}
                  </div>
                )}
                <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end', flexWrap: 'wrap' }}>
                  <button onClick={() => setShowAddressModal(false)} style={{ padding: '0.5rem 1rem', background: 'transparent', color: 'var(--text-color)', border: '1px solid var(--border-color)' }}>Cancel</button>
                  <button 
                    onClick={handleBypassPayment}
                    disabled={!selectedAddressId || isCheckingOut}
                    style={{ padding: '0.5rem 1rem', backgroundColor: 'var(--success-color, #10b981)', color: 'white', border: 'none', borderRadius: 'var(--radius)' }}
                  >
                    Bypass Payment (Test)
                  </button>
                  <button 
                    onClick={selectedPaymentMethod === 'stripe' ? handleCheckout : handleRazorpayCheckout} 
                    disabled={!selectedAddressId || isCheckingOut} 
                    style={{ padding: '0.5rem 1rem', backgroundColor: 'var(--primary-color)', color: 'white', border: 'none', display: 'flex', alignItems: 'center', justifyItems: 'center', gap: '0.5rem', minWidth: '150px' }}>
                    {isCheckingOut ? <Loader2 className="animate-spin" size={16} /> : `Pay with ${selectedPaymentMethod === 'stripe' ? 'Stripe' : 'Razorpay'}`}
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default CartPage;
