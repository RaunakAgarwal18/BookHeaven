import { useState } from 'react';

const FLOWS = [
  {
    id: 'purchase',
    name: '🛍️ Complete Purchase Flow',
    description: 'End-to-end flow from browsing → cart → checkout → payment → confirmation → settlement',
    steps: [
      { actor: '🖥️ Frontend', action: 'GET /api/book', target: '📚 Book Service', note: 'Browse paginated books (cached in Redis)' },
      { actor: '🖥️ Frontend', action: 'POST /api/cart/item', target: '🛒 Cart Service', note: 'Add item to cart' },
      { actor: '🛒 Cart', action: 'POST /api/book/bulk', target: '📚 Book Service', note: 'Fetch live book data for cart response' },
      { actor: '🖥️ Frontend', action: 'POST /api/order/checkout', target: '📦 Order Service', note: 'Initiate checkout (rate limited: 3/min)' },
      { actor: '📦 Order', action: 'GET /api/cart', target: '🛒 Cart Service', note: 'Fetch cart contents' },
      { actor: '📦 Order', action: 'Validate stock', target: '📦 Order', note: 'Check qty <= available for each item' },
      { actor: '📦 Order', action: 'GET /api/user/me', target: '👤 User Service', note: 'Fetch user profile + address' },
      { actor: '📦 Order', action: 'POST /api/book/bulk', target: '📚 Book Service', note: 'Fetch seller info for order items' },
      { actor: '📦 Order', action: 'Create Order (PENDING)', target: '🗄️ MySQL', note: '6-char alphanumeric reference' },
      { actor: '📦 Order', action: 'OrderTimeoutEvent', target: '🐇 RabbitMQ', note: '60s TTL queue → DLX pattern' },
      { actor: '📦 Order', action: 'POST /api/payment/initiate', target: '💳 Payment Service', note: 'Create Razorpay order (paise)' },
      { actor: '💳 Payment', action: 'Create Razorpay Order', target: '🏦 Razorpay API', note: 'Returns gatewayOrderId' },
      { actor: '💳 Payment', action: 'Save Payment (PENDING)', target: '🗄️ MySQL', note: 'Store payment record' },
      { actor: '🖥️ Frontend', action: 'Open Razorpay Checkout UI', target: '🏦 Razorpay', note: 'User completes payment' },
      { actor: '🏦 Razorpay', action: 'POST /api/payment/webhook', target: '💳 Payment Service', note: 'payment.captured event' },
      { actor: '💳 Payment', action: 'Verify HMAC-SHA256 signature', target: '💳 Payment', note: 'Idempotency guard' },
      { actor: '💳 Payment', action: 'PUT /order/{id}/confirm', target: '📦 Order Service', note: 'Confirm order' },
      { actor: '📦 Order', action: 'PUT /api/book/reduce-stock', target: '📚 Book Service', note: 'Decrement copiesAvailable' },
      { actor: '📦 Order', action: 'DELETE /api/cart', target: '🛒 Cart Service', note: 'Clear cart' },
      { actor: '📦 Order', action: 'OrderConfirmedEvent', target: '📧 Email Service', note: 'Buyer confirmation email' },
      { actor: '📦 Order', action: 'SellerOrderEvent (per seller)', target: '📧 Email Service', note: 'Seller notification email' },
      { actor: '📦 Order', action: 'OrderLedgerEvent', target: '💳 Payment Service', note: 'Create seller ledger entries (10% commission)' },
    ],
  },
  {
    id: 'auth',
    name: '🔐 Authentication Flow',
    description: 'OTP-based signup with BCrypt-hashed OTPs, JWT tokens in httpOnly cookies, and Redis sessions',
    steps: [
      { actor: '🖥️ Frontend', action: 'POST /api/user/auth/sign-up', target: '👤 User Service', note: 'Username, email, password' },
      { actor: '👤 User', action: 'Check uniqueness', target: '🗄️ MySQL', note: 'Verify email/username not taken' },
      { actor: '👤 User', action: 'Store signup:{email}', target: '⚡ Redis', note: '30 min TTL, JSON payload' },
      { actor: '👤 User', action: 'Store otp:{email}', target: '⚡ Redis', note: '5 min TTL, BCrypt-hashed OTP' },
      { actor: '👤 User', action: 'Check otp:cooldown:{email}', target: '⚡ Redis', note: '1 min cooldown between sends' },
      { actor: '👤 User', action: 'POST /api/email/send-otp', target: '📧 Email Service', note: 'X-Service-Secret header' },
      { actor: '📧 Email', action: 'Send OTP via SMTP', target: '📫 Gmail', note: 'HTML template with OTP code' },
      { actor: '🖥️ Frontend', action: 'POST /api/user/auth/verify-otp', target: '👤 User Service', note: 'Email + OTP' },
      { actor: '👤 User', action: 'BCrypt.matches(otp, stored)', target: '⚡ Redis', note: 'Validate OTP hash' },
      { actor: '👤 User', action: 'Create User entity', target: '🗄️ MySQL', note: 'BCrypt password, roles: [USER]' },
      { actor: '👤 User', action: 'Generate JWT tokens', target: '👤 User', note: 'Access (1d) + Refresh (7d)' },
      { actor: '👤 User', action: 'Store refresh:{email}', target: '⚡ Redis', note: '7 day TTL' },
      { actor: '👤 User', action: 'WelcomeEmailEvent', target: '🐇 RabbitMQ', note: 'Async welcome email' },
      { actor: '👤 User', action: 'Set-Cookie (httpOnly)', target: '🖥️ Frontend', note: 'accessToken + refreshToken' },
    ],
  },
  {
    id: 'settlement',
    name: '💰 Seller Settlement Pipeline',
    description: 'End-of-day batch settlement: ledger entries → aggregation → Razorpay Transfers → seller payouts',
    steps: [
      { actor: '📦 Order', action: 'OrderLedgerEvent', target: '🐇 RabbitMQ', note: 'On order confirmation' },
      { actor: '💳 LedgerConsumer', action: 'Process event', target: '🗄️ MySQL', note: 'Create SellerLedger per item' },
      { actor: '💳 LedgerConsumer', action: 'Calculate: gross × 10% = commission', target: '💳 Payment', note: 'net = gross - commission - discount' },
      { actor: '⏰ Scheduler', action: '@Scheduled(cron = "0 50 23 * * ?")', target: '💳 Payment', note: 'EOD at 23:50' },
      { actor: '💳 Settlement', action: 'GROUP BY seller_id WHERE status=PENDING', target: '🗄️ MySQL', note: 'Aggregate net payouts' },
      { actor: '💳 Settlement', action: 'Set status → PROCESSING', target: '🗄️ MySQL', note: 'Assign batch settlement_id' },
      { actor: '💳 Settlement', action: 'SellerPayoutEvent', target: '🐇 RabbitMQ', note: 'One event per seller' },
      { actor: '💳 PayoutConsumer', action: 'Fetch seller profile', target: '👤 User Service', note: 'Get razorpayAccountId' },
      { actor: '💳 PayoutConsumer', action: 'Razorpay Transfers API', target: '🏦 Razorpay', note: 'Route funds to linked account' },
      { actor: '💳 PayoutConsumer', action: 'Set status → SETTLED', target: '🗄️ MySQL', note: 'Store gateway_transfer_id' },
      { actor: '💳 PayoutConsumer', action: 'If no razorpayAccountId → FAILED', target: '📧 Email', note: 'MissingRazorpayIdEvent' },
    ],
  },
  {
    id: 'cancellation',
    name: '❌ Order Cancellation & Refund',
    description: 'User-initiated cancellation with stock restoration and Razorpay refund',
    steps: [
      { actor: '🖥️ Frontend', action: 'DELETE /api/order/{id}/cancel', target: '📦 Order Service', note: 'Ownership check' },
      { actor: '📦 Order', action: 'Validate status', target: '📦 Order', note: 'Cannot cancel SHIPPED or DELIVERED' },
      { actor: '📦 Order', action: 'PUT /api/book/restore-stock', target: '📚 Book Service', note: 'Increment copiesAvailable' },
      { actor: '📦 Order', action: 'If CONFIRMED → POST /refund', target: '💳 Payment Service', note: 'Razorpay refund API' },
      { actor: '💳 Payment', action: 'Razorpay refund()', target: '🏦 Razorpay', note: 'Amount in paise' },
      { actor: '📦 Order', action: 'Set status → REFUNDED or CANCELLED', target: '🗄️ MySQL', note: 'REFUNDED if payment was made' },
    ],
  },
  {
    id: 'oauth2',
    name: '🔑 OAuth2 Google Login Flow',
    description: 'Google sign-in via Authorization Code Flow — frontend redirects to Google, backend exchanges code and issues JWTs',
    steps: [
      { actor: '🖥️ Frontend', action: 'Click "Continue with Google"', target: '🖥️ Frontend', note: 'Builds Google OAuth URL with client_id, redirect_uri, scope, state=google' },
      { actor: '🖥️ Frontend', action: 'window.location.href = OAuth URL', target: '🔵 Google', note: 'Browser redirects to accounts.google.com consent screen' },
      { actor: '👤 User', action: 'Grant consent', target: '🔵 Google', note: 'User selects Google account and approves permissions (openid, email, profile)' },
      { actor: '🔵 Google', action: 'Redirect → /oauth2/callback?code=AUTH_CODE&state=google', target: '🖥️ Frontend', note: 'Browser redirects back to React app with auth code' },
      { actor: '🖥️ Frontend', action: 'OAuth2CallbackPage captures code + state', target: '🖥️ Frontend', note: 'Extracts code and provider name from URL params' },
      { actor: '🖥️ Frontend', action: 'POST /api/user/auth/oauth2/callback', target: '🌐 API Gateway', note: '{provider: "google", code: "AUTH_CODE"}' },
      { actor: '🌐 Gateway', action: 'Forward (no JWT needed — public)', target: '👤 User Service', note: 'Endpoint is under /api/user/auth/** permit pattern' },
      { actor: '👤 User', action: 'POST https://oauth2.googleapis.com/token', target: '🔵 Google', note: 'Exchange auth code for access_token (code + client_secret)' },
      { actor: '👤 User', action: 'GET googleapis.com/oauth2/v2/userinfo', target: '🔵 Google', note: 'Fetch profile: {id, email, name, picture}' },
      { actor: '👤 User', action: 'SELECT * WHERE auth_provider=GOOGLE AND provider_id=id', target: '🗄️ MySQL', note: 'Check if returning Google user' },
      { actor: '👤 User', action: 'If new → SELECT * WHERE email=email', target: '🗄️ MySQL', note: 'Check for existing LOCAL account with same email' },
      { actor: '👤 User', action: 'If email conflict → REJECT (409)', target: '👤 User', note: '"Account already exists. Sign in with your password."' },
      { actor: '👤 User', action: 'If new email → INSERT User', target: '🗄️ MySQL', note: 'authProvider=GOOGLE, auto-generated username, random password' },
      { actor: '👤 User', action: 'Generate JWT tokens', target: '👤 User', note: 'Access (1d) + Refresh (7d) — same as normal login' },
      { actor: '👤 User', action: 'SETEX refresh:{email}', target: '⚡ Redis', note: 'Store refresh token with 15-day TTL' },
      { actor: '👤 User', action: 'Set-Cookie (httpOnly)', target: '🖥️ Frontend', note: 'accessToken + refreshToken cookies' },
      { actor: '🖥️ Frontend', action: 'Zustand login() + fetchCart()', target: '🖥️ Frontend', note: 'Store user in state, redirect to homepage' },
    ],
  },
];

export default function FlowsPage() {
  const [activeFlow, setActiveFlow] = useState('purchase');
  const flow = FLOWS.find((f) => f.id === activeFlow);

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Data Flows</h1>
        <p className="page-subtitle">
          Interactive step-by-step visualization of the major system flows.
        </p>
      </div>

      <div className="detail-tabs" style={{ marginBottom: '1.5rem' }}>
        {FLOWS.map((f) => (
          <button
            key={f.id}
            className={`detail-tab${activeFlow === f.id ? ' active' : ''}`}
            onClick={() => setActiveFlow(f.id)}
          >
            {f.name}
          </button>
        ))}
      </div>

      {flow && (
        <div className="glass-card">
          <div className="glass-card-header">
            <span className="glass-card-title">{flow.name}</span>
          </div>
          <div className="glass-card-body" style={{ padding: '0.5rem 1rem' }}>
            <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem', fontSize: '0.9rem', padding: '0.5rem' }}>
              {flow.description}
            </p>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 0 }}>
              {flow.steps.map((step, i) => (
                <FlowStep key={i} step={step} index={i} total={flow.steps.length} />
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function FlowStep({ step, index, total }) {
  const [expanded, setExpanded] = useState(false);

  return (
    <div style={{ position: 'relative', paddingLeft: 40 }}>
      {/* Timeline line */}
      {index < total - 1 && (
        <div style={{
          position: 'absolute',
          left: 15,
          top: 28,
          bottom: -4,
          width: 2,
          background: 'linear-gradient(to bottom, var(--accent-indigo) 0%, var(--border-glass) 100%)',
        }} />
      )}

      {/* Timeline dot */}
      <div style={{
        position: 'absolute',
        left: 8,
        top: 14,
        width: 16,
        height: 16,
        borderRadius: '50%',
        background: 'var(--accent-indigo)',
        border: '3px solid var(--bg-secondary)',
        zIndex: 1,
        boxShadow: '0 0 8px rgba(99, 102, 241, 0.4)',
      }} />

      <div
        onClick={() => setExpanded(!expanded)}
        style={{
          padding: '12px 16px',
          marginBottom: 4,
          background: expanded ? 'var(--bg-glass-hover)' : 'transparent',
          border: '1px solid transparent',
          borderRadius: 'var(--radius-sm)',
          cursor: 'pointer',
          transition: 'all var(--transition-fast)',
        }}
        onMouseEnter={(e) => {
          if (!expanded) e.currentTarget.style.background = 'var(--bg-glass)';
        }}
        onMouseLeave={(e) => {
          if (!expanded) e.currentTarget.style.background = 'transparent';
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: 10, flexWrap: 'wrap' }}>
          <span style={{
            fontSize: '0.65rem',
            fontWeight: 700,
            padding: '2px 8px',
            borderRadius: 10,
            background: 'var(--accent-indigo-dim)',
            color: 'var(--accent-indigo)',
            fontFamily: "'JetBrains Mono', monospace",
          }}>
            {String(index + 1).padStart(2, '0')}
          </span>
          <span style={{ fontSize: '0.82rem', color: 'var(--text-muted)', minWidth: 90 }}>
            {step.actor}
          </span>
          <span style={{ color: 'var(--accent-indigo)', fontSize: '0.9rem' }}>→</span>
          <span style={{
            fontFamily: "'JetBrains Mono', monospace",
            fontSize: '0.82rem',
            color: 'var(--accent-cyan)',
            fontWeight: 500,
          }}>
            {step.action}
          </span>
          <span style={{ color: 'var(--accent-indigo)', fontSize: '0.9rem' }}>→</span>
          <span style={{ fontSize: '0.82rem', color: 'var(--text-primary)', fontWeight: 500 }}>
            {step.target}
          </span>
        </div>
        {expanded && (
          <div style={{
            marginTop: 8,
            padding: '8px 12px',
            background: 'var(--bg-glass)',
            borderRadius: 'var(--radius-sm)',
            fontSize: '0.82rem',
            color: 'var(--text-secondary)',
            borderLeft: '3px solid var(--accent-indigo)',
          }}>
            {step.note}
          </div>
        )}
      </div>
    </div>
  );
}
