import { useState, useRef, useCallback } from 'react';
import { CustomFieldDefinitionsTab } from '../../components/admin/CustomFieldDefinitionsTab';
import { CustomFieldTemplatesTab } from '../../components/admin/CustomFieldTemplatesTab';

type Tab = 'definitions' | 'templates';

const TABS: { id: Tab; label: string; testId: string }[] = [
  { id: 'definitions', label: 'Field Definitions', testId: 'tab-definitions' },
  { id: 'templates', label: 'Field Templates', testId: 'tab-templates' },
];

const PANEL_ID = 'custom-field-tabpanel';

export function CustomFieldBuilder() {
  const [activeTab, setActiveTab] = useState<Tab>('definitions');
  const tabRefs = useRef<(HTMLButtonElement | null)[]>([]);

  const handleKeyDown = useCallback((e: React.KeyboardEvent<HTMLButtonElement>, index: number) => {
    let nextIndex: number | null = null;
    if (e.key === 'ArrowRight') {
      nextIndex = (index + 1) % TABS.length;
    } else if (e.key === 'ArrowLeft') {
      nextIndex = (index - 1 + TABS.length) % TABS.length;
    } else if (e.key === 'Home') {
      nextIndex = 0;
    } else if (e.key === 'End') {
      nextIndex = TABS.length - 1;
    }
    if (nextIndex !== null) {
      e.preventDefault();
      setActiveTab(TABS[nextIndex].id);
      tabRefs.current[nextIndex]?.focus();
    }
  }, []);

  const activeTabIndex = TABS.findIndex(t => t.id === activeTab);

  return (
    <div className="custom-field-builder" data-testid="custom-field-builder">
      <h1>Custom Field Builder</h1>

      <nav className="tab-nav" aria-label="Custom field sections" role="tablist">
        {TABS.map((tab, index) => (
          <button
            key={tab.id}
            ref={el => { tabRefs.current[index] = el; }}
            type="button"
            id={`tab-${tab.id}`}
            role="tab"
            aria-selected={activeTab === tab.id}
            aria-controls={PANEL_ID}
            tabIndex={activeTab === tab.id ? 0 : -1}
            className={`tab-btn${activeTab === tab.id ? ' tab-btn--active' : ''}`}
            data-testid={tab.testId}
            onClick={() => setActiveTab(tab.id)}
            onKeyDown={e => handleKeyDown(e, index)}
          >
            {tab.label}
          </button>
        ))}
      </nav>

      <div
        id={PANEL_ID}
        className="tab-content"
        role="tabpanel"
        aria-labelledby={`tab-${TABS[activeTabIndex].id}`}
        tabIndex={0}
      >
        {activeTab === 'definitions' && <CustomFieldDefinitionsTab />}
        {activeTab === 'templates' && <CustomFieldTemplatesTab />}
      </div>
    </div>
  );
}
