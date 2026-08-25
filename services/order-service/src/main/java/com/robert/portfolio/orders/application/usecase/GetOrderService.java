package com.robert.portfolio.orders.application.usecase;

import com.robert.portfolio.orders.application.port.in.GetOrderUseCase;
import com.robert.portfolio.orders.application.port.out.OrderRepositoryPort;
import com.robert.portfolio.orders.domain.exception.OrderNotFoundException;
import com.robert.portfolio.orders.domain.model.Order;
import com.robert.portfolio.orders.domain.model.OrderId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetOrderService implements GetOrderUseCase {

    private final OrderRepositoryPort orderRepository;

    public GetOrderService(OrderRepositoryPort orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Order get(UUID orderId) {
        OrderId id = new OrderId(orderId);
        return orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }
}
