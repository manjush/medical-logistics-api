package com.medical.logistics.application.order.commands;

import com.medical.logistics.domian.order.OrderId;

import java.util.Objects;

/**
 * Command for cancelling an order
 */
public class CancelOrderCommand {
    private final OrderId orderId;

    public CancelOrderCommand(OrderId orderId) {
        this.orderId = Objects.requireNonNull(orderId, "OrderId cannot be null");
    }

    public OrderId getOrderId() {
        return orderId;
    }
}
