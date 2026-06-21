export default function SecurityPage() {
  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Security Architecture</h1>
        <p className="page-subtitle">
          Multi-layered security model covering authentication, authorization, and inter-service trust.
        </p>
      </div>

      {/* Security Layers */}
      <div className="glass-card" style={{ marginBottom: '2rem' }}>
        <div className="glass-card-header">
          <span style={{ fontSize: '1.1rem' }}>🛡️</span>
          <span className="glass-card-title">Security Layers</span>
        </div>
        <div className="glass-card-body">
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <SecurityLayer
              number={1}
              title="API Gateway — Edge Security"
              color="#818cf8"
              items={[
                'JWTTokenValidatorFilter: Extracts JWT from Authorization header or accessToken cookie',
                'Injects downstream headers: X-User-Id, X-User-Email, X-User-Role',
                'GatewaySecretFilter: Injects X-Gateway-Secret on all proxied requests',
                'SlidingWindowRateLimiter: Bucket4j-based, keyed by IP (X-Forwarded-For)',
                'Invalid/expired JWT → 401 Unauthorized',
              ]}
            />
            <SecurityLayer
              number={2}
              title="Microservice — JwtAuthFilter"
              color="#34d399"
              items={[
                'Check X-Service-Secret → Internal call (ROLE_SYSTEM, system UUID)',
                'Check X-Gateway-Secret → Gateway call (parse JWT, set Authentication)',
                'Neither header present → 403 Forbidden: "Direct access is blocked"',
                'Skip filter for: /actuator/**, /api/user/auth/**, /api/user/login/**',
              ]}
            />
            <SecurityLayer
              number={3}
              title="Business Logic — Authentication Object"
              color="#fbbf24"
              items={[
                'Principal = UUID userId (extracted from JWT)',
                'Credentials = JWT token string',
                'Authorities = [ROLE_USER, ROLE_SELLER, ROLE_ADMIN, ROLE_SYSTEM]',
                'Ownership checks on every mutation (user can only modify their own data)',
              ]}
            />
          </div>
        </div>
      </div>

      {/* Secret Headers */}
      <div className="glass-card" style={{ marginBottom: '2rem' }}>
        <div className="glass-card-header">
          <span style={{ fontSize: '1.1rem' }}>🔑</span>
          <span className="glass-card-title">Secret Headers</span>
        </div>
        <div className="glass-card-body">
          <table className="schema-table">
            <thead>
              <tr>
                <th>Header</th>
                <th>Value</th>
                <th>Purpose</th>
                <th>Injected By</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td><span className="col-name">X-Gateway-Secret</span></td>
                <td><code style={{ color: 'var(--accent-emerald)', fontFamily: "'JetBrains Mono', monospace", fontSize: '0.78rem' }}>${'{GATEWAY_COMMUNICATION_SECRET}'}</code></td>
                <td style={{ color: 'var(--text-secondary)', fontSize: '0.82rem' }}>Proves request came via API Gateway</td>
                <td style={{ color: 'var(--text-secondary)', fontSize: '0.82rem' }}>API Gateway</td>
              </tr>
              <tr>
                <td><span className="col-name">X-Service-Secret</span></td>
                <td><code style={{ color: 'var(--accent-emerald)', fontFamily: "'JetBrains Mono', monospace", fontSize: '0.78rem' }}>${'{INTERNAL_SERVICE_SECRET}'}</code></td>
                <td style={{ color: 'var(--text-secondary)', fontSize: '0.82rem' }}>Proves inter-service call (trusted)</td>
                <td style={{ color: 'var(--text-secondary)', fontSize: '0.82rem' }}>Any service (RestTemplate)</td>
              </tr>
              <tr>
                <td><span className="col-name">X-Razorpay-Signature</span></td>
                <td><code style={{ color: 'var(--accent-emerald)', fontFamily: "'JetBrains Mono', monospace", fontSize: '0.78rem' }}>HMAC-SHA256(payload, webhook_secret)</code></td>
                <td style={{ color: 'var(--text-secondary)', fontSize: '0.82rem' }}>Razorpay webhook verification</td>
                <td style={{ color: 'var(--text-secondary)', fontSize: '0.82rem' }}>Razorpay API</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      {/* JWT Details */}
      <div className="glass-card" style={{ marginBottom: '2rem' }}>
        <div className="glass-card-header">
          <span style={{ fontSize: '1.1rem' }}>🎟️</span>
          <span className="glass-card-title">JWT Token Configuration</span>
        </div>
        <div className="glass-card-body">
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1rem' }}>
            <TokenCard
              title="Access Token"
              items={[
                { label: 'Expiration', value: '86,400,000 ms (1 day)' },
                { label: 'Cookie Name', value: 'accessToken' },
                { label: 'Cookie Flags', value: 'HttpOnly, SameSite=Lax, Path=/' },
                { label: 'Claims: sub', value: 'email' },
                { label: 'Claims: id', value: 'UUID' },
                { label: 'Claims: username', value: 'username' },
                { label: 'Claims: role', value: 'primary role' },
              ]}
            />
            <TokenCard
              title="Refresh Token"
              items={[
                { label: 'Expiration', value: '604,800,000 ms (7 days)' },
                { label: 'Cookie Name', value: 'refreshToken' },
                { label: 'Cookie Flags', value: 'HttpOnly, SameSite=Lax, Path=/' },
                { label: 'Storage', value: 'Cookie + Redis (refresh:{email})' },
                { label: 'Validation', value: 'Token must match Redis value' },
                { label: 'Rotation', value: 'New tokens on every refresh' },
              ]}
            />
          </div>

          <div style={{ marginTop: '1.5rem', padding: '1rem', background: 'var(--bg-glass)', borderRadius: 'var(--radius-md)' }}>
            <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: 6 }}>
              Shared JWT Secret (All Services)
            </div>
            <code style={{
              fontFamily: "'JetBrains Mono', monospace",
              fontSize: '0.85rem',
              color: 'var(--accent-amber)',
              wordBreak: 'break-all',
            }}>
              Loaded from environment variable: JWT_SECRET
            </code>
          </div>
        </div>
      </div>

      {/* OTP Security */}
      <div className="glass-card" style={{ marginBottom: '2rem' }}>
        <div className="glass-card-header">
          <span style={{ fontSize: '1.1rem' }}>📱</span>
          <span className="glass-card-title">OTP Security</span>
        </div>
        <div className="glass-card-body">
          <div className="info-grid">
            <InfoItem label="Storage" value="BCrypt-hashed in Redis (otp:{email})" />
            <InfoItem label="Expiry" value="5 minutes TTL" />
            <InfoItem label="Cooldown" value="1 minute between requests (otp:cooldown:{email})" />
            <InfoItem label="Rate Limit" value="Max 5 OTPs per 15-minute window (otp:count:{email})" />
            <InfoItem label="Validation" value="BCrypt.matches(submitted, stored)" />
            <InfoItem label="Gateway Rate Limit" value="AUTH bucket: 10 req/5min" />
          </div>
        </div>
      </div>

      {/* OAuth2 Social Login */}
      <div className="glass-card" style={{ marginBottom: '2rem' }}>
        <div className="glass-card-header">
          <span style={{ fontSize: '1.1rem' }}>🔑</span>
          <span className="glass-card-title">OAuth2 Google Login</span>
        </div>
        <div className="glass-card-body">
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginBottom: '1.25rem', lineHeight: 1.6 }}>
            Supports Google sign-in via the <strong style={{ color: 'var(--text-primary)' }}>Authorization Code Flow</strong>.
            The frontend redirects to Google, receives an auth code, and sends it to the backend.
            The backend exchanges the code for an access token, fetches the user profile, and issues JWTs — converging into the same auth system as email/password login.
          </p>

          <div className="info-grid" style={{ marginBottom: '1.25rem' }}>
            <InfoItem label="Provider" value="Google (accounts.google.com)" />
            <InfoItem label="Auth URL" value="accounts.google.com/o/oauth2/v2/auth" />
            <InfoItem label="Token URL" value="oauth2.googleapis.com/token" />
            <InfoItem label="User Info URL" value="googleapis.com/oauth2/v2/userinfo" />
            <InfoItem label="Scopes" value="openid email profile" />
            <InfoItem label="Redirect URI" value="http://localhost:5173/oauth2/callback" />
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <SecurityLayer
              number={1}
              title="Account Conflict Policy — Reject (No Auto-Link)"
              color="#fb7185"
              items={[
                'If Google email matches an existing LOCAL account → returns error: "Account already exists. Sign in with your password."',
                'Prevents account takeover attacks where an attacker controls a Google account with the victim\'s email',
                'Users can only sign in via the method they originally used to create their account',
              ]}
            />
            <SecurityLayer
              number={2}
              title="OAuth User Guards"
              color="#34d399"
              items={[
                'OAuth users have authProvider set to GOOGLE (never LOCAL)',
                'Password login endpoint rejects Google users: "This account uses GOOGLE sign-in. Please use the social login button instead."',
                'OAuth users get a random UUID password (BCrypt-hashed) — unusable but satisfies DB constraints',
              ]}
            />
            <SecurityLayer
              number={3}
              title="Client ID Security"
              color="#818cf8"
              items={[
                'Client ID (public) is stored in frontend .env file (VITE_GOOGLE_CLIENT_ID)',
                'Client secret (private) is stored in backend application.properties — never sent to browser',
                'state parameter carries the provider name and prevents CSRF on the OAuth callback',
                'Redirect URI must exactly match the one registered in Google Cloud Console',
              ]}
            />
          </div>
        </div>
      </div>

      {/* Validation Rules */}
      <div className="glass-card">
        <div className="glass-card-header">
          <span style={{ fontSize: '1.1rem' }}>✅</span>
          <span className="glass-card-title">Input Validation Rules</span>
        </div>
        <div className="glass-card-body">
          <table className="schema-table">
            <thead>
              <tr>
                <th>Field</th>
                <th>Rule</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td><span className="col-name">Username</span></td>
                <td style={{ color: 'var(--text-secondary)', fontSize: '0.82rem' }}>3-50 characters, alphanumeric + underscore only</td>
              </tr>
              <tr>
                <td><span className="col-name">Email</span></td>
                <td style={{ color: 'var(--text-secondary)', fontSize: '0.82rem' }}>Valid email format (Jakarta @Email)</td>
              </tr>
              <tr>
                <td><span className="col-name">Password</span></td>
                <td style={{ color: 'var(--text-secondary)', fontSize: '0.82rem' }}>8-128 chars, must contain uppercase, lowercase, digit, and special character</td>
              </tr>
              <tr>
                <td><span className="col-name">Profile Picture</span></td>
                <td style={{ color: 'var(--text-secondary)', fontSize: '0.82rem' }}>Max 2MB, must be image/* content type, stored as base64</td>
              </tr>
              <tr>
                <td><span className="col-name">Rating</span></td>
                <td style={{ color: 'var(--text-secondary)', fontSize: '0.82rem' }}>1.0 to 5.0 (Jakarta @Min/@Max)</td>
              </tr>
              <tr>
                <td><span className="col-name">Review</span></td>
                <td style={{ color: 'var(--text-secondary)', fontSize: '0.82rem' }}>Max 5000 characters</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

function SecurityLayer({ number, title, color, items }) {
  return (
    <div style={{
      padding: '1.25rem',
      background: `${color}08`,
      border: `1px solid ${color}22`,
      borderRadius: 'var(--radius-md)',
      borderLeft: `4px solid ${color}`,
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 10 }}>
        <span style={{
          width: 28,
          height: 28,
          borderRadius: '50%',
          background: `${color}22`,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: '0.8rem',
          fontWeight: 700,
          color: color,
        }}>
          {number}
        </span>
        <span style={{ fontSize: '0.95rem', fontWeight: 600, color: 'var(--text-primary)' }}>
          {title}
        </span>
      </div>
      <ul style={{ paddingLeft: '2.5rem', margin: 0 }}>
        {items.map((item, i) => (
          <li key={i} style={{
            fontSize: '0.82rem',
            color: 'var(--text-secondary)',
            lineHeight: 1.7,
            listStyleType: 'disc',
          }}>
            {item}
          </li>
        ))}
      </ul>
    </div>
  );
}

function TokenCard({ title, items }) {
  return (
    <div style={{
      padding: '1.25rem',
      background: 'var(--bg-glass)',
      border: '1px solid var(--border-glass)',
      borderRadius: 'var(--radius-md)',
    }}>
      <h4 style={{
        fontSize: '0.9rem',
        fontWeight: 700,
        color: 'var(--accent-indigo)',
        marginBottom: '0.75rem',
      }}>
        {title}
      </h4>
      {items.map((item, i) => (
        <div key={i} style={{
          display: 'flex',
          justifyContent: 'space-between',
          padding: '6px 0',
          borderBottom: i < items.length - 1 ? '1px solid var(--border-glass)' : 'none',
        }}>
          <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)', fontWeight: 500 }}>
            {item.label}
          </span>
          <span style={{
            fontSize: '0.78rem',
            color: 'var(--text-primary)',
            fontFamily: "'JetBrains Mono', monospace",
          }}>
            {item.value}
          </span>
        </div>
      ))}
    </div>
  );
}

function InfoItem({ label, value }) {
  return (
    <div className="info-block glass-card" style={{ padding: '1rem' }}>
      <div className="info-label">{label}</div>
      <div className="info-value" style={{ fontSize: '0.85rem' }}>{value}</div>
    </div>
  );
}
