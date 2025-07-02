package com.medical.logistics.interfaces.rest;

import com.medical.logistics.interfaces.rest.dto.CreateOrderRequest;
import com.medical.logistics.interfaces.rest.dto.OrderItemDto;
import com.medical.logistics.interfaces.rest.dto.OrderResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Should complete order lifecycle")
    void shouldCompleteOrderLifecycle() {
        // Create order
        CreateOrderRequest createRequest = new CreateOrderRequest();
        createRequest.setItems(List.of(
                new OrderItemDto("Syringe", 10),
                new OrderItemDto("Bandage", 20)
        ));

        ResponseEntity<OrderResponse> createResponse = restTemplate.postForEntity(
                "/api/orders",
                createRequest,
                OrderResponse.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().getStatus()).isEqualTo("PENDING");

        UUID orderId = createResponse.getBody().getId();

        // Approve order
        ResponseEntity<OrderResponse> approveResponse = restTemplate.exchange(
                "/api/orders/{orderId}/approve",
                HttpMethod.PUT,
                null,
                OrderResponse.class,
                orderId
        );

        assertThat(approveResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(approveResponse.getBody().getStatus()).isEqualTo("APPROVED");

        // Try to cancel approved order (should fail)
        ResponseEntity<String> cancelResponse = restTemplate.exchange(
                "/api/orders/{orderId}/cancel",
                HttpMethod.PUT,
                null,
                String.class,
                orderId
        );

        assertThat(cancelResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should create and cancel order")
    void shouldCreateAndCancelOrder() {
        // Create order
        CreateOrderRequest createRequest = new CreateOrderRequest();
        createRequest.setItems(List.of(new OrderItemDto("Mask", 100)));

        ResponseEntity<OrderResponse> createResponse = restTemplate.postForEntity(
                "/api/orders",
                createRequest,
                OrderResponse.class
        );

        UUID orderId = createResponse.getBody().getId();

        // Cancel order
        ResponseEntity<OrderResponse> cancelResponse = restTemplate.exchange(
                "/api/orders/{orderId}/cancel",
                HttpMethod.PUT,
                null,
                OrderResponse.class,
                orderId
        );

        assertThat(cancelResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cancelResponse.getBody().getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    @DisplayName("Should get all orders")
    void shouldGetAllOrders() {
        // Create some orders
        CreateOrderRequest request1 = new CreateOrderRequest();
        request1.setItems(List.of(new OrderItemDto("Item1", 10)));
        restTemplate.postForEntity("/api/orders", request1, OrderResponse.class);

        CreateOrderRequest request2 = new CreateOrderRequest();
        request2.setItems(List.of(new OrderItemDto("Item2", 20)));
        restTemplate.postForEntity("/api/orders", request2, OrderResponse.class);

        // Get all orders
        ResponseEntity<OrderResponse[]> response = restTemplate.getForEntity(
                "/api/orders",
                OrderResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSizeGreaterThanOrEqualTo(2);
    }
}
