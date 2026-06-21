import { useState } from 'react';

export default function MessagingPage({ events, exchanges }) {
  const [filter, setFilter] = useState('');

  const filteredEvents = events.filter(
    (e) =>
      e.name.toLowerCase().includes(filter.toLowerCase()) ||
      e.producer.toLowerCase().includes(filter.toLowerCase()) ||
      e.consumer.toLowerCase().includes(filter.toLowerCase()) ||
      e.exchange.toLowerCase().includes(filter.toLowerCase())
  );

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Messaging Topology</h1>
        <p className="page-subtitle">
          RabbitMQ exchanges, queues, and event routing across all services.
        </p>
      </div>

      {/* Exchanges */}
      <h2 className="section-heading">Exchanges</h2>
      <div className="services-grid" style={{ marginBottom: '2rem' }}>
        {exchanges.map((ex) => (
          <div key={ex.name} className="glass-card" style={{ padding: '1.25rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 10 }}>
              <span style={{ fontSize: '1.2rem' }}>🔀</span>
              <span style={{
                fontFamily: "'JetBrains Mono', monospace",
                fontSize: '0.88rem',
                color: 'var(--accent-violet)',
                fontWeight: 600,
              }}>
                {ex.name}
              </span>
            </div>
            <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
              <span className="service-badge" style={{ background: 'rgba(167, 139, 250, 0.12)', color: '#a78bfa' }}>
                {ex.type}
              </span>
              <span className="service-badge" style={{ background: 'rgba(52, 211, 153, 0.12)', color: '#34d399' }}>
                {ex.queues} queue{ex.queues > 1 ? 's' : ''}
              </span>
              <span className="service-badge" style={{ background: 'rgba(251, 191, 36, 0.12)', color: '#fbbf24' }}>
                {ex.declaredBy}
              </span>
            </div>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: 10 }}>
              {ex.purpose}
            </p>
          </div>
        ))}
      </div>

      {/* Events */}
      <h2 className="section-heading">Event Catalog</h2>
      <div style={{ marginBottom: '1rem' }}>
        <input
          type="text"
          placeholder="Filter events..."
          value={filter}
          onChange={(e) => setFilter(e.target.value)}
          style={{
            width: '100%',
            maxWidth: 400,
            padding: '10px 16px',
            background: 'var(--bg-glass)',
            border: '1px solid var(--border-glass)',
            borderRadius: 'var(--radius-md)',
            color: 'var(--text-primary)',
            fontSize: '0.9rem',
            outline: 'none',
          }}
        />
      </div>

      <div style={{ overflowX: 'auto' }}>
        <table className="schema-table">
          <thead>
            <tr>
              <th>Event</th>
              <th>Producer</th>
              <th>Consumer</th>
              <th>Exchange</th>
              <th>Routing Key</th>
              <th>Queue</th>
            </tr>
          </thead>
          <tbody>
            {filteredEvents.map((ev, i) => (
              <tr key={i}>
                <td>
                  <span style={{
                    fontFamily: "'JetBrains Mono', monospace",
                    color: 'var(--accent-cyan)',
                    fontWeight: 600,
                    fontSize: '0.82rem',
                  }}>
                    {ev.name}
                  </span>
                </td>
                <td style={{ color: 'var(--accent-emerald)', fontSize: '0.82rem' }}>📤 {ev.producer}</td>
                <td style={{ color: 'var(--accent-violet)', fontSize: '0.82rem' }}>📥 {ev.consumer}</td>
                <td>
                  <span style={{
                    fontFamily: "'JetBrains Mono', monospace",
                    fontSize: '0.78rem',
                    color: 'var(--text-secondary)',
                  }}>
                    {ev.exchange}
                  </span>
                </td>
                <td>
                  <span style={{
                    fontFamily: "'JetBrains Mono', monospace",
                    fontSize: '0.78rem',
                    padding: '2px 8px',
                    background: 'var(--accent-indigo-dim)',
                    borderRadius: 4,
                    color: 'var(--accent-indigo)',
                  }}>
                    {ev.key}
                  </span>
                </td>
                <td style={{
                  fontFamily: "'JetBrains Mono', monospace",
                  fontSize: '0.75rem',
                  color: 'var(--text-muted)',
                  maxWidth: 220,
                }}>
                  {ev.queue}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* DLQ Pattern */}
      <div className="glass-card" style={{ marginTop: '2rem' }}>
        <div className="glass-card-header">
          <span style={{ fontSize: '1.1rem' }}>🔄</span>
          <span className="glass-card-title">Retry & Dead Letter Queue (DLQ) Strategy</span>
        </div>
        <div className="glass-card-body">
          <div className="info-grid">
            <InfoBlock label="Retry Policy" value="3 attempts, exponential backoff (1s → 2s → 4s)" />
            <InfoBlock label="Recovery" value="RejectAndDontRequeueRecoverer → routes to DLQ" />
            <InfoBlock label="Dead Letter Exchange" value="email.dlx (DirectExchange)" />
            <InfoBlock label="Dead Letter Queue" value="email.dlq" />
            <InfoBlock label="Idempotency" value="Redis-backed ProcessedMessageTracker" />
            <InfoBlock label="Message ID" value="Deterministic IDs (e.g., order-confirmed-{orderId})" />
          </div>

          <div style={{ marginTop: '1.5rem' }}>
            <h4 className="subsection-heading">Order Timeout (DLX Pattern)</h4>
            <div style={{
              display: 'flex',
              alignItems: 'center',
              gap: 12,
              flexWrap: 'wrap',
              padding: '1rem',
              background: 'var(--bg-glass)',
              borderRadius: 'var(--radius-md)',
            }}>
              <FlowBox label="Publish" sub="OrderTimeoutEvent" />
              <Arrow />
              <FlowBox label="order.timeout.queue" sub="TTL: 60 seconds" highlight />
              <Arrow label="DLX" />
              <FlowBox label="order.timeout.exchange" sub="Reroute after TTL" />
              <Arrow />
              <FlowBox label="processing.queue" sub="OrderTimeoutConsumer" highlight />
              <Arrow />
              <FlowBox label="Check if PENDING" sub="→ Mark FAILED + email" />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

function InfoBlock({ label, value }) {
  return (
    <div className="info-block glass-card" style={{ padding: '1rem' }}>
      <div className="info-label">{label}</div>
      <div className="info-value" style={{ fontSize: '0.85rem' }}>{value}</div>
    </div>
  );
}

function FlowBox({ label, sub, highlight }) {
  return (
    <div style={{
      padding: '10px 14px',
      background: highlight ? 'var(--accent-indigo-dim)' : 'var(--bg-glass)',
      border: `1px solid ${highlight ? 'rgba(129, 140, 248, 0.3)' : 'var(--border-glass)'}`,
      borderRadius: 'var(--radius-sm)',
      textAlign: 'center',
      minWidth: 100,
    }}>
      <div style={{
        fontSize: '0.78rem',
        fontWeight: 600,
        color: highlight ? 'var(--accent-indigo)' : 'var(--text-primary)',
        fontFamily: "'JetBrains Mono', monospace",
      }}>
        {label}
      </div>
      <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', marginTop: 2 }}>{sub}</div>
    </div>
  );
}

function Arrow({ label }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
      <span style={{ color: 'var(--accent-indigo)', fontSize: '1.1rem' }}>→</span>
      {label && <span style={{ fontSize: '0.6rem', color: 'var(--accent-amber)', fontWeight: 600 }}>{label}</span>}
    </div>
  );
}
