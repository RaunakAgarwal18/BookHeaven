import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import useAuthStore from '../store/useAuthStore';
import { 
  LayoutDashboard, 
  LineChart, 
  Flame, 
  Database, 
  Tag, 
  ShieldCheck, 
  ExternalLink,
  ArrowRight,
  Network,
  FileJson,
  RefreshCcw,
  Loader2,
  CheckCircle,
  XCircle
} from 'lucide-react';
import apiClient from '../api/axios';

const AdminDashboardPage = () => {
  const { user, isAuthenticated } = useAuthStore();
  const navigate = useNavigate();
  const [isSyncing, setIsSyncing] = useState(false);
  const [toast, setToast] = useState(null);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login', { replace: true });
      return;
    }
    if (user?.role !== 'ADMIN') {
      navigate('/');
    }
  }, [isAuthenticated, user, navigate]);

  if (!isAuthenticated || user?.role !== 'ADMIN') {
    return null; // Prevent flash of content during redirect
  }

  // Auto-dismiss toast
  useEffect(() => {
    if (toast) {
      const timer = setTimeout(() => setToast(null), 4000);
      return () => clearTimeout(timer);
    }
  }, [toast]);

  const showToast = (message, type = 'success') => {
    setToast({ message, type });
  };

  const handleSyncSearch = async () => {
    setIsSyncing(true);
    try {
      await apiClient.post('/book/admin/sync-search');
      showToast('Successfully triggered Elasticsearch sync for all books!');
    } catch (err) {
      console.error(err);
      showToast(err.response?.data?.message || 'Failed to sync to Elasticsearch', 'error');
    } finally {
      setIsSyncing(false);
    }
  };

  const adminTools = [
    {
      title: "Grafana Monitor",
      subtitle: "JVM & System Health",
      description: "Analyze live charts for JVM CPU usage, heap/non-heap memory, garbage collection, HTTP requests rate, and logback exceptions.",
      url: "http://localhost:3000",
      external: true,
      badge: "Real-time Metrics",
      badgeColor: "rgba(59, 130, 246, 0.15)",
      badgeTextColor: "#3b82f6",
      icon: <LineChart size={32} style={{ color: '#3b82f6' }} />
    },
    {
      title: "Prometheus",
      subtitle: "Telemetry Data Scraper",
      description: "Monitor active scrape targets across all 8 backend microservices, query raw time-series metrics, and inspect system scrape status.",
      url: "http://localhost:9090",
      external: true,
      badge: "Raw Database",
      badgeColor: "rgba(249, 115, 22, 0.15)",
      badgeTextColor: "#f97316",
      icon: <Flame size={32} style={{ color: '#f97316' }} />
    },
    {
      title: "Metabase BI",
      subtitle: "Business Intelligence",
      description: "Visualize business sales volumes, check daily transactions history, evaluate active customer carts, and analyze registration growth.",
      url: "http://localhost:3002",
      external: true,
      badge: "Sales Analytics",
      badgeColor: "rgba(16, 185, 129, 0.15)",
      badgeTextColor: "#10b981",
      icon: <Database size={32} style={{ color: '#10b981' }} />
    },
    {
      title: "Architecture Explorer",
      subtitle: "System Documentation",
      description: "Interactively explore the full microservices architecture, API endpoints, data flows, RabbitMQ messaging topology, and database schemas.",
      url: "http://localhost:3001",
      external: true,
      badge: "System Design",
      badgeColor: "rgba(129, 140, 248, 0.15)",
      badgeTextColor: "#818cf8",
      icon: <Network size={32} style={{ color: '#818cf8' }} />
    },
    {
      title: "Promo Coupons",
      subtitle: "Promotion Campaigns",
      description: "Configure discount codes, manage usage limits, set flat or percentage reductions, and configure active campaign timelines.",
      url: "/admin/coupons",
      external: false,
      badge: "Store Operations",
      badgeColor: "rgba(168, 85, 247, 0.15)",
      badgeTextColor: "#a855f7",
      icon: <Tag size={32} style={{ color: '#a855f7' }} />
    },
    {
      title: "Swagger UI",
      subtitle: "API Documentation",
      description: "Interactive OpenAPI documentation. Explore, test, and validate API endpoints across the microservices.",
      url: "http://localhost:8090/webjars/swagger-ui/index.html",
      external: true,
      badge: "API Contracts",
      badgeColor: "rgba(236, 72, 153, 0.15)",
      badgeTextColor: "#ec4899",
      icon: <FileJson size={32} style={{ color: '#ec4899' }} />
    },
    {
      title: "Elasticsearch Sync",
      subtitle: "Data Operations",
      description: "Force sync all existing database books into the Elasticsearch index. Use this if the search engine falls out of sync or after a fresh startup.",
      isAction: true,
      actionFn: handleSyncSearch,
      badge: "Search Index",
      badgeColor: "rgba(234, 179, 8, 0.15)",
      badgeTextColor: "#eab308",
      icon: <RefreshCcw size={32} style={{ color: '#eab308' }} />
    }
  ];

  const handleLaunch = (tool) => {
    if (tool.isAction) {
      tool.actionFn();
      return;
    }
    if (tool.external) {
      window.open(tool.url, '_blank', 'noopener,noreferrer');
    } else {
      navigate(tool.url);
    }
  };

  return (
    <div style={{ width: '100%', maxWidth: '1500px', margin: '2rem auto', padding: '0 2rem 4rem 2rem' }}>
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

      {/* Dashboard Title Header */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        gap: '1rem',
        marginBottom: '2rem',
        borderBottom: '1px solid var(--border-color)',
        paddingBottom: '1.5rem'
      }}>
        <div style={{
          backgroundColor: 'rgba(59, 130, 246, 0.1)',
          padding: '0.75rem',
          borderRadius: '12px',
          border: '1px solid rgba(59, 130, 246, 0.2)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center'
        }}>
          <LayoutDashboard size={32} style={{ color: 'var(--primary-color)' }} />
        </div>
        <div>
          <h1 style={{ 
            fontSize: '2.2rem', 
            margin: '0',
            fontWeight: '800',
            background: 'linear-gradient(90deg, #60a5fa, #a78bfa)', 
            WebkitBackgroundClip: 'text', 
            WebkitTextFillColor: 'transparent'
          }}>
            Admin Portal
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem', margin: '0.25rem 0 0 0', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <ShieldCheck size={16} style={{ color: '#10b981' }} /> Restricted to System Administrators
          </p>
        </div>
      </div>

      {/* Grid of Action Cards */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
        gap: '2rem'
      }}>
        {adminTools.map((tool, index) => (
          <div 
            key={index} 
            onClick={() => handleLaunch(tool)}
            className="card glass-panel"
            style={{
              padding: '2rem',
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'space-between',
              cursor: 'pointer',
              minHeight: '340px',
              height: 'auto',
              transition: 'transform 0.25s ease, border-color 0.25s ease, box-shadow 0.25s ease',
              border: '1px solid rgba(255, 255, 255, 0.08)'
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.transform = 'translateY(-6px)';
              e.currentTarget.style.borderColor = 'var(--primary-color)';
              e.currentTarget.style.boxShadow = '0 12px 24px -10px rgba(59, 130, 246, 0.3)';
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.transform = 'translateY(0)';
              e.currentTarget.style.borderColor = 'rgba(255, 255, 255, 0.08)';
              e.currentTarget.style.boxShadow = 'none';
            }}
          >
            <div>
              {/* Header Info */}
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1.25rem' }}>
                <div style={{
                  backgroundColor: 'rgba(255, 255, 255, 0.03)',
                  padding: '0.6rem',
                  borderRadius: '10px',
                  border: '1px solid rgba(255, 255, 255, 0.05)',
                  display: 'inline-flex'
                }}>
                  {tool.icon}
                </div>
                <span style={{
                  fontSize: '0.75rem',
                  fontWeight: '700',
                  padding: '0.25rem 0.6rem',
                  borderRadius: '20px',
                  backgroundColor: tool.badgeColor,
                  color: tool.badgeTextColor
                }}>
                  {tool.badge}
                </span>
              </div>

              {/* Title & Subtitle */}
              <h2 style={{ fontSize: '1.4rem', marginBottom: '0.25rem', fontWeight: '700' }}>
                {tool.title}
              </h2>
              <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: '600', marginBottom: '1rem' }}>
                {tool.subtitle}
              </div>

              {/* Description */}
              <p style={{ 
                fontSize: '0.9rem', 
                color: 'var(--text-muted)', 
                lineHeight: '1.5',
                margin: 0
              }}>
                {tool.description}
              </p>
            </div>

            {/* Launch Button / Footer */}
            <div style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              marginTop: '1.5rem',
              paddingTop: '1rem',
              borderTop: '1px solid rgba(255, 255, 255, 0.05)',
              color: 'var(--text-color)',
              fontWeight: '600',
              fontSize: '0.9rem'
            }}>
              <span style={{ display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                {tool.isAction ? (
                  isSyncing && tool.title === 'Elasticsearch Sync' ? 'Syncing...' : 'Run Operation'
                ) : (
                  tool.external ? "Launch Platform" : "Open Manager"
                )}
                {tool.external && <ExternalLink size={14} style={{ opacity: 0.8 }} />}
              </span>
              <div style={{
                width: '32px',
                height: '32px',
                borderRadius: '50%',
                backgroundColor: 'rgba(255, 255, 255, 0.03)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                transition: 'background-color 0.2s'
              }}
              onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'var(--primary-color)'}
              onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'rgba(255, 255, 255, 0.03)'}
              >
                {tool.isAction && isSyncing && tool.title === 'Elasticsearch Sync' ? (
                  <Loader2 size={16} className="animate-spin" />
                ) : (
                  <ArrowRight size={16} />
                )}
              </div>
            </div>
          </div>
        ))}
      </div>
      <style>{`
        @keyframes slideInRight {
          from { transform: translateX(100%); opacity: 0; }
          to { transform: translateX(0); opacity: 1; }
        }
      `}</style>
    </div>
  );
};

export default AdminDashboardPage;
