import React, { useState } from 'react';
import { Button } from '../ui/Button/Button';
import { FormField } from '../ui/FormField/FormField';
import { Input } from '../ui/Input/Input';
import { Modal } from '../ui/Modal/Modal';
import { SkipLinks } from '../ui/SkipLinks/SkipLinks';
import { DataTable } from '../ui/DataTable/DataTable';
import { FocusTrap } from './FocusTrap';
import { KeyboardNavigation } from './KeyboardNavigation';
import { useAccessibility } from './AccessibilityProvider';
import styles from './AccessibilityDemo.module.css';

export const AccessibilityDemo: React.FC = () => {
  const [modalOpen, setModalOpen] = useState(false);
  const [focusTrapActive, setFocusTrapActive] = useState(false);
  const [formData, setFormData] = useState({ name: '', email: '' });
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});
  
  const { announceToScreenReader, prefersReducedMotion, prefersHighContrast } = useAccessibility();

  const sampleData = [
    { id: 1, name: 'John Doe', email: 'john@example.com', role: 'Developer' },
    { id: 2, name: 'Jane Smith', email: 'jane@example.com', role: 'Designer' },
    { id: 3, name: 'Bob Johnson', email: 'bob@example.com', role: 'Manager' },
  ];

  const tableColumns = [
    { key: 'name', title: 'Name' },
    { key: 'email', title: 'Email' },
    { key: 'role', title: 'Role' },
  ];

  const navigationItems = ['Dashboard', 'Users', 'Settings', 'Help'];

  const handleFormSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const errors: Record<string, string> = {};
    
    if (!formData.name.trim()) {
      errors.name = 'Name is required';
    }
    
    if (!formData.email.trim()) {
      errors.email = 'Email is required';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      errors.email = 'Email format is invalid';
    }
    
    setFormErrors(errors);
    
    if (Object.keys(errors).length === 0) {
      announceToScreenReader('Form submitted successfully!', 'polite');
    } else {
      announceToScreenReader('Form has validation errors. Please check the fields.', 'assertive');
    }
  };

  const handleNavSelect = (item: string, index: number) => {
    announceToScreenReader(`Selected ${item}`, 'polite');
  };

  return (
    <div className={styles.demo}>
      <SkipLinks />
      
      <header role="banner" className={styles.header}>
        <h1>Accessibility Features Demo</h1>
        <div className={styles.preferences}>
          <p>User Preferences:</p>
          <ul>
            <li>Reduced Motion: {prefersReducedMotion ? 'Yes' : 'No'}</li>
            <li>High Contrast: {prefersHighContrast ? 'Yes' : 'No'}</li>
          </ul>
        </div>
      </header>

      <nav role="navigation" aria-label="Main navigation" className={styles.nav}>
        <h2>Keyboard Navigation Demo</h2>
        <KeyboardNavigation
          items={navigationItems}
          onSelect={handleNavSelect}
          ariaLabel="Main navigation menu"
          className={styles.navList}
        >
          {navigationItems.map((item, index) => (
            <div
              key={index}
              className={styles.navItem}
              tabIndex={index === 0 ? 0 : -1}
              role="menuitem"
            >
              {item}
            </div>
          ))}
        </KeyboardNavigation>
        <p className={styles.instructions}>
          Use arrow keys to navigate, Enter to select
        </p>
      </nav>

      <main role="main" id="main-content" className={styles.main}>
        <section className={styles.section}>
          <h2>Button Accessibility</h2>
          <div className={styles.buttonGroup}>
            <Button variant="primary" size="md">
              Primary Button
            </Button>
            <Button variant="secondary" size="md" disabled>
              Disabled Button
            </Button>
            <Button 
              variant="outline" 
              size="md"
              loading
              ariaLabel="Loading button example"
            >
              Loading Button
            </Button>
            <Button
              variant="danger"
              size="sm"
              ariaLabel="Delete item"
              onClick={() => announceToScreenReader('Item deleted', 'assertive')}
            >
              Delete
            </Button>
          </div>
        </section>

        <section className={styles.section}>
          <h2>Form Accessibility</h2>
          <form onSubmit={handleFormSubmit} className={styles.form}>
            <FormField 
              label="Full Name" 
              required 
              error={formErrors.name}
              helperText="Enter your first and last name"
            >
              <Input
                type="text"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder="John Doe"
              />
            </FormField>

            <FormField 
              label="Email Address" 
              required 
              error={formErrors.email}
              helperText="We'll never share your email"
            >
              <Input
                type="email"
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                placeholder="john@example.com"
              />
            </FormField>

            <div className={styles.formActions}>
              <Button type="submit" variant="primary">
                Submit Form
              </Button>
              <Button 
                type="button" 
                variant="secondary"
                onClick={() => {
                  setFormData({ name: '', email: '' });
                  setFormErrors({});
                  announceToScreenReader('Form cleared', 'polite');
                }}
              >
                Clear Form
              </Button>
            </div>
          </form>
        </section>

        <section className={styles.section}>
          <h2>Data Table Accessibility</h2>
          <DataTable
            data={sampleData}
            columns={tableColumns}
            caption="Employee directory with 3 employees"
            ariaLabel="Employee information table"
            className={styles.table}
          />
        </section>

        <section className={styles.section}>
          <h2>Modal and Focus Management</h2>
          <div className={styles.modalDemo}>
            <Button onClick={() => setModalOpen(true)}>
              Open Modal
            </Button>
            
            <Modal
              isOpen={modalOpen}
              onClose={() => setModalOpen(false)}
              title="Accessible Modal"
              ariaDescribedBy="modal-description"
            >
              <p id="modal-description">
                This modal demonstrates proper focus management and keyboard navigation.
                Focus is trapped within the modal, and you can close it with the Escape key.
              </p>
              <div className={styles.modalActions}>
                <Button variant="primary" onClick={() => setModalOpen(false)}>
                  Confirm
                </Button>
                <Button variant="secondary" onClick={() => setModalOpen(false)}>
                  Cancel
                </Button>
              </div>
            </Modal>
          </div>
        </section>

        <section className={styles.section}>
          <h2>Focus Trap Demo</h2>
          <div className={styles.focusTrapDemo}>
            <Button onClick={() => setFocusTrapActive(!focusTrapActive)}>
              {focusTrapActive ? 'Deactivate' : 'Activate'} Focus Trap
            </Button>
            
            <FocusTrap active={focusTrapActive} className={styles.focusTrapArea}>
              <div className={styles.focusTrapContent}>
                <h3>Focus Trapped Area</h3>
                <p>When active, focus is trapped within this area.</p>
                <Button>First Button</Button>
                <Button>Second Button</Button>
                <Input placeholder="Trapped input" />
              </div>
            </FocusTrap>
          </div>
        </section>

        <section className={styles.section}>
          <h2>Live Region Announcements</h2>
          <div className={styles.announcements}>
            <Button 
              onClick={() => announceToScreenReader('This is a polite announcement', 'polite')}
            >
              Polite Announcement
            </Button>
            <Button 
              onClick={() => announceToScreenReader('This is an urgent announcement!', 'assertive')}
            >
              Assertive Announcement
            </Button>
          </div>
        </section>
      </main>

      <aside role="complementary" className={styles.sidebar}>
        <h2>Accessibility Tips</h2>
        <ul>
          <li>Use Tab to navigate between interactive elements</li>
          <li>Use Enter or Space to activate buttons</li>
          <li>Use Arrow keys for list navigation</li>
          <li>Use Escape to close modals and dropdowns</li>
          <li>Screen readers will announce form errors and status updates</li>
        </ul>
      </aside>

      <footer role="contentinfo" className={styles.footer}>
        <p>This demo showcases WCAG 2.1 AA compliant accessibility features.</p>
      </footer>
    </div>
  );
};