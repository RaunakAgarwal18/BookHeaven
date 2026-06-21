import { useState } from 'react';

export default function ServicesPage({ services, onSelectService }) {
  const [search, setSearch] = useState('');

  const filtered = services.filter(
    (s) =>
      s.name.toLowerCase().includes(search.toLowerCase()) ||
      s.description.toLowerCase().includes(search.toLowerCase()) ||
      s.tech.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Services</h1>
        <p className="page-subtitle">
          Click on any service to explore its API endpoints, data entities, events, and configuration.
        </p>
      </div>

      <div style={{ marginBottom: '1.5rem' }}>
        <input
          type="text"
          placeholder="Search services..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
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
            transition: 'border-color var(--transition-fast)',
          }}
          onFocus={(e) => (e.target.style.borderColor = 'var(--accent-indigo)')}
          onBlur={(e) => (e.target.style.borderColor = 'var(--border-glass)')}
        />
      </div>

      <div className="services-grid">
        {filtered.map((service) => (
          <div
            key={service.id}
            className="glass-card service-card"
            style={{ '--service-color': service.color, '--service-bg': service.colorDim }}
            onClick={() => onSelectService(service)}
          >
            <div className="service-card-inner">
              <div className="service-icon" style={{ background: service.colorDim, color: service.color }}>
                {service.icon}
              </div>
              <div className="service-info">
                <div className="service-name">{service.name}</div>
                <div className="service-desc">{service.description}</div>
                <div className="service-meta">
                  <span className="service-badge badge-port">:{service.port}</span>
                  <span className="service-badge badge-tech">{service.tech}</span>
                  {service.endpoints.length > 0 && (
                    <span className="service-badge" style={{ background: 'rgba(129, 140, 248, 0.12)', color: '#818cf8' }}>
                      {service.endpoints.length} endpoints
                    </span>
                  )}
                  {service.tables.length > 0 && (
                    <span className="service-badge badge-db">{service.tables.length} tables</span>
                  )}
                  {service.events.length > 0 && (
                    <span className="service-badge" style={{ background: 'rgba(167, 139, 250, 0.12)', color: '#a78bfa' }}>
                      {service.events.length} events
                    </span>
                  )}
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
