package com.robert.portfolio.orders.application.port.in;

import com.robert.portfolio.orders.domain.model.Order;

import java.util.UUID;

public interface GetOrderUseCase {

    Order get(UUID orderId);
}
