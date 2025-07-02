package com.medical.logistics.interfaces.rest;


import com.medical.logistics.application.order.OrderApplicationService;
import com.medical.logistics.application.order.commands.*;
import com.medical.logistics.domian.order.Order;
import com.medical.logistics.domian.order.OrderId;
import com.medical.logistics.interfaces.rest.dto.CreateOrderRequest;
import com.medical.logistics.interfaces.rest.dto.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for Order operations
 * <p>
 * In I/O-heavy scenarios, could return DeferredResult or use WebFlux for non-blocking responses.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderApplicationService orderService;
    private final OrderMapper orderMapper;

    public OrderController(OrderApplicationService orderService) {
        this.orderService = orderService;
        this.orderMapper = new OrderMapper();
    }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody CreateOrderRequest request) {
        PlaceOrderCommand command = new PlaceOrderCommand(
                request.getItems().stream()
                        .map(item -> new PlaceOrderCommand.OrderItemCommand(
                                item.getName(), item.getQuantity()
                        ))
                        .collect(Collectors.toList())
        );

        OrderId orderId = orderService.handle(command);
        Order order = orderService.getOrder(orderId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderMapper.toResponse(order));
    }

    @PutMapping("/{orderId}/approve")
    public ResponseEntity<OrderResponse> approveOrder(@PathVariable UUID orderId) {
        orderService.handle(new ApproveOrderCommand(OrderId.of(orderId)));
        Order order = orderService.getOrder(OrderId.of(orderId));
        return ResponseEntity.ok(orderMapper.toResponse(order));
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable UUID orderId) {
        orderService.handle(new CancelOrderCommand(OrderId.of(orderId)));
        Order order = orderService.getOrder(OrderId.of(orderId));
        return ResponseEntity.ok(orderMapper.toResponse(order));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        List<OrderResponse> responses = orders.stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID orderId) {
        Order order = orderService.getOrder(OrderId.of(orderId));
        return ResponseEntity.ok(orderMapper.toResponse(order));
    }
}
