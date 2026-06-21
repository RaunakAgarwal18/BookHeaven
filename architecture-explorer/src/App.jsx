import { useState } from 'react';
import { SERVICES, EVENTS, EXCHANGES } from './data';
import Header from './components/Header';
import OverviewPage from './pages/OverviewPage';
import ServicesPage from './pages/ServicesPage';
import FlowsPage from './pages/FlowsPage';
import MessagingPage from './pages/MessagingPage';
import SecurityPage from './pages/SecurityPage';
import DataPage from './pages/DataPage';
import DesignDecisionsPage from './pages/DesignDecisionsPage';
import ServiceDetail from './components/ServiceDetail';
import { DESIGN_DECISIONS } from './data';

const TABS = [
  { id: 'overview', label: 'Overview' },
  { id: 'services', label: 'Services' },
  { id: 'flows', label: 'Data Flows' },
  { id: 'messaging', label: 'Messaging' },
  { id: 'security', label: 'Security' },
  { id: 'data', label: 'Database' },
  { id: 'decisions', label: 'Design Decisions' },
];

export default function App() {
  const [activeTab, setActiveTab] = useState('overview');
  const [selectedService, setSelectedService] = useState(null);
  const [mobileNavOpen, setMobileNavOpen] = useState(false);

  const handleTabChange = (tabId) => {
    setActiveTab(tabId);
    setMobileNavOpen(false);
  };

  const renderPage = () => {
    switch (activeTab) {
      case 'overview':
        return <OverviewPage services={SERVICES} onSelectService={setSelectedService} onNavigate={setActiveTab} />;
      case 'services':
        return <ServicesPage services={SERVICES} onSelectService={setSelectedService} />;
      case 'flows':
        return <FlowsPage />;
      case 'messaging':
        return <MessagingPage events={EVENTS} exchanges={EXCHANGES} />;
      case 'security':
        return <SecurityPage />;
      case 'data':
        return <DataPage services={SERVICES} />;
      case 'decisions':
        return <DesignDecisionsPage decisions={DESIGN_DECISIONS} />;
      default:
        return <OverviewPage services={SERVICES} onSelectService={setSelectedService} onNavigate={setActiveTab} />;
    }
  };

  return (
    <div className="app-container">
      <div className="app-bg-glow" />
      <Header
        tabs={TABS}
        activeTab={activeTab}
        onTabChange={handleTabChange}
        mobileNavOpen={mobileNavOpen}
        onToggleMobileNav={() => setMobileNavOpen(!mobileNavOpen)}
      />
      <main className="main-content">
        {renderPage()}
      </main>

      {selectedService && (
        <ServiceDetail
          service={selectedService}
          onClose={() => setSelectedService(null)}
        />
      )}
    </div>
  );
}
