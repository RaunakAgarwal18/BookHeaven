export default function Header({ tabs, activeTab, onTabChange, mobileNavOpen, onToggleMobileNav }) {
  return (
    <header className="app-header">
      <div className="header-inner">
        <div className="header-logo" onClick={() => onTabChange('overview')} style={{ cursor: 'pointer' }}>
          <div className="header-logo-icon">B</div>
          <span className="header-logo-text">BookHeaven</span>
          <span className="header-logo-badge">Architecture</span>
        </div>

        <button className="mobile-nav-toggle" onClick={onToggleMobileNav}>
          {mobileNavOpen ? '✕' : '☰'}
        </button>

        <ul className={`nav-tabs${mobileNavOpen ? ' mobile-open' : ''}`}>
          {tabs.map((tab) => (
            <li
              key={tab.id}
              className={`nav-tab${activeTab === tab.id ? ' active' : ''}`}
              onClick={() => onTabChange(tab.id)}
            >
              {tab.label}
            </li>
          ))}
        </ul>
      </div>
    </header>
  );
}
