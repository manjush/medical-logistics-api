package com.medical.logistics.domian.order.exceptions;

/**
 * Domain exception for invalid order state transitions
 */
public class InvalidOrderStateException extends RuntimeException {
    public InvalidOrderStateException(String msg) {
        super(msg);
    }
}
