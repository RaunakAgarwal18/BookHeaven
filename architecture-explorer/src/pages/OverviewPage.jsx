import { useState } from 'react';

export default function OverviewPage({ services, onSelectService, onNavigate }) {
  const totalEndpoints = services.reduce((sum, s) => sum + s.endpoints.length, 0);
  const totalTables = services.reduce((sum, s) => sum + s.tables.length, 0);
  const totalEvents = services.reduce((sum, s) => sum + s.events.filter(e => e.direction === 'produces').length, 0);
  const totalRedisKeys = services.reduce((sum, s) => sum + s.redisKeys.length, 0);

  return (
    <div>
      {/* Hero Section */}
      <div style={{ textAlign: 'center', padding: '3rem 1rem 2rem' }}>
        <h1 className="page-title" style={{ fontSize: '2.8rem', marginBottom: '1rem' }}>
          📚 BookHeaven Architecture
        </h1>
        <p className="page-subtitle" style={{ margin: '0 auto', maxWidth: '700px', fontSize: '1.05rem', lineHeight: 1.7 }}>
          An interactive explorer for the BookHeaven microservices platform.
          Click on any service to dive deep into its API endpoints, data models, events, and caching strategies.
        </p>
      </div>

      {/* Stats */}
      <div className="stats-banner">
        <StatCard value={services.length} label="Microservices" />
        <StatCard value={totalEndpoints} label="API Endpoints" />
        <StatCard value={totalTables} label="Database Tables" />
        <StatCard value={totalEvents} label="Event Types" />
        <StatCard value={totalRedisKeys} label="Redis Key Patterns" />
      </div>

      {/* Quick Navigation */}
      <div style={{ display: 'flex', gap: 8, marginBottom: '2rem', flexWrap: 'wrap', justifyContent: 'center' }}>
        {['services', 'flows', 'messaging', 'security', 'data', 'decisions'].map((tab) => (
          <button
            key={tab}
            onClick={() => onNavigate(tab)}
            style={{
              padding: '8px 18px',
              background: 'var(--bg-glass)',
              border: '1px solid var(--border-glass)',
              borderRadius: 'var(--radius-md)',
              color: 'var(--text-secondary)',
              cursor: 'pointer',
              fontSize: '0.85rem',
              fontWeight: 500,
              transition: 'all var(--transition-fast)',
            }}
            onMouseEnter={(e) => {
              e.target.style.borderColor = 'var(--border-glass-hover)';
              e.target.style.color = 'var(--text-primary)';
              e.target.style.background = 'var(--bg-glass-hover)';
            }}
            onMouseLeave={(e) => {
              e.target.style.borderColor = 'var(--border-glass)';
              e.target.style.color = 'var(--text-secondary)';
              e.target.style.background = 'var(--bg-glass)';
            }}
          >
            {tab === 'decisions' ? 'Design Decisions' : tab.charAt(0).toUpperCase() + tab.slice(1)}
          </button>
        ))}
      </div>

      {/* Architecture Overview Diagram */}
      <div className="glass-card" style={{ marginBottom: '2rem' }}>
        <div className="glass-card-header">
          <span style={{ fontSize: '1.1rem' }}>🏗️</span>
          <span className="glass-card-title">System Architecture</span>
        </div>
        <div className="glass-card-body">
          <ArchitectureDiagram services={services} onSelectService={onSelectService} />
        </div>
      </div>

      {/* Tech Stack */}
      <div className="glass-card" style={{ marginBottom: '2rem' }}>
        <div className="glass-card-header">
          <span style={{ fontSize: '1.1rem' }}>⚡</span>
          <span className="glass-card-title">Technology Stack</span>
        </div>
        <div className="glass-card-body">
          <div className="info-grid">
            <TechItem label="Architecture" value="Microservices + API Gateway" />
            <TechItem label="Service Discovery" value="Netflix Eureka" />
            <TechItem label="Gateway" value="Spring Cloud Gateway (Reactive)" />
            <TechItem label="Authentication" value="JWT (httpOnly Cookies)" />
            <TechItem label="Database" value="MySQL 8 (Shared Instance)" />
            <TechItem label="Caching" value="Redis" />
            <TechItem label="Messaging" value="RabbitMQ (Topic + DLX)" />
            <TechItem label="Payments" value="Razorpay (Webhook-driven)" />
            <TechItem label="Email" value="JavaMailSender (Gmail SMTP)" />
            <TechItem label="Rate Limiting" value="Bucket4j (Sliding Window)" />
            <TechItem label="Observability" value="Prometheus + Grafana + Loki + Micrometer" />
            <TechItem label="Frontend" value="React (Vite)" />
          </div>
        </div>
      </div>

      {/* Service Grid */}
      <h2 className="section-heading" style={{ marginTop: '2rem' }}>All Services</h2>
      <div className="services-grid">
        {services.map((service) => (
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
                  {service.tables.length > 0 && (
                    <span className="service-badge badge-db">{service.tables.length} tables</span>
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

function StatCard({ value, label }) {
  return (
    <div className="glass-card stat-card animate-pulse-glow">
      <div className="stat-value">{value}</div>
      <div className="stat-label">{label}</div>
    </div>
  );
}

function TechItem({ label, value }) {
  return (
    <div className="info-block glass-card" style={{ padding: '1rem' }}>
      <div className="info-label">{label}</div>
      <div className="info-value">{value}</div>
    </div>
  );
}

function ArchitectureDiagram({ services, onSelectService }) {
  const [hovered, setHovered] = useState(null);

  // Node positions (x, y) on a 1200x720 canvas
  const nodes = [
    { id: 'frontend',   label: 'React Frontend', sub: ':5173',  icon: '🖥️', x: 600, y: 30,  color: '#818cf8', w: 150 },
    { id: 'api-gateway', label: 'API Gateway',   sub: ':8090',  icon: '🌐', x: 600, y: 120, color: '#818cf8', w: 140, svcId: 'api-gateway' },
    { id: 'eureka',     label: 'Eureka Server',  sub: ':8761',  icon: '🔍', x: 1020, y: 120, color: '#64748b', w: 140, svcId: 'eureka-server' },
    { id: 'user',       label: 'User Service',   sub: ':8081',  icon: '👤', x: 120, y: 270, color: '#34d399', w: 130, svcId: 'user-service' },
    { id: 'book',       label: 'Book Service',   sub: ':8079',  icon: '📚', x: 330, y: 270, color: '#fbbf24', w: 130, svcId: 'book-service' },
    { id: 'cart',       label: 'Cart Service',   sub: ':8084',  icon: '🛒', x: 540, y: 270, color: '#22d3ee', w: 130, svcId: 'cart-service' },
    { id: 'order',      label: 'Order Service',  sub: ':8078',  icon: '📦', x: 750, y: 270, color: '#fb7185', w: 130, svcId: 'order-service' },
    { id: 'payment',    label: 'Payment Service', sub: ':8076', icon: '💳', x: 960, y: 270, color: '#a78bfa', w: 145, svcId: 'payment-service' },
    { id: 'review',     label: 'Review Service', sub: ':8083',  icon: '⭐', x: 200, y: 400, color: '#fb923c', w: 140, svcId: 'review-service' },
    { id: 'email',      label: 'Email Service',  sub: ':8082',  icon: '📧', x: 510, y: 400, color: '#f472b6', w: 130, svcId: 'email-service' },
    { id: 'mysql',      label: 'MySQL',           sub: ':3306',  icon: '🗄️', x: 200, y: 555, color: '#fbbf24', w: 110 },
    { id: 'redis',      label: 'Redis',           sub: ':6379',  icon: '⚡', x: 420, y: 555, color: '#fb7185', w: 100 },
    { id: 'rabbitmq',   label: 'RabbitMQ',        sub: ':5672',  icon: '🐇', x: 640, y: 555, color: '#a78bfa', w: 120 },
    { id: 'razorpay',   label: 'Razorpay',        sub: 'API',    icon: '🏦', x: 860, y: 555, color: '#34d399', w: 120 },
    { id: 'smtp',       label: 'Gmail SMTP',      sub: 'External', icon: '📫', x: 1040, y: 555, color: '#f472b6', w: 120 },
    { id: 'prometheus', label: 'Prometheus',      sub: ':9090',  icon: '📊', x: 830, y: 400, color: '#64748b', w: 130 },
  ];

  // Connections: [fromId, toId, type, label?]
  //   type: 'rest' (solid cyan), 'mq' (dashed violet), 'data' (dotted amber), 'ext' (solid emerald)
  const connections = [
    // Frontend → Gateway
    ['frontend', 'api-gateway', 'rest', 'HTTP'],
    // Gateway → services
    ['api-gateway', 'user', 'rest', ''],
    ['api-gateway', 'book', 'rest', ''],
    ['api-gateway', 'cart', 'rest', ''],
    ['api-gateway', 'order', 'rest', ''],
    ['api-gateway', 'payment', 'rest', ''],
    ['api-gateway', 'review', 'rest', ''],
    // Eureka discovery (all services register)
    ['api-gateway', 'eureka', 'data', 'discover'],
    ['user', 'eureka', 'data', ''],
    ['book', 'eureka', 'data', ''],
    ['cart', 'eureka', 'data', ''],
    ['order', 'eureka', 'data', ''],
    ['payment', 'eureka', 'data', ''],
    ['email', 'eureka', 'data', ''],
    ['review', 'eureka', 'data', ''],
    // Inter-service REST
    ['order', 'cart', 'rest', 'GET cart'],
    ['order', 'user', 'rest', 'GET user'],
    ['order', 'book', 'rest', 'stock'],
    ['order', 'payment', 'rest', 'initiate'],
    ['cart', 'book', 'rest', 'bulk fetch'],
    ['review', 'book', 'rest', 'update rating'],
    ['user', 'email', 'rest', 'send OTP'],
    ['payment', 'order', 'rest', 'confirm/fail'],
    // RabbitMQ events
    ['user', 'rabbitmq', 'mq', 'Welcome'],
    ['order', 'rabbitmq', 'mq', 'Events'],
    ['payment', 'rabbitmq', 'mq', 'Payout'],
    ['rabbitmq', 'email', 'mq', 'Emails'],
    ['rabbitmq', 'payment', 'mq', 'Ledger'],
    ['rabbitmq', 'order', 'mq', 'Timeout'],
    // Database
    ['user', 'mysql', 'data', ''],
    ['book', 'mysql', 'data', ''],
    ['cart', 'mysql', 'data', ''],
    ['order', 'mysql', 'data', ''],
    ['payment', 'mysql', 'data', ''],
    ['review', 'mysql', 'data', ''],
    // Redis
    ['user', 'redis', 'data', 'OTP/JWT'],
    ['book', 'redis', 'data', 'Cache'],
    ['email', 'redis', 'data', 'Dedup'],
    // External
    ['payment', 'razorpay', 'ext', 'Pay/Refund'],
    ['email', 'smtp', 'ext', 'Send'],
    // Prometheus
    ['prometheus', 'api-gateway', 'data', 'scrape'],
  ];

  const nodeMap = {};
  nodes.forEach(n => { nodeMap[n.id] = n; });

  const getCenter = (id) => {
    const n = nodeMap[id];
    return { x: n.x, y: n.y + 18 };
  };

  const isConnected = (nodeId) => {
    if (!hovered) return true;
    return nodeId === hovered || connections.some(
      ([a, b]) => (a === hovered && b === nodeId) || (b === hovered && a === nodeId)
    );
  };

  const connStyle = {
    rest: { stroke: '#22d3ee', dasharray: 'none' },
    mq:   { stroke: '#a78bfa', dasharray: '6,4' },
    data: { stroke: '#fbbf2488', dasharray: '3,3' },
    ext:  { stroke: '#34d399', dasharray: 'none' },
  };

  const svgW = 1180;
  const svgH = 620;

  return (
    <div>
      <div style={{ overflowX: 'auto', margin: '-0.5rem' }}>
        <svg
          viewBox={`0 0 ${svgW} ${svgH}`}
          style={{ width: '100%', minWidth: 700, height: 'auto', display: 'block' }}
        >
          <defs>
            {/* Glow filter */}
            <filter id="glow">
              <feGaussianBlur stdDeviation="3" result="blur" />
              <feMerge><feMergeNode in="blur" /><feMergeNode in="SourceGraphic" /></feMerge>
            </filter>
            {/* Animated dash for MQ lines */}
            <style>{`
              @keyframes dash { to { stroke-dashoffset: -20; } }
              .mq-line { animation: dash 1.5s linear infinite; }
              .arch-node { transition: opacity 0.2s; }
              .arch-conn { transition: opacity 0.2s; }
            `}</style>
          </defs>

          {/* Layer backgrounds */}
          {[
            { y: 8, h: 50, label: 'CLIENT', labelX: 38 },
            { y: 90, h: 65, label: 'EDGE / DISCOVERY', labelX: 38 },
            { y: 225, h: 80, label: 'BUSINESS SERVICES', labelX: 38 },
            { y: 365, h: 80, label: 'SUPPORT SERVICES', labelX: 38 },
            { y: 515, h: 70, label: 'INFRASTRUCTURE', labelX: 38 },
          ].map((layer, i) => (
            <g key={i}>
              <rect x={10} y={layer.y} width={svgW - 20} height={layer.h} rx={10}
                fill="rgba(255,255,255,0.015)" stroke="rgba(255,255,255,0.04)" strokeWidth={1} />
              <text x={layer.labelX} y={layer.y + 16} fill="rgba(255,255,255,0.18)"
                fontSize={9} fontWeight={700} letterSpacing={1.5} fontFamily="Inter, sans-serif">
                {layer.label}
              </text>
            </g>
          ))}

          {/* Connection lines */}
          {connections.map(([fromId, toId, type, label], i) => {
            const from = getCenter(fromId);
            const to = getCenter(toId);
            const style = connStyle[type];
            const active = isConnected(fromId) && isConnected(toId);
            const highlighted = hovered && (fromId === hovered || toId === hovered);

            // Curved path
            const dx = to.x - from.x;
            const dy = to.y - from.y;
            const mx = (from.x + to.x) / 2;
            const my = (from.y + to.y) / 2;
            // Add slight curve offset
            const cx = mx - dy * 0.08;
            const cy = my + dx * 0.08;
            const path = `M ${from.x} ${from.y} Q ${cx} ${cy} ${to.x} ${to.y}`;

            return (
              <g key={i} className="arch-conn" style={{ opacity: active ? 1 : 0.08 }}>
                <path d={path} fill="none"
                  stroke={highlighted ? style.stroke : style.stroke}
                  strokeWidth={highlighted ? 2 : 1}
                  strokeDasharray={style.dasharray}
                  className={type === 'mq' ? 'mq-line' : ''}
                  style={{ opacity: highlighted ? 1 : 0.5 }}
                  filter={highlighted ? 'url(#glow)' : ''}
                />
                {label && highlighted && (
                  <text x={cx} y={cy - 6} fill={style.stroke} fontSize={8} fontWeight={600}
                    textAnchor="middle" fontFamily="'JetBrains Mono', monospace">
                    {label}
                  </text>
                )}
              </g>
            );
          })}

          {/* Nodes */}
          {nodes.map((node) => {
            const svc = node.svcId ? services.find((s) => s.id === node.svcId) : null;
            const active = isConnected(node.id);
            const isHovered = hovered === node.id;
            const hw = node.w / 2;

            return (
              <g key={node.id}
                className="arch-node"
                style={{ opacity: active ? 1 : 0.15, cursor: svc ? 'pointer' : 'default' }}
                onMouseEnter={() => setHovered(node.id)}
                onMouseLeave={() => setHovered(null)}
                onClick={() => svc && onSelectService(svc)}
              >
                {/* Background rect */}
                <rect
                  x={node.x - hw} y={node.y - 2}
                  width={node.w} height={40} rx={10}
                  fill={isHovered ? node.color + '22' : 'rgba(17,24,39,0.85)'}
                  stroke={isHovered ? node.color : node.color + '44'}
                  strokeWidth={isHovered ? 2 : 1}
                  filter={isHovered ? 'url(#glow)' : ''}
                />
                {/* Icon */}
                <text x={node.x - hw + 14} y={node.y + 24} fontSize={16}
                  textAnchor="middle" dominantBaseline="middle">
                  {node.icon}
                </text>
                {/* Label */}
                <text x={node.x - hw + 28} y={node.y + 16} fill="#f1f5f9"
                  fontSize={11} fontWeight={600} fontFamily="Inter, sans-serif">
                  {node.label}
                </text>
                {/* Port */}
                <text x={node.x - hw + 28} y={node.y + 30} fill={node.color}
                  fontSize={9} fontWeight={500} fontFamily="'JetBrains Mono', monospace">
                  {node.sub}
                </text>
              </g>
            );
          })}
        </svg>
      </div>

      {/* Legend */}
      <div style={{
        display: 'flex', gap: 24, justifyContent: 'center', marginTop: '1rem',
        flexWrap: 'wrap', fontSize: '0.75rem', color: 'var(--text-muted)',
      }}>
        <LegendItem color="#22d3ee" dash="none" label="REST / HTTP" />
        <LegendItem color="#a78bfa" dash="6,4" label="RabbitMQ Event" />
        <LegendItem color="#fbbf24" dash="3,3" label="Data Store / Registry" />
        <LegendItem color="#34d399" dash="none" label="External API" />
        <span style={{ fontStyle: 'italic', color: 'var(--text-muted)' }}>
          Hover a node to see its connections
        </span>
      </div>
    </div>
  );
}

function LegendItem({ color, dash, label }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
      <svg width={24} height={4}>
        <line x1={0} y1={2} x2={24} y2={2}
          stroke={color} strokeWidth={2} strokeDasharray={dash} />
      </svg>
      <span>{label}</span>
    </div>
  );
}
