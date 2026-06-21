import { useState } from 'react';

export default function DataPage({ services }) {
  const [selectedService, setSelectedService] = useState(null);
  const servicesWithEntities = services.filter((s) => s.entities.length > 0);

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Database Schema</h1>
        <p className="page-subtitle">
          All services share a single MySQL database: <code style={{
            fontFamily: "'JetBrains Mono', monospace",
            padding: '2px 6px',
            background: 'var(--bg-glass)',
            borderRadius: 4,
            color: 'var(--accent-amber)',
            fontSize: '0.9rem',
          }}>reactlibrarydatabase</code>
        </p>
      </div>

      {/* DB Info */}
      <div className="glass-card" style={{ marginBottom: '2rem' }}>
        <div className="glass-card-header">
          <span style={{ fontSize: '1.1rem' }}>🗄️</span>
          <span className="glass-card-title">Database Configuration</span>
        </div>
        <div className="glass-card-body">
          <div className="info-grid">
            <InfoItem label="Driver" value="com.mysql.cj.jdbc.Driver" />
            <InfoItem label="URL" value="jdbc:mysql://localhost:3306/reactlibrarydatabase" />
            <InfoItem label="DDL Strategy" value="hibernate.ddl-auto = update" />
            <InfoItem label="JPA" value="Hibernate (show-sql: false, format_sql: true)" />
          </div>
        </div>
      </div>

      {/* Service Filter */}
      <div style={{ display: 'flex', gap: 6, marginBottom: '1.5rem', flexWrap: 'wrap' }}>
        <button
          className={`detail-tab${selectedService === null ? ' active' : ''}`}
          onClick={() => setSelectedService(null)}
        >
          All Tables
        </button>
        {servicesWithEntities.map((s) => (
          <button
            key={s.id}
            className={`detail-tab${selectedService === s.id ? ' active' : ''}`}
            onClick={() => setSelectedService(s.id)}
            style={selectedService === s.id ? { background: s.colorDim, color: s.color } : {}}
          >
            {s.icon} {s.name}
          </button>
        ))}
      </div>

      {/* Tables */}
      {servicesWithEntities
        .filter((s) => selectedService === null || s.id === selectedService)
        .map((service) => (
          <div key={service.id} style={{ marginBottom: '2rem' }}>
            <div style={{
              display: 'flex',
              alignItems: 'center',
              gap: 10,
              marginBottom: '1rem',
            }}>
              <span style={{ fontSize: '1.2rem' }}>{service.icon}</span>
              <h2 style={{
                fontSize: '1.1rem',
                fontWeight: 700,
                color: service.color,
              }}>
                {service.name}
              </h2>
              <span style={{
                fontSize: '0.7rem',
                padding: '3px 10px',
                borderRadius: 12,
                background: service.colorDim,
                color: service.color,
                fontWeight: 600,
              }}>
                {service.tables.join(', ')}
              </span>
            </div>

            {service.entities.map((entity) => (
              <div key={entity.name} className="glass-card" style={{ marginBottom: '1rem' }}>
                <div className="glass-card-header">
                  <span style={{
                    fontFamily: "'JetBrains Mono', monospace",
                    fontSize: '0.9rem',
                    fontWeight: 700,
                    color: 'var(--accent-cyan)',
                  }}>
                    {entity.name}
                  </span>
                  <span style={{
                    fontSize: '0.7rem',
                    color: 'var(--text-muted)',
                    marginLeft: 'auto',
                  }}>
                    {entity.fields.length} columns
                  </span>
                </div>
                <div style={{ overflowX: 'auto' }}>
                  <table className="schema-table">
                    <thead>
                      <tr>
                        <th>Column</th>
                        <th>Type</th>
                        <th>Constraint / Notes</th>
                      </tr>
                    </thead>
                    <tbody>
                      {entity.fields.map((f) => (
                        <tr key={f.name}>
                          <td>
                            <span className="col-name">{f.name}</span>
                            {f.constraint === 'PK' && <span className="col-pk">PK</span>}
                            {f.constraint?.includes('FK') && <span className="col-fk">FK</span>}
                            {f.constraint?.includes('UNIQUE') && (
                              <span style={{
                                display: 'inline-block',
                                fontSize: '0.6rem',
                                padding: '1px 6px',
                                borderRadius: 3,
                                background: 'rgba(34, 211, 238, 0.15)',
                                color: '#22d3ee',
                                fontWeight: 700,
                                marginLeft: 6,
                              }}>UQ</span>
                            )}
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
        ))}

      {/* Redis Keys */}
      <h2 className="section-heading" style={{ marginTop: '2rem' }}>Redis Cache Keys</h2>
      {services
        .filter((s) => s.redisKeys.length > 0)
        .map((service) => (
          <div key={service.id} className="glass-card" style={{ marginBottom: '1rem' }}>
            <div className="glass-card-header">
              <span style={{ fontSize: '1rem' }}>{service.icon}</span>
              <span className="glass-card-title" style={{ color: service.color }}>{service.name}</span>
            </div>
            <div className="glass-card-body" style={{ padding: '0.5rem 1rem' }}>
              <div className="redis-key-list">
                {service.redisKeys.map((k, i) => (
                  <div key={i} className="redis-key-item">
                    <span className="redis-key-name">{k.key}</span>
                    <span className="redis-key-ttl">{k.ttl}</span>
                    <span className="redis-key-purpose">{k.purpose}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        ))}

      {/* Index Summary */}
      <div className="glass-card" style={{ marginTop: '2rem' }}>
        <div className="glass-card-header">
          <span style={{ fontSize: '1.1rem' }}>📊</span>
          <span className="glass-card-title">Database Index Summary</span>
        </div>
        <div className="glass-card-body">
          <div style={{ fontFamily: "'JetBrains Mono', monospace", fontSize: '0.78rem', lineHeight: 2 }}>
            {[
              { table: 'users', indices: 'idx_users_email, idx_users_username, idx_users_phone' },
              { table: 'books', indices: 'idx_book_title, idx_book_author, idx_book_category, idx_book_isbn (unique)' },
              { table: 'seller_listings', indices: 'idx_listing_seller_id, idx_listing_book_id, uq_seller_book (unique)' },
              { table: 'carts', indices: 'idx_cart_user_id (unique)' },
              { table: 'cart_items', indices: 'idx_cart_item_cart_id, idx_cart_item_listing_id, uk_cart_listing (unique)' },
              { table: 'coupons', indices: 'idx_coupon_code (unique)' },
              { table: 'orders', indices: 'idx_orders_user_id, idx_orders_status, idx_orders_reference' },
              { table: 'order_items', indices: 'idx_order_items_order_id, idx_order_items_listing_id' },
              { table: 'payments', indices: 'idx_payments_order_id, idx_payments_user_id, idx_payments_gateway_order_id' },
              { table: 'seller_ledger', indices: 'idx_ledger_seller_id, idx_ledger_order_id, idx_ledger_status, idx_ledger_settlement_id' },
              { table: 'reviews', indices: 'idx_review_book_id, idx_review_user_id, uq_review_user_book (unique)' },
            ].map((row) => (
              <div key={row.table} style={{
                display: 'flex',
                gap: 16,
                padding: '4px 0',
                borderBottom: '1px solid var(--border-glass)',
              }}>
                <span style={{ color: 'var(--accent-amber)', minWidth: 140, fontWeight: 600 }}>
                  {row.table}
                </span>
                <span style={{ color: 'var(--text-secondary)' }}>{row.indices}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

function InfoItem({ label, value }) {
  return (
    <div className="info-block glass-card" style={{ padding: '1rem' }}>
      <div className="info-label">{label}</div>
      <div className="info-value" style={{
        fontSize: '0.82rem',
        fontFamily: "'JetBrains Mono', monospace",
      }}>
        {value}
      </div>
    </div>
  );
}
