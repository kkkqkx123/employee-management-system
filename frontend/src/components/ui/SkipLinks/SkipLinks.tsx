import React from 'react';
import styles from './SkipLinks.module.css';

interface SkipLink {
  href: string;
  label: string;
}

interface SkipLinksProps {
  links?: SkipLink[];
}

const defaultLinks: SkipLink[] = [
  { href: '#main-content', label: 'Skip to main content' },
  { href: '#navigation', label: 'Skip to navigation' },
  { href: '#search', label: 'Skip to search' },
];

export const SkipLinks: React.FC<SkipLinksProps> = ({ 
  links = defaultLinks 
}) => {
  return (
    <div className={styles.skipLinks} role="navigation" aria-label="Skip links">
      {links.map((link, index) => (
        <a
          key={index}
          href={link.href}
          className={styles.skipLink}
          onClick={(e) => {
            // Ensure the target element receives focus
            const target = document.querySelector(link.href);
            if (target) {
              e.preventDefault();
              (target as HTMLElement).focus();
              (target as HTMLElement).scrollIntoView({ behavior: 'smooth' });
            }
          }}
        >
          {link.label}
        </a>
      ))}
    </div>
  );
};