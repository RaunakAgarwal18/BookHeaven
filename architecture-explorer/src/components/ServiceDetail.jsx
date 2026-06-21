import { useState } from 'react';

export default function ServiceDetail({ service, onClose }) {
  const [activeTab, setActiveTab] = useState('overview');

  const tabs = [
    { id: 'overview', label: 'Overview' },
    service.endpoints.length > 0 && { id: 'api', label: `API (${service.endpoints.length})` },
    service.entities.length > 0 && { id: 'entities', label: 'Entities' },
    service.events.length > 0 && { id: 'events', label: 'Events' },
    service.redisKeys.length > 0 && { id: 'redis', label: 'Redis' },
    service.rateLimits && { id: 'ratelimits', label: 'Rate Limits' },
  ].filter(Boolean);

  return (
    <div className="detail-overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="detail-backdrop" onClick={onClose} />
      <div className="detail-panel">
        <button className="detail-close" onClick={onClose}>✕</button>

        <div className="detail-header">
          <div className="service-icon" style={{ background: service.colorDim, color: service.color }}>
            {service.icon}
          </div>
          <div>
            <div className="detail-title" style={{ color: service.color }}>{service.name}</div>
            <div style={{ display: 'flex', gap: 8, marginTop: 6 }}>
              <span className="service-badge badge-port">:{service.port}</span>
              <span className="service-badge badge-tech">{service.tech}</span>
              {service.tables.length > 0 && (
                <span className="service-badge badge-db">{service.tables.length} tables</span>
              )}
            </div>
          </div>
        </div>

        <div className="detail-tabs">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              className={`detail-tab${activeTab === tab.id ? ' active' : ''}`}
              onClick={() => setActiveTab(tab.id)}
            >
              {tab.label}
            </button>
          ))}
        </div>

        <div className="detail-content">
          {activeTab === 'overview' && <OverviewTab service={service} />}
          {activeTab === 'api' && <ApiTab endpoints={service.endpoints} />}
          {activeTab === 'entities' && <EntitiesTab entities={service.entities} />}
          {activeTab === 'events' && <EventsTab events={service.events} />}
          {activeTab === 'redis' && <RedisTab keys={service.redisKeys} />}
          {activeTab === 'ratelimits' && service.rateLimits && <RateLimitsTab limits={service.rateLimits} />}
        </div>
      </div>
    </div>
  );
}

function parseText(text) {
  const parts = text.split(/(\*\*[^*]+\*\*)/g);
  return parts.map((part, i) => {
    if (part.startsWith('**') && part.endsWith('**')) {
      return (
        <strong key={i} style={{ color: 'var(--text-primary)', fontWeight: 600 }}>
          {part.slice(2, -2)}
        </strong>
      );
    }
    return part;
  });
}

function OverviewTab({ service }) {
  const lines = service.details.split('\n');

  return (
    <div>
      <div style={{ color: 'var(--text-secondary)', lineHeight: 1.8, fontSize: '0.9rem' }}>
        {lines.map((line, i) => {
          const trimmed = line.trim();
          if (trimmed.startsWith('•')) {
            return (
              <div key={i} style={{ display: 'flex', gap: 8, padding: '3px 0', paddingLeft: '0.5rem' }}>
                <span style={{ color: 'var(--accent-indigo)', flexShrink: 0 }}>•</span>
                <span>{parseText(trimmed.slice(1).trim())}</span>
              </div>
            );
          }
          if (trimmed === '') return <div key={i} style={{ height: 8 }} />;
          return <p key={i} style={{ margin: '4px 0' }}>{parseText(trimmed)}</p>;
        })}
      </div>
      {service.tables.length > 0 && (
        <div style={{ marginTop: '1.5rem' }}>
          <h4 className="subsection-heading">Database Tables</h4>
          <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
            {service.tables.map((t) => (
              <span key={t} className="service-badge badge-db" style={{ fontSize: '0.78rem', padding: '5px 12px' }}>
                {t}
              </span>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

function ApiTab({ endpoints }) {
  const [filter, setFilter] = useState('');
  const filtered = endpoints.filter(
    (ep) =>
      ep.path.toLowerCase().includes(filter.toLowerCase()) ||
      ep.desc.toLowerCase().includes(filter.toLowerCase()) ||
      ep.method.toLowerCase().includes(filter.toLowerCase())
  );

  return (
    <div>
      <input
        type="text"
        placeholder="Filter endpoints..."
        value={filter}
        onChange={(e) => setFilter(e.target.value)}
        style={{
          width: '100%',
          padding: '10px 14px',
          marginBottom: '1rem',
          background: 'var(--bg-glass)',
          border: '1px solid var(--border-glass)',
          borderRadius: 'var(--radius-sm)',
          color: 'var(--text-primary)',
          fontSize: '0.85rem',
          fontFamily: "'JetBrains Mono', monospace",
          outline: 'none',
        }}
      />
      <div className="endpoint-list">
        {filtered.map((ep, i) => (
          <div key={i} className="endpoint-item">
            <span className={`method-badge method-${ep.method}`}>{ep.method}</span>
            <span className="endpoint-path">{ep.path}</span>
            <span className="endpoint-desc">{ep.desc}</span>
            <span className={`endpoint-auth ${ep.auth === 'JWT' ? 'auth-jwt' : ep.auth === 'None' || ep.auth === 'Public' ? 'auth-none' : 'auth-internal'}`}>
              {ep.auth}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}

function EntitiesTab({ entities }) {
  return (
    <div>
      {entities.map((entity) => (
        <div key={entity.name} style={{ marginBottom: '2rem' }}>
          <h4 className="section-heading" style={{ color: 'var(--accent-cyan)' }}>
            {entity.name}
          </h4>
          <div style={{ overflowX: 'auto' }}>
            <table className="schema-table">
              <thead>
                <tr>
                  <th>Column</th>
                  <th>Type</th>
                  <th>Constraint</th>
                </tr>
              </thead>
              <tbody>
                {entity.fields.map((f) => (
                  <tr key={f.name}>
                    <td>
                      <span className="col-name">{f.name}</span>
                      {f.constraint === 'PK' && <span className="col-pk">PK</span>}
                      {f.constraint?.includes('FK') && <span className="col-fk">FK</span>}
                    </td>
                    <td><span className="col-type">{f.type}</span></td>
                    <td style={{ color: 'var(--text-secondary)', fontSize: '0.78rem' }}>{f.constraint}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ))}
    </div>
  );
}

function EventsTab({ events }) {
  return (
    <div className="event-list">
      {events.map((ev, i) => (
        <div
          key={i}
          className="event-item"
          style={{ borderLeftColor: ev.direction === 'produces' ? 'var(--accent-emerald)' : 'var(--accent-violet)' }}
        >
          <div className="event-name">{ev.name}</div>
          <div className="event-meta">
            <span>
              {ev.direction === 'produces' ? '📤' : '📥'} {ev.direction}
            </span>
            <span>🔀 {ev.exchange}</span>
            <span>🔑 {ev.key}</span>
          </div>
        </div>
      ))}
    </div>
  );
}

function RedisTab({ keys }) {
  return (
    <div className="redis-key-list">
      {keys.map((k, i) => (
        <div key={i} className="redis-key-item">
          <span className="redis-key-name">{k.key}</span>
          <span className="redis-key-ttl">{k.ttl}</span>
          <span className="redis-key-purpose">{k.purpose}</span>
        </div>
      ))}
    </div>
  );
}

function RateLimitsTab({ limits }) {
  return (
    <div className="endpoint-list">
      {limits.map((lim, i) => (
        <div key={i} className="endpoint-item">
          <span className="method-badge method-PUT" style={{ minWidth: 80 }}>{lim.name}</span>
          <span className="endpoint-path" style={{ flex: 1 }}>{lim.limit}</span>
          <span className="endpoint-desc">{lim.bucket}</span>
        </div>
      ))}
    </div>
  );
}
