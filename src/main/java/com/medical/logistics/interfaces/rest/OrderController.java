package com.medical.logistics.interfaces.rest;


import com.medical.logistics.application.order.OrderApplicationService;
import com.medical.logistics.application.order.commands.ApproveOrderCommand;
import com.medical.logistics.application.order.commands.CancelOrderCommand;
import com.medical.logistics.application.order.commands.PlaceOrderCommand;
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

    public OrderController(OrderApplicationService orderService) {
        this.orderService = orderService;
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

        OrderResponse orderResponse = orderService.placeOrder(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderResponse);
    }

    @PutMapping("/{orderId}/approve")
    public ResponseEntity<OrderResponse> approveOrder(@PathVariable UUID orderId) {
        orderService.approveOrder(new ApproveOrderCommand(OrderId.of(orderId)));
        OrderResponse orderResponse = orderService.getOrder(OrderId.of(orderId));
        return ResponseEntity.ok(orderResponse);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable UUID orderId) {
        orderService.cancelOrder(new CancelOrderCommand(OrderId.of(orderId)));
        OrderResponse orderResponse = orderService.getOrder(OrderId.of(orderId));
        return ResponseEntity.ok(orderResponse);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orderResponses = orderService.getAllOrders();
        return ResponseEntity.ok(orderResponses);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID orderId) {
        OrderResponse orderResponse = orderService.getOrder(OrderId.of(orderId));
        return ResponseEntity.ok(orderResponse);
    }
}
