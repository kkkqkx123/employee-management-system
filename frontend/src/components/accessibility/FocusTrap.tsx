import React, { useEffect, useRef } from 'react';
import { FocusTrap as FocusTrapUtil } from '../../utils/accessibility';

interface FocusTrapProps {
  children: React.ReactNode;
  active: boolean;
  restoreFocus?: boolean;
  className?: string;
}

export const FocusTrap: React.FC<FocusTrapProps> = ({
  children,
  active,
  restoreFocus = true,
  className,
}) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const focusTrapRef = useRef<FocusTrapUtil | null>(null);

  useEffect(() => {
    if (!containerRef.current) return;

    if (active) {
      focusTrapRef.current = new FocusTrapUtil(containerRef.current);
      focusTrapRef.current.activate();
    } else {
      if (focusTrapRef.current) {
        focusTrapRef.current.deactivate();
        focusTrapRef.current = null;
      }
    }

    return () => {
      if (focusTrapRef.current) {
        focusTrapRef.current.deactivate();
      }
    };
  }, [active]);

  return (
    <div ref={containerRef} className={className}>
      {children}
    </div>
  );
};