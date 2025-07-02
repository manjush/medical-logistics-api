package com.medical.logistics.application.order.commands;

import java.util.List;
import java.util.Objects;

/**
 * Command for placing a new order
 */
public class PlaceOrderCommand {
    private final List<OrderItemCommand> items;

    public PlaceOrderCommand(List<OrderItemCommand> items) {
        this.items = Objects.requireNonNull(items, "Items cannot be null");
    }

    public List<OrderItemCommand> getItems() {
        return items;
    }

    public record OrderItemCommand(String name, int quantity) {
        public OrderItemCommand {
            Objects.requireNonNull(name, "Item name cannot be null");
            if (quantity < 1) {
                throw new IllegalArgumentException("Quantity must be at least 1");
            }
        }
    }
}
