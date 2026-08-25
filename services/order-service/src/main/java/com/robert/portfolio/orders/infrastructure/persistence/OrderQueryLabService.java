package com.robert.portfolio.orders.infrastructure.persistence;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class OrderQueryLabService {

    private final SpringDataOrderRepository repository;

    public OrderQueryLabService(SpringDataOrderRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<OrderQueryProjection> loadWithNPlusOne() {
        // This method is intentionally inefficient so the query count can be observed in a test.
        return repository.findAll().stream()
                .map(this::toProjectionAfterAccessingLines)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderQueryProjection> loadWithFetchJoin() {
        // The transaction keeps the lazy collection usable while the comparison is performed.
        return repository.findAllWithLinesUsingFetchJoin().stream()
                .map(this::toProjectionAfterAccessingLines)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderQueryProjection> loadWithEntityGraph() {
        return repository.findAllWithLinesUsingEntityGraph().stream()
                .map(this::toProjectionAfterAccessingLines)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderQueryProjection> loadWithDtoProjection() {
        return repository.findOrderSummariesUsingProjection();
    }

    private OrderQueryProjection toProjectionAfterAccessingLines(OrderJpaEntity entity) {
        return new OrderQueryProjection(entity.getId(), entity.getCustomerId(), entity.getLines().size());
    }
}
