package com.medical.logistics.interfaces.rest;

import com.medical.logistics.domian.order.Order;
import com.medical.logistics.interfaces.rest.dto.OrderItemDto;
import com.medical.logistics.interfaces.rest.dto.OrderResponse;

import java.util.stream.Collectors;

/**
 * Mapper between domain objects and DTOs
 */
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId().getValue())
                .status(order.getStatus().name())
                .items(order.getItems().stream()
                        .map(item -> new OrderItemDto(item.getName(), item.getQuantity()))
                        .collect(Collectors.toList()))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
