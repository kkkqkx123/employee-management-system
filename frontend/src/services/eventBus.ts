/**
 * Event Bus for managing real-time events across the application
 * Implements the Event Bus pattern for decoupled communication
 */

export interface EventBusEvent {
  type: string;
  data: any;
  timestamp: string;
  source?: string;
}

type EventCallback = (event: EventBusEvent) => void;

export class EventBus {
  private listeners: Map<string, EventCallback[]> = new Map();
  private eventHistory: EventBusEvent[] = [];
  private maxHistorySize = 100;

  /**
   * Subscribe to events of a specific type
   */
  subscribe(eventType: string, callback: EventCallback): () => void {
    if (!this.listeners.has(eventType)) {
      this.listeners.set(eventType, []);
    }

    const listeners = this.listeners.get(eventType)!;
    listeners.push(callback);

    // Return unsubscribe function
    return () => {
      const index = listeners.indexOf(callback);
      if (index > -1) {
        listeners.splice(index, 1);
      }
    };
  }

  /**
   * Subscribe to multiple event types with a single callback
   */
  subscribeToMultiple(eventTypes: string[], callback: EventCallback): () => void {
    const unsubscribeFunctions = eventTypes.map(eventType => 
      this.subscribe(eventType, callback)
    );

    return () => {
      unsubscribeFunctions.forEach(unsubscribe => unsubscribe());
    };
  }

  /**
   * Emit an event to all subscribers
   */
  emit(eventType: string, data: any, source?: string): void {
    const event: EventBusEvent = {
      type: eventType,
      data,
      timestamp: new Date().toISOString(),
      source,
    };

    // Add to history
    this.addToHistory(event);

    // Notify listeners
    const listeners = this.listeners.get(eventType) || [];
    listeners.forEach(callback => {
      try {
        callback(event);
      } catch (error) {
        console.error(`Error in event listener for ${eventType}:`, error);
      }
    });

    // Also emit to wildcard listeners (*)
    const wildcardListeners = this.listeners.get('*') || [];
    wildcardListeners.forEach(callback => {
      try {
        callback(event);
      } catch (error) {
        console.error(`Error in wildcard event listener:`, error);
      }
    });
  }

  /**
   * Get all listeners for a specific event type
   */
  getListeners(eventType: string): EventCallback[] {
    return this.listeners.get(eventType) || [];
  }

  /**
   * Get all registered event types
   */
  getEventTypes(): string[] {
    return Array.from(this.listeners.keys());
  }

  /**
   * Clear all listeners for a specific event type
   */
  clearListeners(eventType: string): void {
    this.listeners.delete(eventType);
  }

  /**
   * Clear all listeners
   */
  clearAllListeners(): void {
    this.listeners.clear();
  }

  /**
   * Get event history
   */
  getEventHistory(): EventBusEvent[] {
    return [...this.eventHistory];
  }

  /**
   * Get recent events of a specific type
   */
  getRecentEvents(eventType: string, limit: number = 10): EventBusEvent[] {
    return this.eventHistory
      .filter(event => event.type === eventType)
      .slice(-limit);
  }

  /**
   * Clear event history
   */
  clearHistory(): void {
    this.eventHistory = [];
  }

  /**
   * Add event to history with size limit
   */
  private addToHistory(event: EventBusEvent): void {
    this.eventHistory.push(event);
    
    if (this.eventHistory.length > this.maxHistorySize) {
      this.eventHistory = this.eventHistory.slice(-this.maxHistorySize);
    }
  }

  /**
   * Wait for a specific event (returns a Promise)
   */
  waitForEvent(eventType: string, timeout: number = 5000): Promise<EventBusEvent> {
    return new Promise((resolve, reject) => {
      const timeoutId = setTimeout(() => {
        unsubscribe();
        reject(new Error(`Timeout waiting for event: ${eventType}`));
      }, timeout);

      const unsubscribe = this.subscribe(eventType, (event) => {
        clearTimeout(timeoutId);
        unsubscribe();
        resolve(event);
      });
    });
  }

  /**
   * Emit event with delay
   */
  emitDelayed(eventType: string, data: any, delay: number, source?: string): void {
    setTimeout(() => {
      this.emit(eventType, data, source);
    }, delay);
  }
}

// Create singleton instance
export const eventBus = new EventBus();

// Export default for convenience
export default eventBus;