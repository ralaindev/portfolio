package com.robert.portfolio.orders.infrastructure.persistence;

import com.robert.portfolio.orders.application.port.out.OrderRepositoryPort;
import com.robert.portfolio.orders.domain.model.IdempotencyKey;
import com.robert.portfolio.orders.domain.model.Order;
import com.robert.portfolio.orders.domain.model.OrderId;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JpaOrderRepositoryAdapter implements OrderRepositoryPort {

    private final SpringDataOrderRepository repository;
    private final OrderPersistenceMapper mapper;

    public JpaOrderRepositoryAdapter(SpringDataOrderRepository repository, OrderPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Order save(Order order) {
        return mapper.toDomain(repository.save(mapper.toEntity(order)));
    }

    @Override
    public Optional<Order> findById(OrderId orderId) {
        return repository.findById(orderId.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<Order> findByIdempotencyKey(IdempotencyKey idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey.value()).map(mapper::toDomain);
    }
}
