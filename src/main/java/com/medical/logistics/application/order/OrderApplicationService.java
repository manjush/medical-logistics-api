package com.medical.logistics.application.order;

import com.medical.logistics.application.order.commands.ApproveOrderCommand;
import com.medical.logistics.application.order.commands.CancelOrderCommand;
import com.medical.logistics.application.order.commands.PlaceOrderCommand;
import com.medical.logistics.domian.order.Order;
import com.medical.logistics.domian.order.OrderId;
import com.medical.logistics.domian.order.OrderItem;
import com.medical.logistics.domian.order.OrderRepository;
import com.medical.logistics.domian.order.exceptions.OrderNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Order Application Service
 * <p>
 * Design Consideration: Currently synchronous but designed for easy async migration.
 * In an I/O-heavy system, methods would return CompletableFuture or Mono for
 * non-blocking operations. The command pattern enables easy integration with
 * message queues for asynchronous processing.
 */
@Slf4j
@Service
public class OrderApplicationService {
    private final OrderRepository orderRepository;

    public OrderApplicationService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Handles order placement
     */
    public OrderId handle(PlaceOrderCommand command) {
        log.info("Processing PlaceOrderCommand with {} items", command.getItems().size());

        try {
            List<OrderItem> items = command.getItems().stream()
                    .map(item -> {
                        log.debug("Creating OrderItem: {} x{}", item.name(), item.quantity());
                        return new OrderItem(item.name(), item.quantity());
                    })
                    .collect(Collectors.toList());

            Order order = Order.create(items);
            Order savedOrder = orderRepository.save(order);
            log.info("Successfully placed order {} ", savedOrder.getId());

            // publish OrderPlacedEvent
            return order.getId();
        } catch (Exception e) {
            log.error("Failed to place order: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Handles order approval
     * <p>
     * I/O Consideration: External service calls (inventory, payment) would be handled asynchronously with circuit breakers
     *
     */
    public void handle(ApproveOrderCommand command) {
        log.info("Processing ApproveOrderCommand for order {}", command.getOrderId());
        try {
        Order order = findOrder(command.getOrderId());
        order.approve();
        orderRepository.save(order);
        log.info("Successfully approved order {}", command.getOrderId());
        // publish OrderApprovedEvent
        } catch (Exception e) {
            log.error("Failed to approve order {}: {}", command.getOrderId(), e.getMessage(), e);
            throw e;
        }
    }

    public void handle(CancelOrderCommand command) {
        log.info("Processing CancelOrderCommand for order {}", command.getOrderId());
        try {
            Order order = findOrder(command.getOrderId());
            order.cancel();
            orderRepository.save(order);
            log.info("Successfully cancelled order {}", command.getOrderId());
            // publish OrderCancelledEvent
        } catch (Exception e) {
            log.error("Failed to cancel order {}: {}", command.getOrderId(), e.getMessage(), e);
            throw e;
        }
    }

    public List<Order> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        log.info("Retrieved {} orders", orders.size());
        return orders;
    }

    public Order getOrder(OrderId orderId) {
        Order order = findOrder(orderId);
        log.info("Retrieved order {} with status {}", orderId, order.getStatus());
        return order;
    }

    private Order findOrder(OrderId orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order not found: {}", orderId);
                    return new OrderNotFoundException("Order not found with id: " + orderId );
                });
    }
}