import { renderHook, act } from '@testing-library/react';
import { useEventBus, useEventSubscription } from '../useEventBus';
import { eventBus } from '../../services/eventBus';

// Mock the event bus
jest.mock('../../services/eventBus', () => ({
  eventBus: {
    subscribe: jest.fn(),
    subscribeToMultiple: jest.fn(),
    emit: jest.fn(),
    waitForEvent: jest.fn(),
    getEventHistory: jest.fn(),
    getRecentEvents: jest.fn(),
  },
}));

const mockEventBus = eventBus as jest.Mocked<typeof eventBus>;

describe('useEventBus', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should provide event bus methods', () => {
    const { result } = renderHook(() => useEventBus());

    expect(result.current).toHaveProperty('subscribe');
    expect(result.current).toHaveProperty('subscribeToMultiple');
    expect(result.current).toHaveProperty('emit');
    expect(result.current).toHaveProperty('waitForEvent');
    expect(result.current).toHaveProperty('getEventHistory');
    expect(result.current).toHaveProperty('getRecentEvents');
  });

  it('should call eventBus.subscribe when subscribe is called', () => {
    const mockUnsubscribe = jest.fn();
    mockEventBus.subscribe.mockReturnValue(mockUnsubscribe);

    const { result } = renderHook(() => useEventBus());
    const callback = jest.fn();

    act(() => {
      result.current.subscribe('test-event', callback);
    });

    expect(mockEventBus.subscribe).toHaveBeenCalledWith('test-event', callback);
  });

  it('should call eventBus.emit when emit is called', () => {
    const { result } = renderHook(() => useEventBus());

    act(() => {
      result.current.emit('test-event', { data: 'test' }, 'test-source');
    });

    expect(mockEventBus.emit).toHaveBeenCalledWith('test-event', { data: 'test' }, 'test-source');
  });

  it('should clean up subscriptions on unmount', () => {
    const mockUnsubscribe = jest.fn();
    mockEventBus.subscribe.mockReturnValue(mockUnsubscribe);

    const { result, unmount } = renderHook(() => useEventBus());
    const callback = jest.fn();

    act(() => {
      result.current.subscribe('test-event', callback);
    });

    unmount();

    expect(mockUnsubscribe).toHaveBeenCalled();
  });
});

describe('useEventSubscription', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should subscribe to single event type', () => {
    const mockUnsubscribe = jest.fn();
    mockEventBus.subscribe.mockReturnValue(mockUnsubscribe);

    const callback = jest.fn();
    renderHook(() => useEventSubscription('test-event', callback));

    expect(mockEventBus.subscribe).toHaveBeenCalledWith('test-event', callback);
  });

  it('should subscribe to multiple event types', () => {
    const mockUnsubscribe = jest.fn();
    mockEventBus.subscribeToMultiple.mockReturnValue(mockUnsubscribe);

    const callback = jest.fn();
    const eventTypes = ['event1', 'event2'];
    
    renderHook(() => useEventSubscription(eventTypes, callback));

    expect(mockEventBus.subscribeToMultiple).toHaveBeenCalledWith(eventTypes, callback);
  });

  it('should clean up subscription on unmount', () => {
    const mockUnsubscribe = jest.fn();
    mockEventBus.subscribe.mockReturnValue(mockUnsubscribe);

    const callback = jest.fn();
    const { unmount } = renderHook(() => useEventSubscription('test-event', callback));

    unmount();

    expect(mockUnsubscribe).toHaveBeenCalled();
  });
});