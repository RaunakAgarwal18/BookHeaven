import React from 'react';

export default function DesignDecisionsPage({ decisions }) {
  return (
    <div style={{ maxWidth: '900px', margin: '0 auto', padding: '2rem 1rem' }}>
      <div style={{ textAlign: 'center', marginBottom: '3rem' }}>
        <h1 className="page-title" style={{ fontSize: '2.5rem', marginBottom: '1rem' }}>
          🤔 Design Decisions
        </h1>
        <p className="page-subtitle" style={{ fontSize: '1.1rem', color: 'var(--text-secondary)' }}>
          An ongoing log of architectural questions and technical decisions made during the development of BookHeaven.
        </p>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '3rem' }}>
        {decisions && decisions.length > 0 ? (
          decisions.map((section, sectionIdx) => (
            <div key={sectionIdx}>
              <h2 style={{ fontSize: '1.8rem', color: 'var(--text-primary)', marginBottom: '1.5rem', borderBottom: '2px solid var(--border-glass)', paddingBottom: '0.5rem' }}>
                {section.service}
              </h2>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
                {section.decisions.map((decision, index) => (
                  <div key={index} className="glass-card" style={{ padding: '2rem' }}>
                    <h3 style={{ fontSize: '1.3rem', color: 'var(--text-primary)', marginBottom: '1rem', display: 'flex', gap: '0.5rem', alignItems: 'flex-start' }}>
                      <span style={{ color: 'var(--primary-color)' }}>Q:</span> 
                      {decision.question}
                    </h3>
                    <div 
                      style={{ 
                        color: 'var(--text-secondary)', 
                        lineHeight: '1.7',
                        borderLeft: '3px solid var(--border-glass-hover)',
                        paddingLeft: '1.5rem',
                        marginTop: '1.5rem'
                      }}
                      dangerouslySetInnerHTML={{ __html: decision.answer.replace(/\n/g, '<br/>') }}
                    />
                  </div>
                ))}
              </div>
            </div>
          ))
        ) : (
          <div className="glass-card" style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
            No design decisions recorded yet.
          </div>
        )}
      </div>
    </div>
  );
}
