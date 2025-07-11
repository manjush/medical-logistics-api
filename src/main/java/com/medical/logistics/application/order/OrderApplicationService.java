package com.medical.logistics.application.order;

import com.medical.logistics.application.order.commands.ApproveOrderCommand;
import com.medical.logistics.application.order.commands.CancelOrderCommand;
import com.medical.logistics.application.order.commands.PlaceOrderCommand;
import com.medical.logistics.domian.order.OrderId;
import com.medical.logistics.interfaces.rest.dto.OrderResponse;

import java.util.List;

public interface OrderApplicationService {

    /**
     * Places a new order
     * @param command containing order items
     * @return OrderResponse with created order details
     */
    OrderResponse placeOrder(PlaceOrderCommand command);

    /**
     * Approves a pending order
     * @param command containing order ID to approve
     */
    void approveOrder(ApproveOrderCommand command);

    /**
     * Cancels a pending order
     * @param command containing order ID to cancel
     */
    void cancelOrder(CancelOrderCommand command);

    /**
     * Retrieves all orders
     * @return list of all orders
     */
    List<OrderResponse> getAllOrders();

    /**
     * Retrieves a specific order
     * @param orderId the order ID
     * @return order details
     */
    OrderResponse getOrder(OrderId orderId);
}
