import { EventBus } from '../eventBus';

describe('EventBus', () => {
  let eventBus: EventBus;

  beforeEach(() => {
    eventBus = new EventBus();
  });

  afterEach(() => {
    eventBus.clearAllListeners();
    eventBus.clearHistory();
  });

  describe('subscribe and emit', () => {
    it('should subscribe to events and receive them', () => {
      const callback = jest.fn();
      const testData = { message: 'test' };

      eventBus.subscribe('test-event', callback);
      eventBus.emit('test-event', testData);

      expect(callback).toHaveBeenCalledTimes(1);
      expect(callback).toHaveBeenCalledWith(
        expect.objectContaining({
          type: 'test-event',
          data: testData,
          timestamp: expect.any(String),
        })
      );
    });

    it('should support multiple subscribers for the same event', () => {
      const callback1 = jest.fn();
      const callback2 = jest.fn();
      const testData = { message: 'test' };

      eventBus.subscribe('test-event', callback1);
      eventBus.subscribe('test-event', callback2);
      eventBus.emit('test-event', testData);

      expect(callback1).toHaveBeenCalledTimes(1);
      expect(callback2).toHaveBeenCalledTimes(1);
    });

    it('should support wildcard listeners', () => {
      const wildcardCallback = jest.fn();
      const specificCallback = jest.fn();
      const testData = { message: 'test' };

      eventBus.subscribe('*', wildcardCallback);
      eventBus.subscribe('test-event', specificCallback);
      eventBus.emit('test-event', testData);

      expect(wildcardCallback).toHaveBeenCalledTimes(1);
      expect(specificCallback).toHaveBeenCalledTimes(1);
    });
  });

  describe('unsubscribe', () => {
    it('should unsubscribe from events', () => {
      const callback = jest.fn();
      const unsubscribe = eventBus.subscribe('test-event', callback);

      eventBus.emit('test-event', { message: 'test1' });
      expect(callback).toHaveBeenCalledTimes(1);

      unsubscribe();
      eventBus.emit('test-event', { message: 'test2' });
      expect(callback).toHaveBeenCalledTimes(1); // Should not be called again
    });
  });

  describe('subscribeToMultiple', () => {
    it('should subscribe to multiple event types', () => {
      const callback = jest.fn();
      const unsubscribe = eventBus.subscribeToMultiple(['event1', 'event2'], callback);

      eventBus.emit('event1', { data: 'test1' });
      eventBus.emit('event2', { data: 'test2' });
      eventBus.emit('event3', { data: 'test3' });

      expect(callback).toHaveBeenCalledTimes(2);
      expect(callback).toHaveBeenNthCalledWith(1, expect.objectContaining({ type: 'event1' }));
      expect(callback).toHaveBeenNthCalledWith(2, expect.objectContaining({ type: 'event2' }));

      unsubscribe();
      eventBus.emit('event1', { data: 'test4' });
      expect(callback).toHaveBeenCalledTimes(2); // Should not be called again
    });
  });

  describe('event history', () => {
    it('should maintain event history', () => {
      eventBus.emit('event1', { data: 'test1' });
      eventBus.emit('event2', { data: 'test2' });

      const history = eventBus.getEventHistory();
      expect(history).toHaveLength(2);
      expect(history[0].type).toBe('event1');
      expect(history[1].type).toBe('event2');
    });

    it('should get recent events of specific type', () => {
      eventBus.emit('event1', { data: 'test1' });
      eventBus.emit('event2', { data: 'test2' });
      eventBus.emit('event1', { data: 'test3' });

      const recentEvent1 = eventBus.getRecentEvents('event1', 5);
      expect(recentEvent1).toHaveLength(2);
      expect(recentEvent1[0].data.data).toBe('test1');
      expect(recentEvent1[1].data.data).toBe('test3');
    });

    it('should limit history size', () => {
      // Create an event bus with a small history limit for testing
      const smallEventBus = new EventBus();
      
      // Emit more events than the max history size
      for (let i = 0; i < 150; i++) {
        smallEventBus.emit('test-event', { index: i });
      }

      const history = smallEventBus.getEventHistory();
      expect(history.length).toBeLessThanOrEqual(100); // Default max size
    });
  });

  describe('waitForEvent', () => {
    it('should resolve when event is emitted', async () => {
      const testData = { message: 'test' };
      
      setTimeout(() => {
        eventBus.emit('test-event', testData);
      }, 100);

      const event = await eventBus.waitForEvent('test-event', 1000);
      expect(event.type).toBe('test-event');
      expect(event.data).toEqual(testData);
    });

    it('should reject on timeout', async () => {
      await expect(eventBus.waitForEvent('non-existent-event', 100))
        .rejects
        .toThrow('Timeout waiting for event: non-existent-event');
    });
  });

  describe('error handling', () => {
    it('should handle errors in event listeners gracefully', () => {
      const errorCallback = jest.fn(() => {
        throw new Error('Test error');
      });
      const normalCallback = jest.fn();
      
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();

      eventBus.subscribe('test-event', errorCallback);
      eventBus.subscribe('test-event', normalCallback);
      
      eventBus.emit('test-event', { data: 'test' });

      expect(errorCallback).toHaveBeenCalled();
      expect(normalCallback).toHaveBeenCalled();
      expect(consoleSpy).toHaveBeenCalled();

      consoleSpy.mockRestore();
    });
  });
});