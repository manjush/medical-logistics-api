package com.medical.logistics.domian.order.exceptions;

/**
 * Domain exception for order not found scenarios
 */
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String msg) {
        super(msg);
    }
}
