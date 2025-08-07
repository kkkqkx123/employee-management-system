import React, { useEffect, useCallback, useRef, useState } from 'react';
import { eventBus, EventBusEvent } from '../services/eventBus';

type EventCallback = (event: EventBusEvent) => void;

/**
 * Hook for subscribing to event bus events
 */
export const useEventBus = () => {
  const subscriptionsRef = useRef<(() => void)[]>([]);

  // Subscribe to a single event type
  const subscribe = useCallback((eventType: string, callback: EventCallback) => {
    const unsubscribe = eventBus.subscribe(eventType, callback);
    subscriptionsRef.current.push(unsubscribe);
    return unsubscribe;
  }, []);

  // Subscribe to multiple event types
  const subscribeToMultiple = useCallback((eventTypes: string[], callback: EventCallback) => {
    const unsubscribe = eventBus.subscribeToMultiple(eventTypes, callback);
    subscriptionsRef.current.push(unsubscribe);
    return unsubscribe;
  }, []);

  // Emit an event
  const emit = useCallback((eventType: string, data: any, source?: string) => {
    eventBus.emit(eventType, data, source);
  }, []);

  // Wait for a specific event
  const waitForEvent = useCallback((eventType: string, timeout?: number) => {
    return eventBus.waitForEvent(eventType, timeout);
  }, []);

  // Clean up subscriptions on unmount
  useEffect(() => {
    return () => {
      subscriptionsRef.current.forEach(unsubscribe => unsubscribe());
      subscriptionsRef.current = [];
    };
  }, []);

  return {
    subscribe,
    subscribeToMultiple,
    emit,
    waitForEvent,
    getEventHistory: eventBus.getEventHistory.bind(eventBus),
    getRecentEvents: eventBus.getRecentEvents.bind(eventBus),
  };
};

/**
 * Hook for subscribing to specific event types with automatic cleanup
 */
export const useEventSubscription = (
  eventType: string | string[],
  callback: EventCallback,
  deps: React.DependencyList = []
) => {
  const { subscribe, subscribeToMultiple } = useEventBus();

  useEffect(() => {
    if (Array.isArray(eventType)) {
      return subscribeToMultiple(eventType, callback);
    } else {
      return subscribe(eventType, callback);
    }
  }, [eventType, callback, subscribe, subscribeToMultiple, ...deps]);
};

/**
 * Hook for WebSocket connection status
 */
export const useWebSocketStatus = () => {
  const [status, setStatus] = useState({
    connected: false,
    connecting: false,
    error: null as string | null,
    reconnectAttempts: 0,
  });

  useEventSubscription([
    'connection:status',
    'connection:error',
    'connection:failed'
  ], (event) => {
    switch (event.type) {
      case 'connection:status':
        setStatus(prev => ({
          ...prev,
          connected: event.data.connected,
          connecting: false,
          error: null,
        }));
        break;
      case 'connection:error':
        setStatus(prev => ({
          ...prev,
          connected: false,
          connecting: false,
          error: event.data.error,
        }));
        break;
      case 'connection:failed':
        setStatus(prev => ({
          ...prev,
          connected: false,
          connecting: false,
          error: event.data.message,
        }));
        break;
    }
  });

  return status;
};