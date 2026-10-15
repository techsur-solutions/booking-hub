import { useState } from 'react';
import { CustomFieldDefinitionsTab } from '../../components/admin/CustomFieldDefinitionsTab';
import { CustomFieldTemplatesTab } from '../../components/admin/CustomFieldTemplatesTab';

type Tab = 'definitions' | 'templates';

export function CustomFieldBuilder() {
  const [activeTab, setActiveTab] = useState<Tab>('definitions');

  return (
    <div className="custom-field-builder" data-testid="custom-field-builder">
      <h1>Custom Field Builder</h1>

      <nav className="tab-nav" aria-label="Custom field sections" role="tablist">
        <button
          type="button"
          role="tab"
          aria-selected={activeTab === 'definitions'}
          className={`tab-btn${activeTab === 'definitions' ? ' tab-btn--active' : ''}`}
          data-testid="tab-definitions"
          onClick={() => setActiveTab('definitions')}
        >
          Field Definitions
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={activeTab === 'templates'}
          className={`tab-btn${activeTab === 'templates' ? ' tab-btn--active' : ''}`}
          data-testid="tab-templates"
          onClick={() => setActiveTab('templates')}
        >
          Field Templates
        </button>
      </nav>

      <div className="tab-content" role="tabpanel">
        {activeTab === 'definitions' && <CustomFieldDefinitionsTab />}
        {activeTab === 'templates' && <CustomFieldTemplatesTab />}
      </div>
    </div>
  );
}
