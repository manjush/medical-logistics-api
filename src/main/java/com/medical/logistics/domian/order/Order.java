package com.medical.logistics.domian.order;

import com.medical.logistics.domian.order.exceptions.InvalidOrderStateException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Order Aggregate Root
 */
@Slf4j
public class Order {
    @Getter
    private final OrderId id;
    private final List<OrderItem> items;
    @Getter
    private OrderStatus status;
    @Getter
    private final LocalDateTime createdAt;
    @Getter
    private LocalDateTime updatedAt;


    /**
     * Factory method for creating new orders
     */
    public static Order create(List<OrderItem> items) {
        log.debug("Creating new order with {} items", items != null ? items.size() : 0);
        validateItems(items);
        Order order = new Order(
                OrderId.generate(),
                new ArrayList<>(items),
                OrderStatus.PENDING,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        log.info("Created new order with ID: {} containing {} items", order.getId(), order.getItems().size());
        return order;
    }

    // Constructor used when loading from database
    public Order(OrderId id, List<OrderItem> items, OrderStatus status,
                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "Order ID cannot be null");
        this.items = new ArrayList<>(Objects.requireNonNull(items, "Items cannot be null"));
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Created date cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Updated date cannot be null");
        log.debug("Loaded order {} with status {}", id, status);
    }

    public void approve() {
        log.info("Attempting to approve order {}", id);
        OrderStatus previousStatus = this.status;

        if (!canTransitionTo(OrderStatus.APPROVED)) {
            log.error("Cannot approve order {} in {} status", id, status);
            throw new InvalidOrderStateException(String.format("Cannot approve order in %s status", status));
        }

        this.status = OrderStatus.APPROVED;
        this.updatedAt = LocalDateTime.now();
        log.info("Order {} approved successfully. Status changed from {} to {}", id, previousStatus, status);
    }

    public void cancel() {
        log.info("Attempting to cancel order {}", id);
        OrderStatus previousStatus = this.status;

        if (!canTransitionTo(OrderStatus.CANCELLED)) {
            log.error("Cannot cancel order {} in {} status", id, status);
            throw new InvalidOrderStateException(String.format("Cannot cancel order in %s status", status));
        }

        this.status = OrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
        log.info("Order {} cancelled successfully. Status changed from {} to {}", id, previousStatus, status);
        // emit OrderCancelledEvent
    }

    private boolean canTransitionTo(OrderStatus targetStatus) {
        boolean canTransition = status == OrderStatus.PENDING;
        log.debug("Can transition from {} to {}? {}", status, targetStatus, canTransition);
        return canTransition;
    }

    private static void validateItems(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must include at least 1 item");
        }
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
