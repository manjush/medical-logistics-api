package com.medical.logistics.application.order.commands;

import com.medical.logistics.domian.order.OrderId;

import java.util.Objects;

/**
 * Command for approving an order
 */
public class ApproveOrderCommand {
    private final OrderId orderId;

    public ApproveOrderCommand(OrderId orderId) {
        this.orderId = Objects.requireNonNull(orderId, "OrderId cannot be null");
    }

    public OrderId getOrderId() {
        return orderId;
    }
}
