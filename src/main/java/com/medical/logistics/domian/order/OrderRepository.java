package com.medical.logistics.domian.order;

import java.util.List;
import java.util.Optional;

/**
 * Order Repository Interface
 *
 * Design Consideration: Interface allows for multiple implementations
 * - In-memory for testing
 * - Async/Reactive for I/O-heavy production use
 * - Cached for performance optimization
 */
public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(OrderId id);
    List<Order> findAll();
}
